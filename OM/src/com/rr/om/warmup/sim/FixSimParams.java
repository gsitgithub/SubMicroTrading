/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.sim;

import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.ThreadUtils;
import com.rr.mds.client.MDSConsumer;

public class FixSimParams {
    private static final Logger _log = LoggerFactory.console( FixSimParams.class );

    private final boolean _hasUpstream;
    private final boolean _hasHub;
    private final boolean _hasDownstream;
    private final String  _appName;

    private boolean _removePersistence = false;
    private boolean _debug             = false;
    private int     _numOrders         =  1;
    private String  _upSenderCompId;
    private String  _upTargetCompId;
    private int     _upPort;
    private int     _upLocalPort;
    private String  _upHost;
    private String  _upAdapter;

    private String  _downSenderCompId;
    private String  _downTargetCompId;
    private String  _downHost;
    private int     _downLocalPort;
    private int     _downPort;
    private String  _downAdapter;
    
    private String  _hubSenderCompId;
    private String  _hubTargetCompId;
    private String  _hubHost;
    private int     _hubPort;
    private String  _hubAdapter;
    
    private int     _mdsListenPort = MDSConsumer.DEFAULT_MDS_PORT;
    private String  _fileName;
    private int     _batchSize = 1;
    private int     _delayMicros   = 1000;

    private boolean _optimiseForThroughPut = false;
    private boolean _optimiseForLatency    = false;
    private boolean _allowClientParams     = false;
    private boolean _disableNanoStats      = false;
    private int     _warmupCount           = 1100;

    private int     _persistDatPageSize    = 1024 * 1024;
    private int     _persistIdxPreSize     = 1024 * 1024;
    private int     _persistDatPreSize     = 1024 * 1024;
    private String  _idPrefix;

    private boolean _disableEventLogging   = true;
    private boolean _hubEnabled            = false;

    private String  _cpuMasksFile          = ThreadUtils.DEFAULT_THREAD_CONFIG;

    private int     _warmupPortOffset      = 0;

    private boolean _logPojos              = false;

    private String _clientDataFile;

    public FixSimParams( String appName, boolean hasUpstream, boolean hasDownstream, String genIdPrerfix ) {

        _appName       = appName;
        _hasUpstream   = hasUpstream;
        _hasDownstream = hasDownstream;
        _idPrefix      = genIdPrerfix;
        
        _hasHub = ( hasUpstream && hasDownstream ) ;
    }
    
