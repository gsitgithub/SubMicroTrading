/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.id;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;

public abstract class AbstractBase32IDGenerator implements IDGenerator {

    private static final byte[]         _bytes = "1234567890ABCDEFGHIJKLMNOPQRSTUVW".getBytes();

    private        final byte[]         _prefix;
    private              long           _counter;

    private        final int            _totalLen;
    private        final int            _prefixLen;
    
    /**
     * 
     * @param processInstanceSeed   
     * @param len                   length of non seed component
     */
    public AbstractBase32IDGenerator( ZString processInstanceSeed, int len ) {
        _prefix = processInstanceSeed.getBytes();
        
        _totalLen = len + _prefix.length;
        _prefixLen = _prefix.length;
        
        _counter = seed();
    }

    protected abstract long seed();

    @Override
    public void genID( ReusableString id ) {

        _counter++;

        long tmpId = _counter;

        id.setLength( _totalLen );
        
        byte[] dest = id.getBytes();
        
        int destIdx=_totalLen;
        
        while( destIdx > _prefixLen ) {
            dest[--destIdx] = _bytes[ (int)(tmpId & 0x1F) ];
            
            tmpId >>>= 5;                                   // base 32, ideally would be base 64, but only 62 alphaNum chars
        }

        while( --destIdx >= 0 ) {
            dest[destIdx] = _prefix[destIdx];
        }
    }
}
