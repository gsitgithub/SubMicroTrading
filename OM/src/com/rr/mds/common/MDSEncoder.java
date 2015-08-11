/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common;

import com.rr.core.codec.Encoder;
import com.rr.core.codec.binary.BinaryBigEndianEncoderUtils;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.mds.common.events.Subscribe;
import com.rr.mds.common.events.TradingRangeUpdate;

public final class MDSEncoder implements Encoder {

    private static final Logger       _log = LoggerFactory.create( MDSEncoder.class );
    
    private final byte[]                _buf;
    private final int                   _offset;
    private final BinaryBigEndianEncoderUtils    _builder;

    public MDSEncoder( byte[] buf, int offset ) {
    
        _buf    = buf;
        _offset = offset;
        
        _builder = new BinaryBigEndianEncoderUtils( _buf, _offset );
    }
    
    @Override
    public final void encode( final Message msg ) {
        switch( msg.getReusableType().getSubId() ) {
        case MDSReusableTypeConstants.SUB_ID_SUBSCRIBE:
            encodeSubscribe( (Subscribe) msg );
            break;
        case MDSReusableTypeConstants.SUB_ID_TRADING_BAND_UPDATE:
            encodeTradingRangeUpdate( (TradingRangeUpdate) msg );
            break;
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
    }

    /**
     * @TODO REWRITE TO USE STRUCTURE WHICH IS NOT RECYCLED NOT TRADING RANGE UPDATE 
     * 
     * @param msg
     */
    private void encodeTradingRangeUpdate( TradingRangeUpdate msg ) {
        
        _builder.start();

        int count = 0;
        
        TradingRangeUpdate tmp = msg;
        
        while( tmp  != null ) {
            ++count;
            tmp = tmp.getNext();
        }

        if ( count > 255 ) {
            _log.warn( "Truncating trading range update to 255 entries from " + count );
            
            count = 255;
        }
        
        _builder.encodeByte( (byte) MDSReusableTypeConstants.SUB_ID_TRADING_BAND_UPDATE );
        _builder.encodeByte( (byte)count );
        
        for( int i=0 ; i < count ; ++i ) {
            
            _builder.encodeString( msg.getRIC() );

            _builder.encodePrice( msg.getLower() );
            _builder.encodePrice( msg.getUpper() );
            _builder.encodeLong(  msg.getLowerId() );
            _builder.encodeLong(  msg.getUpperId() );
            _builder.encodeInt(   msg.getLowerFlags() );
            _builder.encodeInt(   msg.getUpperFlags() );
            
            msg = msg.getNext();
        }
        
        _builder.end();
    }

    private void encodeSubscribe( final Subscribe msg ) {
        
        final int ricCount = msg.getCount();
        
        _builder.start();
        _builder.encodeByte( (byte)MDSReusableTypeConstants.SUB_ID_SUBSCRIBE );
        _builder.encodeByte( (byte)msg.getSubscriptionType() );
        _builder.encodeByte( (byte)ricCount );
        
        ReusableString chain = msg.getRICChain();
        
        while( chain != null ) {
            
            _builder.encodeString( chain );
            
            chain = chain.getNext();
        }
        
        _builder.end();
    }

    @Override
    public int getOffset() {
        return _offset;
    }

    @Override
    public int getLength() {
        return _builder.getLength();
    }

    @Override
    public Message unableToSend( Message msg, ZString errMsg ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public byte[] getBytes() {
        return _buf;
    }

    @Override public void setTimeZoneCalculator( TimeZoneCalculator calc )              { /* nothing */ }
    @Override public void addStats( ReusableString outBuf, Message msg, long time )     { /* nothing */ }
    @Override public void setNanoStats( boolean nanoTiming )                            { /* nothing */ }
}
