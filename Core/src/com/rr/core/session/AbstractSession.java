/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.Set;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.component.SMTStartContext;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MsgFlag;
import com.rr.core.persister.DummyPersister;
import com.rr.core.persister.PersistentReplayListener;
import com.rr.core.persister.Persister;
import com.rr.core.persister.PersisterException;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.recovery.RecoverySessionContext;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.utils.ReflectUtils;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.rr.core.utils.Utils;


public abstract class AbstractSession implements Session {

    protected static final ErrorCode SHUT_ERR           = new ErrorCode( "ASN100", "Unexpected error in shutdown handler " );
    protected static final ErrorCode ERR_IN_MSG         = new ErrorCode( "ASN210", "Error receiving message : " );
    protected static final ErrorCode ERR_OUT_MSG        = new ErrorCode( "ASN220", "Error sending message : " );
    protected static final ErrorCode ERR_PERSIST_IN     = new ErrorCode( "ASN300", "Failed to persist inbound message" );
    protected static final ErrorCode ERR_PERSIST_OUT    = new ErrorCode( "ASN310", "Failed to persist outbound message" );
    protected static final ErrorCode ERR_PERSIST_MKR    = new ErrorCode( "ASN320", "Failed to mark outbound message persisted" );
    protected static final ErrorCode RECOVER_ERR_IN     = new ErrorCode( "ASN400", "Failed to recover inbound messages" );
    protected static final ErrorCode RECOVER_ERR_OUT    = new ErrorCode( "ASN410", "Failed to recover outbound messages" );

    final Logger _log = LoggerFactory.create( AbstractSession.class );

    private   static final ZString   REJ_DISCONNECTED   = new ViewString( "Rejected as not connected" );
    private   static final ZString   DROP_MSG           = new ViewString( "Session dropping session message as not connected, type=" );
    private   static final ZString   ENCODE_ERR         = new ViewString( "Encoding error : " );
    private   static final ZString   SMT_SEND_ERR       = new ViewString( "SMT send error : " );


    protected final MessageRouter       _inboundRouter;
    protected final ZString             _logInHdr;
    protected final ZString             _logOutHdr;
    private   final ReusableString      _logInbound     = new ReusableString();
    private   final ReusableString      _logOutbound    = new ReusableString();
    protected final MessageDispatcher   _outboundDispatcher;
    protected final Encoder             _encoder;
    protected final Decoder             _decoder;
    protected final Decoder             _recoveryDecoder;
    protected       boolean             _logStats = true;
    
    private         boolean             _rejectIfNotConnected   = true;
    
    private   final String              _name;
    private   final SessionConfig       _config;
    protected       boolean             _logEvents      = true;
    protected       boolean             _logPojos       = false;
    private         State               _state          = State.Disconnected;

    protected final boolean             _markConfirmationEnabled;
    
    protected       boolean             _stopping       = false;
    protected       Session             _chainSession   = null;
    protected       Persister           _inPersister    = new DummyPersister();
    protected       Persister           _outPersister   = new DummyPersister();

    private RecoverySessionContext _recoverySessionContextIn;
    private RecoverySessionContext _recoverySessionContextOut;
    
    private final MessageRecycler       _inboundRecycler;
    private final MessageRecycler       _outboundRecycler;

    protected final ReusableString      _logInErrMsg  = new ReusableString( 100 );
    protected final ReusableString      _logOutErrMsg = new ReusableString( 100 );
    protected final ReusableString      _logOutMsg    = new ReusableString( 100 );


    private final Set<ConnectionListener> _listenerSet = new LinkedHashSet<ConnectionListener>();

    private volatile boolean _outRecovered  = false;
    private volatile boolean _inRecovered   = false;

    private          long    _lastSent      = 0;
    private          long    _reads         = 0;
    private          long    _writes        = 0;      

    private volatile boolean _paused        = false;              // a secondary state

    protected Throttler _throttler = new NullThrottler();

