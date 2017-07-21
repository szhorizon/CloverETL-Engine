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
package org.jetel.connection.jdbc.specific.impl;

import java.sql.Types;

import org.jetel.metadata.DataFieldMetadata;
import org.jetel.util.string.StringUtils;

/**
 * MS SQL specific behavior.
 * 
 * @author Martin Slama (martin.slama@javlin.eu)
 *         (c) Javlin, a.s. (www.cloveretl.com)
 *
 * @created May 22, 2012
 */
public abstract class AbstractMSSQLSpecific extends AbstractJdbcSpecific {

	protected AbstractMSSQLSpecific() {
		super(AutoGeneratedKeysType.SINGLE);
	}
	
    @Override
	public String quoteIdentifier(String identifier) {
        return ('[' + identifier + ']');
    }
	
	@Override
	public String sqlType2str(int sqlType) {
		switch(sqlType) {
		case Types.TIMESTAMP :
			return "DATETIME";
		case Types.BOOLEAN :
			return "BIT";
		case Types.INTEGER :
			return "INT";
		case Types.NUMERIC :
		case Types.DOUBLE :
			return "FLOAT";
		}
		return super.sqlType2str(sqlType);
	}
	
	@Override
	public char sqlType2jetel(int sqlType) {
		switch (sqlType) {
		case Types.BIT:
			return DataFieldMetadata.BOOLEAN_FIELD;
		default:
			return super.sqlType2jetel(sqlType);
		}
	}
	
	@Override
	public String getTablePrefix(String schema, String owner,
			boolean quoteIdentifiers) {
		String tablePrefix;
		String notNullOwner = (owner == null) ? "" : owner;
		if(quoteIdentifiers) {
			tablePrefix = quoteIdentifier(schema)+".";
			//in case when owner is empty or null skip adding
			if(!notNullOwner.isEmpty())
				tablePrefix += quoteIdentifier(notNullOwner);
		} else {
			tablePrefix = schema+"."+notNullOwner;
		}
		return tablePrefix;
	}
	
    @Override
	public String compileSelectQuery4Table(String schema, String owner, String table) {
    	if (isSchemaRequired() && !StringUtils.isEmpty(schema)) {
    		if (StringUtils.isEmpty(owner)) {
				return "select * from " + quoteIdentifier(schema) + "." + quoteIdentifier(table);
			} else {
				return "select * from " + quoteIdentifier(schema) + "." + quoteIdentifier(owner) + "." + quoteIdentifier(table);
			}
    	} else {
    		return "select * from " + quoteIdentifier(table);
    	}
    }

	@Override
	public boolean isSchemaRequired() {
		return true;
	}
}
