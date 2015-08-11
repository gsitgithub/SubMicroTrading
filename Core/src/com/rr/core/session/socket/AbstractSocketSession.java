/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session.socket;

import java.io.Closeable;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.os.SocketFactory;
import com.rr.core.persister.PersisterException;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.DisconnectedException;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.Receiver;
import com.rr.core.session.Session;
import com.rr.core.session.SessionConstants;
import com.rr.core.session.ThreadedReceiver;
import com.rr.core.socket.LiteServerSocket;
import com.rr.core.socket.LiteSocket;
import com.rr.core.socket.ServerSocketChannelProxy;
import com.rr.core.socket.SocketChannelProxy;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.Utils;

public abstract class AbstractSocketSession extends AbstractSession {

    protected static final long     MAX_SPIN_FAIL_WRITES_MICROS        = 10;
    private   static final int      INITIAL_FAILED_WRITE_DELAY_MICROS  = 1;   
    private   static final int      MAX_FAILED_WRITE_DELAY             = 500;

    private   static final int      CLIENT_REACHABLE_TIMEOUT           = 10000;

    protected static final ZString  DELAYED_WRITE                      = new ViewString( " Slow consumer delayed write : " );
    protected static final ZString  MICROS                             = new ViewString( " micros " );
    protected static final ZString  NUM_WRITES                         = new ViewString( ", numWrites=" );

    protected final SocketConfig    _socketConfig;
    private   final ThreadPriority  _receiverPriority;

    private final Logger            _log = LoggerFactory.create( AbstractSocketSession.class );

    protected final long            _nioWaitPeriodMS;

    protected LiteSocket            _socketChannel;           
    
    /**
     * buffer for receiving messages, to avoid extra mempcy on log place the log hdr at start of buffer
     */
    protected final ByteBuffer      _inNativeByteBuffer   = ByteBuffer.allocateDirect( SizeConstants.DEFAULT_MAX_SESSION_BUFFER );
    protected final byte[]          _inBuffer             = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
    protected final ByteBuffer      _inByteBuffer         = ByteBuffer.wrap( _inBuffer );
    protected final ViewString      _inLogBuf             = new ViewString( _inBuffer );            
    protected final ReusableString  _logInMsg             = new ReusableString( 200 ); // error msg for inbound thread
    protected final int             _inHdrLen;            
    protected final int             _maxInUsableBuffer;
    protected       int             _inPreBuffered        = 0; // amount left over from last read
    
    protected final ByteBuffer      _outNativeByteBuffer  = ByteBuffer.allocateDirect( SizeConstants.DEFAULT_MAX_SESSION_BUFFER );
    protected final byte[]          _outBuffer;
    protected final ByteBuffer      _outByteBuffer;
    protected final ReusableString  _outLogBuf;
    protected final ReusableString  _outMessage           = new ReusableString(80);
    protected final int             _outHdrLen;
    
    private Receiver                _receiver;

    private boolean                 _loggedNotInSession   = false;    // dont spam error

    
    private int                     _nextRetryDelayMS     = SessionConstants.CONNECT_WAIT_DELAY_MS;
    private int                     _maxDelayRetryMS      = SessionConstants.DEFAULT_MAX_DELAY_MS;
    private int                     _maxConnectAttempts   = SessionConstants.DEFAULT_MAX_CONNECT_ATTEMPTS;   
    protected long                  _delayedWrites        = 0;
    private int                     _connectAttempts      = 0;