    public AbstractSession( String              name,
                            MessageRouter       inboundRouter, 
                            SessionConfig       config, 
                            MessageDispatcher   dispatcher, 
                            Encoder             encoder, 
                            Decoder             decoder, 
                            Decoder             recoveryDecoder ) {
        super();
        
        _config                 = config;
        _name                   = name;
        _inboundRouter          = inboundRouter;
        
        _outboundDispatcher     = dispatcher;
        _encoder                = encoder;
        _decoder                = decoder;
        _recoveryDecoder        = recoveryDecoder;
        
        _inboundRecycler        = ReflectUtils.create( config.getRecycler() );
        _outboundRecycler       = ReflectUtils.create( config.getRecycler() );
        
        _logInHdr  = new ViewString( "  IN [" + name + "]: " );
        _logOutHdr = new ViewString( " OUT [" + name + "]: ");
    
        _logInbound.copy( _logInHdr );
        _logOutbound.copy( _logOutHdr );

        _markConfirmationEnabled = config.isMarkConfirmationEnabled();

        if ( config.getMaxMsgsPerSecond() > 0 ) {
            setThrottle( config.getMaxMsgsPerSecond(), 0, 1000 );
        }
        
        ShutdownManager.instance().register( new Callback(){

                                            @Override
                                            public void shuttingDown() {
                                                
                                                try {
                                                    ReusableString msg = new ReusableString();
                                                    finalLog( msg );
                                                    _log.info( msg );
                                                    stop();
                                                    disconnect( false );
                                                } catch( Throwable t ) {
                                                    _log.error( SHUT_ERR, "Unexpected exception in session shutdown: " + t.getMessage(), t );
                                                }
                                                
                                            }} );
    }

    @Override
    public String info() {
        String chain = (_chainSession==null) ? "N/A" : _chainSession.getComponentId();
        
        return( this.getClass().getSimpleName() + " id=" + getComponentId() + "  " + _config.info() + ", " + _outboundDispatcher.info() + 
                ", logEvents=" + _logEvents + ", logStats=" + _logStats + ", chainSession=" + chain );
    }
    
    @Override
    public SessionConfig getConfig() {
        return _config;
    }
    
    @Override
    public String toString() {
        ReusableString msg = new ReusableString();
        finalLog( msg );
        return msg.toString();
    }
    
    @Override
    public void setLogStats( boolean logStats ) {
        _logStats = logStats;
    }
    
    public static int getLogHdrLen( String name, boolean isInbound ) {
        if ( isInbound ) return (" IN [" + name + "]: ").length();
    
        return (" OUT [" + name + "]: ").length();
    }
    
    @Override
    public void setLogEvents( boolean on ) {
        _logEvents = on;
    }
    
    @Override
    public boolean isLogEvents() {
        return _logEvents;
    }
    
    @Override
    public synchronized void registerConnectionListener( ConnectionListener listener ) {
        _listenerSet.add( listener );
    }
    
    @Override
    public final String getComponentId() {
        return _name;
    }

    @Override
    public final State getSessionState() {
        return _state;
    }

    @Override
    public void setRejectOnDisconnect( boolean reject ) {
        _rejectIfNotConnected = reject;
    }

    @Override
    public boolean getRejectOnDisconnect() {
        return _rejectIfNotConnected;
    }

    @Override
    public boolean isRejectOnDisconnect() {
        return _rejectIfNotConnected;
    }

    @Override
    public final void handle( final Message msg ) {
        _outboundDispatcher.dispatch( msg );
    }

    @Override
    public void init( SMTStartContext ctx ) {
        try {
            init();
        } catch( PersisterException e ) {
            throw new SMTRuntimeException( getComponentId() + " failed to initialise : " + e.getMessage(), e );
        }
    }

    @Override
    public void prepare() {
        // nothing
    }

    @Override
    public void startWork() {
        connect();
    }

    @Override
    public void stopWork() {
        stop();
    }
    
    /**
     * ensure message is encoded and send (if possible), message may be recyled/sent to chain session after this
     *  
     * @param msg
     * @throws IOException
     */
    protected abstract void sendNow( Message msg ) throws IOException;
    
