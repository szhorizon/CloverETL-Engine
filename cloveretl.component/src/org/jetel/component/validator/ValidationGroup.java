/*
 * jETeL/CloverETL - Java based ETL application framework.
 * Copyright (c) Javlin, a.s. (info@cloveretl.com)
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.jetel.component.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jetel.component.validator.params.LanguageSetting;
import org.jetel.component.validator.utils.CustomRulesMapAdapter;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.metadata.DataRecordMetadata;

/**
 * <p>Class for validation group which can contains rules and other groups.
 * Joins its content by AND, OR conjunction in lazy or not lazy way.</p>
 * 
 * <p>Contains language setting for setting formatting mask and timezone on each group.</p>
 * 
 * <p>Container for custom rules (used only on validation root group).</p>
 * 
 * @author drabekj (info@cloveretl.com) (c) Javlin, a.s. (www.cloveretl.com)
 * @created 19.11.2012
 * @see LanguageSetting
 * @see CustomRule
 */
@XmlRootElement(name="group")
@XmlAccessorType(XmlAccessType.NONE)
public class ValidationGroup extends ValidationNode {
	
	@XmlElementWrapper(name="children")
	@XmlElementRef
	private List<ValidationNode> childs = new ArrayList<ValidationNode>();
	@XmlAttribute(required=true)
	private Conjunction conjunction = Conjunction.AND;
	
	@XmlElement(name="languageSetting",required=false)
	private LanguageSetting languageSetting = new LanguageSetting();
	
	/**
	 * Wrapper class for prelimitary condition. Needed for nesting in schema generated by JAXB.
	 * 
	 * Other considered:
	 * <ul>
	 *   <li>Cannot be wrapped as its not collection.</li>
	 *   <li>If it's collection than it can contain more prelimitary conditions (not wanted)</li>
	 *   <li>Cannot be left out as it couldn't be ommited @XmlElementRef(required=false) requires JAXB 2.1+ (not enabled in Java 6 by default)</li> 
	 * </ul>
	 */
	@XmlAccessorType(XmlAccessType.NONE)
	private static class PrelimitaryCondition {
		@XmlElementRef
		private AbstractValidationRule content;
		public AbstractValidationRule getContent() {return content;}
		public void setContent(AbstractValidationRule value) {content = value;}
	}
 
	@XmlElement(name="prelimitaryCondition")
	private PrelimitaryCondition prelimitaryCondition;
	@XmlAttribute
	private boolean laziness = true;
	
	/**
	 * Implementation of conjunction with neutral element
	 */
	@XmlType(name = "conjunction")
	@XmlEnum
	public enum Conjunction {
		AND, OR;
		
		/**
		 * Computes AND operation on three state logic
		 * @param left Left operand
		 * @param right Right operand
		 * @return INVALID when at least one operand is INVALID,
		 *         VALID when at least one operands is VALID (and noone is INVALID), 
		 *         NOT_VALIDATED otherwise
		 */
		public static State and(State left, State right) {
			if(left == State.INVALID || right == State.INVALID) {
				return State.INVALID;
			}
			if(left == State.VALID || right == State.VALID) {
				return State.VALID;
			}
			return State.NOT_VALIDATED;
		}
		
		/**
		 * Computes OR operation on three state logic
		 * @param left Left operand
		 * @param right Right operand
		 * @return VALID when at least one operand is VALID,
		 *         NOT_VALIDATED when all operands are NOT_VALIDATED,
		 *         INVALID otherwise
		 */
		public static State or(State left, State right) {
			if(left == State.VALID || right == State.VALID) {
				return State.VALID;
			}
			if(left == State.NOT_VALIDATED && right == State.NOT_VALIDATED) {
				return State.NOT_VALIDATED;
			}
			return State.INVALID;
		}
	}
	
	/**
	 * Wrapper to hold all custom rules, supposed to be used only on root group.
	 * All methods accessing it should respect that no custom rules means null!
	 * Otherwise it will be in serialized output.
	 */
	@XmlElement(name="customRules")
	@XmlJavaTypeAdapter(CustomRulesMapAdapter.class)
	private Map<Integer, CustomRule> customRules;
	
	@XmlAttribute(name="nextCustomRuleId")
	private Integer nextCustomRuleId = 0; 
	
	@Override
	public void init(DataRecordMetadata metadata, GraphWrapper graphWrapper) throws ComponentNotReadyException {
		super.init(metadata, graphWrapper);
		
		for (ValidationNode child : childs) {
			child.init(metadata, graphWrapper);
			child.setParentLanguageSetting(LanguageSetting.hierarchicMerge(languageSetting, parentLanguageSetting));
		}
		
		AbstractValidationRule prelimitaryConditionRule = getPrelimitaryConditionRule();
		if (prelimitaryConditionRule != null) {
			prelimitaryConditionRule.setParentLanguageSetting(LanguageSetting.hierarchicMerge(languageSetting, parentLanguageSetting));
		}
	}
	
	@Override
	public void preExecute() {
		super.preExecute();
		
		for (ValidationNode child : childs) {
			child.preExecute();
		}
	}
	
	@Override
	public void postExecute() {
		super.postExecute();
		
		for (ValidationNode child : childs) {
			child.postExecute();
		}
	}
	
	/**
	 * Adds custom validation rule to tree
	 * @param customRule Custom rule to add
	 */
	public void addCustomRule(CustomRule customRule) {
		if(customRules == null) {
			customRules = new HashMap<Integer, CustomRule>();
		}
		customRules.put(nextCustomRuleId++, customRule);
	}
	
