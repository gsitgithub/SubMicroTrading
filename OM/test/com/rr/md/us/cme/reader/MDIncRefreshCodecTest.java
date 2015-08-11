/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.reader;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.FixField;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.utils.HexUtils;
import com.rr.model.generated.fix.codec.MD44Decoder;
import com.rr.model.generated.fix.codec.MD44Encoder;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MDSnapEntryImpl;
import com.rr.model.generated.internal.events.impl.MDSnapshotFullRefreshImpl;
import com.rr.model.generated.internal.type.MDEntryType;
import com.rr.model.generated.internal.type.MDUpdateAction;


public class MDIncRefreshCodecTest extends BaseTestCase {

    private static final String msg1 = "1128=9|35=X|49=CME|34=5457711|52=20120403-19:43:42.222|268=1|279=1|1023=1|269=0|273=194342000|22=8|48=27069|83=7952927|270=252184990490.1|271=35|346=10|336=2|";
    private static final String snap1 = "35=W|1128=9|49=CME|34=6|52=20120403-13:28:30.030|369=3574404|911=15|83=172|1021=2|48=30381|22=8|1682=17|268=11|269=0|270=-665.0|271=150|1023=1|346=1|269=0|270=-670.0|271=30|1023=2|346=2|269=0|270=-680.0|271=20|1023=3|346=1|269=0|270=-690.0|271=20|1023=4|346=1|269=0|270=-695.0|271=20|1023=5|346=1|269=0|270=-700.0|271=20|1023=6|346=1|269=0|270=-1095.0|271=15|1023=7|346=1|269=1|270=-645.0|271=30|1023=1|346=1|269=1|270=-640.0|271=100|1023=2|346=1|269=1|270=-630.0|271=40|1023=3|346=1|269=2|270=-650.0|1020=0|451=0.0|";

    
    private final byte[] _buf = new byte[8192];
    
    private Decoder _decoder = new MD44Decoder();
    private MD44Encoder _encoder = new MD44Encoder( _buf, 0 );
    
    
    private String             _dateStr = "20120403";
    private TimeZoneCalculator _calc;

    
    @Override
    public void setUp(){
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _decoder.setTimeZoneCalculator( _calc );
        _encoder.setSenderCompId( new ViewString("CME") );
    }
    
    public MDIncRefreshCodecTest() {
        // nothing
    }

    public void testOneMDEntry() {
        MDIncRefreshImpl inc = makeUpdate( 1 );

        _encoder.encode( inc );
        
        System.out.println( new String(_buf, 0, _encoder.getLength()) );
        
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        
        checkEquals( inc, dec );
    }

    public void testFiveMDEntry() {
        MDIncRefreshImpl inc = makeUpdate( 5 );

        _encoder.encode( inc );
        
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        
        checkEquals( inc, dec );
    }

    public void testMDDecodeEncode() {
        byte[] binaryMsg = HexUtils.convert( msg1, (byte) '|', FixField.FIELD_DELIMITER );
        byte[] buf = new byte[8192];
        System.arraycopy( binaryMsg, 0, buf, 0, binaryMsg.length );

        System.out.println( new String( binaryMsg ) );
        
        int numBytes = binaryMsg.length;

        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( buf, 0, numBytes );

        _encoder.encode( dec );
        
        String tres = new String( _buf, 0, _encoder.getLength() );
        byte[] readableRes = HexUtils.convert( tres, FixField.FIELD_DELIMITER , (byte) '|' );
        String res = new String( readableRes );
        
        assertEquals( msg1, res );
    }

