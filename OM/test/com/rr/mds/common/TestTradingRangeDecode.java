/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ViewString;
import com.rr.core.model.Instrument;
import com.rr.core.model.InstrumentLocator;
import com.rr.core.model.Message;
import com.rr.mds.common.events.TradingRangeUpdate;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.dummy.warmup.TradingRangeImpl;

public class TestTradingRangeDecode extends BaseTestCase {

    @SuppressWarnings( "boxing" )
    public void testDecode() {

        InstrumentLocator ip = new DummyInstrumentLocator();
        
        TradingRangeUpdate u1 = new TradingRangeUpdate();
        TradingRangeUpdate u2 = new TradingRangeUpdate();
        
        double  lowerU1         = 12.125009; 
        double  upperU1         = 14.990001;
        long    lowerIdU1       = 150000000001l; 
        long    upperIdU1       = 150000000002l;
        int     lowerFlagsU1    = 123456789;
        int     upperFlagsU1    = 987654321; 
        
        double  lowerU2         = 22.125009; 
        double  upperU2         = 24.990001;
        long    lowerIdU2       = 150000000003l; 
        long    upperIdU2       = 150000000004l;
        int     lowerFlagsU2    = 1;
        int     upperFlagsU2    = Integer.MAX_VALUE; 
        
        ViewString ricU1 = new ViewString( "BT.X" );
        ViewString ricU2 = new ViewString( "VOD.X" );

        u1.getRicForUpdate().copy( ricU1 );
        u2.getRicForUpdate().copy( ricU2 );
        
        Instrument iU1 = ip.getInstrumentByRIC( ricU1 );
        Instrument iU2 = ip.getInstrumentByRIC( ricU2 );
        
        TradingRangeImpl trU1 = (TradingRangeImpl) iU1.getValidTradingRange();
        TradingRangeImpl trU2 = (TradingRangeImpl) iU2.getValidTradingRange();
        
        u1.setBands( lowerU1, upperU1, lowerIdU1, upperIdU1, lowerFlagsU1, upperFlagsU1 );
        u2.setBands( lowerU2, upperU2, lowerIdU2, upperIdU2, lowerFlagsU2, upperFlagsU2 );
        
        u1.setNext( u2 );
        
        byte[] buf = new byte[ Constants.MAX_BUF_LEN ];
        MDSEncoder encoder = new MDSEncoder( buf, 0 );
        MDSDecoder decoder = new MDSDecoder();
        decoder.init( ip );
        
        encoder.encode( u1 );
        
        Message m = decoder.decode( buf, 0, buf.length );
        assertNull( m );

        assertEquals( lowerU1,      trU1.getLower() );
        assertEquals( upperU1,      trU1.getUpper() );
        assertEquals( lowerIdU1,    trU1.getLowerId() );
        assertEquals( upperIdU1,    trU1.getUpperId() );
        assertEquals( lowerFlagsU1, trU1.getLowerFlags() );
        assertEquals( upperFlagsU1, trU1.getUpperFlags() );

        assertEquals( lowerU2,      trU2.getLower() );
        assertEquals( upperU2,      trU2.getUpper() );
        assertEquals( lowerIdU2,    trU2.getLowerId() );
        assertEquals( upperIdU2,    trU2.getUpperId() );
        assertEquals( lowerFlagsU2, trU2.getLowerFlags() );
        assertEquals( upperFlagsU2, trU2.getUpperFlags() );
    }
}