    /**
     * all messages must pass through handleNow which applies configured throttler
     */
    @Override
    public final void handleNow( Message msg ) {
        
        if ( msg != null ) {
                    
            if ( isConnected() ) {                      // IMPORTANT DONT USE loggedIn here, as in log in process need send msgs !
                try {
                    _throttler.checkThrottle( msg );
                    
                    sendNow( msg );

                    postSend( msg );
                    
                    ++_writes;
                    
                } catch( IOException e ) {
                    handleOutboundError( e, msg );
                    
                    sendChain( msg, true );
                    
                } catch( SMTRuntimeException e ) {

                    handleSendSMTException( e, msg );
                    
                } catch( Exception e ) {
                    // not a socket error dont drop socket
                    logOutboundError( e, msg );
                }

            } else {
                
                sendChain( msg, false );
                    
                if ( discardOnDisconnect( msg ) == false ) {
                    
                    if ( rejectMessageUpstream( msg, REJ_DISCONNECTED ) ) {
                        // message recycled by successful reject processing
                    } else {
                        // unable to reject message so dispatch to queue

                        if ( _outboundDispatcher.canQueue() ) {
                            _outboundDispatcher.dispatch( msg );
                        } else {
                            _logOutMsg.copy( DROP_MSG ).append( msg.getReusableType().toString() );
                            
                            _log.info( _logOutMsg );
                            
                            outboundRecycle( msg );
                        }
                    }
                } else {
                    if ( msg.getReusableType() != CoreReusableType.NullMessage ) {
                        _logOutMsg.copy( DROP_MSG ).append( msg.getReusableType().toString() );
                        
                        _log.info( _logOutMsg );

                        outboundRecycle( msg );
                    }
                }
            }
        }
    }

    protected void postSend( Message msg ) {
        sendChain( msg, true );
    }

    protected abstract void sendChain( Message msg, boolean canRecycle );

    protected void handleOutboundError( IOException e, Message msg ) {
        logOutboundError( e, msg );
        disconnect( true );
    }

    @Override
    public final void inboundRecycle( Message msg ) {
        _inboundRecycler.recycle( msg );
    }
    
    @Override
    public final void outboundRecycle( Message msg ) {
        _outboundRecycler.recycle( msg );
    }
    
    @Override
    public final boolean rejectMessageUpstream( Message msg, ZString errMsg ) {
        
        // dont reject if posDup is true OR message is tagged as reconciliation
        
        if ( _rejectIfNotConnected && 
             msg.isFlagSet( MsgFlag.PossDupFlag ) == false && 
             msg.isFlagSet( MsgFlag.Reconciliation ) == false ) {
            
            Message synthMessage = _encoder.unableToSend( msg, errMsg );
            
            if ( synthMessage != null ) {
             
                _inboundRouter.handle( synthMessage );
                
                // DONT RECYCLE MESSAGE IS ATTACHED TO Reject
                
                return true;
            }
        }

        return false;
    }
    
    @Override
    public synchronized void disconnect( boolean tryReconnect ) {

        if ( getSessionState() != Session.State.Disconnected ) {
            
            setState( Session.State.Disconnected );
            
            disconnectCleanup();
            
            if ( !tryReconnect && !_paused ) {
                setPaused( true );
            }
            
            notifyAll();
        }
    }

    @Override
    public void setPaused( boolean paused ) {
        if ( paused != _paused ) {
            _paused = paused;
            
            if ( paused ) {
                _log.info( "Session " + getComponentId() + " now paused so disconnect if connected" );
                if ( isConnected() ) {
                    disconnect( false );
                }
            } else if ( isConnected() ) {
                if ( ! isConnected() ) {
                    _log.info( "Session " + getComponentId() + " try and unpause, connect should occur async" );
                }
            }
        }
        
        synchronized( this ) {
            this.notifyAll();
        }
    }

    protected void aboutToForceDisconnect() {
        // placeholder
    }

    protected abstract void disconnectCleanup();

    @Override
    public final boolean isConnected() {
        return getSessionState() == Session.State.Connected;
    }