	/**
	 * Returns custom validation rule with given id
	 * @param id ID of custom validation rule
	 * @return Custom validation rule
	 */
	public CustomRule getCustomRule(int id) {
		if(customRules == null) {
			return null;
		}
		return customRules.get(Integer.valueOf(id));
	}
	
	/**
	 * Returns all custom validation rules
	 * @return All custom rules
	 */
	public Map<Integer, CustomRule> getCustomRules() {
		return customRules;
	}
	
	/**
	 * Deletes custom validation rules with given id
	 * @param id ID of custom rules to be deleted
	 */
	public void removeCustomRule(int id) {
		if(customRules == null) {
			return;
		}
		customRules.remove(Integer.valueOf(id));
		if(customRules.isEmpty()) {
			customRules = null;
		}
	}
	

	/**
	 * Sets conjunction
	 * @param conjunction Conjunction to be used by group, not null
	 */
	public void setConjunction(Conjunction conjunction) {
		if(conjunction != null) {
			this.conjunction = conjunction;
		}
	}
	
	/**
	 * @return Returns current conjunction
	 */
	public Conjunction getConjunction() {
		return conjunction;
	}

	/**
	 * Sets new prelimitary condition overwriting the previous.
	 * @param prelimitaryCondition Group entrance condition
	 */
	public void setPrelimitaryCondition(AbstractValidationRule prelimitaryCondition) {
		if(prelimitaryCondition == null) {
			this.prelimitaryCondition = null;
			return;
		}
		if (this.prelimitaryCondition == null) {
			this.prelimitaryCondition = new PrelimitaryCondition();
		}
		this.prelimitaryCondition.setContent(prelimitaryCondition);
	}
	
	/**
	 * @return Prelimitary condition
	 */
	public AbstractValidationRule getPrelimitaryConditionRule() {
		if(prelimitaryCondition == null) {
			return null;
		}
		return prelimitaryCondition.getContent();
	}

	/**
	 * Sets whether group will be evaluated lazy
	 * @param laziness True if lazy evaluation is wanted, false otherwise
	 */
	public void setLaziness(boolean laziness) {
		this.laziness = laziness;
	}
	
	/**
	 * @return True if group should be evaluated lazy, false otherwise 
	 */
	public boolean getLaziness() {
		return laziness;
	}

	/**
	 * Adds new child after the last child
	 * @param child Validation node to be added into group
	 */
	public void addChild(ValidationNode child) {
		childs.add(child);
	}
	
	/**
	 * @return All children
	 */
	public List<ValidationNode> getChildren() {
		return childs;
	}

	@Override
	public State isValid(DataRecord record, ValidationErrorAccumulator ea, GraphWrapper graphWrapper) {
		if(!isEnabled()) {
			logNotValidated("Group not enabled.");
			return State.NOT_VALIDATED;
		}
		setPropertyRefResolver(graphWrapper);
		
		AbstractValidationRule prelimitaryConditionRule = getPrelimitaryConditionRule();
		if (isLoggingEnabled()) {
			logParams("Conjunction: " + conjunction + "\n" +
							"Lazy: " + laziness + "\n" +
							"Prelimitary condition: " + ((prelimitaryConditionRule == null)? null: prelimitaryConditionRule.getName()) + "\n" +
							"Language settings: " + languageSetting);
		}
		
		if(prelimitaryConditionRule != null) {
			if(prelimitaryConditionRule.isValid(record, null, graphWrapper) == State.INVALID) {
				logNotValidated("Prelimitary condition of group was invalid.");
				return State.NOT_VALIDATED;
			}
		}
		State currentState = State.NOT_VALIDATED;
		State childState;
		for(int i = 0; i < childs.size(); i++) {
			childState = childs.get(i).isValid(record,ea, graphWrapper);
			if(conjunction == Conjunction.AND) {
				currentState = Conjunction.and(currentState, childState);
				if(laziness && currentState == State.INVALID) {
					break;
				}
			}
			if(conjunction == Conjunction.OR) {
				currentState = Conjunction.or(currentState, childState);
				if(laziness && currentState == State.VALID) {
					break;
				}
			}
		}
		if(currentState == State.INVALID) {
			return State.INVALID;
		}
		if(currentState == State.VALID) {
			return State.VALID;
		}
		logNotValidated("Group has no children.");
		return State.NOT_VALIDATED;
	}
	
	@Override
	public boolean isReady(DataRecordMetadata inputMetadata, ReadynessErrorAcumulator accumulator, GraphWrapper graphWrapper) {
		if(!isEnabled()) {
			return true;
		}
		boolean state = true;
		for(int i = 0; i < childs.size(); i++) {
			childs.get(i).setParentLanguageSetting(LanguageSetting.hierarchicMerge(languageSetting, parentLanguageSetting));
			state &= childs.get(i).isReady(inputMetadata, accumulator, graphWrapper);
		}
		return state;
	}

	@Override
	public String getCommonName() {
		return "Group";
	}

	@Override
	public String getCommonDescription() {
		return "Groups allow creating of complex rules created by joining multiple rules with AND/OR conjunction.";
	}
	
	/**
	 * Sets language setting
	 * @param languageSetting New language setting
	 */
	public void setLanguageSetting(LanguageSetting languageSetting) {
		this.languageSetting = languageSetting;
	}
	
	/**
	 * @return Current language settings
	 */
	public LanguageSetting getLanguageSetting() {
		return languageSetting;
	}

}
