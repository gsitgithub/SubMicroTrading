/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.field;

import com.rr.core.codec.binary.fastfix.common.def.int32.IntMandWriterDefault;
import com.rr.core.codec.binary.fastfix.msgdict.DictComponentFactory;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int64.LongMandWriterDelta;
import com.rr.core.lang.BaseTestCase;

public class ComponentFactoryTest extends BaseTestCase {

    @SuppressWarnings( "boxing" )
    public void testCompFactory() {
        DictComponentFactory cf = new DictComponentFactory();
        
        IntMandWriterDefault exp  = cf.getWriter( IntMandWriterDefault.class, "F1Exp",  100, 2 );
        LongMandWriterDelta  mant = cf.getWriter( LongMandWriterDelta.class,  "F1Mant", 100, 10000L );
        
        assertEquals( 2,     exp.getInitValue() );
        assertEquals( 10000, mant.getInitValue() );

        IntMandWriterDefault exp2  = cf.getWriter( IntMandWriterDefault.class, "F1Exp",  100, 2 );
        LongMandWriterDelta  mant2 = cf.getWriter( LongMandWriterDelta.class,  "F1Mant", 100, 10000L );
        
        assertSame( exp, exp2 );
        assertSame( mant, mant2 );
    }
}
