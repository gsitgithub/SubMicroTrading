/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.sim;

import java.util.ArrayList;
import java.util.List;

import com.rr.core.codec.CodecFactory;
import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.FixVersion;
import com.rr.core.model.Message;
import com.rr.core.utils.StringUtils;
import com.rr.core.utils.Utils;
import com.rr.model.generated.fix.codec.CMEDecoder;
import com.rr.model.generated.fix.codec.CodecFactoryPopulator;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.om.client.OMClientProfileImpl;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.exchange.CodecLoader;
import com.rr.om.warmup.FixTestUtils;

public class WarmupUtils {

    private static final byte[] _tag52       = { 0x01, '5', '2', '=' };
    private static final byte[] _tag60       = { 0x01, '6', '0', '=' };
    private static final byte[] _csTagmatch  = { 0x01, '1', '0', '=' };
    private static final byte[] _lenTagmatch = { 0x01, '9', '=' };


    // length and checksum will be regenerated
    public static final String[] _warmupRequests = {
        "8=FIX.4.4;9=153;35=D;49=WARMUPCLIENT1;56=LLT1_IN;52=20100810-13:00:50.316;34=859;40=2;54=1;55=X;11=99999910000000000;21=1;60=20100810-13:00:50.316;38=2;44=1.1;59=0;22=5;48=VODl.CHI;10=202;"
//        ,"8=FIX.4.4;9=153;35=D;49=WARMUPCLIENT1;56=LLT1_IN;52=20100810-13:00:50.316;34=859;40=2;54=1;55=X;11=99999910000000001;21=1;60=20100810-13:00:50.316;38=2;44=1.1;59=0;22=5;48=VODl.CHI;10=202;"
//        ,"8=FIX.4.4;9=153;35=G;49=WARMUPCLIENT1;56=LLT1_IN;52=20100810-13:00:51.316;34=859;40=2;54=1;55=X;11=99999950000000000;41=99999910000000001;21=1;60=20100810-13:00:51.316;38=3;44=1.2;59=0;22=5;48=VODl.CHI;10=202;"                                                
//        ,"8=FIX.4.4;9=153;35=D;49=WARMUPCLIENT1;56=LLT1_IN;52=20100810-13:00:50.316;34=859;40=2;54=1;55=X;11=99999910000000002;21=1;60=20100810-13:00:50.316;38=2;44=1.1;59=0;22=5;48=VODl.CHI;10=202;"
//        ,"8=FIX.4.4;9=153;35=F;49=WARMUPCLIENT1;56=LLT1_IN;52=20100810-13:00:52.316;34=859;40=2;54=1;55=X;11=99999960000000000;41=99999910000000002;21=1;60=20100810-13:00:52.316;38=3;44=1.2;59=0;22=5;48=VODl.CHI;10=202;"
//        
//        ,"8=FIX.4.4;9=153;35=D;49=CLIENT1;56=LLT1_IN;52=20100810-13:00:53.316;34=859;40=2;54=1;55=X;11=C001015E;21=1;60=20100810-13:00:50.316;38=2;44=1.1;59=0;22=5;48=VODl.CHI;10=202;"                                                
//        ,"8=FIX.4.4;9=153;35=G;49=CLIENT1;56=LLT1_IN;52=20100810-13:00:51.316;34=859;40=2;54=1;55=X;11=C001015F;41=C001015E;21=1;60=20100810-13:00:51.316;38=3;44=1.2;59=0;22=5;48=VODl.CHI;10=202;"
//        
//        ,"8=FIX.4.4;9=153;35=D;49=CLIENT1;56=LLT1_IN;52=20100810-13:00:53.316;34=859;40=2;54=1;55=X;11=C001015G;21=1;60=20100810-13:00:50.316;38=2;44=1.1;59=0;22=5;48=VODl.CHI;10=202;"                                                
//        ,"8=FIX.4.4;9=153;35=G;49=CLIENT1;56=LLT1_IN;52=20100810-13:00:51.316;34=859;40=2;54=1;55=X;11=C001015H;41=C001015G;21=1;60=20100810-13:00:51.316;38=3;44=1.2;59=0;22=5;48=VODl.CHI;10=202;"
//        
//        ,"8=FIX.4.4;9=153;35=D;49=CLIENT1;56=LLT1_IN;52=20100810-13:00:50.316;34=859;40=2;54=1;55=YY;11=C001015I;21=1;60=20100810-13:00:50.316;38=2;44=1.1;59=0;22=5;48=BT.CHI;10=202;"                                                
//        ,"8=FIX.4.4;9=153;35=D;49=CLIENT1;56=LLT1_IN;52=20100810-13:00:50.316;34=859;40=2;54=2;55=YY;11=C001015J;21=1;60=20100810-13:00:50.316;38=2;44=1.1;59=0;22=5;48=BT.CHI;10=202;"                                                
//
//        ,"8=FIX.4.4;9=153;35=D;49=CLIENT1;56=LLT1_IN;52=20100810-13:00:50.316;34=859;40=2;54=2;55=YY;11=C001015K;21=1;60=20100810-13:00:50.316;38=2;44=1.1;59=0;22=5;48=BP.CHI;10=202;"                                                
//        ,"8=FIX.4.4;9=153;35=D;49=CLIENT1;56=LLT1_IN;52=20100810-13:00:50.316;34=859;40=2;54=1;55=YY;11=C001015L;21=1;60=20100810-13:00:50.316;38=2;44=1.1;59=0;22=5;48=BP.CHI;10=202;"                                                
    };
    
