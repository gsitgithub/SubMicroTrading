/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;

import com.rr.core.codec.BaseReject;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixField;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Currency;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.CMEDecoder;
import com.rr.model.generated.fix.codec.RecoveryStandard44Decoder;
import com.rr.model.generated.fix.codec.Standard42Decoder;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.model.generated.internal.events.impl.ClientCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRejectImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketVagueOrderRejectImpl;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.client.OMClientProfileImpl;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.order.Order;
import com.rr.om.order.OrderImpl;
import com.rr.om.order.OrderVersion;
import com.rr.om.utils.FixUtils;
import com.rr.om.warmup.sim.WarmupUtils;

/**
 * this code was originally in test package, moved to warmup to facilitate warmup
 */
public class FixTestUtils {

    private static final Logger       _log = LoggerFactory.create( FixTestUtils.class );
    
    private static String  _dateStr = today();
    private static byte[]  _dateStrBytes = _dateStr.getBytes();

    // templates for syntehsizing messages for decoding
    
    private static final  byte[] cnosP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=208;35=D;49=CLIENTXY;56=XXWWZZQQ1;34=85299;52=" );
    private static final  byte[] cnosP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.618;15=GBP;60=" );
    private static final  byte[] cnosP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.618;1=CLNT_JSGT33;48=ICAD.PA;21=1;22=5;38=" );
    private static final  byte[] cnosP4 = FixTestUtils.semiColonToFixDelim( ";54=1;40=2;55=ICAD.PA;100=TST;11=" );
    private static final  byte[] cnosP5 = FixTestUtils.semiColonToFixDelim( ";58=SWP;59=0;44=" );
    private static final  byte[] cnosP6 = FixTestUtils.semiColonToFixDelim( ";10=999;" );
    
    private static final  byte[] ccrrP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=209;35=G;49=CLIENTXY;56=XXWWZZQQ1;34=85299;52=" );
    private static final  byte[] ccrrP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.618;15=GBP;60=" );
    private static final  byte[] ccrrP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.618;1=CLNT_JSGT33;48=ICAD.PA;21=1;22=5;38=" );
    private static final  byte[] ccrrP4 = FixTestUtils.semiColonToFixDelim( ";54=1;40=2;55=ICAD.PA;100=TST;11=" );
    private static final  byte[] ccrrP5 = FixTestUtils.semiColonToFixDelim( ";58=SWP;59=0;44=" );
    private static final  byte[] ccrrP6 = FixTestUtils.semiColonToFixDelim( ";41=" );
    private static final  byte[] ccrrP7 = FixTestUtils.semiColonToFixDelim( ";10=999;" );
    
    private static final  byte[] mackP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=225;35=8;49=CHIX;56=ME01;34=85795;52=" ); 
    private static final  byte[] mackP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;57=ST;20=0;60=" ); 
    private static final  byte[] mackP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;37=" ); 
    private static final  byte[] mackP4 = FixTestUtils.semiColonToFixDelim( ";150=0;39=0;40=2;54=1;38=" );  
    private static final  byte[] mackP5 = FixTestUtils.semiColonToFixDelim( ";55=ICADp;44=" ); 
    private static final  byte[] mackP6 = FixTestUtils.semiColonToFixDelim( ";47=P;59=0;109=ME01;14=0;6=0.00;17=" );  
    private static final  byte[] mackP7 = FixTestUtils.semiColonToFixDelim( ";32=0;31=0.00;151=133;11=" );  
    private static final  byte[] mackP8 = FixTestUtils.semiColonToFixDelim( ";10=999;" );
    
    private static final  byte[] mrepP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=225;35=8;49=CHIX;56=ME01;34=85795;52=" ); 
    private static final  byte[] mrepP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;57=ST;20=0;60=" ); 
    private static final  byte[] mrepP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;37=" ); 
    private static final  byte[] mrepP4 = FixTestUtils.semiColonToFixDelim( ";150=5;39=5;40=2;54=1;38=" );  
    private static final  byte[] mrepP5 = FixTestUtils.semiColonToFixDelim( ";55=ICADp;44=" ); 
    private static final  byte[] mrepP6 = FixTestUtils.semiColonToFixDelim( ";47=P;59=0;109=ME01;14=0;6=0.00;17=" );  
    private static final  byte[] mrepP7 = FixTestUtils.semiColonToFixDelim( ";32=0;31=0.00;151=133;11=" );  
    private static final  byte[] mrepP8 = FixTestUtils.semiColonToFixDelim( ";41=" );
    private static final  byte[] mrepP9 = FixTestUtils.semiColonToFixDelim( ";10=999;" );
    