    @Override
    public void processIncoming() {

        try {
            while( !_stopping ) { // dont use the sync metod isStopping, will hit sync barrier when end msg
                processNextInbound();
            }
            
        } catch( SessionException e ) {

            logInboundError( e );
            disconnect( !e.isForcedLogout() ); // pause session on forced logout
            
        } catch( DisconnectedException e ) {

            if ( ! _stopping && _state != State.Disconnected ) logDisconnected( e );
            
            disconnect( true );

        } catch( IOException e ) {

            if ( ! _stopping && _state != State.Disconnected ) logInboundError( e );
            
            disconnect( true );

        } catch( RuntimeDecodingException e ) {
            logInboundDecodingError( e );
        } catch( Exception e ) {
            // not a socket error dont drop socket
            logInboundError( e );
        }
    }

    @Override
    public synchronized final boolean isStopping() {
        return _stopping;
    }

    @Override
    public synchronized void stop() {
        if ( _stopping )
            return;
        _stopping = true;
        
        notifyAll();
    }

    @Override
    public final void dispatchInbound( Message msg ) {
        Message tmp;
        while( msg != null ) {
            tmp = msg.getNextQueueEntry();
            if ( tmp != null ) {
                msg.detachQueue();
            }
            msg.setMessageHandler( this );
            _inboundRouter.handle( msg );
            msg = tmp;
        }
        ++_reads;        
    }
    
    @Override
    public final void setChainSession( Session sess ) {
        _chainSession  = sess;
    }
    
    @Override
    public Session getChainSession() {
        return _chainSession;
    }

    @Override
    public boolean isLoggedIn() {
        return isConnected();
    }
    
    /*
     * PROTECTED METHODS
     */

    protected abstract void logInEvent( ZString event );
    protected abstract void logOutEvent( ZString event );
    
    protected void finalLog( ReusableString msg ) {
        msg.append( "Session " + getComponentId() + ", reads=" + _reads + ", writes=" + _writes );
    }
    
    protected synchronized final State setState( State newState ) {
        if ( newState != _state ) {
            
            _log.info( "Session change from " + _state.toString() + " to " + newState.toString() + " : " + info() );

            _state = newState;

            if ( newState == State.Connected ) {
                _outboundDispatcher.handlerStatusChange( this, true );
                connected();
            } else if ( newState == State.Disconnected ) {
                _outboundDispatcher.handlerStatusChange( this, false );
                disconnected();
            }

            for ( ConnectionListener listener : _listenerSet ) {
                if ( newState == State.Connected ) {
                    listener.connected( this );
                } else if ( newState == State.Disconnected ) {
                    listener.disconnected( this );
                }
            }
        }
        
        return _state;
    }

    protected void disconnected() {
        // for specialisation
    }

    protected void connected() {
        // for specialisation
    }

    protected final MessageRouter getRouter() {
        return _inboundRouter;
    }

    /*
     * PRIVATE METHODS
     */
    public void logInboundError( Exception e ) {
        _logInErrMsg.copy( getComponentId() ).append( ' ' ).append( e.getMessage() );
        _log.error( ERR_IN_MSG, _logInErrMsg, e );
    }

    public void logInboundDecodingError( RuntimeDecodingException e ) {
        _logInErrMsg.copy( getComponentId() ).append( ' ' ).append( e.getMessage() );
        _logInErrMsg.append( ' ' ).append( e.getFixMsg() );
        _log.error( ERR_IN_MSG, _logInErrMsg );
    }

    public final void logDisconnected( Exception e ) {
        _logInErrMsg.copy( getComponentId() ).append( ' ' ).append( e.getMessage() );
        _log.warn( _logInErrMsg );
    }
    
    public void logOutboundEncodingError( RuntimeEncodingException e ) {
        _logOutErrMsg.copy( getComponentId() ).append( ' ' ).append( e.getMessage() ).append( ":: " );
        _log.error( ERR_OUT_MSG, _logOutErrMsg, e );
    }

    private final void logOutboundError( Exception e, Message msg ) {
        _logOutErrMsg.copy( getComponentId() ).append( ' ' ).append( e.getMessage() ).append( ":: " );
        ReflectUtils.dump( _logInErrMsg, msg );
        
        _log.error( ERR_OUT_MSG, _logOutErrMsg, e );
    }

    public static int getDataOffset( String name, boolean isInbound ) {
        return AbstractSession.getLogHdrLen( name, isInbound );
    }
    