    public void testMDDecodeSnap() {
        byte[] binaryMsg = HexUtils.convert( snap1, (byte) '|', FixField.FIELD_DELIMITER );
        byte[] buf = new byte[8192];
        System.arraycopy( binaryMsg, 0, buf, 0, binaryMsg.length );

        System.out.println( new String( binaryMsg ) );
        
        int numBytes = binaryMsg.length;

        MDSnapshotFullRefreshImpl dec = (MDSnapshotFullRefreshImpl) _decoder.decode( buf, 0, numBytes );

        MDSnapEntryImpl root = (MDSnapEntryImpl) dec.getMDEntries();
        
        int cnt=0;

        while( root != null ) {
            ++cnt;
            root = root.getNext();
        }
        
        assertEquals( dec.getNoMDEntries(), cnt );
    }

    public static void checkEquals( MDIncRefreshImpl exp, MDIncRefreshImpl decoded ) {
        assertEquals( exp.getSendingTime(), decoded.getSendingTime() );
        assertEquals( exp.getMsgSeqNum(),   decoded.getMsgSeqNum() );
        assertEquals( exp.getPossDupFlag(), decoded.getPossDupFlag() );
        assertEquals( exp.getMsgSeqNum(),   decoded.getMsgSeqNum() );
        
        int expEntries = exp.getNoMDEntries();
        
        assertEquals( exp.getNoMDEntries(), decoded.getNoMDEntries() );
        
        MDEntryImpl expEntry     = (MDEntryImpl) exp.getMDEntries();
        MDEntryImpl decodedEntry = (MDEntryImpl) decoded.getMDEntries();
        
        for( int i=0 ; i < expEntries ; i++ ) {

            assertEquals( expEntry.getSecurityIDSource(),   decodedEntry.getSecurityIDSource() );
            assertEquals( expEntry.getSecurityID(),         decodedEntry.getSecurityID() );
            assertEquals( expEntry.getMdUpdateAction(),     decodedEntry.getMdUpdateAction() );
            assertEquals( expEntry.getRepeatSeq(),          decodedEntry.getRepeatSeq() );
            assertEquals( expEntry.getNumberOfOrders(),     decodedEntry.getNumberOfOrders() );
            assertEquals( expEntry.getMdPriceLevel(),       decodedEntry.getMdPriceLevel() );
            assertEquals( expEntry.getMdEntryType(),        decodedEntry.getMdEntryType() );
            assertEquals( expEntry.getMdEntryPx(),          decodedEntry.getMdEntryPx(), Constants.TICK_WEIGHT );
            assertEquals( expEntry.getMdEntrySize(),        decodedEntry.getMdEntrySize() );
            assertEquals( expEntry.getMdEntryTime(),        decodedEntry.getMdEntryTime() );
            
            expEntry = expEntry.getNext();
            decodedEntry = decodedEntry.getNext();
        }
        
    }

    @SuppressWarnings( "null" )
    private MDIncRefreshImpl makeUpdate( int numMDEntries ) {
        
        MDIncRefreshImpl inc = new MDIncRefreshImpl();
        
        inc.setSendingTime( 20120403194342222L );
        inc.setMsgSeqNum( 1000000 );
        inc.setPossDupFlag( false );

        inc.setNoMDEntries( numMDEntries );
        
        MDEntryImpl first = null;  
        MDEntryImpl tmp = null;
        
        for ( int i=0 ; i < numMDEntries ; i++ ) {
            
            if ( first == null ) {
                tmp = first = new MDEntryImpl();
            } else {
                tmp.setNext( new MDEntryImpl() );
                tmp = tmp.getNext();
            }
            
            tmp.setSecurityIDSource( SecurityIDSource.ExchangeSymbol );
            tmp.setSecurityID( 12345678 );
            tmp.setMdUpdateAction( MDUpdateAction.New );
            tmp.setRepeatSeq( i+1 );
            tmp.setNumberOfOrders( i*10 );
            tmp.setMdPriceLevel( i+1 );
            tmp.setMdEntryType( MDEntryType.Bid );
            tmp.setMdEntryPx( 1000.12345 + i );
            tmp.setMdEntrySize( 100+i*10 );
            tmp.setMdEntryTime( 800000+i );
        }
        
        inc.setMDEntries( first ); 

        return inc;
    }
}