    private static final  byte[] mcanP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=225;35=8;49=CHIX;56=ME01;34=85795;52=" ); 
    private static final  byte[] mcanP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;57=ST;20=0;60=" ); 
    private static final  byte[] mcanP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;37=" ); 
    private static final  byte[] mcanP4 = FixTestUtils.semiColonToFixDelim( ";150=4;39=4;40=2;54=1;38=" );  
    private static final  byte[] mcanP5 = FixTestUtils.semiColonToFixDelim( ";55=ICADp;44=" ); 
    private static final  byte[] mcanP6 = FixTestUtils.semiColonToFixDelim( ";47=P;59=0;109=ME01;14=0;6=0.00;17=" );  
    private static final  byte[] mcanP7 = FixTestUtils.semiColonToFixDelim( ";32=0;31=0.00;151=133;11=" );  
    private static final  byte[] mcanP8 = FixTestUtils.semiColonToFixDelim( ";41=" );  
    private static final  byte[] mcanP9 = FixTestUtils.semiColonToFixDelim( ";10=999;" );
    
    private static final  byte[] ccanP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=187;35=F;49=CLIENTXY;56=XXWWZZQQ1;34=85299;52=" );
    private static final  byte[] ccanP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.618;15=GBP;60=" );
    private static final  byte[] ccanP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.618;48=ICAD.PA;54=1;55=ICAD.PA;100=TST;11=" );
    private static final  byte[] ccanP4 = FixTestUtils.semiColonToFixDelim( ";58=SWP;59=0;41=" );
    private static final  byte[] ccanP5 = FixTestUtils.semiColonToFixDelim( ";10=999;" );

    private static final  byte[] mcanrejP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=186;35=9;49=CHIX;56=ME01;34=85795;52=" ); 
    private static final  byte[] mcanrejP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;57=ST;20=0;60=" ); 
    private static final  byte[] mcanrejP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;37=" ); 
    private static final  byte[] mcanrejP4 = FixTestUtils.semiColonToFixDelim( ";58=" );  
    private static final  byte[] mcanrejP5 = FixTestUtils.semiColonToFixDelim( ";102=" ); 
    private static final  byte[] mcanrejP6 = FixTestUtils.semiColonToFixDelim( ";434=" );  
    private static final  byte[] mcanrejP7 = FixTestUtils.semiColonToFixDelim( ";11=" );  
    private static final  byte[] mcanrejP8 = FixTestUtils.semiColonToFixDelim( ";41=" );  
    private static final  byte[] mcanrejP9 = FixTestUtils.semiColonToFixDelim( ";39=" );
    private static final  byte[] mcanrejPA = FixTestUtils.semiColonToFixDelim( ";10=999;" );
    
    private static final  byte[] mfillP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=218;35=8;49=CHIX;56=ME01;34=85795;52=" ); 
    private static final  byte[] mfillP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;57=ST;20=0;60=" ); 
    private static final  byte[] mfillP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;37=" ); 
    private static final  byte[] mfillP4 = FixTestUtils.semiColonToFixDelim( ";150=F;39=" );
    private static final  byte[] mfillP5 = FixTestUtils.semiColonToFixDelim( ";40=2;54=1;32=" ); 
    private static final  byte[] mfillP6 = FixTestUtils.semiColonToFixDelim( ";55=ICADp;31=" ); 
    private static final  byte[] mfillP7 = FixTestUtils.semiColonToFixDelim( ";47=P;59=0;109=ME01;14=0;6=0.00;17=" );  
    private static final  byte[] mfillP8 = FixTestUtils.semiColonToFixDelim( ";151=133;11=" );  
    private static final  byte[] mfillP9 = FixTestUtils.semiColonToFixDelim( ";10=999;" );

    private static final  byte[] mtrcanP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=218;35=8;49=CHIX;56=ME01;34=85795;52=" ); 
    private static final  byte[] mtrcanP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;57=ST;20=0;60=" ); 
    private static final  byte[] mtrcanP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;37=" ); 
    private static final  byte[] mtrcanP4 = FixTestUtils.semiColonToFixDelim( ";150=H;39=" );
    private static final  byte[] mtrcanP5 = FixTestUtils.semiColonToFixDelim( ";40=2;54=1;32=" ); 
    private static final  byte[] mtrcanP6 = FixTestUtils.semiColonToFixDelim( ";55=ICADp;31=" ); 
    private static final  byte[] mtrcanP7 = FixTestUtils.semiColonToFixDelim( ";47=P;59=0;109=ME01;14=0;6=0.00;17=" );  
    private static final  byte[] mtrcanP8 = FixTestUtils.semiColonToFixDelim( ";151=133;11=" );  
    private static final  byte[] mtrcanP9 = FixTestUtils.semiColonToFixDelim( ";19=" );
    private static final  byte[] mtrcanPA = FixTestUtils.semiColonToFixDelim( ";10=999;" );