    /**
     * BLOCKING call waiting for recovery to finish replay
     */
    @Override
    public void waitForRecoveryToComplete() {
        while( _outRecovered == false || _inRecovered == false ) {

            Utils.delay( 500 );
        }
    }

    @Override
    public long getLastSent() {
        return _lastSent;
    }
    
    @Override
    public void recover( final RecoveryController ctl ) {

        setInboundRecoveryFinished( false );
        setOutboundRecoveryFinished( false );
        
        Thread inRec = new Thread( new Runnable() {
                    
                                @Override
                                public void run() {
                                    recoverInbound( ctl );
                                }}, "REC_IN_" + getComponentId() );

        Thread outRec = new Thread( new Runnable() {
                                
                                @Override
                                public void run() {
                                    recoverOutBound( ctl );
                                }}, "REC_OUT_" + getComponentId() );
        
        inRec.start();
        outRec.start();
    }
    
    public boolean isLogPojos() {
        return _logPojos;
    }

    public void setLogPojos( boolean logPojos ) {
        _logPojos = logPojos;
    }

    @Override
    public boolean isPaused() {
        return _paused;
    }
    
    @Override
    public SessionDirection getDirection() {
        return _config.getDirection();
    }
    
    /**
     * set throttler for sending of messages, messages exceeding this rate will be rejected
     * 
     * @param throttleNoMsgs - restrict new messages to this many messages per period (throttler may allow cancels and reject NOS/REP)
     * @param disconnectLimit - total message limit in period (all messages rejected)
     * @param throttleTimeIntervalMS - throttle period in ms
     */
    @Override
    public void setThrottle( int throttleNoMsgs, int disconnectLimit, long throttleTimeIntervalMS ) {
        Class<? extends Throttler> throttlerClass = _config.getThrottlerClass();

        _log.info( "Installing throttler for " + getComponentId() + " msgsPerPeriod=" + throttleNoMsgs + ", periodMS=" + throttleTimeIntervalMS );

        Throttler t = ReflectUtils.create( throttlerClass );
        
        t.setThrottleNoMsgs( throttleNoMsgs );
        t.setThrottleTimeIntervalMS( throttleTimeIntervalMS );
        t.setDisconnectLimit( disconnectLimit );
        
        _throttler = t;
    }
    
    @Override
    public Message recoverEvent( boolean isInbound, long persistKey, ReusableString tmpBigBuf, ByteBuffer tmpCtxBuf ) {
        Message msg = null;
        
        Persister p = (isInbound) ? _inPersister : _outPersister;

        final byte[] buf = tmpBigBuf.getBytes();

        try {
            int len = p.read( persistKey, buf, 0, tmpCtxBuf );
            
            if ( len > 0 ) {
                int ctxLen = tmpCtxBuf.position();
                if ( ctxLen > 0 ) {
                    byte[] optBuf = tmpCtxBuf.array();
                    msg = recoveryDecodeWithContext( buf, 0, len, optBuf, 0, ctxLen, isInbound );
                } else {
                    msg = recoveryDecode( buf, 0, len, isInbound );
                }
            }
        } catch( PersisterException e ) {
            _log.warn( "RecoverEvent " + getComponentId() + " : " + e.getMessage() );
        }
        
        return msg;
    }
    
    protected void setOutboundRecoveryFinished( boolean finished ) {
        _outRecovered = finished;
    }

    protected void setInboundRecoveryFinished( boolean finished ) {
        _inRecovered  = finished;
    }

