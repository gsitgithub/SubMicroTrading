/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.progs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.rr.core.session.DisconnectedException;

public class PingPongServer {

    private static final int    MSG_SIZE             = 16;  // 8 bytes for long pingId, 8 bytes for nanoSent
    
    private ServerSocketChannel _serverSocketChannel = null;
    private SocketChannel       _channel             = null; 
    private ServerSocket        _serverSocket        = null;
    private int                 _port;

    protected final ByteBuffer      _nativeByteBuffer   = ByteBuffer.allocateDirect( MSG_SIZE+1 );
    
    public static void main( String[] args ) {
        if ( args.length != 1 ) {
            System.out.println( "Usage : port to listen too" );
            System.exit( -1 );
        }

        PingPongServer tsl = new PingPongServer( Integer.parseInt( args[0] ) );
        
        try {
            
            tsl.connect();
            
            tsl.pong();
            
        } catch ( Exception e ) {
            System.out.println( "TstSocketListener : exception " + e.getMessage() );
        }
        
        tsl.close();
    }
    
    public PingPongServer( int port ) {
        System.out.println( "Listen to " + port );
        _port = port;
    }

    private void pong() throws Exception {
        
        while( true ) {
            int read = getMessage( MSG_SIZE );
            
            if ( read > 0 ) {
                sendBack( MSG_SIZE );
            } else{
                // if using selectors then would invoke select again
            }
        }
    }

    private int getMessage( int msgSize ) throws Exception {
        int totalRead = 0;
        int curRead;

        _nativeByteBuffer.position( 0 );
        _nativeByteBuffer.limit( msgSize );

        while( totalRead < msgSize ) {
            
            curRead = _channel.read( _nativeByteBuffer );

            if ( curRead == -1 ) throw new DisconnectedException( "Detected socket disconnect" );

            // spurious wakeup
            
            if ( curRead == 0 && totalRead == 0 ) {
                return 0;
            }
            
            totalRead += curRead;
        }
        
        _nativeByteBuffer.flip();
        
        return totalRead;
    }

    private void sendBack( int msgSize ) throws IOException {
        writeSocket();
    }
    
    protected final void writeSocket() throws IOException {
        
        long failWriteCount = 0;

        do {
            if ( _channel.write( _nativeByteBuffer ) == 0 ) {
                if ( failWriteCount++ >= 5 ) {
                    try{ Thread.sleep( 1 ); } catch( Exception e ) {  /* dont care */ }
                    
                    System.out.println( "WARN: Delayed Write : possible slow consumer" );
                }
            }
        } while( _nativeByteBuffer.hasRemaining() );
    }

    public void connect() throws IOException {
        _serverSocketChannel = ServerSocketChannel.open();
        _serverSocketChannel.configureBlocking( true );
        SocketAddress addr = new InetSocketAddress( _port );
        _serverSocket = _serverSocketChannel.socket();
        _serverSocket.bind( addr );
        _channel = _serverSocketChannel.accept();
        _channel.configureBlocking( false );

        Socket socket = _channel.socket();
        _channel.socket().setTcpNoDelay( true );
        
        while( !_channel.finishConnect() ) {
            try{ Thread.sleep( 200 ); } catch( Exception e ) {  /* dont care */ }
        }

        System.out.println( "Connected " );

        socket.setKeepAlive( false );
        socket.setSoLinger( false, 0 );
    }
    
    public void close() {
        try { if ( _channel != null )             _channel.close();             } catch( Exception e ) { /* NADA */ }
        try { if ( _serverSocket != null )        _serverSocket.close();        } catch( Exception e ) { /* NADA */ }
        try { if ( _serverSocketChannel != null ) _serverSocketChannel.close(); } catch( Exception e ) { /* NADA */ }
    }
}
