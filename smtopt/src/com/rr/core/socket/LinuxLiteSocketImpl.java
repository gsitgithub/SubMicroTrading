/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.UnresolvedAddressException;

import com.rr.core.lang.ReusableString;

/**
 * lite socket 
 * 
 * @NOTE not threadsafe (expected to be used from single thread for read and single thread for write) 
 */

public final class LinuxLiteSocketImpl implements LiteSocket {

    private enum State {
        Unconnected, Pending, Connected, Closed
    }

    private       State             _state;

    private final Object            _stateLock       = new Object();

    private final Object            _regLock         = new Object();

    private       int               _fd;
    private       InetSocketAddress _remoteAddress;
    private       boolean           _blocking        = true;            
    private final ByteBuffer        _inBuf;
    private final ByteBuffer        _outBuf;

    /**
     * constructor invoked on an accepted socket
     */
    public LinuxLiteSocketImpl( int fd, InetSocketAddress isa, ByteBuffer inBuf, ByteBuffer outBuf ) {
        _inBuf = inBuf;
        _outBuf = outBuf;
        _fd = fd;
        _remoteAddress = isa;
        _state = State.Connected;
    }

    public LinuxLiteSocketImpl( ByteBuffer inBuf, ByteBuffer outBuf ) {
        _fd = LinuxSocketImpl.socket( true, false );
        _state = State.Unconnected;
        _inBuf = inBuf;
        _outBuf = outBuf;
    }

    @Override
    public void close() throws IOException {
        synchronized( _stateLock ) {
            switch( _state ) {
            case Closed:
                return;
            case Connected:
            case Pending:
                LinuxSocketImpl.close( _fd );
                _state = State.Closed;
                _fd = -1;
                break;
            case Unconnected:
                _state = State.Closed;
                return;
            }
        }
    }

    public boolean isBlocking() {
        synchronized( _regLock ) {
            return _blocking;
        }
    }

    @Override
    public int read() throws IOException {

        if ( !isOpen() ) return -1;

        return (_blocking) ? readBlocking() : readNonBlock();
    }

    @Override
    public int write() throws IOException {

        if ( !isOpen() ) return -1;

        return (_blocking) ? writeBlocking() : writeNonBlock();
    }

    @Override
    public void info( ReusableString out ) {
        int flags = LinuxSocketImpl.getFlags( _fd );
        
        out.append( ", fd=" ).append( _fd ).append( ", flags=" ).append( flags );
    }

    @Override
    public void configureBlocking( boolean isBlocking ) throws IOException {
        if ( !isOpen() )
            throw new ClosedChannelException();

        synchronized( _regLock ) {

            int res = LinuxSocketImpl.configureBlocking( _fd, isBlocking );
            
            if ( res != 0 ) {
                throw new IOException( "Failed to set socket to blocking, res=" + res );
            }
            
            _blocking = isBlocking;
        }
    }

    private boolean isOpen() {
        return _fd >= 0;
    }

    @Override
    public void bind( InetSocketAddress local ) throws IOException {
        if ( local.isUnresolved() )
            throw new UnresolvedAddressException(); 
        LinuxSocketImpl.bind( _fd, local.getAddress(), local.getPort() );
    }

    @Override
    public boolean connect( SocketAddress addr ) throws IOException {
        int trafficClass = 0; 

        ensureOpenAndUnconnected();
        InetSocketAddress isa = LinuxSocketImpl.checkAddress( addr );
        SecurityManager sm = System.getSecurityManager();
        
        if ( sm != null )
            sm.checkConnect( isa.getAddress().getHostAddress(), isa.getPort() );
        
        synchronized( _regLock ) {
            int res = 0;
            
            try {
                synchronized( _stateLock ) {
                    if ( !isOpen() ) {
                        return false;
                    }
                }
        
                for ( ; ; ) {
                    InetAddress ia = isa.getAddress();
                    if ( ia.isAnyLocalAddress() ) ia = InetAddress.getLocalHost();
                    res = LinuxSocketImpl.connect( _fd, ia, isa.getPort(), trafficClass );
                    if ( (res == LinuxSocketImpl.INTERRUPTED) && isOpen() )
                        continue;
                    break;
                }
            } catch( IOException e ) {
                close();
                throw e;
            }
            
            synchronized( _stateLock ) {
                _remoteAddress = isa;
                if ( res > 0 ) {
                    _state = State.Connected;
                    return true;
                }
                if ( !isBlocking() )
                    _state = State.Pending;
                else
                    assert false;
            }
        }

        return false;
    }

