/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.utils.Utils;

// test using selector for reading from channel

public class TestSelector extends BaseTestCase {

    private static final int    MSG_SIZE             = 16;  // 8 bytes for long pingId, 8 bytes for nanoSent
    
    private ServerSocketChannel _inServerSocketChannel = null;
    private SocketChannel       _inChannel             = null; 
    private ServerSocket        _inServerSocket        = null;
    private int                 _port;
    private Selector            _inSelector;
    private SelectionKey        _inSelectionKey;
    private final ByteBuffer    _inNativeByteBuffer    = ByteBuffer.allocateDirect( MSG_SIZE+1 );

    private String              _host                  = "127.0.0.1";
    private int                 _msDelay;
    private SocketChannel       _outChannel            = null; 
    private final ByteBuffer    _outNativeByteBuffer   = ByteBuffer.allocateDirect( MSG_SIZE+1 );

            int                 _runSize;
    private int                 _in;
    private int                 _out;
    

    public void testOneWayNIO() throws Exception {
        _port    = 14880;
        _runSize = 10000;
        _msDelay = 1;
        
        int maxTime = 20000;
        
        Thread consumer = new Thread( new Runnable() {
            @Override
            public void run() {
                try {
                    inConnect();
                    
                    consume( _runSize );
                    
                    inClose();
                } catch( Exception e ) {
                    fail( e.getMessage() );
                }
            }}, "CONSUMER"  );
        
        Thread producer = new Thread( new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    outConnect();
                                                    
                                                    produce( _runSize );
                                                    
                                                    outClose();
                                                } catch( Exception e ) {
                                                    fail( e.getMessage() );
                                                }
                                            }}, "PRODUCER"  );
        
        consumer.start();
        producer.start();
        
        consumer.join( maxTime );
        producer.join( maxTime );
        
        assertEquals( _runSize, _in );
        assertEquals( _runSize, _out );
    }
    
    void consume( int max ) throws Exception {
        _in = 0;
        
        while( _in < max ) {
            int read = getMessage( MSG_SIZE );
            
            if ( read > 0 ) {
                long now = Utils.nanoTime();
                
                long id   = _inNativeByteBuffer.getLong();
                long sent = _inNativeByteBuffer.getLong();
                
                long delay = Math.abs( (sent - now) / 1000 );
                
                System.out.println( "Read id=" + id + ", usecs=" + delay );
                
                ++_in;
            }
        }
    }

    void produce( int max ) throws Exception {
        _out = 0;
        
        while( _out < max ) {
            send( _out, Utils.nanoTime() );
            
            ++_out;

            // System.out.println( "Sent id=" + _out );
            
            try{ Thread.sleep( _msDelay ); } catch( Exception e ) {  /* dont care */ }
        }
    }
    
    private void send( long idx, long nanoTime ) throws IOException {

        _outNativeByteBuffer.clear();
        _outNativeByteBuffer.putLong( idx );
        _outNativeByteBuffer.putLong( nanoTime );
        
        _outNativeByteBuffer.flip();
        
        writeSocket();
    }

    private int getMessage( int msgSize ) throws Exception {
        int totalRead = 0;
        int curRead;

        _inNativeByteBuffer.position( 0 );
        _inNativeByteBuffer.limit( msgSize );

        while( totalRead < msgSize ) {
            
            if ( _inSelector.select(1000) <= 0 ) continue;
            if ( ! _inSelectionKey.isValid() || !_inSelectionKey.isReadable() ) continue;
            Set<SelectionKey> readyKeys = _inSelector.selectedKeys();
            if ( ! readyKeys.isEmpty() ) { 
                Iterator<SelectionKey> iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    iterator.next();
                    iterator.remove();
                }
                
                curRead = _inChannel.read( _inNativeByteBuffer );
    
                if ( curRead == -1 ) throw new Exception( "Detected socket disconnect" );
    
                // spurious wakeup
                
                if ( curRead == 0 && totalRead == 0 ) {
                    return 0;
                }
                
                totalRead += curRead;
            }
        }
        
        _inNativeByteBuffer.flip();
        
        return totalRead;
    }

    protected final void writeSocket() throws IOException {
        
        long failWriteCount = 0;

        do {
            if ( _outChannel.write( _outNativeByteBuffer ) == 0 ) {
                if ( failWriteCount++ >= 5 ) {
                    try{ Thread.sleep( 1 ); } catch( Exception e ) {  /* dont care */ }
                    
                    System.out.println( "WARN: Delayed Write : possible slow consumer" );
                }
            }
        } while( _outNativeByteBuffer.hasRemaining() );
    }

    public void inConnect() throws IOException {
        
        _inServerSocketChannel = ServerSocketChannel.open();
        _inServerSocketChannel.configureBlocking( true );
        SocketAddress addr = new InetSocketAddress( _port );
        _inServerSocket = _inServerSocketChannel.socket();
        _inServerSocket.bind( addr );
        _inChannel = _inServerSocketChannel.accept();
        _inChannel.configureBlocking( false );

        Socket socket = _inChannel.socket();
        _inChannel.socket().setTcpNoDelay( true );
        
        while( !_inChannel.finishConnect() ) {
            try{ Thread.sleep( 200 ); } catch( Exception e ) {  /* dont care */ }
        }

        System.out.println( "Connected " );

        _inSelector = Selector.open();
        _inSelectionKey = _inChannel.register( _inSelector, SelectionKey.OP_READ );
        
        socket.setKeepAlive( false );
        socket.setSoLinger( false, 0 );
    }
    
    public void outConnect() throws IOException {
        _outChannel = SocketChannel.open();

        Socket socket = _outChannel.socket();
        socket.setTcpNoDelay( true );
        socket.setKeepAlive( false );
        socket.setSoLinger( false, 0 );

        SocketAddress addr = new InetSocketAddress( _host, _port );
        
        _outChannel.connect( addr );
        _outChannel.configureBlocking( false );
        while( !_outChannel.finishConnect() ) {
            try{ Thread.sleep( 200 ); } catch( Exception e ) {  /* dont care */ }
        }

        System.out.println( "Connected " );
    }
    
    public void inClose() {
        try { if ( _inChannel != null )             _inChannel.close();             } catch( Exception e ) { /* NADA */ }
        try { if ( _inServerSocket != null )        _inServerSocket.close();        } catch( Exception e ) { /* NADA */ }
        try { if ( _inServerSocketChannel != null ) _inServerSocketChannel.close(); } catch( Exception e ) { /* NADA */ }
    }
    
    public void outClose() {
        try { if ( _outChannel != null )             _outChannel.close();           } catch( Exception e ) { /* NADA */ }
    }
}
