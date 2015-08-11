/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti;

import com.rr.core.lang.BaseTestCase;
import com.rr.om.emea.exchange.eti.trading.ApplMsgID;

public class ApplMsgIDTest extends BaseTestCase {

    public void testInitial() {
        doTest( 0, 0, 0, 1 );
        doTest( 0, 0, 0, 100 );
        doTest( 0, 0, 0, -1 );
        doTest( 0, 0, 0, -1000 );
        doTest( 0, 0, 100, 0 );
        doTest( 0, 0, 100, 1 );
        doTest( 0, 0, -1000, 1 );
        doTest( 0, 0, 0, 0 );
    }

    public void testInitialFail() {
        doTestFail( -1, 0, 0, 0 );
        doTestFail( 0, 1, 0, 0 );
        doTestFail( 1, 1, 0, 0 );
        doTestFail( 0, -1, 0, 0 );
    }

    public void testSimple() {
        doTest( 0, 1, 0, 2 );
        doTest( 100, 2, 100, 3 );
        doTest( 10, -1, 11, 0 );
        doTest( -2, -1, -1, 0 );
        doTest( 1, Long.MAX_VALUE, 1, Long.MIN_VALUE );
        doTest( 1, Long.MIN_VALUE, 1, Long.MIN_VALUE + 1 );
        doTest( -1, Long.MAX_VALUE, -1, Long.MIN_VALUE );
        doTest( -1, Long.MIN_VALUE, -1, Long.MIN_VALUE + 1 );
    }

    public void testSimpleFail() {
        doTestFail( 0, 2, 0, 2 );
        doTestFail( 0, 2, -1, 3 );
        doTestFail( 0, 2, Long.MIN_VALUE, 3 );
        doTestFail( 100, 2, 100, 1 );
        doTestFail( 10, -1, 9, 0 );
        doTestFail( 10, -1, 12, 0 );
        doTestFail( 10, -1, 10, 0 );
        doTestFail( -2, -1, -2, 0 );
        doTestFail( -2, -1, -2, -1 );
        doTestFail( 1, Long.MAX_VALUE, 1, Long.MAX_VALUE );
        doTestFail( 1, Long.MIN_VALUE, 1, Long.MIN_VALUE );
        doTestFail( 1, 10, 1, (11 | 0x8000000000000000L) );
        doTestFail( 1, 10, 1, -11 );
        doTestFail( -1, Long.MIN_VALUE, -1, Long.MIN_VALUE + 2 );
    }

    private void doTest( long oldUpper, long oldLower, long newUpper, long newLower ) {
        ApplMsgID oldId = new ApplMsgID( oldUpper, oldLower );
        ApplMsgID newId = new ApplMsgID( newUpper, newLower );
        
        assertTrue( newId.isSequential( oldId ) );
    }

    private void doTestFail( long oldUpper, long oldLower, long newUpper, long newLower ) {
        ApplMsgID oldId = new ApplMsgID( oldUpper, oldLower );
        ApplMsgID newId = new ApplMsgID( newUpper, newLower );
        
        assertFalse( newId.isSequential( oldId ) );
    }
}