    private static final  byte[] mtrcorP1 = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=218;35=8;49=CHIX;56=ME01;34=85795;52=" ); 
    private static final  byte[] mtrcorP2 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;57=ST;20=0;60=" ); 
    private static final  byte[] mtrcorP3 = FixTestUtils.semiColonToFixDelim( "-08:29:08.622;37=" ); 
    private static final  byte[] mtrcorP4 = FixTestUtils.semiColonToFixDelim( ";150=G;39=" );
    private static final  byte[] mtrcorP5 = FixTestUtils.semiColonToFixDelim( ";40=2;54=1;32=" ); 
    private static final  byte[] mtrcorP6 = FixTestUtils.semiColonToFixDelim( ";55=ICADp;31=" ); 
    private static final  byte[] mtrcorP7 = FixTestUtils.semiColonToFixDelim( ";47=P;59=0;109=ME01;14=0;6=0.00;17=" );  
    private static final  byte[] mtrcorP8 = FixTestUtils.semiColonToFixDelim( ";151=133;11=" );  
    private static final  byte[] mtrcorP9 = FixTestUtils.semiColonToFixDelim( ";19=" );
    private static final  byte[] mtrcorPA = FixTestUtils.semiColonToFixDelim( ";10=999;" );


    public static void setDateStr( String testDate ) {
        _dateStr = testDate;
        _dateStrBytes = _dateStr.getBytes();
    }
    
    private static String today() {
        DateFormat utcFormat = new SimpleDateFormat( "yyyyMMdd" );
        String today = utcFormat.format( new Date() );
        FixTestUtils.setDateStr( today );
        return today;
    }

    public static String getDateStr() {
        return _dateStr;
    }
   
    public static ClientProfile getTestClient() {
        ZString id = new ViewString( "TestClient" );
        return new OMClientProfileImpl( id, Double.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE, 60, 80, 90 );
    }

    public static byte[] getDateBytes() {
        return _dateStrBytes;
    }
   
    public static Map<String, String> msgToMap( String msg ) {

        Map<String,String> vals = new LinkedHashMap<String,String>();
        
        byte[] bytes = msg.getBytes();
        
        int startKey = 0;
        int endKey = -1;
        int startVal = -1;
        int endVal =-1;
        
        for( int i=0 ; i < bytes.length ;++i ) {
        
            byte b = bytes[i];
            
            if ( b == '=' ) {
                endKey = i;
                startVal = i+1;
            } else if ( b == FixField.FIELD_DELIMITER ) {
                endVal = i;
                if ( endKey > 0 ){
                    String key = msg.substring(startKey,endKey);
                    String val = msg.substring(startVal,endVal); 
                    vals.put( key, val );
                }
                startKey = i+1;
                endKey = -1;
            }
        }
        
        return vals;
    }

    public static String toFixDelim( String msg ) {
        StringBuilder sb = new StringBuilder();
        
        char[] chars = msg.toCharArray();
        
        for( int i=0 ; i < chars.length-1 ; ++i ) {

            if ( chars[ i ] == ';' && chars[ i+1 ] == ' ' ) {
                sb.append( (char) FixField.FIELD_DELIMITER );
                ++i;
            } else {
                sb.append( chars[i] );
            }
        }
        
        if ( chars[ chars.length-1 ] == ';' ) {
            sb.append( FixField.FIELD_DELIMITER );
        }
        
        return sb.toString();
    }

    public static byte[] semiColonToFixDelim( String msg ) {
        StringBuilder sb = new StringBuilder();
        
        char[] chars = msg.toCharArray();
        
        for( int i=0 ; i < chars.length ; ++i ) {

            if ( chars[ i ] == ';' ) {
                sb.append( (char) FixField.FIELD_DELIMITER );
            } else {
                sb.append( chars[i] );
            }
        }
        
        return sb.toString().getBytes();
    }

    public static String semiColonToFixDelimStr( String msg ) {
        StringBuilder sb = new StringBuilder();
        
        char[] chars = msg.toCharArray();
        
        for( int i=0 ; i < chars.length ; ++i ) {

            if ( chars[ i ] == ';' ) {
                sb.append( (char) FixField.FIELD_DELIMITER );
            } else {
                sb.append( chars[i] );
            }
        }
        
        return sb.toString();
    }

