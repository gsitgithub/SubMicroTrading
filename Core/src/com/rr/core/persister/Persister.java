/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.rr.core.lang.ReusableString;

/**
 * Messages are persisted at the boundary
 * 
 * in general the message persisted is exactly the same as the message received or sent, possibly with an extra context block if needed
 * 
 * this means on replay, the session that stored the message needs to decode it from byte[]to PoJo, replay into the session
 * also allows the session to consume the context block as needed
 * 
 * @NOTE only one thread can be writing and one thread reading,  if need more readers then SYNC the read 
 * 
 * @NOTE BE VERY CAREFUL TO CHECKOUT THE RESOURCE COST OF A PERSISTER ESPECIALLY AS EACH SESSION WILL HAVE TWO
 */
public interface Persister {

    /**
     * open required resources and get ready for persistence
     * @throws PersisterException 
     */
    public void open() throws PersisterException;
    
    /**
     * close down persistence freeing up resources
     */
    public void close();
    
    /**
     * replay all messages in the file from start too finish
     * 
     * at the end of replay the persister ready to start amending new records
     * @throws PersisterException 
     */
    
    public void replay( PersistentReplayListener listener ) throws PersisterException;
    
    /**
     * archive existing data, then start with persistence empty/clean, acts like truncation 
     * @throws PersisterException if unable to reopen persistence file
     */
    public void rollPersistence() throws PersisterException;
    
    /**
     * persist the buffer for the messsage 

     * @return          a long key which can be used to identify a persisted record used in some update methods
     * @throws PersisterException 
     * @throws IOException 
     */
    public long persist( byte[] inBuffer, int offset, int length ) throws PersisterException;

    /**
     * persist the buffer for the messsage and an optional context buffer 

     * @return          a long key which can be used to identify a persisted record used in some update methods
     * @throws PersisterException 
     * @throws IOException 
     */
    public long persist( byte[] inBuffer, int offset, int length, byte[] optional, int optOffset, int optLen ) throws PersisterException;

    /**
     * retrieve the record at the specified key location
     * @return number of bytes read into the buffer
     * @throws PersisterException 
     */
    public int read( long key, byte[] outBuffer, int offset ) throws PersisterException;
    
    /**
     * retrieve the record at the specified key location, also populates the optionalContext buffer which must be big enough to accomodate it
     * 
     * @return number of bytes placed into outBuffer
     * @throws PersisterException 
     */
    public int read( long key, byte[] outBuffer, int offset, ByteBuffer optionalContext  ) throws PersisterException;
    
    /**
     * find the record identified by the key then "OR" the upper (MSB) flag byte with supplied flag
     * 
     * as memory is being written too directly it isnt possible to force flush from CPU cache
     * so be careful using the flags
     * @throws PersisterException 
     */
    public void setUpperFlags( long persistedKey, byte flags ) throws PersisterException;

    /**
     * find the record identified by the key then "OR" the lower flag byte with supplied flag
     * 
     * as memory is being written too directly it isnt possible to force flush from CPU cache
     * so be careful using the flags
     * @throws PersisterException 
     */
    public void setLowerFlags( long persistedKey, byte flags ) throws PersisterException;
    
    /**
     * append current state to log message 
     * 
     * @param logMsg
     * @return the supplied buf
     */
    public ReusableString log( ReusableString logMsg );
}
