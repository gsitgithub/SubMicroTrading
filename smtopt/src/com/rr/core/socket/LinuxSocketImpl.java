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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;

import sun.nio.ch.DirectBuffer;

final class LinuxSocketImpl {

    static final int EOF              = -1; // End of file
    static final int UNAVAILABLE      = -2; // Nothing available (non-blocking)
    static final int INTERRUPTED      = -3; // System call interrupted
    static final int UNSUPPORTED      = -4; // Operation not supported
    static final int THROWN           = -5; // Exception thrown in JNI code
    static final int UNSUPPORTED_CASE = -6; // This case not supported

    static native void init();

    static native int socket( boolean stream, boolean reuse ); 

    static native int connect( int fd, InetAddress remote, int remotePort, int trafficClass ) throws IOException; 

    static native int checkConnect( int fd, boolean block, boolean ready ) throws IOException; 

    static native int configureBlocking( int fd, boolean isBlocking ); 

    static native int getFlags( int fd );

    static native void close( int fd ); 

    static native void bind( int fd, InetAddress address, int port ); 

    static native void listen( int fd, int backlog ) throws IOException; 

    // alloc a C struct sockaddr_in which can then be passed to sendTo
    static native long mcastIP4_server_makeSockAddrIn( String mcastAddrIP, int port );

    static native void mcastIP4_server_setSendMulticastLocalIF( int sd, String localInterfaceIP ) throws IOException;

    static native int mcastIP4_server_sendTo( int sd, long databuf, int datalen, long sendGroupSockAddress ) throws IOException;

    // bind the port to the socket for any interface
    static native void mcastIP4_client_bindAnyIF_IP4( int sd, int port ) throws IOException;

    // join a multicast group at mcastAddrIP via specified local interface
    static native void mcastIP4_client_join( int sd, String mcastAddrIP, String localInterfaceIP ) throws IOException;

    // use read for the mcast client socket

    /**
     * @param fd
     * @param isaa
     * @return if < 0 error code, if >=0 the file descriptor
     */
    static native int accept( int fd, InetSocketAddress[] isaa ); 

    static native void join( int fd, InetAddress inetaddr, NetworkInterface netIf ) throws IOException; 

    static native void leave( int fd, InetAddress inetaddr, NetworkInterface netIf ) throws IOException; 

    private static native int read( int fd, long address, int position ) throws IOException; 

    private static native int receiveIgnoreSrc( int fd, long address, int position ) throws IOException; 

    private static native int write( int fd, long address, int position ) throws IOException; 

    private static native int onloadWrite( int fd, long address, int position, boolean isDummyWriteToWarmCPU ) throws IOException; 

    private static native int getIntOption( int fd, int opt ) throws IOException; 

    private static native void setIntOption( int fd, int opt, int arg ) throws IOException; 

    static int receiveIntoNativeBufferIgnoreSrcAddr( int fd, ByteBuffer bb ) throws IOException {
        int pos = bb.position();
        int lim = bb.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        if ( rem == 0 )
            return 0;

        int n = receiveIgnoreSrc( fd, ((DirectBuffer) bb).address() + pos, rem );

        if ( n > 0 )
            bb.position( pos + n );
        return n;
    }

    static int readIntoNativeBuffer( int fd, ByteBuffer bb ) throws IOException {
        int pos = bb.position();
        int lim = bb.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        if ( rem == 0 )
            return 0;

        int n = read( fd, ((DirectBuffer) bb).address() + pos, rem );

        if ( n > 0 )
            bb.position( pos + n );
        return n;
    }

    static int writeFromNativeBuffer( int fd, ByteBuffer bb ) throws IOException {
        int pos = bb.position();
        int lim = bb.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        int written = 0;
        if ( rem == 0 )
            return 0;

        written = write( fd, ((DirectBuffer) bb).address() + pos, rem );

        if ( written > 0 )
            bb.position( pos + written );
        return written;
    }

    static int sendFromNativeBuffer( int fd, ByteBuffer bb, long sendAddr ) throws IOException {
        int pos = bb.position();
        int lim = bb.limit();
        assert (pos <= lim);
        int rem = (pos <= lim ? lim - pos : 0);

        int written = 0;
        if ( rem == 0 )
            return 0;

        written = mcastIP4_server_sendTo( fd, ((DirectBuffer) bb).address() + pos, rem, sendAddr );

        if ( written > 0 )
            bb.position( pos + written );
        return written;
    }

    static boolean check( long n ) {
        return (n >= UNAVAILABLE);
    }

    static int normalize( int n ) { 
        if ( n == UNAVAILABLE )
            return 0;
        return n;
    }

    static void throwSocketException( Exception e ) throws SocketException {
        if ( e instanceof SocketException ) throw (SocketException) e;
        
        SocketException se = new SocketException( e.getMessage() );
        se.initCause( e );

        throw se;
    }

    static void throwIOException( Exception e ) throws IOException {
        if ( e instanceof IOException ) throw (IOException) e;
        
        IOException ioe = new IOException( e.getMessage() );
        ioe.initCause( e );

        throw ioe;
    }

    static InetSocketAddress checkAddress( SocketAddress sa ) {
        if ( sa == null )
            throw new IllegalArgumentException();
        if ( !(sa instanceof InetSocketAddress) )
            throw new UnsupportedAddressTypeException(); 
        InetSocketAddress isa = (InetSocketAddress) sa;
        if ( isa.isUnresolved() )
            throw new UnresolvedAddressException(); 
        InetAddress addr = isa.getAddress();
        if ( !(addr instanceof Inet4Address || addr instanceof Inet6Address) )
            throw new IllegalArgumentException( "Invalid address type" );
        return isa;
    }

    static void setOption( int fd, int opt, int val ) throws SocketException {
        try {
            setIntOption( fd, opt, val );
        } catch( Exception e ) {
            throwSocketException( e );
        }
    }

    static int getOption( int fd, int opt ) throws SocketException {
        try {
            return getIntOption( fd, opt );
        } catch( Exception e ) {
            throwSocketException( e );
            return 0; 
        }
    }
}
