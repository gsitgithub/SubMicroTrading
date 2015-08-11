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
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import com.rr.core.lang.ReusableString;

public final class MulticastSocketAdapter implements LiteMulticastSocket {

    private static final int DEFAULT_TIMEOUT_MS = 100;
    
    private final MulticastSocket      _inSocket;
    private final MulticastSocket      _outSocket;
    
    private final ByteBuffer           _inBuf;
    private final DatagramPacket       _inPacket;
    
    private final ByteBuffer           _outBuf;
    private final DatagramPacket       _outPacket;
    private final boolean              _inShareArr;
    private final boolean              _outShareArr;
    private final byte[]               _inArr;
    private final byte[]               _outArr;
    private final int                  _port;

    public MulticastSocketAdapter( int port, boolean disableLoopback, int qos, int ttl, ByteBuffer inBuf, ByteBuffer outBuf ) throws IOException {
        _port = port;
        _outBuf = outBuf;
        _inBuf = inBuf;
        
        _inSocket  = new MulticastSocket( port );
        _outSocket = new MulticastSocket();         // send socket doesnt need port

        _inSocket.setReuseAddress( true );
        _outSocket.setReuseAddress( true );
        
        _inSocket.setTimeToLive( ttl );
        _outSocket.setTimeToLive( ttl );
        
        _inSocket.setTrafficClass( qos );
        _outSocket.setTrafficClass( qos );

        _inSocket.setLoopbackMode( disableLoopback );
        _outSocket.setLoopbackMode( disableLoopback );

        _inSocket.setBroadcast( true );
        _outSocket.setBroadcast( true );
        _outSocket.setTimeToLive( ttl );

        _inShareArr  = inBuf.hasArray();
        _outShareArr = outBuf.hasArray();
        
        _inArr  = ( _inShareArr ? inBuf.array()  : new byte[inBuf.capacity()] );
        _outArr = ( _outShareArr ? outBuf.array() : new byte[outBuf.capacity()] );
        
        _inPacket   = new DatagramPacket( _inArr,  _inArr.length,  null, 0 );
        _outPacket  = new DatagramPacket( _outArr, _outArr.length, null, 0 );
        
    }

    @Override
    public void close() throws IOException {
        _inSocket.close();
        _outSocket.close();
    }

    @Override
    public int read() throws IOException {
        try {
            _inSocket.receive( _inPacket );

            final int read = _inPacket.getLength(); 
            if ( read > 0 ) {
                if ( _inShareArr == false ) {
                    _inBuf.position( 0 );
                    _inBuf.put( _inArr, 0, read );
                }
                _inBuf.position( read );
            }
            
            return read;
        } catch( SocketTimeoutException e ) {
            return 0;
        }
    }

    @Override
    public int write() throws IOException {
        final int bytes = _outBuf.limit();
        if ( _outShareArr == false ) {
            _outBuf.get( _outArr, 0, bytes );
        }
        _outPacket.setLength( bytes );
        _outSocket.send( _outPacket );
        
        return _outPacket.getLength(); // ASSUMES SUCCESS - DODGY
    }

    @Override
    public void configureBlocking( boolean isBlocking ) throws IOException {
        _inSocket.setSoTimeout( (isBlocking) ? 0 :  DEFAULT_TIMEOUT_MS );
    }

    @Override
    public void setTcpNoDelay( boolean tcpNoDelay ) throws SocketException {
        // N/A
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return false;
    }

    @Override
    public void info( ReusableString out ) {
        // nothing
    }
    
    @Override
    public void setKeepAlive( boolean b ) throws SocketException {
        // N/A
    }

    @Override
    public int getSoLinger() throws SocketException {
        return 0;
    }

    @Override
    public void setSoLinger( boolean b, int soLinger ) throws SocketException {
        // N/A
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return _outSocket.getSoTimeout();
    }

    @Override
    public void setSoTimeout( int soTimeout ) throws SocketException {
        _outSocket.setSoTimeout( soTimeout );
    }

    @Override
    public void bind( InetSocketAddress sa ) throws IOException {
        // N/A
    }

    @Override
    public boolean connect( SocketAddress addr ) throws IOException {
        
        _outSocket.connect( addr ); // connect out socket so that send can be achieved with write under the covers

        // if running on same host loopback must be enabled
        
        return true;
    }

    @Override
    public boolean finishConnect() throws IOException {
        return true;
    }

    @Override
    public void joinGroup( String mcastGroupAddrIP, String localInterfaceIP ) throws IOException {
        InetSocketAddress group = new InetSocketAddress( mcastGroupAddrIP, _port );
        _inSocket.joinGroup( group.getAddress() );
    }
}
