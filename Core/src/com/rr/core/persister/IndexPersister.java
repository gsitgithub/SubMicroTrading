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

public interface IndexPersister extends Persister {

    /**
     * persist the buffer for the messsage, also persist the index entry for appSeqNum to the address of the message in the delegate persister

     * @return          a long key which can be used to identify a persisted record used in some update methods
     * @throws PersisterException 
     * @throws IOException 
     */
    public long persistIdxAndRec( int appSeqNum, byte[] inBuffer, int offset, int length ) throws PersisterException;

    /**
     * persist the buffer for the messsage and an optional context buffer, 
     * also persist the index entry for appSeqNum to the address of the message in the delegate persister 

     * @return          a long key which can be used to identify a persisted record used in some update methods
     * @throws PersisterException 
     * @throws IOException 
     */
    public long persistIdxAndRec( int appSeqNum, byte[] inBuffer, int offset, int length, byte[] optional, int optOffset, int optLen ) throws PersisterException;

    /**
     * retrieve the record at the specified key location
     * @return number of bytes read into the buffer, can be zero if index entry for seqNum is -1
     * @throws PersisterException 
     */
    public int readFromIndex( int appSeqNum, byte[] outBuffer, int offset ) throws PersisterException;
    
    /**
     * retrieve the record at the specified key location, also populates the optionalContext buffer which must be big enough to accomodate it
     * @return number of bytes placed into outBuffer
     * @throws PersisterException 
     */
    public int readFromIndex( int appSeqNum, byte[] outBuffer, int offset, ByteBuffer optionalContext  ) throws PersisterException;

    
    /**
     * ensure each seqnum in range has an index entry, if not add one with key -1 
     * upto and including toSeqNum
     * 
     * @param fromSeqNum
     * @param toSeqNum
     * @return true if successful
     */
    public boolean addIndexEntries( int fromSeqNum, int toSeqNum );

    /**
     * wipe index entries in range 
     * 
     * @param fromSeqNum
     * @param toSeqNum
     * @return
     */
    public boolean removeIndexEntries( int fromSeqNum, int toSeqNum );

    /**
     * used in recovery to ensure index is correct
     * 
     * @param key - key returned by persister
     * @param appSeqNum - application seq num expected for supplied key
     * 
     * @return if appseqNum matched key was valid
     */
    public boolean verifyIndex( long key, int appSeqNum );
}