    public void procArgs( String[] args ) {

        for( int i=0 ; i < args.length ; i++ ) {
            String arg = args[i];
            
            if ( arg.charAt( 0 ) != '-' || arg.length() > 2 ) {
                exitBadArg( i, arg );
            }
            
            // culdrnEzZNWLBFMT asthpo ASTHPO vVxXy fbDi  
            
            switch( arg.charAt( 1 ) ) {
            case 'W':
                _warmupCount = getIntArg( args, ++i );
                continue;
            case 'F':
                _warmupPortOffset = getIntArg( args, ++i );
                continue;
            case 'B':
                _hubEnabled = true;
                continue;
            case 'L':
                _disableEventLogging = false;
                continue;
            case 'u':
                _optimiseForThroughPut = true;
                continue;
            case 'l':
                _optimiseForLatency = true;
                continue;
            case 'd':
                _debug = true;
                continue;
            case 'c':
                _cpuMasksFile = getStringArg( args, ++i );
                continue;
            case 'N':
                _disableNanoStats = true;
                continue;
            case 'r':
                _removePersistence = true;
                continue;
            case 'n':
                _numOrders          = getIntArg( args, ++i );

                // 1mill orders = 8mill events, between 1 and 2GB
                
                int estDataSize = 256 * _numOrders * 8;
                
                _persistDatPageSize = Math.min(4096 + Math.abs( estDataSize / 10), 10000000) ;
                _persistIdxPreSize  = 1024 * 1024 + (_numOrders*8*16);
                _persistDatPreSize  = 1024 * 1024 + estDataSize;
                
                continue;
            case 'E':
                _persistDatPageSize = getIntArg( args, ++i );
                continue;
            case 'z':
                _persistIdxPreSize  = getIntArg( args, ++i );
                continue;
            case 'Z':
                _persistDatPreSize  = getIntArg( args, ++i );
                continue;
            case 'M':
                _mdsListenPort  = getIntArg( args, ++i );
                continue;
            case 'T':
                _clientDataFile   = getStringArg( args, ++i );
                continue;
            }

            if ( _hasUpstream )  {
                switch( arg.charAt( 1 ) ) {
                case 'a':
                    _upAdapter          = getStringArg( args, ++i );
                    continue;
                case 's':
                    _upSenderCompId     = getStringArg( args, ++i );
                    continue;
                case 't':
                    _upTargetCompId     = getStringArg( args, ++i );
                    continue;
                case 'p':
                    _upPort             = getIntArg( args, ++i );
                    continue;
                case 'o':
                    _upLocalPort        = getIntArg( args, ++i );
                    continue;
                case 'h':
                    _upHost             = getStringArg( args, ++i );
                    continue;
                }
            }

            if ( _hasDownstream )  {
                switch( arg.charAt( 1 ) ) {
                case 'A':
                    _downAdapter        = getStringArg( args, ++i );
                    continue;
                case 'S':
                    _downSenderCompId   = getStringArg( args, ++i );
                    continue;
                case 'T':
                    _downTargetCompId   = getStringArg( args, ++i );
                    continue;
                case 'H':
                    _downHost           = getStringArg( args, ++i );
                    continue;
                case 'P':
                    _downPort           = getIntArg( args, ++i );
                    continue;
                case 'O':
                    _downLocalPort      = getIntArg( args, ++i );
                    continue;
                }
            }

            if ( _hasHub )  { 
                switch( arg.charAt( 1 ) ) {
                case 'y':
                    _hubAdapter          = getStringArg( args, ++i );
                    continue;
                case 'v':
                    _hubSenderCompId     = getStringArg( args, ++i );
                    continue;
                case 'V':
                    _hubTargetCompId     = getStringArg( args, ++i );
                    continue;
                case 'x':
                    _hubPort             = getIntArg( args, ++i );
                    continue;
                case 'X':
                    _hubHost             = getStringArg( args, ++i );
                    continue;
                }
            }

            if ( _allowClientParams ){
                switch( arg.charAt( 1 ) ) {
                case 'f':
                    _fileName           = getStringArg( args, ++i );
                    continue;
                case 'b':
                    _batchSize          = getIntArg( args, ++i );
                    continue;
                case 'D':
                    _delayMicros            = getIntArg( args, ++i );
                    continue;
                case 'i':
                    _idPrefix           = getStringArg( args, ++i );
                    continue;
                }
            }
            
            exitBadArg( i, arg );
        }
    }

    public void enableClientParams() {
        _allowClientParams = true;
    }

    public String getFileName() {
        return _fileName;
    }
    
    public int getBatchSize() {
        return _batchSize;
    }

    public int getDelayMicros() {
        return _delayMicros;
    }

    public boolean isRemovePersistence() {
        return _removePersistence;
    }

    public String getAppName() {
        return _appName;
    }

    public boolean isDebug() {
        return _debug;
    }

    public int getNumOrders() {
        return _numOrders;
    }

    public String getUpSenderCompId() {
        return _upSenderCompId;
    }

    public String getUpTargetCompId() {
        return _upTargetCompId;
    }

    public int getMDSListenPort() {
        return _mdsListenPort;
    }

    public int getUpPort() {
        return _upPort;
    }

    public String getUpHost() {
        return _upHost;
    }

    public String getDownSenderCompId() {
        return _downSenderCompId;
    }

    public String getDownTargetCompId() {
        return _downTargetCompId;
    }

    public String getDownHost() {
        return _downHost;
    }

    public int getDownPort() {
        return _downPort;
    }

    public int getPersistDatPageSize() {
        return _persistDatPageSize;
    }

    public int getPersistIdxPreSize() {
        return _persistIdxPreSize;
    }

    public int getPersistDatPreSize() {
        return _persistDatPreSize;
    }

    public String getCpuMasksFile() {
        return _cpuMasksFile;
    }

    public String getIdPrefix() {
        return _idPrefix;
    }

    public boolean isOptimiseForThroughPut() {
        return _optimiseForThroughPut;
    }

    public boolean isOptimiseForLatency() {
        return _optimiseForLatency;
    }
    
    public boolean isDisableNanoStats() {
        return _disableNanoStats;
    }

    public int getWarmupCount() {
        return _warmupCount;
    }

    public boolean isHubEnabled() {
        return _hubEnabled;
    }
    
    public String getUpAdapter() {
        return _upAdapter;
    }

    public String getDownAdapter() {
        return _downAdapter;
    }

    public int getMdsListenPort() {
        return _mdsListenPort;
    }