    public AbstractSocketSession( String            name, 
                                  MessageRouter     inboundRouter, 
                                  SocketConfig      socketConfig,
                                  MessageDispatcher dispatcher,
                                  Encoder           encoder,
                                  Decoder           decoder,
                                  Decoder           recoveryDecoder, 
                                  ThreadPriority    receiverPriority ) {
        
        super( name, inboundRouter, socketConfig, dispatcher, encoder, decoder, recoveryDecoder );

        _outBuffer            = encoder.getBytes();
        _outByteBuffer        = ByteBuffer.wrap( _outBuffer );
        _outLogBuf            = new ReusableString();
        
        _outLogBuf.setBuffer( _outBuffer, 0 );            
        
        _socketConfig       = socketConfig;
        _receiverPriority   = receiverPriority;
        _nioWaitPeriodMS    = socketConfig.nioSelectorWaitPeriod();
        
        _inPersister        = socketConfig.getInboundPersister();
        _outPersister       = socketConfig.getOutboundPersister();
        
        int maxDelayMS = socketConfig.getMaxWaitBeforeConnectRetryMS();
        if ( maxDelayMS > SessionConstants.CONNECT_WAIT_DELAY_MS ) _maxDelayRetryMS = maxDelayMS;   

        int maxConnAttempts = socketConfig.getMaxConnectAttempts();
        if ( maxConnAttempts > SessionConstants.CONNECT_WAIT_DELAY_MS ) _maxConnectAttempts = maxConnAttempts ;
        
        _inHdrLen          = writeHdr( _inByteBuffer, _logInHdr );
        _maxInUsableBuffer = _inByteBuffer.capacity() - (_inHdrLen+1); 
        _outHdrLen         = writeHdr( _outByteBuffer, _logOutHdr );
    }

    @Override
    public synchronized void init() throws PersisterException {
        _inPersister.open();
        _outPersister.open();
    }

    @Override
    public void attachReceiver( Receiver receiver ) {
        _receiver = receiver;
    }
    
    public boolean isNIO() {
        return _socketConfig.isNIO();
    }
    
    @Override
    public void threadedInit() {
        // nothing
    }
    
    @Override
    public synchronized void connect() {
        _log.info( "Session " + getComponentId() + " connect invoked" );
        
        if ( _receiver == null ) {
            _receiver = new ThreadedReceiver( this, _receiverPriority );
        }
        
        startReceiver();        // this WILL try and connect
        startTransmitter();
    }
    
    /**
     * Only intended to be  invoked by threaded receiver
     * WILL BLOCK IF NOT NIO
     */
    @Override
    public void internalConnect() {

        while ( isPaused() ) {
            _log.info( "Session " + getComponentId() + " connection on hold as session is paused" );
            synchronized( this ) {
                try {
                    this.wait();
                } catch( InterruptedException e ) {
                    //
                }
            }
            _log.info( "Session " + getComponentId() + " unpaused" );
        }
        
        if ( getSessionState() == State.Disconnected ) {

            if ( _socketConfig.isOpenUTC( System.currentTimeMillis() ) ) {
                _loggedNotInSession = false;
                
                _log.info( "Session "  + getComponentId() + " attempting to connect " );
                _socketChannel = connectSocket();
                _connectAttempts = 0;

                if ( _socketChannel != null ) {
                    setState( Session.State.Connected );
                }
            } else if ( ! _loggedNotInSession ) {
                _log.info( "Session "  + getComponentId() + " CANNOT connect as config period is not open " );
                _loggedNotInSession = true;
            }
        }
        
        synchronized( this ) {
            notifyAll();
        }
    }
    
    /**
     * waitConnect is used by receive/transmit thread to be notified when reconnected
     * @param string 
     */
    public void waitConnect( String key ) {
        
        _log.info( "Session " + getComponentId() + " " + key + " waiting for connection" );

        while( getSessionState() != State.Connected ) {
            
            Utils.delay( this, SessionConstants.CONNECT_WAIT_DELAY_MS );
        }

        _log.info( "Session " + getComponentId() + " " + key + " confirm connected" );
    }
    
    public long getDelayedWriteCount() {
        return _delayedWrites;
    }
    
    @Override
    public synchronized void stop() {
        if (  isStopping() ) return;
        
        if ( _receiver != null ) _receiver.setStopping( true );
        _outboundDispatcher.setStopping();
        super.stop();
        disconnect( false );
        _inPersister.close();
        _outPersister.close();
    }
    
    /**
     * PROTECTED METHODS
     */

    @Override
    protected final void disconnectCleanup() {
        cleanUp( _socketChannel );
    }

    protected final int writeHdr( ByteBuffer buf, ZString hdr ) {
        buf.clear();
        buf.put( hdr.getBytes(), hdr.getOffset(), hdr.length() );
        return buf.position();
    }

    protected final Message decode( int offset, int len ) {

        return _decoder.decode( _inBuffer, offset, len+offset );
    }
    
