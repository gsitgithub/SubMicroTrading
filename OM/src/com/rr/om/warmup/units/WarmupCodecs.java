/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.units;

import java.nio.ByteBuffer;

import com.rr.core.codec.FixEncodeBuilder;
import com.rr.core.codec.FixEncodeBuilderImpl;
import com.rr.core.dummy.warmup.DummyMessageHandler;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.warmup.JITWarmup;
import com.rr.model.generated.internal.events.impl.ClientAlertLimitBreachImpl;
import com.rr.model.generated.internal.events.impl.ClientAlertTradeMissingOrdersImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelRejectImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelledImpl;
import com.rr.model.generated.internal.events.impl.ClientDoneForDayImpl;
import com.rr.model.generated.internal.events.impl.ClientExpiredImpl;
import com.rr.model.generated.internal.events.impl.ClientForceCancelImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.ClientOrderStatusImpl;
import com.rr.model.generated.internal.events.impl.ClientRejectedImpl;
import com.rr.model.generated.internal.events.impl.ClientReplacedImpl;
import com.rr.model.generated.internal.events.impl.ClientRestatedImpl;
import com.rr.model.generated.internal.events.impl.ClientResyncSentMsgsImpl;
import com.rr.model.generated.internal.events.impl.ClientStoppedImpl;
import com.rr.model.generated.internal.events.impl.ClientSuspendedImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeCancelImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeCorrectImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeNewImpl;
import com.rr.model.generated.internal.events.impl.HeartbeatImpl;
import com.rr.model.generated.internal.events.impl.LogonImpl;
import com.rr.model.generated.internal.events.impl.LogoutImpl;
import com.rr.model.generated.internal.events.impl.MarketAlertLimitBreachImpl;
import com.rr.model.generated.internal.events.impl.MarketAlertTradeMissingOrdersImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRejectImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelledImpl;
import com.rr.model.generated.internal.events.impl.MarketDoneForDayImpl;
import com.rr.model.generated.internal.events.impl.MarketExpiredImpl;
import com.rr.model.generated.internal.events.impl.MarketForceCancelImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketOrderStatusImpl;
import com.rr.model.generated.internal.events.impl.MarketRejectedImpl;
import com.rr.model.generated.internal.events.impl.MarketReplacedImpl;
import com.rr.model.generated.internal.events.impl.MarketRestatedImpl;
import com.rr.model.generated.internal.events.impl.MarketStoppedImpl;
import com.rr.model.generated.internal.events.impl.MarketSuspendedImpl;
import com.rr.model.generated.internal.events.impl.MarketTradeCancelImpl;
import com.rr.model.generated.internal.events.impl.MarketTradeCorrectImpl;
import com.rr.model.generated.internal.events.impl.MarketTradeNewImpl;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.RecoveryRejectedImpl;
import com.rr.model.generated.internal.events.impl.RecoveryTradeNewImpl;
import com.rr.model.generated.internal.events.impl.ResendRequestImpl;
import com.rr.model.generated.internal.events.impl.SequenceResetImpl;
import com.rr.model.generated.internal.events.impl.SessionRejectImpl;
import com.rr.model.generated.internal.events.impl.TestRequestImpl;

public class WarmupCodecs implements JITWarmup {

    private int _warmupCount;
    
    public WarmupCodecs( int warmupCount ) {
        _warmupCount = warmupCount;
    }
    
    @Override
    public String getName() {
        return "Codecs";
    }

    @Override
    public void warmup() throws InstantiationException, IllegalAccessException {
        warmupCodec();
        warmupEncodeLong();
        warmupTime();
    }
    
    public void warmupCodec() throws InstantiationException, IllegalAccessException {
        // @TODO generate a warmup class
        @SuppressWarnings( "rawtypes" )
        Class[] classes = { 
          RecoveryRejectedImpl.class,RecoveryTradeNewImpl.class,RecoveryNewOrderSingleImpl.class,
          ClientNewOrderSingleImpl.class,MarketNewOrderSingleImpl.class,ClientCancelReplaceRequestImpl.class,
          MarketCancelReplaceRequestImpl.class,ClientCancelRequestImpl.class,MarketCancelRequestImpl.class,
          ClientForceCancelImpl.class,MarketForceCancelImpl.class,ClientCancelRejectImpl.class,MarketCancelRejectImpl.class,
          ClientAlertLimitBreachImpl.class,MarketAlertLimitBreachImpl.class,ClientAlertTradeMissingOrdersImpl.class,
          MarketAlertTradeMissingOrdersImpl.class,ClientNewOrderAckImpl.class,
          MarketNewOrderAckImpl.class,ClientTradeNewImpl.class,MarketTradeNewImpl.class,ClientRejectedImpl.class,MarketRejectedImpl.class,
          ClientCancelledImpl.class,MarketCancelledImpl.class,ClientReplacedImpl.class,MarketReplacedImpl.class,ClientDoneForDayImpl.class,
          MarketDoneForDayImpl.class,ClientStoppedImpl.class,MarketStoppedImpl.class,ClientExpiredImpl.class,MarketExpiredImpl.class,
          ClientSuspendedImpl.class,MarketSuspendedImpl.class,ClientRestatedImpl.class,MarketRestatedImpl.class,ClientTradeCorrectImpl.class,
          MarketTradeCorrectImpl.class,ClientTradeCancelImpl.class,MarketTradeCancelImpl.class,ClientOrderStatusImpl.class,
          MarketOrderStatusImpl.class,HeartbeatImpl.class,LogonImpl.class,LogoutImpl.class,SessionRejectImpl.class,ResendRequestImpl.class,
          ClientResyncSentMsgsImpl.class,SequenceResetImpl.class,TestRequestImpl.class,HeartbeatImpl.class
        };
                               
        Message t = new ClientNewOrderSingleImpl();
        MessageHandler h = new DummyMessageHandler(); 
        
        for ( int j=0 ; j < classes.length ; j++ ) {
            
            Message m = (Message) classes[j].newInstance();
            
            for ( int i=0 ; i < _warmupCount ; i++ ) {
                m.attachQueue( t );
                m.setMsgSeqNum( i );
                m.detachQueue();
                m.setMessageHandler( h );
            }
        }
    }

    private void warmupEncodeLong() {
        final byte[] bufLong = new byte[512];
        FixEncodeBuilder  encoderLong = new FixEncodeBuilderImpl( bufLong, 0, (byte)'4', (byte)'4' );

        for ( int i=0 ; i < _warmupCount ; i++ ) {
            encoderLong.start();
            encoderLong.encodeLong( 38, i << 10 + i );
        }
    }

    private void warmupTime() {
        ByteBuffer db = ByteBuffer.allocateDirect( 100 );
        
        long now   = System.currentTimeMillis();
        int  today = TimeZoneCalculator.instance().utcToLocal( now );
        
        ReusableString rs = new ReusableString();
        
        for ( int i=0 ; i < _warmupCount ; i++ ) {
            TimeZoneCalculator.instance().utcFullTimeToLocal( db, now+i );
            TimeZoneCalculator.instance().utcTimeToShortLocal( rs, today+i );
            
            rs.reset();
            db.clear();
        }
    }
}