    public static void compare( String expectedMsg, String checkMsg, boolean ignoreSendTime ) {

        Map<String,String> expected = msgToMap( expectedMsg );
        Map<String,String> check    = msgToMap( checkMsg );
        
        for( String key : expected.keySet() ) {

            String expVal   = expected.get( key );
            String checkVal = check.get( key );

            if ( ignoreSendTime && ("52".equals( key ) || "60".equals( key ) || "10".equals( key ) ) ) {
                continue;
            } 
            
            Assert.assertEquals( "tag " + key, expVal, checkVal );
        }
        
        for( String key : check.keySet() ) {

            String checkVal = check.get( key );

            if ( ignoreSendTime && ("52".equals( key ) || "60".equals( key ) || "10".equals( key ) ) ) {
                continue;
            } 
            
            Assert.assertTrue( "Unexpected tag " + key + " val=" + checkVal, expected.containsKey( key ) );
        }
        
        Assert.assertEquals( expected.size(), check.size() );
    }

    public static void compare( String      expectedMsg, 
                                String      checkMsg, 
                                Set<String> tagsToCompare, 
                                boolean     tagsAreIgnore, 
                                boolean     ignoreSendTime ) {
        
        Map<String,String> expected = msgToMap( expectedMsg );
        Map<String,String> check    = msgToMap( checkMsg );
        
        if ( tagsAreIgnore ) {
            for( String key : expected.keySet() ) {

                if ( tagsToCompare.contains( key ) ) {
                    continue;
                }

                String expVal   = expected.get( key );
                String checkVal = check.get( key );

                if ( ignoreSendTime && ("52".equals( key ) || "60".equals( key ) || "10".equals( key ) ) ) {
                    continue;
                } 
                
                if ( "44".equals( key ) || "6".equals(  key  ) || "31".equals(  key  ) ) {
                    Assert.assertEquals( "tag " + key, Double.parseDouble( expVal ), Double.parseDouble( checkVal ), Constants.TICK_WEIGHT );
                } else{
                    Assert.assertEquals( "tag " + key, expVal, checkVal );
                }
            }
        } else {
            for( String key : tagsToCompare ) {
                
                String expVal   = expected.get( key );
                String checkVal = check.get( key );
    
                if ( ignoreSendTime && ("52".equals( key ) || "60".equals( key ) || "10".equals( key ) ) ) {
                    continue;
                } 
                
                if ( "44".equals( key ) || "31".equals( key ) || "6".equals(  key  ) ) {
                    Assert.assertEquals( "tag " + key, Double.parseDouble( expVal ), Double.parseDouble( checkVal ), Constants.TICK_WEIGHT );
                } else{
                    Assert.assertEquals( "tag " + key, expVal, checkVal );
                }
            }
        }
    }
    
    public static ClientNewOrderAckImpl getClientAck( MarketNewOrderAckImpl mktAck, ClientNewOrderSingleImpl nosEvent ) {
        
        ClientNewOrderAckImpl clientAck = new ClientNewOrderAckImpl();
        
        clientAck.setSrcEvent( nosEvent );
        
        clientAck.getOrderIdForUpdate().setValue( mktAck.getOrderId() ); 
        clientAck.getExecIdForUpdate().setValue( mktAck.getExecId() ); 
        clientAck.setExecType( mktAck.getExecType() ); 
        clientAck.setOrdStatus( mktAck.getOrdStatus() ); 
        clientAck.setMsgSeqNum( mktAck.getMsgSeqNum() );
        clientAck.setLeavesQty( nosEvent.getOrderQty() );
        
        return clientAck;
    }

    public static MarketNewOrderSingleImpl getMarketNOS( ClientNewOrderSingleImpl clientNOS ) {
        MarketNewOrderSingleImpl mktNOS = new MarketNewOrderSingleImpl();
        
        mktNOS.setSrcEvent( clientNOS );
        mktNOS.getClOrdIdForUpdate().setValue( clientNOS.getClOrdId() );
        mktNOS.setPrice( clientNOS.getPrice() );
        mktNOS.setOrderQty( clientNOS.getOrderQty() );
        mktNOS.setCurrency( clientNOS.getInstrument().getCurrency() );
        mktNOS.setSrcEvent( clientNOS );
        mktNOS.setMsgSeqNum( clientNOS.getMsgSeqNum() );
        
        return mktNOS;
    }
    