    protected Receiver getReciever() {
        return _receiver;
    }

    @Override
    protected void finalLog( ReusableString msg ) {
        super.finalLog( msg );
        msg.append( ", delayedWrites=" + _delayedWrites );
    }
    
    @Override
    protected void disconnected() {
        super.disconnect( true );
        _inPreBuffered = 0;
    }
    
    protected final int initialChannelRead( final int preBuffered, int minBytes ) throws Exception {
        final int maxBytes = _inByteBuffer.remaining();
        int totalRead = preBuffered;
        int curRead;
        
        if ( minBytes - preBuffered <= 0 ) return preBuffered;

        _inNativeByteBuffer.position( 0 );
        _inNativeByteBuffer.limit( maxBytes );

        while( totalRead < minBytes ) {
            curRead = _socketChannel.read();

            if ( curRead == -1 ) throw new DisconnectedException( "Detected socket disconnect" );

            // spurious wakeup
            
            if ( curRead == 0 && totalRead == preBuffered ) {
                return 0;
            }
            
            totalRead += curRead;
        }
        
        _inNativeByteBuffer.flip();
        
        // @TODO bypass the soft ref creation in IOUtil.read for heap based buffers
        _inByteBuffer.put( _inNativeByteBuffer );

        return totalRead;
    }

    /**
     * read fixed number of expected bytes
     * @param expectedBytes
     * @return
     * @throws Exception
     */
    protected final int readFixedExpectedBytes( final int preBuffered, int minBytes ) throws Exception {
        final int maxBytes = _inByteBuffer.remaining();        
        int totalRead = preBuffered;
        int curRead;
        if ( minBytes - preBuffered <= 0 ) return preBuffered;

        _inNativeByteBuffer.position( 0 );
        _inNativeByteBuffer.limit( maxBytes );

        while( totalRead < minBytes ) {
            curRead = _socketChannel.read();

            if ( curRead == -1 ) throw new DisconnectedException( "Detected socket disconnect" );

            totalRead += curRead;
        }
        
        _inNativeByteBuffer.flip();
        
        // @TODO bypass the soft ref creation in IOUtil.read for heap based buffers
        _inByteBuffer.put( _inNativeByteBuffer );

        return totalRead;
    }
    
    /**
     * write out the buffer to the channel, note if consumer is slow
     * that the write can fail ! 
     * 
     * to avoid CPU spinning sleep after 5 attempts
     * 
     * @param byteBuffer
     * @throws IOException
     */
    protected final void blockingWriteSocket() throws IOException {
        
        long failWriteCount = 0;
        int  nextFailDelay  = INITIAL_FAILED_WRITE_DELAY_MICROS;
        
        _outNativeByteBuffer.clear();
        _outNativeByteBuffer.put( _outByteBuffer );
        _outNativeByteBuffer.flip();
        
        final long start = Utils.nanoTime();
        if ( _logStats ) lastSent( start );
        
        do {
            if ( _socketChannel.write() == 0 ) {
                if ( failWriteCount++ >= MAX_SPIN_FAIL_WRITES_MICROS ) {
                    Utils.delayMicros( nextFailDelay );
                    
                    nextFailDelay *= 2;
                    
                    if ( nextFailDelay > MAX_FAILED_WRITE_DELAY ) nextFailDelay = MAX_FAILED_WRITE_DELAY;
                    
                    if ( _delayedWrites == 0 ) {
                        _log.warn( getComponentId() + " Delayed Write : possible slow consumer" );
                    }
                }
            }
        } while( _outNativeByteBuffer.hasRemaining() && ! _stopping );
        
        if ( failWriteCount > 0 ) {
            _delayedWrites++;
            
            final long duration = Math.abs( Utils.nanoTime() - start );
            
            if ( duration > _socketConfig.getLogDelayedWriteNanos() ) {
                _outMessage.reset();
                _outMessage.append( getComponentId() );
                _outMessage.append( DELAYED_WRITE ).append( duration / 1000 ).append( MICROS );
                _log.info( _outMessage );
            }
        }
    }

    protected final void cleanUp( Closeable obj ) {
        if ( obj == null ) return;
        try { obj.close(); } catch( Exception e ) { /* dont care */ }
    }
    
