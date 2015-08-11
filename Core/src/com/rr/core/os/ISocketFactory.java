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

import com.rr.core.session.socket.SocketConfig;
import com.rr.core.socket.LiteMulticastSocket;
import com.rr.core.socket.LiteServerSocket;
import com.rr.core.socket.LiteSocket;

public interface ISocketFactory {

    public LiteServerSocket createServerSocket( SocketConfig socketConfig, ByteBuffer inBuf, ByteBuffer outBuf ) throws IOException;

    public LiteSocket createClientSocket( SocketConfig socketConfig, ByteBuffer inBuf, ByteBuffer outBuf ) throws IOException;

    /**
     * create a multicast socket and connect to first grp
     * 
     * if not server socket then subscribe to all grps 
     * 
     * @param socketConfig
     * @param inBuf
     * @param outBuf
     * @return
     * @throws IOException
     */
    public LiteMulticastSocket createMulticastSocket( SocketConfig socketConfig, ByteBuffer inBuf, ByteBuffer outBuf ) throws IOException;
        
    public String getNicIp( SocketConfig socketConfig ) throws SocketException;
}