    public static ClientNewOrderSingleImpl getClientNOS( FixDecoder decoder, String clOrdId, int qty, double price ) {

        ReusableString rs  = new ReusableString( "8=FIX.4.2;9=209;35=D;49=CLIENTXY;56=XXWWZZQQ1;34=85299;52=" );
        rs.append( FixTestUtils.getDateStr() ).append( "-08:29:08.618;15=GBP;60=" );
        rs.append( FixTestUtils.getDateStr() ).append( "-08:29:08.618;1=CLNT_JSGT33;48=ICAD.PA;21=1;22=5;38=" ).append( qty );
        rs.append( ";133;54=1;40=2;55=ICAD.PA;100=TST;11=" ).append( clOrdId ).append( ";58=SWP;59=0;44=" ).append( price ).append( ";10=999;" );
        
        byte[] cnos = FixTestUtils.semiColonToFixDelim( rs.toString() );

        Message m1 = WarmupUtils.doDecode( decoder, cnos, 0, cnos.length );

        if ( m1 instanceof BaseReject<?> ) {
            BaseReject<?> rej = (BaseReject<?>)m1;
            
            _log.info( "Failed to decode [" + new String(cnos) + "] " + rej.getMessage() );
            
            return null;
        }
        
        return (ClientNewOrderSingleImpl) m1;
    }

    public static Standard44Decoder getDecoder44( ClientProfile testClient ) {
        Standard44Decoder decoder = new Standard44Decoder( (byte)'4', (byte)'4' );
        decoder.setClientProfile( testClient );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setDate( FixTestUtils.getDateStr() );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        decoder.setValidateChecksum( true );
        return decoder;
    }

    public static Standard42Decoder getDecoder42( ClientProfile testClient ) {
        Standard42Decoder decoder = new Standard42Decoder( (byte)'4', (byte)'2' );
        decoder.setClientProfile( testClient );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setDate( FixTestUtils.getDateStr() );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        decoder.setValidateChecksum( true );
        return decoder;
    }

    public static CMEDecoder getDecoderCME42( ClientProfile testClient ) {
        CMEDecoder decoder = new CMEDecoder( (byte)'4', (byte)'2' );
        decoder.setClientProfile( testClient );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setDate( FixTestUtils.getDateStr() );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        decoder.setValidateChecksum( true );
        return decoder;
    }

    public static RecoveryStandard44Decoder getRecoveryDecoder44( ClientProfile testClient ) {
        RecoveryStandard44Decoder decoder = new RecoveryStandard44Decoder( (byte)'4', (byte)'4' );
        decoder.setClientProfile( testClient );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setDate( FixTestUtils.getDateStr() );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        decoder.setValidateChecksum( false );
        return decoder;
    }

    public static Standard44Encoder getEncoder44( byte[] buf, int offset ) {
        Standard44Encoder encoder = new Standard44Encoder( (byte)'4', (byte)'4', buf, offset );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setDate( FixTestUtils.getDateStr() );
        encoder.setTimeZoneCalculator( calc );
        return encoder;
    }

    public static Standard44Decoder getDecoder44() {
        return getDecoder44( getTestClient() );
    }

    public static Standard42Decoder getDecoder42() {
        return getDecoder42( getTestClient() );
    }

    public static CMEDecoder getDecoderCME42() {
        return getDecoderCME42( getTestClient() );
    }

    public static RecoveryStandard44Decoder getRecoveryDecoder44() {
        return getRecoveryDecoder44( getTestClient() );
    }

    public static ClientNewOrderSingleImpl getClientNOS( FixDecoder decoder, String clOrdId, int qty, double price, MessageHandler handler ) {
        ClientNewOrderSingleImpl m = getClientNOS( decoder, clOrdId, qty, price );
        m.setMessageHandler( handler );
        return m;
    }

    public static<T extends Message> T getMessage( String msg, FixDecoder decoder, MessageHandler msgHandler ) {
        
        msg = FixUtils.chkDelim( msg );
        
        ReusableString buffer = new ReusableString( msg );
        
        decoder.setReceived( Utils.nanoTime() );
        
        @SuppressWarnings( "unchecked" )
        T m1 = (T) WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        m1.setMessageHandler( msgHandler );
        
        return m1;
    }

    public static ClientNewOrderSingleImpl getClientNOS( ReusableString     buffer, 
                                                         FixDecoder         decoder, 
                                                         ReusableString     key, 
                                                         int                qty, 
                                                         double             price,
                                                         MessageHandler     msgHandler ) {
        
        buffer.reset();
        buffer.append( cnosP1 );
        buffer.append( FixTestUtils.getDateBytes() );
        buffer.append( cnosP2 );
        buffer.append( FixTestUtils.getDateBytes() );
        buffer.append( cnosP3 );
        buffer.append( qty );
        buffer.append( cnosP4 );
        buffer.append( key );
        buffer.append( cnosP5 );
        buffer.append( price );
        buffer.append( cnosP6 );
        
        decoder.setReceived( Utils.nanoTime() );
        ClientNewOrderSingleImpl m1 = (ClientNewOrderSingleImpl) WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        m1.setMessageHandler( msgHandler );
        
        return m1;
    }

