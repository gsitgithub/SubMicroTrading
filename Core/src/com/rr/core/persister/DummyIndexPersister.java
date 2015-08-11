/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister;

import java.nio.ByteBuffer;

public class DummyIndexPersister extends DummyPersister implements IndexPersister {

    @Override
    public long persistIdxAndRec( int appSeqNum, byte[] inBuffer, int offset, int length ) throws PersisterException {
        return 0;
    }

    @Override
    public long persistIdxAndRec( int appSeqNum, byte[] inBuffer, int offset, int length, byte[] optional, int optOffset, int optLen ) throws PersisterException {
        return 0;
    }

    @Override
    public int readFromIndex( int appSeqNum, byte[] outBuffer, int offset ) throws PersisterException {
        return 0;
    }

    @Override
    public int readFromIndex( int appSeqNum, byte[] outBuffer, int offset, ByteBuffer optionalContext ) throws PersisterException {
        return 0;
    }

    @Override
    public boolean addIndexEntries( int fromSeqNum, int toSeqNum ) {
        return true;
    }

    @Override
    public boolean removeIndexEntries( int fromSeqNum, int toSeqNum ) {
        return true;
    }

    @Override
    public boolean verifyIndex( long key, int appSeqNum ) {
        return true;
    }
}
