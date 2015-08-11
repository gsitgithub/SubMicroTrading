/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.order.collections;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.OMUtils;
import com.rr.om.order.Order;
import com.rr.om.order.OrderImpl;

public class OrderMapTest extends BaseTestCase {

    private static final int ORDER_SIZE = 900000;
    
    private static final ZString SOME_KEY = new ViewString( "SOMEKEY" );

    public void testConcOrderMapInsertWithResizing() {
        doInsert( new SegmentOrderMap( 2, 0.75f, 1  ), 2, ORDER_SIZE );
        doInsert( new SegmentOrderMap( 2, 0.75f, 16 ), 2, ORDER_SIZE );
        doInsert( new SegmentOrderMap( 2, 0.75f, 64 ), 2, ORDER_SIZE );
    }
    
    public void testSimpleOrderMapInsertWithResizing() {
        doInsert( new SimpleOrderMap( 2, 0.75f ), 2, ORDER_SIZE );
    }
    
    public void testSegmentOrderMapInsertWithResizing() {
        doInsert( new SegmentOrderMap( 2, 0.75f, 1  ), 2, ORDER_SIZE );
        doInsert( new SegmentOrderMap( 2, 0.75f, 16 ), 2, ORDER_SIZE );
        doInsert( new SegmentOrderMap( 2, 0.75f, 64 ), 2, ORDER_SIZE );
    }
    
    public void testConcReplace() {
        doTestReplace( new SegmentOrderMap( 16, 0.75f, 1 ) );
    }

    public void testConcReplaceB() {
        doTestReplace( new SegmentOrderMap( 16, 0.75f, 16 ) );
    }

    public void testSegmentReplace() {
        doTestReplace( new SegmentOrderMap( 16, 0.75f, 1 ) );
    }

    public void testSegmentReplaceB() {
        doTestReplace( new SegmentOrderMap( 16, 0.75f, 16 ) );
    }

    public void testSimpleReplace() {
        doTestReplace( new SimpleOrderMap( 16, 0.75f ) );
    }

    public void testProblem1()  {
        doTestFromFile( "data/testOrderMap.txt" );
    }
    
    public void testProblem2()  {
        doTestFromFile( "data/testOrderMap2.txt" );
    }
    
    private void doTestFromFile( String file )  {
        OrderMap mapNew = new SegmentOrderMap( 3000, 0.75f, 1 );
        Map<ViewString,OrderImpl> mapOld = new HashMap<ViewString,OrderImpl>( 2 );
        
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader( new FileReader( file ) );

            Set<String> keys = new LinkedHashSet<String>();
            
            for ( String line = reader.readLine() ; line != null ; line = reader.readLine() ) {
                keys.add( line.trim() );
            }
            
            ReusableString buf = new ReusableString();
            
            for( String key : keys ) {
                buf.setValue( key );
                OrderImpl order = OMUtils.mkOrder( buf, 0 ); 
                mapNew.put( order.getClientClOrdIdChain(), order );
                mapOld.put( order.getClientClOrdIdChain(), order );
            }
            
            for( String key : keys ) {
                buf.setValue( key );
                
                assertTrue( mapNew.containsKey( buf ) );
                
                Order newOrder = mapNew.get( buf );
                Order oldOrder = mapOld.get( buf );
                
                assertNotNull( oldOrder );
                assertNotNull( newOrder );
                
                assertSame( oldOrder, newOrder);
            }
            
        } catch( Exception e ) {
            fail( e.getMessage() );
        } finally {
            FileUtils.close( reader );
        }
    }
    
    private void doInsert( OrderMap mapNew, int presize, int orders ) {
        Map<ViewString,OrderImpl> mapOld = new HashMap<ViewString,OrderImpl>( 2 );
        
        int numOrders = orders;
        
        ReusableString buf = new ReusableString();
        
        for ( int i=0 ; i < numOrders ; ++i ) {
            OrderImpl order = OMUtils.mkOrder( mkKey( buf, i ), i ); 
            
            mapNew.put( order.getClientClOrdIdChain(), order );
            assertTrue( mapNew.containsKey(order.getClientClOrdIdChain() ));
            mapOld.put( order.getClientClOrdIdChain(), order );
        }
        
        assertEquals( mapOld.size(), mapNew.size() );
        
        Set<Entry<ViewString, OrderImpl>> entries = mapOld.entrySet();
        
        for( Entry<ViewString, OrderImpl> entry : entries ) {
            ViewString key = entry.getKey();
            
            assertTrue( mapNew.containsKey( key ) );
            assertTrue( mapNew.containsKey( new ViewString(key) ) );
            
            Order order = mapNew.get( key );
            assertSame( entry.getValue(), order );
            assertSame( entry.getValue(), mapNew.get( new ViewString(key) ) );
        }
        
        for ( int i=0 ; i < numOrders ; ++i ) {
            mkKey( buf, i );
            
            Order oldOrder = mapOld.get( buf );
            Order newOrder = mapNew.get( buf );
            
            assertNotNull( oldOrder );
            assertNotNull( newOrder );
            
            assertSame( oldOrder, newOrder );
        }
        
        mapNew.clear();
    }
    
    private void doTestReplace( OrderMap mapNew ) {
        ReusableString buf = new ReusableString();
        OrderImpl    order1 = OMUtils.mkOrder( mkKey( buf, 1000 ), 1000 ); 
        OrderImpl    order2 = OMUtils.mkOrder( mkKey( buf, 1001 ), 1001 ); 
        OrderImpl    order3 = OMUtils.mkOrder( mkKey( buf, 1000 ), 1002 ); 
        
        assertNull( mapNew.put( order1.getClientClOrdIdChain(), order1 ) );
        assertNull( mapNew.put( order2.getClientClOrdIdChain(), order2 ) );
        assertSame( order1, mapNew.put( order3.getClientClOrdIdChain(), order3 ) );
        assertSame( order3, mapNew.get( order1.getClientClOrdIdChain() ) );
        assertSame( order3, mapNew.get( order3.getClientClOrdIdChain() ) );
    }

    private ReusableString mkKey( ReusableString buf, int i ) {
        buf.copy( SOME_KEY ).append( 1000000+i );
        
        return buf;
    }
}
