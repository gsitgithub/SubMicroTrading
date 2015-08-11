/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import com.rr.core.codec.FixEncodeBuilder;
import com.rr.core.codec.FixEncodeBuilderImpl;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Currency;
import com.rr.core.model.Message;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.fix.model.defn.FixDictionary44;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.om.warmup.FixTestUtils;

/**
 * test the performance difference between using ReusableStrings in a NOS and ViewStrings
 *
 * @author Richard Rose
 */

public class PerfTestEncode extends BaseTestCase {

    private static final byte NOS = (byte)'D';
    
    private final byte[] buf = new byte[8192];
    private final FixEncodeBuilder encoder =  new FixEncodeBuilderImpl( buf, 0, (byte)'4',  (byte)'4' );
    private final TimeZoneCalculator   tzCalculator = new TimeZoneCalculator();

    private ViewString _senderCompID = new ViewString( "ME" );
    private ViewString _targetCompID = new ViewString( "PROPA" );
    
    public void testStringPerf() {
    
        int runs = 5;
        int iterations = 100000;
        
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long duration = perfTestReusableString( iterations );
            
            System.out.println( "MIN TEST Run " + idx + " duration=" + duration + ", aveNano=" + (duration / iterations) );
        }

        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long duration = fullTest( iterations );
            