    private static CodecFactory _cf = initCF();
    
    public static List<byte[]> getTemplateRequests() {
        List<byte[]> list = new ArrayList<byte[]>();
        
        for( String req : _warmupRequests ) {
            byte[] msg = FixTestUtils.semiColonToFixDelim( req );
            list.add( msg );
        }
        
        return list;
    }
    
    private static CodecFactory initCF() {
        CodecFactory codecFactory = new CodecFactory();
        CodecFactoryPopulator pop = new CodecLoader();
        pop.register( codecFactory );
        return codecFactory;
    }

    public static void setCodecFactory( CodecFactory cf ) {
        _cf = cf;
    }
    
    public static ClientProfile getWarmupClient() {
        ZString id = new ViewString( "WarmupClient" );
        return new OMClientProfileImpl( id, 10, 10, 10000, 10000, 60, 80, 90 );
    }
    
    public static FixDecoder getRecoveryDecoder( FixVersion ver ) {
        FixDecoder decoder = FixFactory.createRecoveryDecoder( ver );
        decoder.setClientProfile( getWarmupClient() );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        decoder.setReceived( Utils.nanoTime() );
        decoder.setValidateChecksum( false );
        return decoder;
    }

    public static FixDecoder getDecoder( FixVersion ver ) {
        FixDecoder decoder = FixFactory.createFixDecoder( ver );
        decoder.setClientProfile( getWarmupClient() );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        decoder.setReceived( Utils.nanoTime() );
        decoder.setValidateChecksum( false );
        return decoder;
    }

    public static FixEncoder getEncoder( FixVersion ver, byte[] outBuf, int offset ) {
        FixEncoder encoder = FixFactory.createFixEncoder( ver, outBuf, offset );
        return encoder;
    }

    public static Decoder getRecoveryDecoder( CodecId codec ) {
        Decoder decoder = _cf.getRecoveryDecoder( codec );
        decoder.setClientProfile( getWarmupClient() );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        decoder.setReceived( Utils.nanoTime() );
        if( decoder instanceof FixDecoder ) {
            ((FixDecoder)decoder).setValidateChecksum( false );
        }
        return decoder;
    }

    public static Decoder getDecoder( CodecId codec ) {
        Decoder decoder = _cf.getDecoder( codec );
        decoder.setClientProfile( getWarmupClient() );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        decoder.setReceived( Utils.nanoTime() );
        if( decoder instanceof FixDecoder ) {
            ((FixDecoder)decoder).setValidateChecksum( false );
        }
        return decoder;
    }

    public static Encoder getEncoder( CodecId codec, byte[] outBuf, int offset ) {
        Encoder encoder = _cf.getEncoder( codec, outBuf, offset );
        return encoder;
    }

