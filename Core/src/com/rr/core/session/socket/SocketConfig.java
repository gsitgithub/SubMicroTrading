/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.session.socket;

import java.util.Arrays;

import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.persister.DummyPersister;
import com.rr.core.persister.Persister;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.session.TradingSessionConfig;
import com.rr.core.utils.SMTRuntimeException;

public class SocketConfig extends TradingSessionConfig {

    private boolean       _server;
    private ZString       _hostname;
    private int           _port;

    private ZString       _secHostname;
    private int           _secPort;

    // adapter if specified is the name of the NIC to attach to, if a specific local port is specified then that will be used  
    // ofcause localPort is only relevant for client sessions, server sessions bind to the network adapter using the listening _port
    
    private ZString       _nic;
    private int           _localPort             = 0;                       // ephemeral port, let OS allocate

    private int           _soLingerSECS          = -1;
    private int           _maxWaitBeforeReconMS  = 60000;
    private boolean       _useNIO                = true;
    private int           _maxConnectAttempts    = 10;
    private boolean       _keepAlive             = false;
    private boolean       _tcpNoDelay            = true;
    private long          _nioSelectorWait       = 0;
    private int           _soDelayMS             = 0;
    private Persister     _inPersister           = new DummyPersister();
    private Persister     _outPersister          = new DummyPersister();
    private long          _logDelayedWriteNanos  = 2000;    // log any delays longer than 2 micros 
    private boolean       _disableLoopback       = true;
    private int           _qos                   = 0x10;
    private int           _ttl                   = 5;
    private boolean       _multicast             = false;
    private ZString[]     _multicastGroups       = new ZString[0];

    public SocketConfig() {
        super();
    }

    public SocketConfig( String id ) {
        super( id );
    }

    public SocketConfig( Class<? extends MessageRecycler> recycler,
                         boolean isServer, 
                         ZString host, 
                         ZString nic, 
                         int     port ) {
        
        super( recycler );
        
        _server               = isServer;
        _hostname               = host;
        _nic                    = nic;
        _port                   = port;
    }

    public SocketConfig( Class<? extends MessageRecycler> recycler ) {
        super( recycler );
    }

    @Override
    public String info() {
        
        String info = super.info() + 
                ", isServer="        + _server          +
                ", isMulticastCast=" + _multicast     +
                ", hostName="        + _hostname        +
                ", port="            + _port            +
                ", secHostName="     + _secHostname     +
                ", secPort="         + _secPort         +
                ", localPort="       + _localPort       +
                ", nic="             + _nic             +
                ", nio="             + _useNIO          +
                ", tcpNoDelay="      + _tcpNoDelay      +
                ", soLingerSecs="    + _soLingerSECS    +
                ", soDelayMS="       + _soDelayMS       +
                ", disLoopback="     + _disableLoopback +
                ", keepAlive="       + _keepAlive;

        if ( _multicast ) {
            info += ", grps[";
                   
            int i=0;
            
            for( ZString addr : _multicastGroups ) {
                if ( i++ > 0 ) {
                    info += ", ";
                }
                info += addr.toString();
            }
            
            info += "]";
        }
        
        return info;
    }
    
    public boolean isServer() {
        return _server;
    }

    public boolean isNIO() {
        return _useNIO;
    }

    public int getMaxWaitBeforeConnectRetryMS() {
        return _maxWaitBeforeReconMS;
    }

    public ZString getHostname() {
        return _hostname;
    }
    
    public ZString getNic() {
        return _nic;
    }

    public int getPort() {
        return _port;
    }

    public int getMaxConnectAttempts() {
        return _maxConnectAttempts;
    }

    public boolean getTcpNoDelay() {
        return _tcpNoDelay;
    }

    public boolean getKeepAlive() {
        return _keepAlive;
    }

    public long nioSelectorWaitPeriod() {
        return _nioSelectorWait;
    }

    public void setDisableLoopback( boolean disableLoopback ) {
        _disableLoopback = disableLoopback;
    }

    /**
     * @return -1 to disable or the soLinger period in SECONDS
     */
    public int getSoLinger() {
        return _soLingerSECS;
    }