    public static ClientCancelRequestImpl getClientCancelRequest( ReusableString     buffer, 
                                                                  FixDecoder         decoder, 
                                                                  ViewString         clOrdId, 
                                                                  ViewString         origClOrdId, 
                                                                  MessageHandler     msgHandler ) {
        
        buffer.reset();
        buffer.append( ccanP1 );
        buffer.append( FixTestUtils.getDateBytes() );
        buffer.append( ccanP2 );
        buffer.append( FixTestUtils.getDateBytes() );
        buffer.append( ccanP3 );
        buffer.append( clOrdId );
        buffer.append( ccanP4 );
        buffer.append( origClOrdId );
        buffer.append( ccanP5 );
        
        decoder.setReceived( Utils.nanoTime() );
        Message m1 = WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        m1.setMessageHandler( msgHandler );

        return (ClientCancelRequestImpl) m1;
    }

    public static Cancelled getCancelled( ReusableString buffer, 
                                          FixDecoder     decoder,
                                          ViewString     clOrdId, 
                                          ViewString     origClOrdId, 
                                          int            qty, 
                                          double         price, 
                                          ViewString     orderId, 
                                          ViewString     execId ) { 
        
        buffer.reset();

        buffer.append( mcanP1 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mcanP2 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mcanP3 );
        buffer.append( orderId );
        buffer.append( mcanP4 );
        buffer.append( qty );
        buffer.append( mcanP5 );
        buffer.append( price ); 
        buffer.append( mcanP6 );
        buffer.append( execId ); 
        buffer.append( mcanP7 );
        buffer.append( clOrdId ); 
        buffer.append( mcanP8 );
        buffer.append( origClOrdId ); 
        buffer.append( mcanP9 );
        
        Message m1 = WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        Cancelled ack = (Cancelled)m1;
        
        return ack;
    }

    public static MarketCancelRejectImpl getMarketCancelReject( ReusableString   buffer, 
                                                                FixDecoder       decoder,
                                                                ZString          clOrdId, 
                                                                ZString          origClOrdId, 
                                                                ZString          orderId,
                                                                ZString          rejectReason, 
                                                                CxlRejReason     reason, 
                                                                CxlRejResponseTo msgTypeRejected, 
                                                                OrdStatus        status ) {
        
        buffer.reset();

        buffer.append( mcanrejP1 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mcanrejP2 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mcanrejP3 );
        buffer.append( orderId );
        buffer.append( mcanrejP4 );
        buffer.append( rejectReason );
        buffer.append( mcanrejP5 );
        buffer.append( reason.getVal() ); 
        buffer.append( mcanrejP6 );
        buffer.append( msgTypeRejected.getVal() ); 
        buffer.append( mcanrejP7 );
        buffer.append( clOrdId ); 
        buffer.append( mcanrejP8 );
        buffer.append( origClOrdId ); 
        buffer.append( mcanrejP9 );
        buffer.append( status.getVal() ); 
        buffer.append( mcanrejPA );
        
        Message m1 = WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        MarketCancelRejectImpl reject = (MarketCancelRejectImpl)m1;
        
        return reject;
    }

    public static MarketVagueOrderRejectImpl getMarketVagueReject( ZString clOrdId, ZString rejectReason, boolean isTerminal ) {
        
        MarketVagueOrderRejectImpl reject = new MarketVagueOrderRejectImpl();
        reject.getClOrdIdForUpdate().copy( clOrdId );
        reject.getTextForUpdate().copy( rejectReason );
        reject.setIsTerminal( isTerminal );
        
        return reject;
    }

    public static MarketNewOrderAckImpl getMarketACK( ReusableString buffer, 
                                                      FixDecoder     decoder,
                                                      ViewString     clOrdId, 
                                                      int            qty, 
                                                      double         price, 
                                                      ReusableString orderId, 
                                                      ReusableString execId ) { 
        
        buffer.reset();

        buffer.append( mackP1 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mackP2 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mackP3 );
        buffer.append( orderId );
        buffer.append( mackP4 );
        buffer.append( qty );
        buffer.append( mackP5 );
        buffer.append( price ); 
        buffer.append( mackP6 );
        buffer.append( execId ); 
        buffer.append( mackP7 );
        buffer.append( clOrdId ); 
        buffer.append( mackP8 );
        
        long now = Utils.nanoTime();
        Message m1 = WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        MarketNewOrderAckImpl ack = (MarketNewOrderAckImpl)m1;
        
        ack.setAckReceived( now );

        return ack;
    }