    public static FixDecoder getCMEDecoder() {
        FixDecoder decoder = new CMEDecoder( FixVersion.Fix4_2._major, FixVersion.Fix4_2._minor );
        decoder.setClientProfile( getWarmupClient() );
        decoder.setInstrumentLocator( new DummyInstrumentLocator() );
        decoder.setReceived( Utils.nanoTime() );
        decoder.setValidateChecksum( false );
        return decoder;
    }

    /**
     * set the proper body length, checksum and override tag 52, tag 60
     * 
     * Body length

        The Body length is the byte count starting at tag 35 (included) all the way to tag 10 (excluded). SOH separators do count in the body length.
        For Example:
        8=FIX.4.2|9=65|35=A|49=SERVER|56=CLIENT|34=177|52=20090107-18:15:16|98=0|108=30|10=062|
        Has a Body length of 65 with the following breakdown, length(tag#)
        5(35) + 10(49) + 10(56) + 7(34) + 21(52) + 5(98) + 7(108)
        
        The SOH delimiter at the end of a Tag=Value belongs to the Tag
        
     * @return
     */
    public static Message doDecode( Decoder decoder, byte[] bytes, int offset, int length ) {

        // search for 9= and replace with correct length
        
        int bodyStart = overrideTagLen( bytes, offset, length );

        overrideDates( bytes, offset, length, bodyStart );

        overrideTagCheckSum( bytes, offset, length, bodyStart );
        
        return decoder.decode(  bytes, offset, length );
    }

    private static int overrideTagLen( byte[] bytes, int offset, int length ) {

        int matchIdxStart = StringUtils.findMatch( bytes, offset, length, _lenTagmatch );
        
        int bodyStart = -1;
        
        if ( matchIdxStart >=0 ) {
        
            int sizeLenTag  = 7;     // to move to end of len tag for ';9=123;'
            int checkSumLen = 7;     // 10=123;
            
            bodyStart         = matchIdxStart + sizeLenTag;
            int checksumStart = length - checkSumLen;

            int bodyLen = checksumStart - bodyStart;
            
            bytes[ matchIdxStart+3] = (byte)((bodyLen / 100) + '0');
            bytes[ matchIdxStart+4] = (byte)(((bodyLen % 100) / 10) + '0');
            bytes[ matchIdxStart+5] = (byte)((bodyLen % 10) + '0');
        }
        
        return bodyStart;
    }

    private static void overrideDates( byte[] bytes, int offset, int length, int bodyStart ) {
        
        int now = TimeZoneCalculator.instance().getTimeUTC();
        
        int matchIdxStart = StringUtils.findMatch( bytes, offset, length, _tag52 );

        if ( matchIdxStart >=0 && bodyStart >= 0 ) {
            TimeZoneCalculator.instance().dateTimeSecsToUTC( bytes, matchIdxStart+4, now );
        }

        matchIdxStart = StringUtils.findMatch( bytes, offset, length, _tag60 );

        if ( matchIdxStart >=0 && bodyStart >= 0 ) {
            TimeZoneCalculator.instance().dateTimeSecsToUTC( bytes, matchIdxStart+4, now );
        }
    }

    private static void overrideTagCheckSum( byte[] bytes, int offset, int length, int bodyStart ) {
        
        int matchIdxStart = StringUtils.findMatch( bytes, offset, length, _csTagmatch );

        if ( matchIdxStart >=0 && bodyStart >= 0 ) {

            int checkSum = calcCheckSum( bytes, offset, matchIdxStart+1 );
            
            bytes[ matchIdxStart+4] = (byte)((checkSum / 100) + '0');
            bytes[ matchIdxStart+5] = (byte)(((checkSum % 100) / 10) + '0');
            bytes[ matchIdxStart+6] = (byte)((checkSum % 10) + '0');
        }
    }


    /**
     * checksum is across the body from start of tag 35 to the delimiter at end of tag before checksum
     * 
     * @param data
     * @param offset
     * @param maxIdx    upto not including maxIdx
     * @return
     */
    public static int calcCheckSum( byte[] data, int offset, int maxIdx ) {
        int val = 0;
        
        for( int idx=offset ; idx < maxIdx ; ) {
            val += data[ idx++ ];
        }
        
        val = val & 0xFF;
        
        return val;
    }
}
