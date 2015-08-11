/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order.collections;

import com.rr.core.java.FieldOffsetDict;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.HasReusableType;
import com.rr.core.lang.ReusableType;
import com.rr.core.model.Message;

public class FieldOffsetDictTest extends BaseTestCase {

    @SuppressWarnings( "unused" )
    public static class Simple implements HasReusableType {

        private          long    _stuff = 0;
        private volatile Message _nextMessage = null; 
        
        private static ReusableType _type = CoreReusableType.LogEventSmall;

        @Override
        public ReusableType getReusableType() {
            return _type;
        }
    }
    
    public void testDict() {

        FieldOffsetDict s = new FieldOffsetDict( HasReusableType.class, "_stuff" );
        FieldOffsetDict d = new FieldOffsetDict( HasReusableType.class, "_nextMessage" );
        
        long offset = d.getOffset( new Simple(), true );

        long stuffOffset = s.getOffset( new Simple(), false );
        
        assertEquals( 12, offset );
        assertEquals( 16, stuffOffset );
    }
}
