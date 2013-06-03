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
package org.jetel.data;

import static org.jetel.util.bytes.ByteBufferUtils.decodeLength;
import static org.jetel.util.bytes.ByteBufferUtils.encodeLength;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.graph.ContextProvider;
import org.jetel.graph.runtime.IAuthorityProxy;
import org.jetel.util.bytes.ByteBufferUtils;
import org.jetel.util.bytes.CloverBuffer;

/**
 *  Class implementing DynamicRecordBuffer backed by temporary file - i.e. unlimited
 *  size<br>
 *  Implements FIFO: write & read operations can be interleaved, however it
 *  deteriorates performance if data has to be swap to disk (if internal
 *  buffer is exhausted).<br>
 *  
 *
 *@author     David Pavlis
 *@since      November 20, 2006
 */
public class DynamicRecordBuffer {
	
	private static final Logger log = Logger.getLogger(DynamicRecordBuffer.class);

	protected CloverBuffer readDataBuffer;
    protected CloverBuffer writeDataBuffer;
    private CloverBuffer tmpDataRecord;

    private AtomicInteger bufferedRecords;
    
    private boolean awaitingData;
    
    private int initialBufferSize;

    private TempFile tempFile;
    private LinkedList<TempFile> obsoleteTempFiles; //TODO should it be synchronized?
    
	private volatile boolean isClosed;  // indicates whether buffer has been closed - no more r&w can occure

	private final static String TMP_FILE_PREFIX = "fbufdrb";
	// prefix of temporary file generated by system
	private final static String TMP_FILE_SUFFIX = ".tmp";
	// suffix of temporary file generated by system
	private final static String TMP_FILE_MODE = "rw";
    
    private final static int EOF = Integer.MAX_VALUE; // EOF indicates that no more records will be written to buffer

    /** Variables readerWaitingTime and writerWaitingTime are calculated only in verbose mode. */
	private boolean verbose;
	
	/** How long has been reader thread blocked on this buffer (in nanoseconds)? */ 
	private long readerWaitingTime;

	/** How long has been writer thread blocked on this buffer (in nanoseconds)? */ 
	private long writerWaitingTime;

	/**
	 * Constructor of the DynamicRecordBuffer with tmp file
     * created under java.io.tmpdir dir.
	 * @param dataBufferSize
	 */
	public DynamicRecordBuffer() {
	    this(Defaults.Record.RECORDS_BUFFER_SIZE);
    }

	/**
	 *  Constructor of the DynamicRecordBuffer object
	 *
	 *@param  tmpFilePath     Name of the subdirectory where to create TMP files or
	 *      NULL (the system default will be used)
	 *@param  initialBufferSize  The initial size of internal in memory buffer - two
     *          buffers of exactly the same size are created - one for reading, one
     *          for writing.
	 */
	public DynamicRecordBuffer(int initialBufferSize) {
        this.initialBufferSize = initialBufferSize;
    }

	/**
	 * @return <code>true</code> if the buffer is closed and further read/write operations are going to fail,
	 * <code>false</code> otherwise
	 */
	public boolean isClosed() {
		return isClosed;
	}

	/**
     * Initializes the buffer. Must be called before any write or read operation
     * is performed.
     * 
     * @since 27.11.2006
     */
    public void init() {
        obsoleteTempFiles = new LinkedList<DynamicRecordBuffer.TempFile>();
        isClosed = false;
        readDataBuffer = CloverBuffer.allocateDirect(initialBufferSize);
        writeDataBuffer = CloverBuffer.allocateDirect(initialBufferSize);
        tmpDataRecord = CloverBuffer.allocateDirect(Defaults.Record.RECORD_INITIAL_SIZE, Defaults.Record.RECORD_LIMIT_SIZE);
        awaitingData = false;
        bufferedRecords = new AtomicInteger(0);
        readDataBuffer.flip();
    }
    
    /**
	 *  Closes buffer, removes temporary file (is exists)
	 *
	 *@exception  IOException  Description of Exception
	 *@since                   September 17, 2002
	 */
	public void close() throws IOException {
		isClosed = true;
		
		for (TempFile tempFile : obsoleteTempFiles) {
			try {
				tempFile.close();
			} catch (IOException e) {
				log.error("Failed to close temp file.", e);
			}
		}
		
		if (tempFile != null) {
			try {
				tempFile.close();
			} catch (IOException e) {
				log.error("Failed to close temp file.", e);
			}
		}
		
        readDataBuffer = null;
        writeDataBuffer = null;
	}
	
