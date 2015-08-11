/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.binary.BinaryBigEndianDecoderUtils;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Instrument;
import com.rr.core.model.InstrumentLocator;
import com.rr.core.model.Message;

public final class MDSDecoder implements Decoder {

    private final BinaryBigEndianDecoderUtils  _builder;
    private final ReusableString               _tmpRIC = new ReusableString();

    InstrumentLocator      _instLocator;

    public MDSDecoder() {
    
        _builder = new BinaryBigEndianDecoderUtils();
    }
    
    public void init( InstrumentLocator il ) {
        _instLocator = il;
    }
    
    @Override public InstrumentLocator getInstrumentLocator() { 
        return _instLocator; 
    }
    
    @Override
    public int getLength() {
        return _builder.getLength();
    }
    
    /**
     * @NOTE ignore the params, buffer already setup in builder
     */
    @Override
    public Message decode( byte[] msg, int offset, int maxIdx ) {
        
        _builder.start( msg, offset, maxIdx );
        
        byte operation =_builder.decodeByte();
        
        switch( operation ) {
        case MDSReusableTypeConstants.SUB_ID_SUBSCRIBE:
            return decodeSubscribe();
        case MDSReusableTypeConstants.SUB_ID_TRADING_BAND_UPDATE:
            return decodeTradingRangeUpdate();
        case MDSReusableTypeConstants.SUB_ID_FX_SNAPSHOT:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_ACTIVE_BBO:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_ACTIVE_DEPTH:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_SNAPSHOT_BBO:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_SNAPSHOT_DEPTH:
            break;
        }
        
        return null;
    }

    private Message decodeTradingRangeUpdate() {
        
        int count = 0xFF & _builder.decodeByte();
        
        double  lower;
        double  upper;
        
        long    lowerId;                  
        long    upperId;                  
        
        int     lowerFlags;
        int     upperFlags;
        
        for( int i=0 ; i < count ; ++i ) {
            
            _builder.decodeString( _tmpRIC );

            lower      = _builder.decodePrice();
            upper      = _builder.decodePrice();
            lowerId    = _builder.decodeLong();
            upperId    = _builder.decodeLong();
            lowerFlags = _builder.decodeInt();
            upperFlags = _builder.decodeInt();
            
            Instrument inst = _instLocator.getInstrumentByRIC( _tmpRIC );
            
            if ( inst != null ) {
                if ( upper > 0 ) inst.getValidTradingRange().setMaxBuy(  upperId, upper, upperFlags );
                inst.getValidTradingRange().setMinSell( lowerId, lower, lowerFlags );
            }
        }
        
        return null;
    }

    private Message decodeSubscribe() {
        
        byte subOp =_builder.decodeByte();
        
        int count = 0xFF & _builder.decodeByte();

        ReusableString chain = null;
        ReusableString tmp;
        
        for( int i=0 ; i < count ;  i++ ) {
            
            tmp = TLC.instance().getString();
            
            _builder.decodeString( tmp );
               
            tmp.setNext( chain );
            chain = tmp;
        }
        
        _builder.end();

        switch( subOp ) {
        case MDSReusableTypeConstants.SUB_ID_SUBSCRIBE:
            break;
        case MDSReusableTypeConstants.SUB_ID_TRADING_BAND_UPDATE:
            return subscribeTradingRangeUpdate( chain );
        case MDSReusableTypeConstants.SUB_ID_FX_SNAPSHOT:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_ACTIVE_BBO:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_ACTIVE_DEPTH:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_SNAPSHOT_BBO:
            break;
        case MDSReusableTypeConstants.SUB_ID_MARKET_DATA_SNAPSHOT_DEPTH:
            break;
        }
        
        return null;
    }

    private Message subscribeTradingRangeUpdate( ReusableString chain ) {
        
        TLC.instance().recycleChain( chain );
        
        return null;
    }

    @Override public ResyncCode resync( byte[] fixMsg, int offset, int maxIdx )         { return null; }
    @Override public int getSkipBytes()                                                 { return 0; }
    @Override public void setReceived( long nanos )                                     { /*dont care */ }
    @Override public long getReceived()                                                 { return 0; }
    @Override public int  parseHeader( byte[] inBuffer, int inHdrLen, int bytesRead )   { return 0; }
    @Override public Message postHeaderDecode()                                         { return null; }
    @Override public void setClientProfile( ClientProfile client )                      { /* dont care */ }
    @Override public void setInstrumentLocator( InstrumentLocator instrumentLocator )    { /* dont care */ }
    @Override public void setTimeZoneCalculator( TimeZoneCalculator calc )              { /* dont care */ }
    @Override public void setNanoStats( boolean nanoTiming )                            { /* dont care */ }
}
