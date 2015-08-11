/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.instrument;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;

public class TestTickBands extends BaseTestCase {

    public void testOneBand() {

        doTestOneBand( 0, 5, 0.0025, 3.005, 3.00251, " price 3.00251 not a multiple of 0.0025" );
        doTestOneBand( 2, 6, 2,      4,     4.00001, " price 4.00001 not a multiple of 2.0" );
        doTestOneBand( 2, 0, 2,      4,     4.00001, " price 4.00001 not a multiple of 2.0" );
    }

    public void testThreeBands() {

        TickScale bands = new TickScale( new ViewString("SCALE1") );
        bands.addBand( new TickBand(  0.0,      5.0, 0.00025 ) );
        bands.addBand( new TickBand(  5.00025, 10.0, 0.0005 ) );
        bands.addBand( new TickBand( 15.00025,  0,   0.05 ) );
        
        
        doTestThreeBands( bands,  3.005, -1.0,     " Price doesnt fall within any tick band, price=-1.0" );
        doTestThreeBands( bands,  3.005,  3.00003, " price 3.00003 not a multiple of 0.00025" );
        doTestThreeBands( bands,  3.005,  3.00251, " price 3.00251 not a multiple of 0.00025" );
        doTestThreeBands( bands,  5.0,    5.00255, " price 5.00255 not a multiple of 0.0005" );
        doTestThreeBands( bands, 17.35,  17.13,    " price 17.13 not a multiple of 0.05" );
    }

    private void doTestOneBand( double lower, double upper, double tickSize, double sampleGoodPrice, double sampleBadPrice, String err ) {
        TickScale bands = new TickScale( new ViewString("SCALE1") );
        bands.addBand( new TickBand( lower, upper, tickSize ) );
        
        assertTrue( bands.canVerifyPrice() );
        assertTrue( bands.isValid( sampleGoodPrice ) );
        assertFalse( bands.isValid( sampleBadPrice ) );
        
        ReusableString msg = new ReusableString();
        bands.writeError( sampleBadPrice, msg );
        assertEquals( err, msg.toString() );
    }

    private void doTestThreeBands( TickScale bands, 
                                   double    sampleGoodPrice, 
                                   double    sampleBadPrice, 
                                   String    err ) {
        
        assertTrue( bands.canVerifyPrice() );
        assertTrue( bands.isValid( sampleGoodPrice ) );
        assertFalse( bands.isValid( sampleBadPrice ) );
        
        ReusableString msg = new ReusableString();
        bands.writeError( sampleBadPrice, msg );
        assertEquals( err, msg.toString() );
    }

    public void testAddBadBands() {
        doTestFail(  6, 5, 0.005 );
        doTestFail( -1, 5, 0.005 );
        doTestFail(  1, 5, 0 );
    }

    public void doTestFail( double lower, double upper, double tickSize ) {
        TickScale bands = new TickScale( new ViewString("SCALE1") );
        
        try {
            bands.addBand( new TickBand( lower, upper, tickSize ) );
            
            assertTrue( "didnt fail as expected", false );
            
        } catch( RuntimeException e ) {
            assertTrue( e.getMessage().startsWith( "Invalid band" ) );
        }
    }
}
