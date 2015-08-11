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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;

public final class ServerSocketChannelProxy implements LiteServerSocket {

    private final ServerSocketChannel _ssc;
    private final ByteBuffer          _inBuf;
    private final ByteBuffer          _outBuf;
    
    public ServerSocketChannelProxy( ServerSocketChannel ssc, ByteBuffer inBuf, ByteBuffer outBuf ) {
        _ssc = ssc;
        _inBuf = inBuf;
        _outBuf = outBuf;
    }

    @Override
    public void configureBlocking( boolean isBlocking ) throws IOException {
        _ssc.configureBlocking( true );
    }

    @Override
    public LiteSocket accept() throws IOException {
        return new SocketChannelProxy( _ssc.accept(), _inBuf, _outBuf );
    }

    @Override
    public void bind( SocketAddress saddr ) throws IOException {
        _ssc.socket().bind( saddr );
    }

    @Override
    public void close() throws IOException {
        _ssc.close();
    }
}
