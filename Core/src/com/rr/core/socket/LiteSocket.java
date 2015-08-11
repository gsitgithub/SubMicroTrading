/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.socket;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

import com.rr.core.lang.ReusableString;

public interface LiteSocket extends Closeable {

    public int read() throws IOException;

    public int write() throws IOException;

    public void configureBlocking( boolean isBlocking ) throws IOException;

    public void setTcpNoDelay( boolean tcpNoDelay ) throws SocketException;

    public boolean getKeepAlive() throws SocketException;

    public void setKeepAlive( boolean b ) throws SocketException;

    public int getSoLinger() throws SocketException;

    public void setSoLinger( boolean b, int soLinger ) throws SocketException;

    public int getSoTimeout() throws SocketException;

    public void setSoTimeout( int soTimeout ) throws SocketException;

    /**
     * bind to local port
     * 
     * @param sa
     * @throws IOException 
     */
    public void bind( InetSocketAddress local ) throws IOException;

    public boolean connect( SocketAddress addr ) throws IOException;

    public boolean finishConnect() throws IOException;

    public void info( ReusableString out );
}