    public static TradeNew getMarketTradeNew( ReusableString           buffer, 
                                              FixDecoder               decoder,
                                              ZString                  mktOrderId, 
                                              ZString                  mktClOrdId, 
                                              OrderRequest             creq,          // client request 
                                              int                      lastQty,
                                              double                   lastPx,
                                              ZString                  fillExecId ) {
        buffer.reset();

        buffer.append( mfillP1 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mfillP2 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mfillP3 );
        buffer.append( mktOrderId );
        buffer.append( mfillP4 );
        
        if ( lastQty >= creq.getOrderQty() ) {
            buffer.append( OrdStatus.Filled.getVal() ); 
        } else {
            buffer.append( OrdStatus.PartiallyFilled.getVal() ); 
        }
        buffer.append( mfillP5 );
        buffer.append( lastQty );
        buffer.append( mfillP6 );
        buffer.append( lastPx ); 
        buffer.append( mfillP7 );
        buffer.append( fillExecId ); 
        buffer.append( mfillP8 );
        buffer.append( mktClOrdId ); 
        buffer.append( mfillP9 );
        
        Message m1 = WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        TradeNew fill = (TradeNew)m1;
        
        return fill;
    }

    public static TradeNew getMarketTradeNew( ReusableString           buffer, 
                                              FixDecoder               decoder,
                                              ZString                  mktOrderId, 
                                              ZString                  mktClOrdId, 
                                              int                      ordQty,           
                                              int                      lastQty,
                                              double                   lastPx,
                                              ZString                  fillExecId ) {
        buffer.reset();

        buffer.append( mfillP1 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mfillP2 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mfillP3 );
        buffer.append( mktOrderId );
        buffer.append( mfillP4 );

        if ( lastQty >= ordQty ) {
            buffer.append( OrdStatus.Filled.getVal() ); 
        } else {
            buffer.append( OrdStatus.PartiallyFilled.getVal() ); 
        }
        buffer.append( mfillP5 );
        buffer.append( lastQty );
        buffer.append( mfillP6 );
        buffer.append( lastPx ); 
        buffer.append( mfillP7 );
        buffer.append( fillExecId ); 
        buffer.append( mfillP8 );
        buffer.append( mktClOrdId ); 
        buffer.append( mfillP9 );
        
        Message m1 = WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        TradeNew fill = (TradeNew)m1;
        
        return fill;
    }

    public static NewOrderAck getMarketACK( FixDecoder decoder, String clOrdId, int qty, double price, ZString mktOrderId, ZString execId ) {
        // TOODO refactor to get rid of temp objs
        byte[] mack = FixTestUtils.semiColonToFixDelim( "8=FIX.4.2;9=225;35=8;49=CHIX;56=ME01;34=85795;52=" + FixTestUtils.getDateStr() + 
                                                        "-08:29:08.622;57=ST;20=0;60=" + FixTestUtils.getDateStr() + "-08:29:08.622;" +
                                                        "37=" + new String(mktOrderId.getBytes()) + ";150=0;39=0;40=2;54=1;38=" + qty + ";55=ICADp;44=" + 
                                                        price + ";47=P;" + "59=0;109=ME01;14=0;6=0.00;17=" + 
                                                        new String(execId.getBytes()) + ";32=0;31=0.00;151=133;" +
                                                        "11=" + clOrdId + ";10=999;" );

        decoder.setReceived( Utils.nanoTime() );

        Message m1 = WarmupUtils.doDecode( decoder, mack, 0, mack.length ); 

        return (NewOrderAck) m1;
    }

    public static ClientCancelReplaceRequestImpl getClientCancelReplaceRequest( ReusableString    buffer, 
                                                                                FixDecoder        decoder, 
                                                                                ZString           clOrdId,
                                                                                ZString           origClOrdId, 
                                                                                int               qty, 
                                                                                double            price,
                                                                                MessageHandler    msgHandler ) {
        
        buffer.reset();
        buffer.append( ccrrP1 );
        buffer.append( FixTestUtils.getDateBytes() );
        buffer.append( ccrrP2 );
        buffer.append( FixTestUtils.getDateBytes() );
        buffer.append( ccrrP3 );
        buffer.append( qty );
        buffer.append( ccrrP4 );
        buffer.append( clOrdId );
        buffer.append( ccrrP5 );
        buffer.append( price );
        buffer.append( ccrrP6 );
        buffer.append( origClOrdId );
        buffer.append( ccrrP7 );
        
        decoder.setReceived( Utils.nanoTime() );
        ClientCancelReplaceRequestImpl m1 = (ClientCancelReplaceRequestImpl) WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        m1.setMessageHandler( msgHandler );
        
        return m1;
    }