	/**
	 * Mark as closed (to prevent locking on reading from buffer). To free buffers, close() method must be called.
	 * This method allows reuse of buffer after reset() is called.
	 * 
	 * @since Jan 11, 2008
	 */
	public void closeTemporarily() {
		isClosed = true;
	}	

	public void preExecute() {
        readerWaitingTime = 0;
        writerWaitingTime = 0;
	}
	
	/**
	 *  Clears the buffer. Temp file (if it was created) remains
	 * unchanged size-wise
	 */
	public void reset() {
		isClosed = false;
		
		//all obsolete temp files are closed, only the biggest/newest one is persist and reset
		if (tempFile != null) {
			tempFile.reset();
		}
		while (!obsoleteTempFiles.isEmpty()) {
			try {
				obsoleteTempFiles.remove().close();
			} catch (IOException e) {
				log.warn("Failed to close temp file.", e);
			}
		}
		
		readDataBuffer.clear();
        writeDataBuffer.clear();
        awaitingData = false;
        bufferedRecords.set(0);
        readDataBuffer.flip();
	}

	/**
	 *  Stores one data record into buffer.
	 *
	 *@param  record             ByteBuffer containing record's data
	 *@exception  IOException  In case of IO failure
	 * @throws InterruptedException 
	 *@since                   September 17, 2002
	 */
	public int writeRecord(CloverBuffer record) throws IOException, InterruptedException {
		if (isClosed) {
			throw new IOException("Buffer has been closed !");
		}
		
		int recordSize = record.remaining();

        if (writeDataBuffer.remaining() < recordSize + ByteBufferUtils.SIZEOF_INT && writeDataBuffer.position() > 0) {
        	//buffer is not flushed if is empty - dynamicity of buffer is used
            flushWriteBuffer();
        }

        encodeLength(writeDataBuffer,recordSize);
		writeDataBuffer.put(record);
		
		bufferedRecords.incrementAndGet();
        
		return recordSize;
	}

    
    /**
     *  Stores one data record into buffer.
     * 
     * @param record    data record to be written
     * @throws IOException
     * @throws InterruptedException 
     * @since 27.11.2006
     */
    public int writeRecord(DataRecord record) throws IOException, InterruptedException {
        if (isClosed) {
            throw new IOException("Buffer has been closed !");
        }

        tmpDataRecord.clear();
        record.serialize(tmpDataRecord);
        tmpDataRecord.flip();
        
        return writeRecord(tmpDataRecord);
    }
    
    
    /**
     * Indicates that there will be no more records written.
     * 
     * @throws IOException
     * @throws InterruptedException 
     * @since 27.11.2006
     */
    public void setEOF() throws IOException, InterruptedException {
        if (isClosed) {
            throw new IOException("Buffer has been closed !");
        }
        if (writeDataBuffer.remaining() < ByteBufferUtils.SIZEOF_INT) {
            flushWriteBuffer();
        }
        encodeLength(writeDataBuffer,EOF);
        flushWriteBuffer();
    }
    

	/**
	 *  Secures that in memory buffer is "mapped" from proper location and
	 *  populated with data from TMP file (is needed)
	 *
	 *@param  position         Description of the Parameter
	 *@param  requestedSize    Description of the Parameter
	 *@exception  IOException  Description of the Exception
	 * @throws InterruptedException 
	 */
	private final synchronized void flushWriteBuffer() throws IOException, InterruptedException {
		// we need to swap data - first try directly read buffer
		if (awaitingData) {
			// swap write & read buffer
			writeDataBuffer.flip();
			readDataBuffer.clear();
			readDataBuffer.put(writeDataBuffer);
			readDataBuffer.flip();
			writeDataBuffer.clear();
			awaitingData = false;
			notify();
		} else {
			DiskSlot diskSlot = getDiskSlotForWrite(writeDataBuffer.capacity());
			writeDataBuffer.flip();
			if (verbose) {
				//in case verbose mode is on, time of data writing is added to writer waiting time
				//this is the best approximation how the writerWaitingTime can be calculated
				long startTime = System.nanoTime();
				diskSlot.write(writeDataBuffer);
				writerWaitingTime += System.nanoTime() - startTime;
			} else {
				diskSlot.write(writeDataBuffer);
			}
			writeDataBuffer.clear();
		}
	}

	private DiskSlot getDiskSlotForWrite(int requestedSlotSize) {
		if (tempFile != null && tempFile.getSlotSize() == requestedSlotSize) {
			return tempFile.getDiskSlotForWrite();
		} else {
			if (tempFile != null) {
				obsoleteTempFiles.addLast(tempFile);
			}
			tempFile = new TempFile(requestedSlotSize);
			tempFile.open();

			return tempFile.getDiskSlotForWrite();
		}
	}
	
