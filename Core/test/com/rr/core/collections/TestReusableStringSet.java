/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.collections;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;

public class TestReusableStringSet extends BaseTestCase {

    public void testSet() {
        ReusableStringSet set = new ReusableStringSet(2);
        
        assertTrue(  set.put( new ReusableString( "AAAA00001" ) ) );
        assertFalse( set.put( new ReusableString( "AAAA00001" ) ) );
        assertEquals( 1, set.size() );
        assertTrue(  set.contains( new ReusableString( "AAAA00001" ) ) );

        int max  = 16384;
        
        for ( int i=2 ; i < max ; ++ i ) {
            assertTrue(  set.put( new ReusableString( "AAAA00001" + i ) ) );
            assertEquals( i, set.size() );
        }
        
        for ( int i=2 ; i < max ; ++ i ) {
            assertTrue(  set.contains( new ReusableString( "AAAA00001" + i ) ) );
        }

        assertFalse(  set.contains( new ReusableString( "AAAA00001" + (max+2) ) ) );

        set.clear();
        assertEquals( 0, set.size() );
    }
}
