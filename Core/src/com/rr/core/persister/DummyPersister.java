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

import com.rr.core.lang.ReusableString;

public class DummyPersister implements Persister {

    @Override
    public void close() {
        // nothing
    }

    @Override
    public void open() throws PersisterException {
        // nothing
    }

    @Override
    public long persist( byte[] inBuffer, int offset, int length ) throws PersisterException {
        return 0;
    }

    @Override
    public long persist( byte[] inBuffer, int offset, int length, byte[] optional, int optOffset, int optLen ) throws PersisterException {
        return 0;
    }

    @Override
    public int read( long key, byte[] outBuffer, int offset ) throws PersisterException {
        return 0;
    }

    @Override
    public int read( long key, byte[] outBuffer, int offset, ByteBuffer optionalContext ) throws PersisterException {
        return 0;
    }

    @Override
    public void replay( PersistentReplayListener listener ) throws PersisterException {
        listener.completed();
    }

    @Override
    public void rollPersistence() throws PersisterException {
        // nothing
    }

    @Override
    public void setLowerFlags( long persistedKey, byte flags ) throws PersisterException {
        // nothing
    }

    @Override
    public void setUpperFlags( long persistedKey, byte flags ) throws PersisterException {
        // nothing
    }

    @Override
    public ReusableString log( ReusableString logMsg ) {
        return logMsg;
    }
}