    @Override
    public boolean finishConnect() throws IOException {
        synchronized( _stateLock ) {
            if ( !isOpen() )
                throw new ClosedChannelException();
            if ( _state == State.Connected )
                return true;
            if ( _state != State.Pending )
                throw new NoConnectionPendingException();
        }
        int res = 0;
        try {
            try {
                synchronized( _regLock ) {
                    synchronized( _stateLock ) {
                        if ( !isOpen() ) {
                            return false;
                        }
                    }
                    if ( !isBlocking() ) {
                        res = nonBlockingConnect( res );
                    } else {
                        res = blockingConnect( res );
                    }
                }
            } finally {
                assert LinuxSocketImpl.check( res );
            }
        } catch( IOException e ) {
            close();
            throw e;
        }
        
        if ( res > 0 ) {
            synchronized( _stateLock ) {
                _state = State.Connected;
            }
            return true;
        }
        
        return false;
    }

    private int blockingConnect( int res ) throws IOException {
        for ( ; ; ) {
            res = LinuxSocketImpl.checkConnect( _fd, true, false );
            if ( res == 0 ) {
                continue;
            }
            if ( (res == LinuxSocketImpl.INTERRUPTED) && isOpen() )
                continue;
            break;
        }
        return res;
    }

    private int nonBlockingConnect( int res ) throws IOException {
        for ( ; ; ) {
            res = LinuxSocketImpl.checkConnect( _fd, false, false );
            if ( (res == LinuxSocketImpl.INTERRUPTED) && isOpen() )
                continue;
            break;
        }
        return res;
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        int v = LinuxSocketImpl.getOption( _fd, SocketOptions.SO_KEEPALIVE );
        
        return (v==0) ? false : true;
    }

    @Override
    public int getSoLinger() throws SocketException {
        return LinuxSocketImpl.getOption( _fd, SocketOptions.SO_LINGER );
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return 0; // non blocking
    }

    @Override
    public void setTcpNoDelay( boolean tcpNoDelay ) throws SocketException {
        LinuxSocketImpl.setOption( _fd, SocketOptions.TCP_NODELAY, (tcpNoDelay) ? 1 : 0 );
    }

    @Override
    public void setKeepAlive( boolean alive ) throws SocketException {
        LinuxSocketImpl.setOption( _fd, SocketOptions.SO_KEEPALIVE, (alive) ? 1 : 0 );
    }

    @Override
    public void setSoLinger( boolean b, int soLinger ) throws SocketException {
        LinuxSocketImpl.setOption( _fd, SocketOptions.SO_LINGER, soLinger );
    }

    @Override
    public void setSoTimeout( int soTimeout ) throws SocketException {
        // nothing to do - non blocking
    }

    public boolean isConnected() {
        synchronized( _stateLock ) {
            return (_state == State.Connected);
        }
    }

    public boolean isConnectionPending() {
        synchronized( _stateLock ) {
            return (_state == State.Pending);
        }
    }

    public InetSocketAddress getRemoteAddress() {
        return _remoteAddress;
    }

    private void ensureOpenAndUnconnected() throws IOException { 
        synchronized( _stateLock ) {
            if ( !isOpen() )
                throw new ClosedChannelException();
            if ( _state == State.Connected )
                throw new AlreadyConnectedException();
            if ( _state == State.Pending )
                throw new ConnectionPendingException();
        }
    }

    private int writeBlocking() throws IOException {
        int n;
        
        for ( ; ; ) {
            n = LinuxSocketImpl.writeFromNativeBuffer( _fd, _outBuf );

            if ( (n == LinuxSocketImpl.INTERRUPTED) && isOpen() )
                continue;

            return LinuxSocketImpl.normalize( n );
        }
    }

    private int writeNonBlock() throws IOException {
        int n = LinuxSocketImpl.writeFromNativeBuffer( _fd, _outBuf );
        
        if ( n == LinuxSocketImpl.INTERRUPTED ) 
            return 0;

        return LinuxSocketImpl.normalize( n );
    }

    private int readBlocking() throws IOException {
        int n;
        
        for ( ; ; ) {
            n = LinuxSocketImpl.readIntoNativeBuffer( _fd, _inBuf );

            if ( (n == LinuxSocketImpl.INTERRUPTED) && isOpen() ) {
                continue;
            }

            return LinuxSocketImpl.normalize( n );
        }
    }

    private int readNonBlock() throws IOException {
        int n = LinuxSocketImpl.readIntoNativeBuffer( _fd, _inBuf );

        if ( n == LinuxSocketImpl.INTERRUPTED ) {
            return 0;
        }

        return LinuxSocketImpl.normalize( n );
    }
}