    public String getHubSenderCompId() {
        return _hubSenderCompId;
    }

    public String getHubTargetCompId() {
        return _hubTargetCompId;
    }

    public String getHubHost() {
        return _hubHost;
    }

    public int getHubPort() {
        return _hubPort;
    }

    public String getHubAdapter() {
        return _hubAdapter;
    }

    public int getUpLocalPort() {
        return _upLocalPort;
    }

    public int getDownLocalPort() {
        return _downLocalPort;
    }
    
    public int getWarmupPortOffset() {
        return _warmupPortOffset;
    }

    public void setWarmupPortOffset( int warmupPortOffset ) {
        _warmupPortOffset = warmupPortOffset;
    }

    public void setUpLocalPort( int upLocalPort ) {
        _upLocalPort = upLocalPort;
    }

    public void setDownLocalPort( int downLocalPort ) {
        _downLocalPort = downLocalPort;
    }

    public void setHubSenderCompId( String hubSenderCompId ) {
        _hubSenderCompId = hubSenderCompId;
    }

    public void setHubTargetCompId( String hubTargetCompId ) {
        _hubTargetCompId = hubTargetCompId;
    }

    public void setHubHost( String hubHost ) {
        _hubHost = hubHost;
    }

    public void setHubPort( int hubPort ) {
        _hubPort = hubPort;
    }

    public void setHubAdapter( String hubAdapter ) {
        _hubAdapter = hubAdapter;
    }

    public void setHubEnabled( boolean hubEnabled ) {
        _hubEnabled = hubEnabled;
    }

    public void setWarmupCount( int warmupCount ) {
        _warmupCount = warmupCount;
    }

    public void setDisableNanoStats( boolean disableNanoStats ) {
        _disableNanoStats = disableNanoStats;
    }

    public void setRemovePersistence( boolean removePersistence ) {
        _removePersistence = removePersistence;
    }

    public void setDebug( boolean debug ) {
        _debug = debug;
    }

    public void setNumOrders( int numOrders ) {
        _numOrders = numOrders;
    }

    public void setUpSenderCompId( String upSenderCompId ) {
        _upSenderCompId = upSenderCompId;
    }

    public void setUpTargetCompId( String upTargetCompId ) {
        _upTargetCompId = upTargetCompId;
    }

    public void setUpPort( int upPort ) {
        _upPort = upPort;
    }

    public void setUpHost( String upHost ) {
        _upHost = upHost;
    }

    public void setDownSenderCompId( String downSenderCompId ) {
        _downSenderCompId = downSenderCompId;
    }

    public void setDownTargetCompId( String downTargetCompId ) {
        _downTargetCompId = downTargetCompId;
    }

    public void setDownHost( String downHost ) {
        _downHost = downHost;
    }

    public void setDownPort( int downPort ) {
        _downPort = downPort;
    }

    public void setBatchSize( int batchSize ) {
        _batchSize = batchSize;
    }

    public void setDelayMicros( int delayMicros ) {
        _delayMicros = delayMicros;
    }

    public void setPersistDatPageSize( int persistDatPageSize ) {
        _persistDatPageSize = persistDatPageSize;
    }

    public void setPersistIdxPreSize( int persistIdxPreSize ) {
        _persistIdxPreSize = persistIdxPreSize;
    }

    public void setPersistDatPreSize( int persistDatPreSize ) {
        _persistDatPreSize = persistDatPreSize;
    }

    public void setIdPrefix( String idPrefix ) {
        _idPrefix = idPrefix;
    }

    public void setOptimiseForThroughPut( boolean optimiseForThroughPut ) {
        _optimiseForThroughPut = optimiseForThroughPut;
    }

    public void setOptimiseForLatency( boolean optimiseForLatency ) {
        _optimiseForLatency = optimiseForLatency;
    }

    public void setFileName( String fileName ) {
        _fileName = fileName;
    }

    private String getStringArg( String[] args, int i ) {
        
        if ( i >= args.length ) {
            exitBadArg( i, "<missing arg>" );
        }
        
        return args[i];
    }

    private int getIntArg( String[] args, int i ) {
        int val = 0;
        
        if ( i >= args.length ) {
            exitBadArg( i, "<missing arg>" );
        }
        
        try {
            val = Integer.parseInt( args[i] );
        } catch( NumberFormatException e ) {
            exitBadArg( i, "non numeric arg : " + args[i] );
        }
        
        return val;
    }

