/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.units;

import com.rr.core.codec.CodecFactory;
import com.rr.core.codec.CodecName;
import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.dummy.warmup.DummyMessageHandler;
import com.rr.core.lang.ReusableType;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.MsgFlag;
import com.rr.core.pool.Recycler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.warmup.JITWarmup;
import com.rr.core.warmup.WarmupRegistry;
import com.rr.model.generated.internal.events.impl.HeartbeatImpl;

public class WarmupHeartbeat implements JITWarmup {

    private final int     _warmupCount;
    private final String  _name;
    private final Decoder _decoder;
    private final Encoder _encoder;
    
    private final Recycler<HeartbeatImpl> _recycler = SuperpoolManager.instance().getRecycler( HeartbeatImpl.class );
    
    public WarmupHeartbeat( CodecFactory cf, CodecName codecId ) {
        _warmupCount = WarmupRegistry.instance().getWarmupCount();
        _name = "Heartbeat" + codecId;
        _decoder = cf.getDecoder( codecId );
        _encoder = cf.getEncoder( codecId, new byte[1024], 0 );
    }
    
    @Override
    public String getName() {        
        return _name;
    }

    @Override
    public void warmup() throws Exception {
        MessageHandler d = new DummyMessageHandler();
        HeartbeatImpl h= new HeartbeatImpl();
        @SuppressWarnings( "unused" )
        ReusableType t;
        for( int i=0 ; i < _warmupCount ; ++i ) {
            t = h.getReusableType();
            h.setMsgSeqNum( i );
            h.setMessageHandler( d );
            h.setFlag( MsgFlag.PossDupFlag, false );
            _encoder.encode( h );
            HeartbeatImpl hb = (HeartbeatImpl) _decoder.decode( _encoder.getBytes(), _encoder.getOffset(), _encoder.getLength() );
            _recycler.recycle( hb );
        }
    }
}