    public static Replaced getMarketReplaced( ReusableString buffer, 
                                              FixDecoder     decoder,
                                              ZString        clOrdId, 
                                              ZString        origClOrdId, 
                                              int            qty, 
                                              double         price, 
                                              ZString        orderId, 
                                              ZString        execId ) { 
        
        buffer.reset();

        buffer.append( mrepP1 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mrepP2 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mrepP3 );
        buffer.append( orderId );
        buffer.append( mrepP4 );
        buffer.append( qty );
        buffer.append( mrepP5 );
        buffer.append( price ); 
        buffer.append( mrepP6 );
        buffer.append( execId ); 
        buffer.append( mrepP7 );
        buffer.append( clOrdId ); 
        buffer.append( mrepP8 );
        buffer.append( origClOrdId ); 
        buffer.append( mrepP9 );
        
        Message m1 = WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        Replaced rep = (Replaced)m1;
        
        return rep;
    }

    public static TradeCancel getMarketTradeCancel( ReusableString           buffer, 
                                                    FixDecoder               decoder,
                                                    ZString                  mktOrderId, 
                                                    ZString                  mktClOrdId, 
                                                    OrderRequest             creq,          // client request 
                                                    int                      lastQty,
                                                    double                   lastPx,
                                                    ZString                  execId,
                                                    ZString                  execRefId,
                                                    OrdStatus                ordStatus ) {
        buffer.reset();

        buffer.append( mtrcanP1 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mtrcanP2 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mtrcanP3 );
        buffer.append( mktOrderId );
        buffer.append( mtrcanP4 );
        buffer.append( ordStatus.getVal() ); 
        buffer.append( mtrcanP5 );
        buffer.append( lastQty );
        buffer.append( mtrcanP6 );
        buffer.append( lastPx ); 
        buffer.append( mtrcanP7 );
        buffer.append( execId ); 
        buffer.append( mtrcanP8 );
        buffer.append( mktClOrdId ); 
        buffer.append( mtrcanP9 );
        buffer.append( execRefId ); 
        buffer.append( mtrcanPA );
        
        Message m1 = WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        TradeCancel tradeCancell = (TradeCancel)m1;
        
        return tradeCancell;
    }

    public static TradeCorrect getMarketTradeCorrect( ReusableString           buffer, 
                                                      FixDecoder               decoder,
                                                      ZString                  mktOrderId, 
                                                      ZString                  mktClOrdId, 
                                                      OrderRequest             creq,          // client request 
                                                      int                      lastQty,
                                                      double                   lastPx,
                                                      ZString                  execId,
                                                      ZString                  execRefId,
                                                      OrdStatus                ordStatus ) {
        buffer.reset();

        buffer.append( mtrcorP1 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mtrcorP2 );
        buffer.append( FixTestUtils.getDateStr() );
        buffer.append( mtrcorP3 );
        buffer.append( mktOrderId );
        buffer.append( mtrcorP4 );
        buffer.append( ordStatus.getVal() ); 
        buffer.append( mtrcorP5 );
        buffer.append( lastQty );
        buffer.append( mtrcorP6 );
        buffer.append( lastPx ); 
        buffer.append( mtrcorP7 );
        buffer.append( execId ); 
        buffer.append( mtrcorP8 );
        buffer.append( mktClOrdId ); 
        buffer.append( mtrcorP9 );
        buffer.append( execRefId ); 
        buffer.append( mtrcorPA );
        
        Message m1 = WarmupUtils.doDecode( decoder, buffer.getBytes(), 0, buffer.length() ); 

        TradeCorrect tradeCorrect = (TradeCorrect)m1;
        
        return tradeCorrect;
    }
    


    public static Order createOrder( OrderRequest src ) {
        OrderImpl    order = new OrderImpl();
        OrderVersion ver   = new OrderVersion();
        
        ver.setBaseOrderRequest( src );
        
        double price = src.getPrice();
        
        final Currency   clientCurrency  = src.getCurrency();
        final Currency   tradingCurrency = src.getInstrument().getCurrency();
        
        if ( clientCurrency != tradingCurrency ) {
            price = clientCurrency.majorMinorConvert( tradingCurrency, price );
        }

        ver.setMarketPrice( price );
        
        order.setLastAckedVerion( ver );
        order.setPendingVersion(  ver );
        
        return order;
    }

    public static Order createOrder( ClientNewOrderSingleImpl nos, ClientCancelReplaceRequestImpl rep ) {
        Order order = createOrder( nos );
        
        OrderVersion pending = new OrderVersion();
        pending.setBaseOrderRequest( rep );
        
        double price = rep.getPrice();
        
        final Currency   clientCurrency  = rep.getCurrency();
        final Currency   tradingCurrency = rep.getInstrument().getCurrency();
        
        if ( clientCurrency != tradingCurrency ) {
            price = clientCurrency.majorMinorConvert( tradingCurrency, price );
        }

        pending.setMarketPrice( price );
        
        order.setPendingVersion( pending );

        pending.setOrdStatus( OrdStatus.PendingReplace );
        
        return order;
    }
}