    protected final void shiftInBufferLeft( final int bytesToShift, final int fromIdx ) {
        // message decoded, shift left any extra bytes before invoke controller as that can throw exception .. avoids extra try-finally
        if ( bytesToShift > 0 ) {
            System.arraycopy( _inBuffer, fromIdx, _inBuffer, _inHdrLen, bytesToShift ); // dont need zero out bytes out on the right
        }
    }
    
    
    protected final int prepareForReadMessage() {
        final int preBuffered = _inPreBuffered;
        
        _inPreBuffered = 0;                                                         // reset the preBuffer .. incase of exception
        final int startPos = _inHdrLen + preBuffered;
        _inByteBuffer.limit( _inByteBuffer.capacity() );
        _inByteBuffer.position( startPos );
        return preBuffered;
    }

    /**
     * PRIVATE METHODS
     */
    
    private void startReceiver() {
        _receiver.start();
    }

    private void startTransmitter() {
        _outboundDispatcher.start();
    }

    private void setSocketOptions( LiteSocket channel ) throws SocketException {
        
        channel.setTcpNoDelay( _socketConfig.getTcpNoDelay() );
        
        /**
         * keep alive adds latency, only use where we get connectivity issues like firewall dropping low use connection
         */
        if ( _socketConfig.getKeepAlive() && ! channel.getKeepAlive() ) {
            _log.info( "Note socket " + getComponentId() + " using KeepAlive " );
            channel.setKeepAlive( true );
        }
        
        int soLinger = _socketConfig.getSoLinger();
        
        if ( soLinger != channel.getSoLinger() ) {
            _log.info( "Setting " + getComponentId() + " SoLinger to " + soLinger + " from " + channel.getSoLinger() );
            if  ( soLinger != -1 ) {
                channel.setSoLinger( true, soLinger );
            } else {
                channel.setSoLinger( false, soLinger );
            }
        }

        int soTimeout = _socketConfig.getSoDelayMS();
        
        if ( soTimeout != channel.getSoTimeout() ) {
            _log.info( "Setting " + getComponentId() + " SoTimeout to " + soTimeout + " from " + channel.getSoTimeout() );
            channel.setSoTimeout( soTimeout );
        }
    }

    private LiteSocket connectSocket() {

        LiteSocket socketChannel = null;

        if ( _socketConfig.isMulticast() ) {
            return createMulticastSocket();
        } 
        
        // server socket, only try once to create
        if ( _socketConfig.isServer() ) {
            if ( _socketConfig.isNIO() ) {
                socketChannel = serverNIOConnect();
            } else {
                socketChannel = serverBlockingConnect();
            }
            
            return socketChannel;
        } 

        _nextRetryDelayMS = SessionConstants.CONNECT_WAIT_DELAY_MS;
        
        // client socket, try multiple times to connect
        while( !isStopping() && ++_connectAttempts < _maxConnectAttempts ) {
            if ( _socketConfig.isNIO() ) {
                socketChannel = clientNIOConnect();
            } else {
                socketChannel = clientBlockingConnect();
            }

            if ( socketChannel != null )  return socketChannel;

            Utils.delay( this, _nextRetryDelayMS );
            
            _nextRetryDelayMS *= 2;
            if ( _nextRetryDelayMS >= _maxDelayRetryMS ) _nextRetryDelayMS = _maxDelayRetryMS;
        }
        
        if ( _connectAttempts >= _maxConnectAttempts ) {
            _log.error( SessionConstants.ERR_MAX_ATT_EXCEEDED, ", maxAttempts=" + _maxConnectAttempts );
            setPaused( true );
        }
        
        return null;
    }

