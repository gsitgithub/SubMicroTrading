/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.FixVersion;
import com.rr.core.model.MessageHandler;
import com.rr.core.persister.IndexPersister;
import com.rr.core.persister.Persister;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.SessionDirection;
import com.rr.core.session.SessionException;
import com.rr.core.utils.ThreadPriority;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.om.session.fixsocket.FixSocketConfig;
import com.rr.om.session.fixsocket.FixSocketSession;
import com.rr.om.session.fixsocket.MemoryIndexedPersister;
import com.rr.om.warmup.AbstractWarmupSession;
import com.rr.om.warmup.sim.FixSimParams;
import com.rr.om.warmup.sim.WarmupUtils;

public class DummyRecoverySession extends FixSocketSession {

    private static FixSimParams _params = AbstractWarmupSession.createParams( "TestRecovery", 0, false, 1 );
    

    public static DummyRecoverySession create( String sname, boolean isUpstream, MessageHandler proc ) throws SessionException, PersisterException {

        String              name              = sname;
        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( name.toString(), false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder          encoder           = WarmupUtils.getEncoder( FixVersion.Fix4_4, outBuf, logHdrOut ); 
        FixDecoder          decoder           = WarmupUtils.getDecoder( FixVersion.Fix4_4 );
        Decoder             recoveryDecoder   = WarmupUtils.getRecoveryDecoder( FixVersion.Fix4_4 );

        ThreadPriority      receiverPriority  = ThreadPriority.Other;
        ZString             senderCompId      = new ViewString( _params.getUpSenderCompId() );
        ZString             senderSubId       = new ViewString( "" );
        ZString             targetCompId      = new ViewString( _params.getUpTargetCompId() ); 
        ZString             targetSubId       = new ViewString( "" );
        ZString             userName          = new ViewString( "" );
        ZString             password          = new ViewString( "" );

        FixSocketConfig     socketConfig      = new FixSocketConfig( EventRecycler.class, 
                                                                     true, 
                                                                     new ViewString( _params.getUpHost() ), 
                                                                     null,
                                                                     _params.getUpPort(),
                                                                     senderCompId, 
                                                                     senderSubId, 
                                                                     targetCompId, 
                                                                     targetSubId,
                                                                     userName,
                                                                     password );
        
        socketConfig.setDirection( isUpstream ? SessionDirection.Upstream : SessionDirection.Downstream );
        
        socketConfig.setHeartBeatIntSecs( 30 );
        socketConfig.setDisconnectOnMissedHB( false );
        socketConfig.setRecoverFromLoginSeqNumTooLow( true );

        MessageDispatcher dispatcher = new DirectDispatcher(); 

        socketConfig.setInboundPersister(  createPersister( name + "_IN" ) ); 
        socketConfig.setOutboundPersister( createPersister( name + "_OUT" ) ); 
        
        DummyRecoverySession sess = new DummyRecoverySession( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, 
                                                              recoveryDecoder, receiverPriority );

        dispatcher.setHandler( sess );

        if ( _params.isDisableNanoStats() ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }
        
        if ( _params.isDisableEventLogging() ) {
            sess.setLogEvents( false );
        }

        return sess;
    }
    
    private static Persister createPersister( String id ) {
        return new MemoryIndexedPersister();
    }

    private DummyRecoverySession( String name, 
                                  MessageRouter inboundRouter, 
                                  FixSocketConfig fixConfig, 
                                  MessageDispatcher dispatcher, 
                                  FixEncoder encoder,
                                  FixDecoder decoder, 
                                  Decoder recoveryDecoder, 
                                  ThreadPriority receiverPriority ) throws SessionException, PersisterException {
        
        super( name, inboundRouter, fixConfig, dispatcher, encoder, decoder, recoveryDecoder, receiverPriority );
    }
    
    public IndexPersister getInboundPersister() {
        return (IndexPersister) _inPersister;
    }
    
    @Override
    public IndexPersister getOutboundPersister() {
        return (IndexPersister) _outPersister;
    }
}