	/**
	 *  Reads next record from the buffer - FIFO order.
	 *
	 *@param  record             ByteBuffer into which store data
	 *@return                  true if successful, otherwise false - meaning no more
     *records in buffer (EOF)
	 *@exception  IOException  Description of the Exception
	 * @throws InterruptedException 
	 */
	public boolean readRecord(CloverBuffer record) throws IOException, InterruptedException {
		if (isClosed) {
			return false;
		}

        // test that we have enough data
        if (readDataBuffer.remaining() == 0) {
			secureReadBuffer();
        }
        int recordSize = decodeLength(readDataBuffer);
        if (recordSize == EOF) {
        	closeTemporarily();
            return false;
        }
        
        int oldLimit = readDataBuffer.limit(); //TODO old limit is same for all reading iteration; could be preset in secureReadBuffer()
        readDataBuffer.limit(readDataBuffer.position() + recordSize);
        record.clear();
        record.put(readDataBuffer);
        readDataBuffer.limit(oldLimit);
        record.flip();
        bufferedRecords.decrementAndGet();
        return true;
	}
    
    /**
     * Reads next record from the buffer - FIFO order.
     * 
     * @param record record into which store data
     * @return  record populated with data or NULL if no more records in buffer (EOF)
     * @throws IOException
     * @throws InterruptedException 
     * @since 27.11.2006
     */
    public DataRecord readRecord(DataRecord record) throws IOException, InterruptedException{
        if(isClosed) {
        	return null;
        }

        // test that we have enough data
        if (readDataBuffer.remaining() == 0) {
			secureReadBuffer();
        }
        
        int recordSize = decodeLength(readDataBuffer);
        if (recordSize == EOF){
        	closeTemporarily();
            return null;
        }
            
        record.deserialize(readDataBuffer);
        bufferedRecords.decrementAndGet();
        
        return record;
    }

	/**
	 * Blind record reading. If the stored record is not required, this method should be used. 
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean readRecord() throws IOException, InterruptedException {
		if (isClosed) {
			return false;
		}

        // test that we have enough data
        if (readDataBuffer.remaining() == 0) {
			secureReadBuffer();
        }

        int recordSize = decodeLength(readDataBuffer);
        if (recordSize == EOF) {
        	closeTemporarily();
            return false;
        }
        
        readDataBuffer.skip(recordSize);
        bufferedRecords.decrementAndGet();
        
        return true;
	}

    private final synchronized void secureReadBuffer() throws IOException, InterruptedException{
    	DiskSlot diskSlot = getDiskSlotForRead();
    	
        if (diskSlot != null) {
            diskSlot.read(readDataBuffer);
        } else {
        	// we may read it from writeBuffer
            // set flag that we are waiting for writer..
            awaitingData = true;
            if (verbose) {
            	//waiting time is measured in verbose mode
            	long startTime = System.nanoTime();
	            while (awaitingData) {
	                notify();
	                wait();
	            }
            	readerWaitingTime += System.nanoTime() - startTime;
            } else {
	            while (awaitingData) {
	                notify();
	                wait();
	            }
            }
        }
    }
    
    private DiskSlot getDiskSlotForRead() {
    	//first try to find a suitable disk slot in obsolete temp files
    	while (!obsoleteTempFiles.isEmpty()) {
    		TempFile obsoleteTempFile = obsoleteTempFiles.getFirst();
    		DiskSlot diskSlot = obsoleteTempFile.getDiskSlotForRead();
    		if (diskSlot != null) {
    			return diskSlot;
    		} else {
    			obsoleteTempFiles.removeFirst();
    		}
    	}
    	
    	return tempFile != null ? tempFile.getDiskSlotForRead() : null;
    }
    
    /**
     * Checks wheter readRecord operation would return data without blocking (
     * waiting for record to be written to buffer)
     * 
     * @return  true if data is ready to be read, otherwise false
     * @since 27.11.2006
     */
    public synchronized boolean hasData() {
        if (readDataBuffer.hasRemaining()) {
        	return true;
        }
        
        for (TempFile obsoleteTempFile : obsoleteTempFiles) {
        	if (obsoleteTempFile.hasData()) {
        		return true;
        	}
        }
        
        return tempFile != null ? tempFile.hasData() : false;
    }

