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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;

import com.rr.core.lang.ReusableString;

/**
 * lite multicast wrapper, uses IP4 native linux multicast
 * 
 * @NOTE not threadsafe (expected to be used from single thread for read and single thread for write)
 * 
 *       THIS CODE MUST NOT BE USED IN ANY FORM OF DOWNLOADED BUNDLE NO SECURITY CHECKS
 */
public class LinuxLiteMulticastSocket implements LiteMulticastSocket {

    private enum State {
        Unconnected, Connected, Closed
    }

    private State            _state;

    private final Object     _stateLock = new Object();

    // Lock for registration and configureBlocking operations
    private final Object     _regLock   = new Object();

    private int              _fdIn;
    private int              _fdOut;
    private boolean          _blocking  = false;
    private final ByteBuffer _inBuf;
    private final ByteBuffer _outBuf;
    private final long       _groupSockAddr;           // pointer to C allocated sockaddr_in

    public LinuxLiteMulticastSocket( int port, boolean disableLoopback, int qos, int ttl, ByteBuffer inBuf, ByteBuffer outBuf, String sendLocalNetInterfaceIP,
                                     String sendMulticastGroupIP ) {
        _outBuf = outBuf;
        _inBuf = inBuf;

        try {
            _fdIn = LinuxSocketImpl.socket( false, true );
            LinuxSocketImpl.mcastIP4_client_bindAnyIF_IP4( _fdIn, port );
            LinuxSocketImpl.configureBlocking( _fdIn, _blocking );
            setOption( _fdIn, SocketOptions.IP_TOS, qos, "qos" );

            _fdOut = LinuxSocketImpl.socket( false, true );
            setOption( _fdOut, SocketOptions.IP_MULTICAST_LOOP, (disableLoopback ? 0 : 1), "disableLoopback" );
            LinuxSocketImpl.mcastIP4_server_setSendMulticastLocalIF( _fdOut, sendLocalNetInterfaceIP );
            _groupSockAddr = LinuxSocketImpl.mcastIP4_server_makeSockAddrIn( sendMulticastGroupIP, port );
            LinuxSocketImpl.configureBlocking( _fdOut, _blocking );
            setOption( _fdOut, SocketOptions.IP_TOS, qos, "qos" );

        } catch( Exception e ) {
            throw new RuntimeException( "LinuxLiteMulticastSocketImpl unable to create out udp socket : " + e.getMessage(), e );
        }

        _state = State.Unconnected;
    }

    public boolean isBlocking() {
        synchronized( _regLock ) {
            return _blocking;
        }
    }

    @Override
    public void close() throws IOException {
        synchronized( _stateLock ) {
            switch( _state ) {
            case Closed:
                return;
            case Connected:
                _state = State.Closed;
                LinuxSocketImpl.close( _fdIn );
                LinuxSocketImpl.close( _fdOut );
                _fdOut = -1;
                _fdIn = -1;
                return;
            case Unconnected:
                _state = State.Closed;
                return;
            }
        }
    }

    @Override
    public void joinGroup( String mcastGroupAddrIP, String localInterfaceIP ) throws IOException {
        LinuxSocketImpl.mcastIP4_client_join( _fdIn, mcastGroupAddrIP, localInterfaceIP );
    }

    @Override
    public int read() throws IOException {

        if ( !isOpen() )
            return -1;

        int n = LinuxSocketImpl.receiveIntoNativeBufferIgnoreSrcAddr( _fdIn, _inBuf );

        if ( n == LinuxSocketImpl.INTERRUPTED ) {
            return 0;
        }

        return LinuxSocketImpl.normalize( n );
    }

    @Override
    public int write() throws IOException {
        if ( !isOpen() )
            return -1;

        int n = LinuxSocketImpl.sendFromNativeBuffer( _fdOut, _outBuf, _groupSockAddr );

        return LinuxSocketImpl.normalize( n );
    }

    @Override
    public void info( ReusableString out ) {
        int flagsIn = LinuxSocketImpl.getFlags( _fdIn );
        int flagsOut = LinuxSocketImpl.getFlags( _fdOut );

        out.append( ", fdIn=" ).append( _fdIn ).append( ", flags=" ).append( flagsIn );
        out.append( ", fdOut=" ).append( _fdOut ).append( ", flags=" ).append( flagsOut );
    }

    @Override
    public void configureBlocking( boolean isBlocking ) throws IOException {
        if ( !isOpen() )
            throw new ClosedChannelException();

        synchronized( _regLock ) {
            LinuxSocketImpl.configureBlocking( _fdIn, isBlocking );
            LinuxSocketImpl.configureBlocking( _fdOut, isBlocking );
            _blocking = isBlocking;
        }
    }

    private boolean isOpen() {
        return _fdOut >= 0;
    }

    @Override
    public void bind( InetSocketAddress local ) throws IOException {
        //
    }

    @Override
    public boolean connect( SocketAddress addr ) throws IOException {

        synchronized( _stateLock ) {
            ensureOpenAndUnconnected();

            // no real connection

            _state = State.Connected;
        }

        return true;
    }

    @Override
    public boolean finishConnect() throws IOException {
        synchronized( _stateLock ) {
            if ( !isOpen() )
                throw new ClosedChannelException();
            if ( _state == State.Connected )
                return true;
            return false;
        }
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return false;
    }

    @Override
    public int getSoLinger() throws SocketException {
        return LinuxSocketImpl.getOption( _fdOut, SocketOptions.SO_LINGER );
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return 0; // non blocking
    }

    @Override
    public void setTcpNoDelay( boolean tcpNoDelay ) throws SocketException {

        LinuxSocketImpl.setOption( _fdIn, SocketOptions.IP_TOS, (tcpNoDelay) ? 0x10 : 0x08 );
        LinuxSocketImpl.setOption( _fdOut, SocketOptions.IP_TOS, (tcpNoDelay) ? 0x10 : 0x08 );
    }

    @Override
    public void setKeepAlive( boolean alive ) throws SocketException {
        // only for real connected sockets
    }

    @Override
    public void setSoLinger( boolean b, int soLinger ) throws SocketException {
        // only for real connected sockets
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
        return false;
    }

    private void ensureOpenAndUnconnected() throws IOException { // package-private
        synchronized( _stateLock ) {
            if ( !isOpen() )
                throw new ClosedChannelException();
            if ( _state == State.Connected )
                throw new AlreadyConnectedException();
        }
    }

    private void setOption( int fd, int option, int value, String key ) {
        try {
            LinuxSocketImpl.setOption( fd, option, value );
        } catch( SocketException e ) {
            throw new RuntimeException( "LinuxLiteMulticastSocketImpl unable to set " + key, e );
        }
    }
}