    private LiteSocket clientBlockingConnect() {
        LiteSocket channel = null;
        try {
            channel = new SocketChannelProxy( SocketChannel.open(), _inNativeByteBuffer, _outNativeByteBuffer );
            setSocketOptions( channel );
            bindClientInterface( channel );
            SocketAddress addr = getSockAddress();
            channel.connect( addr );
            channel.configureBlocking( true );
            setupChannel( channel );
            return channel;
        } catch( ConnectException e ) {
            _log.error( SessionConstants.ERR_OPEN_SOCK, getComponentId() +  " Failed to connect as client blocking socket: " + 
                        ", host=" + _socketConfig.getHostname() + ", port=" + _socketConfig.getPort() + " : " + e.getMessage() );
        } catch(Exception e) {
            _log.error( SessionConstants.ERR_OPEN_SOCK, getComponentId() + " Failed to create client blocking socket: " + e.getMessage() + 
                        ", host=" + _socketConfig.getHostname() + ", port=" + _socketConfig.getPort(), e );
        }

        cleanUp( channel );
        return null;
    }

    private SocketAddress getSockAddress() throws IOException {
        int     port  = _socketConfig.getPort();
        ZString zHost = _socketConfig.getHostname();
        String  host  = null;

        if ( zHost != null ) {
            host = zHost.toString();
            if ( Character.isDigit( host.charAt( 0 ) ) ) { // assume IP address
                InetAddress[] ias = InetAddress.getAllByName( host );
    
                _log.info( "Session " + getComponentId() + ", found " + ias.length + " entries for IP address " + host );
                
                ZString adapter = _socketConfig.getNic();
                
                if ( adapter != null && adapter.length() > 0 ) {
                    NetworkInterface nif = NetworkInterface.getByName( adapter.toString() );
                
                    for ( InetAddress ia : ias ) {
                        if ( ia.isReachable( nif, 0, CLIENT_REACHABLE_TIMEOUT ) ) {
                            _log.info( "Session " + getComponentId() + " reachable by " + adapter + ", host=" + host + ", port=" + port );
                            
                            SocketAddress addr = new InetSocketAddress( ia, _socketConfig.getPort() );
                            return addr;
                        }
                    }
                } else {
                    _log.info( "Session " + getComponentId() + ", no adapter specified for IP address " + host + ", port=" + port );
                    
                    InetAddress ia = InetAddress.getByName( host );
                    SocketAddress addr = new InetSocketAddress( ia, port );
                    return addr;
                }
            }
        }

        _log.info( "Session " + getComponentId() + " getSockAddress " + host + " requires hostname lookup, port=" + port );
        
        SocketAddress addr = new InetSocketAddress( host, _socketConfig.getPort() );
        return addr;
    }

    private LiteSocket clientNIOConnect() {
        LiteSocket socketChannel = null;
        try {
            socketChannel = SocketFactory.instance().createClientSocket( _socketConfig, _inNativeByteBuffer, _outNativeByteBuffer ); 
            socketChannel.configureBlocking( false );
            setSocketOptions( socketChannel );
            bindClientInterface( socketChannel );
            SocketAddress addr = getSockAddress();
            socketChannel.connect( addr );
            while( !socketChannel.finishConnect() ) {
                Utils.delay( 200 );     // Spin until connection is established
            }
            setupChannel( socketChannel );
            return socketChannel;
            
        } catch( ConnectException e ) {
            _log.error( SessionConstants.ERR_OPEN_SOCK, getComponentId() + " Failed to connect as client NIO socket: " + 
                        ", host=" + _socketConfig.getHostname() + ", port=" + _socketConfig.getPort() + " : " + e.getMessage() );
        } catch( Exception e ) {
            _log.error( SessionConstants.ERR_OPEN_SOCK, getComponentId() + " Failed to create client NIO socket: " + e.getMessage() + 
                        ", host=" + _socketConfig.getHostname() + ", port=" + _socketConfig.getPort(), e );
        }

        cleanUp( socketChannel );
        
        return null;
    }

    private LiteSocket serverBlockingConnect() {
        LiteServerSocket    serverChannel = null;
        LiteSocket          channel       = null; 
        try {
            serverChannel = new ServerSocketChannelProxy( ServerSocketChannel.open(), _inNativeByteBuffer, _outNativeByteBuffer );
            serverChannel.configureBlocking( true );
            bindServerInterface( serverChannel );
            setState( Session.State.Listening );
            channel = serverChannel.accept();
            channel.configureBlocking( true );
            setupChannel( channel );
            setSocketOptions( channel );
        } catch(Exception e) {
            _log.error( SessionConstants.ERR_OPEN_SOCK, "Failed to create blocking server socket: " + e.getMessage() + 
                        ", host=" + _socketConfig.getHostname() + ", port=" + _socketConfig.getPort(), e );
        } finally {
            cleanUp( serverChannel );
        }
        return channel;
    }
    