    @SuppressWarnings( "synthetic-access" )
    protected void recoverOutBound( final RecoveryController ctl ) {
        
        try {
            _outPersister.replay( new PersistentReplayListener() {
                    
                    @Override
                    public void started() {
                        _recoverySessionContextOut = ctl.startedOutbound( AbstractSession.this );
                        _recoverySessionContextOut.setPersister( _outPersister );
                    }
                    
                    @Override
                    public void message( Persister p, long key, byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, short flags ) {
                        boolean inBound = false;
                        Message msg = recoveryDecodeWithContext( buf, offset, len, opt, optOffset, optLen, inBound );
                        persistIntegrityCheck( false, key, msg );
                        ctl.processOutbound( _recoverySessionContextOut, key, msg, flags );
                    }
                    
                    @Override
                    public void message( Persister p, long key, byte[] buf, int offset, int len, short flags ) {
                        boolean inBound = false;
                        Message msg = recoveryDecode( buf, offset, len, inBound );
                        persistIntegrityCheck( false, key, msg );
                        ctl.processOutbound( _recoverySessionContextOut, key, msg, flags );
                    }
                    
                    @Override
                    public void failed() {
                        ctl.failedOutbound( _recoverySessionContextOut );
                        setOutboundRecoveryFinished( true );
                    }
                    
                    @Override
                    public void completed() {
                        ctl.completedOutbound( _recoverySessionContextOut );
                        setOutboundRecoveryFinished( true );
                    }
                } );
        } catch( PersisterException e ) {
            _log.error( RECOVER_ERR_OUT, getComponentId(), e );

            ctl.failedOutbound( _recoverySessionContextOut );
        }
    }

    protected abstract Message recoveryDecode( byte[] buf, int offset, int len, boolean inBound );
    protected abstract Message recoveryDecodeWithContext( byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, boolean inBound );

    @SuppressWarnings( "synthetic-access" )
    protected void recoverInbound( final RecoveryController ctl ) {
        
        try {
            _inPersister.replay( new PersistentReplayListener() {
                
                    @Override
                    public void started() {
                        _recoverySessionContextIn = ctl.startedInbound( AbstractSession.this );
                        _recoverySessionContextIn.setPersister( _inPersister );
                    }
                    
                    @Override
                    public void message( Persister p, long key, byte[] buf, int offset, int len, byte[] opt, int optOffset, int optLen, short flags ) {
                        boolean inBound = true;
                        Message msg = recoveryDecodeWithContext( buf, offset, len, opt, optOffset, optLen, inBound );
                        persistIntegrityCheck( true, key, msg );
                        ctl.processInbound( _recoverySessionContextIn, key, msg, flags );
                    }
                    
                    @Override
                    public void message( Persister p, long key, byte[] buf, int offset, int len, short flags ) {
                        boolean inBound = true;
                        Message msg = recoveryDecode( buf, offset, len, inBound );
                        persistIntegrityCheck( true, key, msg );
                        ctl.processInbound( _recoverySessionContextIn, key, msg, flags );
                    }
                    
                    @Override
                    public void failed() {
                        ctl.failedInbound( _recoverySessionContextIn );
                        setInboundRecoveryFinished( true );
                    }
                    
                    @Override
                    public void completed() {
                        ctl.completedInbound( _recoverySessionContextIn );
                        setInboundRecoveryFinished( true );
                    }
                } );
        } catch( PersisterException e ) {
            _log.error( RECOVER_ERR_IN, getComponentId(), e );

            ctl.failedInbound( _recoverySessionContextIn );
        }
    }
    
    protected abstract void persistIntegrityCheck( boolean inbound, long key, Message msg );

    protected final void lastSent( long time ) {
        _lastSent = time;
    }
    
    // log inbound event, useful for Binary protocols
    protected final void logInEventPojo( Message msg ) {
        if ( _logPojos ) {
            _logInbound.setLength( _logInHdr.length() );
            msg.dump( _logInbound );         
            _log.infoLarge( _logInbound );
        }
    }
    
    // log outbound event, useful for Binary protocols
    protected final void logOutEventPojo( Message msg ) {
        if ( _logPojos ) {
            _logOutbound.setLength( _logOutHdr.length() );
            msg.dump( _logOutbound );         
            _log.infoLarge( _logOutbound );
        }
    }

    private void handleSendSMTException( SMTRuntimeException e, Message msg ) {
        if ( e instanceof RuntimeEncodingException ) {
            _logOutMsg.copy( ENCODE_ERR ).append( e.getMessage() );
        } else {
            _logOutMsg.copy( SMT_SEND_ERR ).append( e.getMessage() );
        }
        
        logOutboundError( e, msg );
        
        if ( rejectMessageUpstream( msg, _logOutMsg ) ) {
            // message recycled by successful reject processing
        } else {
            // unable to reject message so log error

            outboundRecycle( msg );
        }
    }
}