    /**
     * Checks whether this buffer already allocated TMP file for
     * swapping buffer's content.
     * 
     * @return the hasFile
     * @since 20.11.2006
     */
    public boolean hasTempFile() {
        return tempFile != null;
    }

    /**
     * Determines number of records currently stored in buffer
     * 
     * @return number of records currently stored in buffer
     * @since 20.11.2006
     */
    public int getBufferedRecords() {
        return bufferedRecords.get();
    }
    
    /**
	 * @return internal buffers capacity (memory footprint)
     */
    public int getBufferSize() {
    	return readDataBuffer.capacity() + writeDataBuffer.capacity() + tmpDataRecord.capacity();
    }
    
	/**
	 * Is readerWaitingTime and writerWaitingTime measured?
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * Turn on/off measuring of readerWaitingTime and writerWaitingTime
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Available only in verbose mode.
	 * @return aggregated time how long the reader thread waits for data
	 */
	public long getReaderWaitingTime() {
		return readerWaitingTime / 1000000;
	}

	/**
	 * Available only in verbose mode.
	 * @return aggregated time how long the writer thread waits for data
	 */
	public long getWriterWaitingTime() {
		return writerWaitingTime / 1000000;
	}

    private static class TempFile {
    	
    	private File tempFile;
    	private FileChannel tempFileChannel;
        private final int slotSize;
    	private LinkedList<DiskSlot> emptyFileBuffers;
        private LinkedList<DiskSlot> fullFileBuffers;
        private int lastSlot;

		public TempFile(int slotSize) {
	        emptyFileBuffers = new LinkedList<DiskSlot>();
	        fullFileBuffers=new LinkedList<DiskSlot>();
	        lastSlot = -1;
	        this.slotSize = slotSize;
		}
		
		private void open() {
			try {
				tempFile = IAuthorityProxy.getAuthorityProxy(ContextProvider.getGraph()).newTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX, -1);
				tempFileChannel = new RandomAccessFile(tempFile, TMP_FILE_MODE).getChannel();
			} catch (Exception e) {
				throw new JetelRuntimeException("Can't open TMP file in", e);
			}
		}

		public void close() throws IOException {
			try {
				fullFileBuffers = null;
		        emptyFileBuffers = null;
				tempFileChannel.close();
			} finally {
		        if (!tempFile.delete()) {
		        	log.warn("Failed to delete temp file: " + tempFile.getAbsolutePath());
		        }
			}
		}

		public void reset() {
		    emptyFileBuffers.addAll(fullFileBuffers);
	        fullFileBuffers.clear();
		}

		public final int getSlotSize() {
			return slotSize;
		}
		
		public void write(CloverBuffer cloverBuffer, long position) {
			try {
				tempFileChannel.write(cloverBuffer.buf(), position);
			} catch (IOException e) {
				throw new JetelRuntimeException(e);
			}
		}

		public void read(CloverBuffer cloverBuffer, long position) {
			try {
				tempFileChannel.read(cloverBuffer.buf(), position);
			} catch (IOException e) {
				throw new JetelRuntimeException(e);
			}
		}

		public DiskSlot getDiskSlotForWrite() {
			DiskSlot diskSlot;
			
			if (emptyFileBuffers.size() > 0) {
				diskSlot = emptyFileBuffers.removeFirst();
			} else {
				diskSlot = new DiskSlot(this, (long) (++lastSlot) * slotSize);
			}
			
			fullFileBuffers.add(diskSlot);
			
			return diskSlot;
		}

		public DiskSlot getDiskSlotForRead() {
			if (!fullFileBuffers.isEmpty()) {
				DiskSlot diskSlot = fullFileBuffers.removeFirst();
				
				emptyFileBuffers.addFirst(diskSlot);
				
				return diskSlot;
			}
			
			return null;
		}
		
		public boolean hasData() {
			return !fullFileBuffers.isEmpty();
		}
    }
    
    private static class DiskSlot {
    	final TempFile tempFile;
    	final long offset;
        int usedBytes;
        
        DiskSlot(final TempFile tempFile, long offset) {
        	this.tempFile = tempFile;
        	this.offset = offset;
        }

        void write(CloverBuffer cloverBuffer) {
			usedBytes = cloverBuffer.limit();
        	tempFile.write(cloverBuffer, offset);
        }

        void read(CloverBuffer cloverBuffer) {
        	cloverBuffer.clear();
        	cloverBuffer.limit(usedBytes);
        	tempFile.read(cloverBuffer, offset);
        	cloverBuffer.flip();
        }
    }
    
}