    private LiteSocket createMulticastSocket() {
        LiteSocket socketChannel = null; 
        try {
            setState( Session.State.Listening );
            socketChannel = SocketFactory.instance().createMulticastSocket( _socketConfig, _inNativeByteBuffer, _outNativeByteBuffer );
            setupChannel( socketChannel );
            setSocketOptions( socketChannel );
            socketChannel.configureBlocking( false );

        } catch(Exception e) {
            _log.error( SessionConstants.ERR_OPEN_SOCK, getComponentId() + " Failed to create server NIO socket: " + e.getMessage() + 
                        ", host=" + _socketConfig.getHostname() + ", port=" + _socketConfig.getPort(), e );
        }
        return socketChannel;
    }

    private LiteSocket serverNIOConnect() {
        LiteServerSocket serverSocketChannel = null;
        LiteSocket       socketChannel       = null; 
        try {
            serverSocketChannel = SocketFactory.instance().createServerSocket( _socketConfig, _inNativeByteBuffer, _outNativeByteBuffer );
            serverSocketChannel.configureBlocking( true );
            bindServerInterface( serverSocketChannel );
            setState( Session.State.Listening );
            socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking( false );
            while( !socketChannel.finishConnect() ) {
                Utils.delay( 200 );     // Spin until connection is established
            }
            setupChannel( socketChannel );
            setSocketOptions( socketChannel );
            
        } catch(Exception e) {
            _log.error( SessionConstants.ERR_OPEN_SOCK, getComponentId() + " Failed to create server NIO socket: " + e.getMessage() + 
                        ", host=" + _socketConfig.getHostname() + ", port=" + _socketConfig.getPort(), e );
        } finally {
            cleanUp( serverSocketChannel );
        }
        return socketChannel;
    }

    private boolean bindClientInterface( LiteSocket socketChannel ) {
        boolean bound = false;
        
        ZString adapter = _socketConfig.getNic();
        
        try {
            if ( adapter != null && adapter.length() > 0 ) {
                NetworkInterface nif = NetworkInterface.getByName( adapter.toString() );
                Enumeration<InetAddress> nifAddresses = nif.getInetAddresses();
   
                InetAddress addr = nifAddresses.nextElement();
                int localPort = _socketConfig.getLocalPort();
                InetSocketAddress sa = new InetSocketAddress( addr, localPort );
                socketChannel.bind( sa );
                
                bound = true;
                
                _log.info( "SocketSession client " + getComponentId() + " bound to interface " + adapter + ", localPort=" + localPort );
            } else {
                _log.info( "SocketSession client " + getComponentId() + " no interface specified wont bind locally" );
            }
            
        } catch( Exception e ) {
            _log.warn( "Failed to bind thru network interface " + adapter + " for " + getComponentId() );
        }
        
        return bound;
    }

    private void bindServerInterface( LiteServerSocket serverSocketChannel ) throws IOException {
        ZString adapter = _socketConfig.getNic();
        
        try {
            if ( adapter != null && adapter.length() > 0 ) {
                NetworkInterface nif = NetworkInterface.getByName( adapter.toString() );
                Enumeration<InetAddress> nifAddresses = nif.getInetAddresses();
   
                InetAddress addr = nifAddresses.nextElement();
                
                SocketAddress saddr = new InetSocketAddress( addr, _socketConfig.getPort() );
                serverSocketChannel.bind( saddr );
                
                _log.info( "SocketSession server " + getComponentId() + " bound to local interface " + adapter + ", port=" + _socketConfig.getPort() );

                return;
            }
        } catch( Exception e ) {
            _log.warn( "Failed to bind thru network interface " + adapter );
        }

        SocketAddress saddr = getSockAddress();
        serverSocketChannel.bind( saddr );
    }

    private void setupChannel( LiteSocket channel ) throws Exception {
        channel.setTcpNoDelay( _socketConfig.getTcpNoDelay() );
    }
}
