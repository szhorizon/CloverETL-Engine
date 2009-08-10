package org.jetel.data.xsd;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class ConvertorRegistry serves as the registry component for convertors between cloverETL data types
 * and another data model system.  
 * @author Pavel Pospichal
 */
public class ConvertorRegistry {

	private static final Log logger = LogFactory.getLog(ConvertorRegistry.class);
	private static List<IGenericConvertor> convertors = new ArrayList<IGenericConvertor>();
	
	static {
		try {
			Class.forName(CloverBooleanConvertor.class.getName());
			Class.forName(CloverByteArrayConvertor.class.getName());
			Class.forName(CloverDateConvertor.class.getName());
			Class.forName(CloverDateTimeConvertor.class.getName());
			Class.forName(CloverDecimalConvertor.class.getName());
			Class.forName(CloverNumericConvertor.class.getName());
			Class.forName(CloverStringConvertor.class.getName());
			Class.forName(CloverIntegerConvertor.class.getName());
			Class.forName(CloverLongConvertor.class.getName());
		} catch (ClassNotFoundException e) {
		}
	}
	
	public static void registerConvertor(IGenericConvertor convertor) {
		if (convertors.contains(convertor)) {
			logger.warn("Convertor [" + convertor.getClass().getName() + "] already registered.");
			return;
		}
		
		convertors.add(convertor);
		logger.debug("Convertor [" + convertor.getClass().getName() + "] registered.");
	}
	
	/* TODO: the selection of particular convertor is elementary based on the resolution of clover-specific data type,
	 * the data types defined by external data model may have more than one appropriate representation 
	 * for particular clover-specific data type, so the concrete convertor have to be specified by addtional criterions  
	 */
	public static IGenericConvertor getConvertor(String cloverDataTypeCriteria, String externalDataTypeCriteria) {
		
		// TODO: should be replaced by some hash access
		for (IGenericConvertor convertor : convertors) {
			if (convertor.supportsCloverType(cloverDataTypeCriteria)) {
				return convertor;
			}
		}
		
		return null;
	}
	
	public static IGenericConvertor getConvertor(String cloverDataTypeCriteria) {
		return getConvertor(cloverDataTypeCriteria, null);
	}
}
