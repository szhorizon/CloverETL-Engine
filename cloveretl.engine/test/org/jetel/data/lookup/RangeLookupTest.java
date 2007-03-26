package org.jetel.data.lookup;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import org.jetel.data.DataRecord;
import org.jetel.data.RecordKey;
import org.jetel.data.parser.DelimitedDataParser;
import org.jetel.data.parser.Parser;
import org.jetel.data.parser.XLSDataParser;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.JetelException;
import org.jetel.main.runGraph;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataRecordMetadata;

import junit.framework.TestCase;

public class RangeLookupTest extends TestCase {

    LookupTable lookup,lookupNotOverlap;
    DataRecordMetadata lookupMetadata, metadata;
    DataRecord record;
    Random random = new Random();
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        runGraph.initEngine(null, null);
    }

    public void test_1() throws IOException,ComponentNotReadyException{
        lookupMetadata = new DataRecordMetadata("lookupTest",DataRecordMetadata.DELIMITED_RECORD);
        lookupMetadata.addField(new DataFieldMetadata("name",DataFieldMetadata.STRING_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("start",DataFieldMetadata.INTEGER_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("end",DataFieldMetadata.INTEGER_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("start1",DataFieldMetadata.INTEGER_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("end1",DataFieldMetadata.INTEGER_FIELD,";"));
        lookup = LookupTableFactory.createLookupTable(null, "rangeLookup", 
        		new Object[]{"RangeLookup",lookupMetadata,null}, 
        		new Class[]{String.class,DataRecordMetadata.class,Parser.class});
        lookupNotOverlap = LookupTableFactory.createLookupTable(null, "rangeLookup", 
        		new Object[]{"RangeLookup",lookupMetadata,null}, 
        		new Class[]{String.class,DataRecordMetadata.class,Parser.class});
        lookup.init();
        lookupNotOverlap.init();
    	record = new DataRecord(lookupMetadata);
    	record.init();
      	record.getField("name").setValue("10-20,100-200");
    	record.getField("start").setValue(10);
       	record.getField("end").setValue(20);
      	record.getField("start1").setValue(100);
      	record.getField("end1").setValue(200);
       	lookup.put(record, record.duplicate());
      	record.getField("name").setValue("15-20,100-200");
    	record.getField("start").setValue(15);
       	record.getField("end").setValue(20);
      	record.getField("start1").setValue(100);
      	record.getField("end1").setValue(200);
       	lookup.put(record, record.duplicate());
      	record.getField("name").setValue("20-25,100-200");
    	record.getField("start").setValue(20);
       	record.getField("end").setValue(25);
      	record.getField("start1").setValue(100);
      	record.getField("end1").setValue(200);
       	lookup.put(record, record.duplicate());
    	record.getField("name").setValue("20-30,0-100");
    	record.getField("start").setValue(20);
       	record.getField("end").setValue(30);
      	record.getField("start1").setValue(0);
      	record.getField("end1").setValue(100);
       	lookup.put(record, record.duplicate());
    	record.getField("name").setValue("20-30,100-200");
    	record.getField("start").setValue(20);
       	record.getField("end").setValue(30);
      	record.getField("start1").setValue(100);
      	record.getField("end1").setValue(200);
       	lookup.put(record, record.duplicate());
    	record.getField("name").setValue("30-40,0-100");
    	record.getField("start").setValue(30);
       	record.getField("end").setValue(40);
      	record.getField("start1").setValue(0);
      	record.getField("end1").setValue(100);
       	lookup.put(record, record.duplicate());
    	record.getField("name").setValue("30-40,100-200");
    	record.getField("start").setValue(30);
       	record.getField("end").setValue(40);
      	record.getField("start1").setValue(100);
      	record.getField("end1").setValue(200);
       	lookup.put(record, record.duplicate());
    	record.getField("name").setValue("0-10,0-100");
    	record.getField("start").setValue(0);
      	record.getField("end").setValue(10);
      	record.getField("start1").setValue(0);
      	record.getField("end1").setValue(100);
       	lookup.put(record, record.duplicate());
       	record.getField("name").setValue("0-10,100-200");
    	record.getField("start").setValue(0);
       	record.getField("start1").setValue(100);
      	record.getField("end").setValue(10);
      	record.getField("end1").setValue(200);
      	lookup.put(record, record.duplicate());
      	record.getField("name").setValue("10-20,0-100");
    	record.getField("start").setValue(10);
       	record.getField("end").setValue(20);
      	record.getField("start1").setValue(0);
      	record.getField("end1").setValue(100);
       	lookup.put(record, record.duplicate());

      	record.getField("name").setValue("10-20,100-200");
    	record.getField("start").setValue(11);
       	record.getField("end").setValue(20);
      	record.getField("start1").setValue(101);
      	record.getField("end1").setValue(200);
       	lookupNotOverlap.put(record, record.duplicate());
    	record.getField("name").setValue("20-30,0-100");
    	record.getField("start").setValue(21);
       	record.getField("end").setValue(30);
      	record.getField("start1").setValue(0);
      	record.getField("end1").setValue(100);
       	lookupNotOverlap.put(record, record.duplicate());
    	record.getField("name").setValue("20-30,100-200");
    	record.getField("start").setValue(21);
       	record.getField("end").setValue(30);
      	record.getField("start1").setValue(101);
      	record.getField("end1").setValue(200);
       	lookupNotOverlap.put(record, record.duplicate());
    	record.getField("name").setValue("30-40,0-100");
    	record.getField("start").setValue(31);
       	record.getField("end").setValue(40);
      	record.getField("start1").setValue(0);
      	record.getField("end1").setValue(100);
       	lookupNotOverlap.put(record, record.duplicate());
    	record.getField("name").setValue("30-40,100-200");
    	record.getField("start").setValue(31);
       	record.getField("end").setValue(40);
      	record.getField("start1").setValue(101);
      	record.getField("end1").setValue(200);
       	lookupNotOverlap.put(record, record.duplicate());
    	record.getField("name").setValue("0-10,0-100");
    	record.getField("start").setValue(0);
      	record.getField("end").setValue(10);
      	record.getField("start1").setValue(0);
      	record.getField("end1").setValue(100);
       	lookupNotOverlap.put(record, record.duplicate());
       	record.getField("name").setValue("0-10,100-200");
    	record.getField("start").setValue(0);
       	record.getField("start1").setValue(101);
      	record.getField("end").setValue(10);
      	record.getField("end1").setValue(200);
      	lookupNotOverlap.put(record, record.duplicate());
      	record.getField("name").setValue("10-20,0-100");
    	record.getField("start").setValue(11);
       	record.getField("end").setValue(20);
      	record.getField("start1").setValue(0);
      	record.getField("end1").setValue(100);
       	lookupNotOverlap.put(record, record.duplicate());

       	metadata = new DataRecordMetadata("in",DataRecordMetadata.DELIMITED_RECORD);
       	metadata.addField(new DataFieldMetadata("id",DataFieldMetadata.INTEGER_FIELD,";"));
       	metadata.addField(new DataFieldMetadata("value",DataFieldMetadata.INTEGER_FIELD,";"));
       	metadata.addField(new DataFieldMetadata("value1",DataFieldMetadata.INTEGER_FIELD,";"));
      	record = new DataRecord(metadata);
    	record.init();
       	RecordKey key = new RecordKey(new int[]{1,2},metadata);
       	lookup.setLookupKey(key);
       	lookupNotOverlap.setLookupKey(key);

       	DataRecord tmp,tmp1;
    	for (Iterator iter = lookup.iterator(); iter.hasNext();) {
			System.out.print(iter.next());
			
		}
    	for (int i=0;i<200;i++){
    		record.getField("id").setValue(i);
    		record.getField("value").setValue(random.nextInt(41));
    		record.getField("value1").setValue(random.nextInt(201));
    		tmp = lookup.get(record);
    		tmp1 = lookupNotOverlap.get(record);
    		System.out.println("Input record " + i + ":\n" + record + "From lookup table:\n" + tmp);
    		assertTrue((Integer)record.getField("value").getValue() >= (Integer)tmp.getField("start").getValue());
    		assertTrue((Integer)record.getField("value").getValue() <= (Integer)tmp.getField("end").getValue());
    		assertTrue((Integer)record.getField("value1").getValue() >= (Integer)tmp.getField("start1").getValue());
    		assertTrue((Integer)record.getField("value1").getValue() <= (Integer)tmp.getField("end1").getValue());
    		System.out.println("From lookupNotOverlap table:\n" + tmp1);
//    		assertEquals(tmp.getField("end"), tmp1.getField("end"));
//    		assertEquals(tmp.getField("end1"), tmp1.getField("end1"));
//    		if ((Integer)record.getField("value").getValue()%10 == 0 || 
//    				(Integer)record.getField("value1").getValue()%100 == 0 ){
//    			System.in.read();
//    		}
    		System.out.println("Num found: " + lookup.getNumFound());
    		System.out.println("Num found: " + lookupNotOverlap.getNumFound());
   		tmp = lookup.getNext();
    		tmp1 = lookupNotOverlap.getNext();
    		while (tmp != null) {
    			System.out.println("Next:\n" + tmp);
        		assertTrue((Integer)record.getField("value").getValue() >= (Integer)tmp.getField("start").getValue());
        		assertTrue((Integer)record.getField("value").getValue() <= (Integer)tmp.getField("end").getValue());
        		assertTrue((Integer)record.getField("value1").getValue() >= (Integer)tmp.getField("start1").getValue());
        		assertTrue((Integer)record.getField("value1").getValue() <= (Integer)tmp.getField("end1").getValue());
    			System.out.println("Next1:\n" + tmp1);
     			tmp = lookup.getNext();
    		}
    	}
  }
    
    public void test_2() throws IOException,ComponentNotReadyException,JetelException{
        lookupMetadata = new DataRecordMetadata("lookupTest",DataRecordMetadata.DELIMITED_RECORD);
        lookupMetadata.addField(new DataFieldMetadata("name",DataFieldMetadata.STRING_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("s1",DataFieldMetadata.INTEGER_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("e1",DataFieldMetadata.INTEGER_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("s2",DataFieldMetadata.NUMERIC_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("e2",DataFieldMetadata.NUMERIC_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("s3",DataFieldMetadata.STRING_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("e3",DataFieldMetadata.STRING_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("s4",DataFieldMetadata.LONG_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("e4",DataFieldMetadata.LONG_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("s5",DataFieldMetadata.INTEGER_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("e5",DataFieldMetadata.INTEGER_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("s6",DataFieldMetadata.NUMERIC_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("e6",DataFieldMetadata.NUMERIC_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("s7",DataFieldMetadata.INTEGER_FIELD,";"));
        lookupMetadata.addField(new DataFieldMetadata("e7",DataFieldMetadata.INTEGER_FIELD,";"));
    	
        XLSDataParser parser = new XLSDataParser();
        parser.setSheetNumber("0");
        parser.init(lookupMetadata);
        parser.setDataSource(new FileInputStream("data\\rangeLookup.dat.xls"));
        
        lookup = LookupTableFactory.createLookupTable(null, "rangeLookup", 
        		new Object[]{"RangeLookup",lookupMetadata,parser}, 
        		new Class[]{String.class,DataRecordMetadata.class,Parser.class});
        lookup.init();

        DataRecord tmp = new DataRecord(lookupMetadata);
        DataRecord tmp1 = new DataRecord(lookupMetadata);
        tmp.init();
        tmp1.init();
        
        Iterator<DataRecord> iter = lookup.iterator();
        tmp1 = iter.next();
        int count = 1;
        
        for (; iter.hasNext();) {
        	System.out.println("Record nr " + count++ + "\n" + tmp1);
        	tmp = tmp1.duplicate();
        	tmp1 = (DataRecord)iter.next();
			assertTrue(checkOrder(tmp, tmp1));
		}

      	metadata = new DataRecordMetadata("in",DataRecordMetadata.DELIMITED_RECORD);
       	metadata.addField(new DataFieldMetadata("id",DataFieldMetadata.INTEGER_FIELD,";"));
       	metadata.addField(new DataFieldMetadata("value1",DataFieldMetadata.INTEGER_FIELD,";"));
       	metadata.addField(new DataFieldMetadata("value2",DataFieldMetadata.NUMERIC_FIELD,";"));
       	metadata.addField(new DataFieldMetadata("value3",DataFieldMetadata.STRING_FIELD,";"));
       	metadata.addField(new DataFieldMetadata("value4",DataFieldMetadata.LONG_FIELD,";"));
       	metadata.addField(new DataFieldMetadata("value5",DataFieldMetadata.INTEGER_FIELD,";"));
       	metadata.addField(new DataFieldMetadata("value6",DataFieldMetadata.NUMERIC_FIELD,";"));
       	metadata.addField(new DataFieldMetadata("value7",DataFieldMetadata.INTEGER_FIELD,";"));
      	record = new DataRecord(metadata);
    	record.init();
       	RecordKey key = new RecordKey(new int[]{1,2,3,4,5,6,7},metadata);
       	lookup.setLookupKey(key);
       	
       	DelimitedDataParser dataParser = new DelimitedDataParser();
       	dataParser.init(metadata);
       	dataParser.setDataSource(new FileInputStream("data\\data.dat"));

    	for (int i=0;i<500;i++){
    		record = dataParser.getNext();
     		System.out.println("Input record " + i + ":\n" + record);
       		tmp = lookup.get(record);
       		System.out.println("Num found: " + lookup.getNumFound());
    		while (tmp != null) {
    			System.out.println("From lookup table:\n" + tmp);
        		assertTrue((Integer)record.getField("value1").getValue() >= (Integer)tmp.getField("s1").getValue());
        		assertTrue((Integer)record.getField("value1").getValue() <= (Integer)tmp.getField("e1").getValue());
        		assertTrue((Double)record.getField("value2").getValue() >= (Double)tmp.getField("s2").getValue());
        		assertTrue((Double)record.getField("value2").getValue() <= (Double)tmp.getField("e2").getValue());
        		assertTrue((record.getField("value3").getValue().toString()).compareTo(tmp.getField("s3").getValue().toString())>=0);
        		assertTrue((record.getField("value3").getValue().toString()).compareTo(tmp.getField("e3").getValue().toString())<=0);
        		assertTrue((Long)record.getField("value4").getValue() >= (Long)tmp.getField("s4").getValue());
        		assertTrue((Long)record.getField("value4").getValue() <= (Long)tmp.getField("e4").getValue());
        		assertTrue((Integer)record.getField("value5").getValue() >= (Integer)tmp.getField("s5").getValue());
        		assertTrue((Integer)record.getField("value5").getValue() <= (Integer)tmp.getField("e5").getValue());
        		assertTrue((Double)record.getField("value6").getValue() >= (Double)tmp.getField("s6").getValue());
        		assertTrue((Double)record.getField("value6").getValue() <= (Double)tmp.getField("e6").getValue());
        		assertTrue((Integer)record.getField("value7").getValue() >= (Integer)tmp.getField("s7").getValue());
        		assertTrue((Integer)record.getField("value7").getValue() <= (Integer)tmp.getField("e7").getValue());
     			tmp = lookup.getNext();
    		}
    	}
    }
    
    private boolean checkOrder(DataRecord previous,DataRecord following){
     	int startComparison;
    	int endComparison;
    	for (int i=1;i<previous.getNumFields()-1;i+=2){
    		startComparison = previous.getField(i).compareTo(following.getField(i));
    		endComparison = previous.getField(i+1).compareTo(following.getField(i+1));
    		if (endComparison == -1) return true;
    		if (endComparison == 0 && (startComparison == 0 && startComparison == 1)) 
    			return true;
    	}
    	return false;
    }
}
