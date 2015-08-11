/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister;

public interface PersistentReplayListener {
    
    public void started();
    
    public void completed();
    
    public void failed();
    
    /**
     * @param p         persister that is replaying the record
     * @param key       long key that can be used to reread the record later if needed
     * @param buf       buffer that holds the recovered message
     * @param offset    
     * @param len
     * @param flags     bit flags persisted with the record
     */
    public void message( Persister p, long key, byte[] buf, int offset, int len, short flags );

    /**
     * call back for a record persisted with an optional context buffer
     */
    public void message( Persister p, long key, byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, short flags );

}
