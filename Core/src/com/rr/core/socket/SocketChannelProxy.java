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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.rr.core.lang.ReusableString;

public final class SocketChannelProxy implements LiteSocket {

    private final SocketChannel _sc;
    private final ByteBuffer    _inBuf;
    private final ByteBuffer    _outBuf;

    public SocketChannelProxy( SocketChannel sc, ByteBuffer inBuf, ByteBuffer outBuf ) {
        _sc     = sc;
        _inBuf  = inBuf;
        _outBuf = outBuf;
    }

    @Override
    public void close() throws IOException {
        _sc.close();
    }

    @Override
    public int read() throws IOException {
        return _sc.read( _inBuf );
    }

    @Override
    public int write() throws IOException {
        return _sc.write( _outBuf );
    }
    
    @Override
    public void info( ReusableString out ) {
        // nothing
    }

    @Override
    public void configureBlocking( boolean isBlocking ) throws IOException {
        _sc.configureBlocking( isBlocking );
    }

    @Override
    public void setTcpNoDelay( boolean tcpNoDelay ) throws SocketException {
        _sc.socket().setTcpNoDelay( tcpNoDelay );
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return _sc.socket().getKeepAlive();
    }

    @Override
    public void setKeepAlive( boolean b ) throws SocketException {
        _sc.socket().setKeepAlive( b );
    }

    @Override
    public int getSoLinger() throws SocketException {
        return _sc.socket().getSoLinger();
    }

    @Override
    public void setSoLinger( boolean b, int soLinger ) throws SocketException {
        _sc.socket().setSoLinger( b, soLinger );
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return _sc.socket().getSoTimeout();
    }

    @Override
    public void setSoTimeout( int soTimeout ) throws SocketException {
        _sc.socket().setSoTimeout( soTimeout );
    }

    @Override
    public void bind( InetSocketAddress sa ) throws IOException {
        _sc.socket().bind( sa );
    }

    @Override
    public boolean connect( SocketAddress addr ) throws IOException {
        return _sc.connect( addr );
    }

    @Override
    public boolean finishConnect() throws IOException {
        return _sc.finishConnect();
    }
}
