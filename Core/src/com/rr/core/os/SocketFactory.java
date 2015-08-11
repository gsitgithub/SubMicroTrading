/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.os;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;

import com.rr.core.lang.Env;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.properties.AppProps;
import com.rr.core.properties.CoreProps;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.socket.LiteMulticastSocket;
import com.rr.core.socket.LiteServerSocket;
import com.rr.core.socket.LiteSocket;
import com.rr.core.utils.ReflectUtils;

public class SocketFactory implements ISocketFactory {

    private static final Logger _log = LoggerFactory.create( SocketFactory.class );

    private final static SocketFactory  _instance = new SocketFactory();
    private final        ISocketFactory _factory;
    
    public static SocketFactory instance() { return _instance; }
    
    private SocketFactory() {
        String sockFacClass = Env.getSocketFactoryClass();
        
        if ( sockFacClass == null ) 
            sockFacClass = AppProps.instance().getProperty( CoreProps.SOCKET_FACTORY, false, "com.rr.core.os.StandardSocketFactory" );

        _log.info( "SocketFactoryClass " + sockFacClass );

        _factory = ReflectUtils.create( sockFacClass );
    }
    
    @Override
    public LiteServerSocket createServerSocket( SocketConfig socketConfig, ByteBuffer inBuf, ByteBuffer outBuf ) throws IOException {
        return _factory.createServerSocket( socketConfig, inBuf, outBuf );
    }

    @Override
    public LiteSocket createClientSocket( SocketConfig socketConfig, ByteBuffer inBuf, ByteBuffer outBuf ) throws IOException {
        return _factory.createClientSocket( socketConfig, inBuf, outBuf );
    }

    @Override
    public LiteMulticastSocket createMulticastSocket( SocketConfig socketConfig, ByteBuffer inBuf, ByteBuffer outBuf ) throws IOException {
        return _factory.createMulticastSocket( socketConfig, inBuf, outBuf );
    }

    @Override
    public String getNicIp( SocketConfig socketConfig ) throws SocketException {
        return _factory.getNicIp( socketConfig );
    }
}