            System.out.println( "FULL TEST Run " + idx + " duration=" + duration + ", aveNano=" + (duration / iterations) );
        }
    }
    
    public long perfTestReusableString( int iterations ) {
        
        byte             msgType      = 'D';
        int              seqNum       = 12243;                              // dummy val
        ZString          senderCompId = new ViewString( "PROPA" );
        ZString          targetCompId = new ViewString( "CLIENTXY" );
        int              sendTimeMS   = (int)(System.currentTimeMillis() % Constants.MS_IN_DAY);
        TimeInForce      tif          = TimeInForce.Day;
        SecurityIDSource secTypeId    = SecurityIDSource.RIC;
        ZString          symbol       = new ViewString( "ICAD.PA" );
        ZString          clOrdId      = new ViewString( "100621100514021" );
        HandlInst        handlInst    = HandlInst.AutoExecPublic;
        //int              txnTime      = (int)(System.currentTimeMillis() % Constants.MS_IN_DAY);
        int              qty          = 133;
        double           price        = 73.4500;
        
        long startTime = Utils.nanoTime();
        
        for ( int i=0 ; i < iterations ; i++ ) {
            final int txnTime = tzCalculator.getTimeUTC( System.currentTimeMillis() );

            encoder.start();
            
            encoder.encodeInt(          35, msgType );
            encoder.encodeInt(          34, seqNum );
            encoder.encodeString(       49, senderCompId );
            encoder.encodeUTCTimestamp( 52, sendTimeMS );
            encoder.encodeString(       56, targetCompId );
            encoder.encodeByte(         59, tif.getVal() );
            encoder.encodeByte(         22, secTypeId.getVal() );
            encoder.encodeString(       55, symbol );
            encoder.encodeString(       11, clOrdId );
            encoder.encodeByte(         21, handlInst.getVal() );
            encoder.encodeUTCTimestamp( 60, txnTime );
            encoder.encodeInt(          38, qty );
            encoder.encodePrice(        44, price );

            Utils.nanoTime();   

            encoder.encodeEnvelope();
        }
        
        long endTime = Utils.nanoTime();
        
        long duration = endTime - startTime;

        return duration;
    }
    
    public long fullTest( int iterations ) {
        
        MarketNewOrderSingleImpl msg = getNOS();
        
        long startTime = Utils.nanoTime();

        for ( int i=0 ; i < iterations ; i++ ) {
            encodeNewOrderSingle( msg );
        }

        long endTime = Utils.nanoTime();
        
        long duration = endTime - startTime;

        return duration;
    }

    public final void tmp_encodeNewOrderSingle( final MarketNewOrderSingleImpl msg ) {
        final int txnTime = tzCalculator.getTimeUTC( System.currentTimeMillis() );

        encoder.start();
        
        encoder.encodeInt(          35, NOS );
        encoder.encodeInt(          34, msg.getMsgSeqNum() );
        encoder.encodeString(       49, _senderCompID );
        encoder.encodeString(       56, _targetCompID );
        encoder.encodeString(       55, msg.getSymbol()  );
        encoder.encodeUTCTimestamp( 52, txnTime );
        encoder.encodeUTCTimestamp( 60, txnTime );
        encoder.encodeByte(         59, msg.getTimeInForce().getVal() );
        encoder.encodeByte(         22, msg.getSecurityIDSource().getVal() );
        encoder.encodeString(       11, msg.getClOrdId()  );
        encoder.encodeLong(         38, msg.getOrderQty() );
        encoder.encodeByte(         21, msg.getHandlInst().getVal() );
        encoder.encodePrice(        44, msg.getPrice() );

        Utils.nanoTime();   

        encoder.encodeEnvelope();
        
    }

    public final void tmp2_encodeNewOrderSingle( final MarketNewOrderSingleImpl msg ) {
        final int now = tzCalculator.getTimeUTC( System.currentTimeMillis() );
        encoder.start();
        encoder.encodeByte( 35, NOS );
        encoder.encodeInt( FixDictionary44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        encoder.encodeString(       49, _senderCompID );
        encoder.encodeString(       56, _targetCompID );
        encoder.encodeString( FixDictionary44.Symbol, msg.getSymbol() );        // tag55
        encoder.encodeUTCTimestamp( FixDictionary44.SendingTime, now );        // tag52
        encoder.encodeUTCTimestamp( FixDictionary44.TransactTime, now );        // tag60
        encoder.encodeString( FixDictionary44.ClOrdId, msg.getClOrdId() );        // tag11
        encoder.encodeLong( FixDictionary44.OrderQty, msg.getOrderQty() );        // tag38
        encoder.encodePrice( FixDictionary44.Price, msg.getPrice() );        // tag44
        
        final long sent = Utils.nanoTime(); // 1usec hit
        
        msg.setOrderSent( sent );        // HOOK
        encoder.encodeEnvelope();
    }

    public final void encodeNewOrderSingle( final MarketNewOrderSingleImpl msg ) {
        final int now = tzCalculator.getTimeUTC( System.currentTimeMillis() );
        encoder.start();
        encoder.encodeByte( 35, NOS );
        encoder.encodeInt( FixDictionary44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        encoder.encodeString(       49, _senderCompID );
        encoder.encodeString(       56, _targetCompID );
        encoder.encodeString( FixDictionary44.Symbol, msg.getSymbol() );        // tag55
        encoder.encodeUTCTimestamp( FixDictionary44.SendingTime, now );        // tag52
        encoder.encodeUTCTimestamp( FixDictionary44.TransactTime, now );        // tag60
        final TimeInForce tTimeInForce = msg.getTimeInForce();
        if ( tTimeInForce != null ) encoder.encodeByte( FixDictionary44.TimeInForce, tTimeInForce.getVal() );        // tag59
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) encoder.encodeByte( FixDictionary44.SecurityIDSource, tSecurityIDSource.getVal() );        // tag22
        encoder.encodeString( FixDictionary44.ClOrdId, msg.getClOrdId() );        // tag11
        final HandlInst tHandlInst = msg.getHandlInst();
        if ( tHandlInst != null ) encoder.encodeByte( FixDictionary44.HandlInst, tHandlInst.getVal() );        // tag21
        encoder.encodeLong( FixDictionary44.OrderQty, msg.getOrderQty() );        // tag38
        encoder.encodePrice( FixDictionary44.Price, msg.getPrice() );        // tag44
        
        encoder.encodeString(       49, _senderCompID );
        encoder.encodeString(       56, _targetCompID );
        encoder.encodeString( FixDictionary44.SecurityID, msg.getSecurityId() );        // tag48
        encoder.encodeByte( FixDictionary44.OrdType, msg.getOrdType().getVal() );        // tag40
        encoder.encodeByte( FixDictionary44.Side, msg.getSide().getVal() );        // tag54
        encoder.encodeString( FixDictionary44.ExDest, msg.getExDest() );        // tag100
        encoder.encodeString( FixDictionary44.Account, msg.getAccount() );        // tag1
        encoder.encodeString( FixDictionary44.Text, msg.getText() );        // tag58
        encoder.encodeString( FixDictionary44.SecurityExchange, msg.getSecurityExchange() );        // tag207
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) encoder.encodeBytes( FixDictionary44.Currency, tCurrency.getVal() );        // tag15
        final OrderCapacity tOrderCapacity = msg.getOrderCapacity();
        if ( tOrderCapacity != null ) encoder.encodeByte( FixDictionary44.OrderCapacity, tOrderCapacity.getVal() );        // tag528

        final long sent = Utils.nanoTime(); // 1usec hit
        
        msg.setOrderSent( sent );        // HOOK
        encoder.encodeEnvelope();
    }

    private MarketNewOrderSingleImpl getNOS() {
        byte[] cnos = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=209;35=D;49=CLIENTXY;56=XXWWZZQQ1;34=85299;52=" + FixTestUtils.getDateStr() + 
                                                        "-08:29:08.618;15=GBP;60=" + FixTestUtils.getDateStr() + "-08:29:08.618;1=CLNT_JSGT33;48=ICAD.PA;" +
                                                        "21=1;22=5;38=133;54=1;40=2;55=ICAD.PA;100=CHI;11=100621100514021;58=SWP;59=0;" +
                                                        "44=73.4500;10=129;" );

        Standard44Decoder decoder = FixTestUtils.getDecoder44();

        TimeZoneCalculator calc = new TimeZoneCalculator();

        calc.setDate( FixTestUtils.getDateStr() );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );

        Message m1 = decoder.decode( cnos, 0, cnos.length ); 

        ClientNewOrderSingleImpl clientNos = (ClientNewOrderSingleImpl) m1;
        MarketNewOrderSingleImpl mktNos    = FixTestUtils.getMarketNOS( clientNos );
        
        return mktNos;
    }
}