    private void exitBadArg( int idx, String arg ) {
        _log.info( "Bad argument at idx=" + idx + ", arg=" + arg );
        
        _log.info( "Format : " + _appName );
        _log.info( "                -d                              use debug console" );
        _log.info( "                -B                              enable Hub session" );
        _log.info( "                -W count                        orders in warmup, 0 to disable warmup" );
        _log.info( "                -F offset                       WARMUP port offset, required for uniq port when mult progs on same server" );
        _log.info( "                -r                              remove persistence on startup" );
        _log.info( "                -n numOrders                    number of orders (calcs size for persistece files)" );
        _log.info( "                -N                              disable nano log stats" );
        _log.info( "                -u                              spin locks and dispatcher queues, optimise for throughput" );
        _log.info( "                -l                              spin locks, optimise for lowest latency" );
        _log.info( "                -E                              persist data page size (bytes)" );
        _log.info( "                -z                              persist index file presize (bytes)" );
        _log.info( "                -Z                              perist index idx presize (bytes)" );
        _log.info( "                -L                              enable event logging (disabled by default)" );
        _log.info( "                -M mdsListeningPort             port to listen to MDS connection " + def( _mdsListenPort ));
        _log.info( "                -C clientDataFile               client data file for simSender" );
        _log.info( "                -E persistDataPageSize          persist data page  size " + def( _persistDatPageSize ));
        _log.info( "                -z persistIdxPreSize            persist index file pre size " + def( _persistIdxPreSize ));
        _log.info( "                -Z persisteDatPreSize           persist data file pre size " + def( _persistDatPreSize ));

        if ( _hasUpstream ) {
            _log.info( "                -a upAdapter                    socket adapter to bind to for upstream" );
            _log.info( "                -s upSenderCompId               senderCompId to use sending upstream "   + def( _upSenderCompId ) );
            _log.info( "                -t upTargetCompId               targetCompId to use sending upstream "   + def( _upTargetCompId ));
            _log.info( "                -h upHost                       hostname for upstream "                  + def( _upHost ));
            _log.info( "                -p upPort                       port for upstream "                      + def( _upPort ));
            _log.info( "                -o upPort                       local port for upstream "                + def( _upLocalPort ));
        }
        
        if ( _hasDownstream ) {
            _log.info( "                -A upAdapter                    socket adapter to bind to for downstream" );
            _log.info( "                -S downSenderCompId             senderCompId to use sending downstream "  + def( _downSenderCompId ));
            _log.info( "                -T downTargetCompId             targetCompId to use sending downstream "  + def( _downTargetCompId ));
            _log.info( "                -H downHost                     hostname for downstream "                 + def( _downHost ));
            _log.info( "                -P downPort                     port for downstream "                     + def( _downPort ));
            _log.info( "                -O downLocalPort                local port for downstream "               + def( _downLocalPort ));
        }

        if ( _hasHub ) {
            _log.info( "                -y hubAdapter                   socket adapter to bind to for Hub" );
            _log.info( "                -v hubSenderCompId              senderCompId to use sending Hub "  + def( _hubSenderCompId ));
            _log.info( "                -V hubTargetCompId              targetCompId to use sending Hub "  + def( _hubTargetCompId ));
            _log.info( "                -X hubHost                      hostname for Hub "                 + def( _hubHost ));
            _log.info( "                -x hubPort                      port for Hub "                     + def( _hubPort ));
        }
        
        if ( _allowClientParams ) {
            _log.info( "                -f clientRequestFile            data file with template order requessts "   + def( _fileName ));
            _log.info( "                -b batchSize                    number of requests per batch "              + def( _batchSize ));
            _log.info( "                -D delayMicros                  delay in microseconds between each batch "  + def( _delayMicros  ));
            _log.info( "                -i                              id generator prefix (use if running multiple instances)" );
        }
        
        System.exit( -1 );
    }

    private String def( int val ) {
        return " (default : " + val + ")";
    }

    private String def( String val ) {
        return " (default : " + val + ")";
    }

    public boolean isDisableEventLogging() {
        return _disableEventLogging;
    }
    
    public void setDisableEventLogging( boolean isDisabled ) {
        _disableEventLogging = isDisabled;
    }

    public boolean isLogPojoEvents() {
        return _logPojos;
    }
    
    public void setLogPojoEvents( boolean logPojos ) {
        _logPojos = logPojos;
    }

    public String getClientDataFile() {
        return _clientDataFile;
    }
    
    public void setClientDataFile( String templateFile ) {
        _clientDataFile = templateFile;
    }
}
