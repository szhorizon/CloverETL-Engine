/*
*    jETeL/Clover - Java based ETL application framework.
*    Copyright (C) 2002-04  David Pavlis <david_pavlis@hotmail.com>
*    
*    This library is free software; you can redistribute it and/or
*    modify it under the terms of the GNU Lesser General Public
*    License as published by the Free Software Foundation; either
*    version 2.1 of the License, or (at your option) any later version.
*    
*    This library is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    
*    Lesser General Public License for more details.
*    
*    You should have received a copy of the GNU Lesser General Public
*    License along with this library; if not, write to the Free Software
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
*/

// FILE: c:/projects/jetel/org/jetel/InputPort.java

package org.jetel.graph;
import java.io.IOException;
import org.jetel.data.DataRecord;
import org.jetel.metadata.DataRecordMetadata;

/**
 * An interface defining operations expected from InputPort object.
 * Input port establishes communication channel between two NODEs.
 *
 * @author     	D.Pavlis
 * @since    	April 2, 2002
 * @see        	OutputPort
 * @see	       	Node
 * @see		Edge
 */
public interface InputPort {

	
	// Operations
	/**
	 * An operation that registeres the node which reads data from this port
	 *
	 * @param  _reader  Description of Parameter
	 * @since           April 2, 2002
	 */
	public void connectReader(Node _reader);


	/**
	 *An operation that reads one record from this port
	 *
	 * @param  record                    Description of Parameter
	 * @return                           Description of the Returned Value
	 * @exception  IOException           Description of Exception
	 * @exception  InterruptedException  Description of Exception
	 * @since                            April 2, 2002
	 */
	public DataRecord readRecord(DataRecord record) throws IOException, InterruptedException;


	/**
	 * An operation that checks whether port is open for reading
	 *
	 * @return    The EOF value
	 * @since     April 2, 2002
	 */
	public boolean isOpen();


	/**
	 *  Gets the Metadata describing data records passing through this port
	 *
	 * @return    The Metadata value
	 * @since     April 4, 2002
	 */
	public DataRecordMetadata getMetadata();


	/**
	 *  Gets the Writer (Node which writes into this port at the other end - if any)
	 *
	 * @return    The Writer value
	 * @since     May 17, 2002
	 */
	public Node getWriter();


	/**
	 *  Gets the number of records passed (so far) through this port
	 *
	 * @return    The RecordCounter value
	 * @since     May 17, 2002
	 */
	public int getRecordCounter();
	
	/**
	 * Method which tests whether data is awaiting/ready to be read
	 * 
	 * @return True if read operation won't block due to lack of data
	 */
	public boolean hasData();
}
/*
 *  end interface InputPort
 */

