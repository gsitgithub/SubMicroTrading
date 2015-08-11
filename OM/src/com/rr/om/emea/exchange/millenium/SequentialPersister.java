/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium;

import java.nio.ByteBuffer;

import com.rr.core.lang.ZString;
import com.rr.core.persister.IndexPersister;
import com.rr.core.persister.PersisterException;
import com.rr.core.persister.memmap.MemMapPersister;
import com.rr.core.utils.ThreadPriority;

/**
 * millenium doesnt provide a unique message sequence number and doesnt take one
 * 
 * millenium persist logs simply need to act as sequential transaction logs 
 * 
 * adapts index writes into serial writes
 */

public class SequentialPersister extends MemMapPersister implements IndexPersister  {

    public SequentialPersister( ZString name, ZString fname, long filePreSize, int pageSize, ThreadPriority priority ) {
        super( name, fname, filePreSize, pageSize, priority );
    }

    @Override
    public long persistIdxAndRec( int appSeqNum, byte[] inBuffer, int offset, int length ) throws PersisterException {
        return persist( inBuffer, offset, length );
    }

    @Override
    public long persistIdxAndRec( int appSeqNum, byte[] inBuffer, int offset, int length, byte[] optional, int optOffset, int optLen ) throws PersisterException {
        return persist( inBuffer, offset, length, optional, optOffset, optLen );
    }

    @Override
    public int readFromIndex( int appSeqNum, byte[] outBuffer, int offset ) throws PersisterException {
        throw new PersisterException( "Indexing not supported by MilleniumPersister" );
    }

    @Override
    public int readFromIndex( int appSeqNum, byte[] outBuffer, int offset, ByteBuffer optionalContext ) throws PersisterException {
        throw new PersisterException( "Indexing not supported by MilleniumPersister" );
    }

    @Override
    public boolean addIndexEntries( int fromSeqNum, int toSeqNum ) {
        return false;
    }

    @Override
    public boolean removeIndexEntries( int fromSeqNum, int toSeqNum ) {
        return false;
    }

    @Override
    public boolean verifyIndex( long key, int appSeqNum ) {
        return true;
    }
}
