/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import java.lang.reflect.Field;
import java.util.Set;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;

public class TestRelectUtils extends BaseTestCase {

    private static class TestReflect {
        public TestReflect() {
            // nothing
        }
        
        private int             _anIntVal;
        private long            _aLongVal;
        private double          _aDoubleVal;
        private String          _aStrVal;
        private ReusableString  _aZStrVal;

        public int              getIntVal()      { return _anIntVal; }
        public long             getLongVal()     { return _aLongVal; }
        public double           getDoubleVal()   { return _aDoubleVal; }
        public String           getStrVal()      { return _aStrVal; }
        public ReusableString   getZStrVal()     { return _aZStrVal; }
    }
    
    public void testReflectUtils() {
        
        TestReflect r1 = new TestReflect();
        
        Set<Field> mems = ReflectUtils.getMembers( r1 );
        
        assertEquals( 5, mems.size() );

        for( Field f : mems ) {
            ReflectUtils.setMember( r1, f, "5" );
        }
        
        assertEquals( 5, r1.getIntVal() );
        assertEquals( 5, r1.getLongVal() );
        assertEquals( 5.0, r1.getDoubleVal(), 0.0000005 );
        assertEquals( "5", r1.getStrVal() );
        assertEquals( new ReusableString("5"), r1.getZStrVal() );
    }
}