    public int getSoLingerSECS() {
        return _soLingerSECS;
    }

    public void setSoLingerSECS( int soLingerSECS ) {
        _soLingerSECS = soLingerSECS;
    }

    public int getMaxWaitBeforeReconMS() {
        return _maxWaitBeforeReconMS;
    }

    public void setMaxWaitBeforeReconMS( int maxWaitBeforeReconMS ) {
        _maxWaitBeforeReconMS = maxWaitBeforeReconMS;
    }

    public boolean isUseNIO() {
        return _useNIO;
    }

    public void setUseNIO( boolean useNIO ) {
        _useNIO = useNIO;
    }

    public long getNioSelectorWait() {
        return _nioSelectorWait;
    }

    public Persister getInPersister() {
        return _inPersister;
    }

    public Persister getOutPersister() {
        return _outPersister;
    }

    public void setHostname( ZString hostname ) {
        _hostname = hostname;
    }

    public void setInPersister( Persister inPersister ) {
        _inPersister = inPersister;
    }

    public void setOutPersister( Persister outPersister ) {
        _outPersister = outPersister;
    }

    public void setNioSelectorWait( long nioSelectorWait ) {
        _nioSelectorWait = nioSelectorWait;
    }

    public void setServer( boolean isServer ) {
        _server = isServer;
    }

    public void setPort( int port ) {
        _port = port;
    }

    public void setMaxConnectAttempts( int maxConnectAttempts ) {
        _maxConnectAttempts = maxConnectAttempts;
    }

    public void setKeepAlive( boolean keepAlive ) {
        _keepAlive = keepAlive;
    }

    public void setTcpNoDelay( boolean tcpNoDelay ) {
        _tcpNoDelay = tcpNoDelay;
    }

    public void setSoDelayMS( int delay ) {
        _soDelayMS = delay;
    }
    
    public int getSoDelayMS() {
        return _soDelayMS;
    }

    public Persister getInboundPersister() {
        return _inPersister;
    }

    public Persister getOutboundPersister() {
        return _outPersister;
    }
    
    public void setInboundPersister( Persister persister ) {
        _inPersister = persister;
    }

    public void setOutboundPersister( Persister persister ) {
        _outPersister = persister;
    }

    public int getLocalPort() {
        return _localPort;
    }

    public void setLocalPort( int localPort ) {
        _localPort = localPort;
    }

    public void setNic( ZString nic ) {
        _nic = nic;
    }
    
    @Override
    public void validate() throws SMTRuntimeException {
        super.validate();
        if ( _port == 0 ) throw new SMTRuntimeException( "SocketConfig missing port" );
        if ( _server == false && _hostname == null ) throw new SMTRuntimeException( "SocketConfig client session missing host" );
    }

    public long getLogDelayedWriteNanos() {
        return          _logDelayedWriteNanos;
    }

    public boolean isDisableLoopback() {
        return _disableLoopback;
    }

    public int getQOS() {
        return _qos;
    }

    public void setQOS( int qos ) {
        _qos = qos;
    }

    public int getTTL() {
        return _ttl;
    }

    public void setTTL( int ttl ) {
        _ttl = ttl;
    }

    public boolean isMulticast() {
        return _multicast;
    }

    public void setMulticast( boolean isMulticast ) {
        _multicast = isMulticast;
    }

    public ZString[] getMulticastGroups() {
        return _multicastGroups;
    }
    
    /**
     * set the multicast groups
     * 
     * the first one is the only one that can be written too
     * 
     * @param grps
     */
    public void setMulticastGroups( ZString[] grps ) {
        _multicastGroups = grps;
    }

    public ZString getSecHostname() {
        return _secHostname;
    }

    public void setSecHostname( ZString secHostname ) {
        _secHostname = secHostname;
    }

    public int getSecPort() {
        return _secPort;
    }

    public void setSecPort( int secPort ) {
        _secPort = secPort;
    }

    public void addMulticastGroup( ZString ip ) {
        int len = _multicastGroups.length;
        
        _multicastGroups = Arrays.copyOf( _multicastGroups, len+1 );
        
        _multicastGroups[ len] = new ViewString( ip );
    }
}
