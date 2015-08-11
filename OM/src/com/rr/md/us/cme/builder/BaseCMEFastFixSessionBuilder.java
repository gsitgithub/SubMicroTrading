/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.rr.core.codec.binary.fastfix.FastFixDecoder;
import com.rr.core.codec.binary.fastfix.NonBlockingFastFixSocketSession;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.MessageHandler;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.SessionDirection;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.md.channel.MarketDataChannel;
import com.rr.md.channel.MarketDataChannelBuilder;
import com.rr.md.fastfix.FastSocketConfig;
import com.rr.md.us.cme.CMEConfig;
import com.rr.md.us.cme.CMEConfig.Channel;
import com.rr.md.us.cme.CMEConnection;
import com.rr.md.us.cme.CMEConnection.Protocol;
import com.rr.md.us.cme.CMEConnections;
import com.rr.md.us.cme.CMENonBlockingFastFixSession;
import com.rr.md.us.cme.Feed;
import com.rr.md.us.cme.FeedType;
import com.rr.md.us.cme.reader.CMEFastFixDecoder;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.om.session.SessionManager;


/**
 * CMESessionBuilder - create sessions and load balance across multiplex receivers as well as md event handlers
 * 
 * @TODO allow slow and fast market multiplexers .. fast spin .... use fast only for incremental updates
 *
 * @author Richard Rose
 */
public abstract class BaseCMEFastFixSessionBuilder implements MarketDataChannelBuilder<Integer> {

    private static final Logger _log = LoggerFactory.create( BaseCMEFastFixSessionBuilder.class );
    
    protected final CMEConfig _cfg;

    protected final List<NonBlockingFastFixSocketSession>         _sessions;
    private   final Map<Integer, NonBlockingFastFixSocketSession> _portToSessionMap;
    
    /**
     * each channel can have a single consumer, this map ensures against attempt to register more than one consumer to the channel
     */
    protected final Map<Integer, MessageHandler>                  _channelToConsumer= new LinkedHashMap<Integer, MessageHandler>();


    private   final SocketConfig                     _baseSocketConfig;
    private   final SessionManager                   _sessMgr;

    private         boolean                          _addInstrumentSessions = true;
    private         boolean                          _addSnapshotSessions = true;
    private         boolean                          _addIncrementalSessions = true;
    
    private         boolean                          _disableNanoStats;
    private         boolean                          _trace;
    private         boolean                          _logEvents = true;
    private         boolean                          _enableEventPojoLogging;
    private         long                             _subChannelMask = -1;

    private         String                           _templateFile = "data/cme/templates.xml";
    private   final String                           _id;

    private   final String                           _nicA;
    private   final String                           _nicB;

    
    /**
     * 
     * @param cfg
     * @param sessMgr - session manager to register sessions with
     * @param multiplexReceivers -  list of receivers which give a timeslice to try and read from the MD session. 
     *                              Multiplex receivers are themselves shared (multipexed) across Control threads
     * @param baseSocketConfig
     * @param nics - comma delim  list of NICs .. at most 2 ... A, B feed
     */
    public BaseCMEFastFixSessionBuilder( String                             id,
                                         CMEConfig                          cfg,
                                         SessionManager                     sessMgr, 
                                         SocketConfig                       baseSocketConfig,
                                         String                             nics ) {
        
        _id                     = id;
        _cfg                    = cfg;
        _sessions               = new ArrayList<NonBlockingFastFixSocketSession>();
        _portToSessionMap       = new HashMap<Integer, NonBlockingFastFixSocketSession>();
        _baseSocketConfig       = baseSocketConfig;
        _sessMgr                = sessMgr;
        _nicA                   = parseNICS( nics, 0 );
        _nicB                   = parseNICS( nics, 1 );
    }

    private String parseNICS( String nics, int idx ) {
        if ( nics == null ) return null;
        
        String[] parts = nics.split( "," );
        
        if ( parts.length == 0 ) return null;
        
        if ( parts.length > 2 ) {
            throw new SMTRuntimeException( "CMEOnDemandSessionBuilder MD NICS list [" + nics + "] can have at most two entries not " + parts.length );
        }
        
        if ( idx >= parts.length ) return parts[0].trim();
        
        return parts[ idx ].trim();
    }

    public void setSubChannelMask( long subChannelMask ) {
        _subChannelMask = subChannelMask;
    }

    @SuppressWarnings( "boxing" )
    protected boolean ensureChannelsExist( MultiSessionThreadedReceiver multiplexReceiver, MessageHandler mdEventConsumer, Channel ch, int sessionIdx ) {
        
        final int channel = ch.getChannelId();
        
        MessageHandler existing = getChannelConsumer( channel );

        if ( existing == mdEventConsumer ) {
            return false;           // channels already exist and associated with consumer
        }
        
        if ( existing != null ) {
            throw new SMTRuntimeException( "Attempt to add duplicate sessions for channel " + ch + 
                                           ", channelId=" + channel + 
                                           " for "        + mdEventConsumer.getComponentId() + 
                                           ", but already registered against " + existing.getComponentId() + ", idx=" + sessionIdx );
        }
        
        String base = "CME_MD_Ch_" + channel; 
        
        CMEConnections conns = ch.getConns();
        
        if ( conns == null ) return false;
        
        boolean added = false;
        
        if ( _addIncrementalSessions) {
            added |= add( ch, ch.getConns().get( FeedType.Incremental, Feed.A ), multiplexReceiver, mdEventConsumer, sessionIdx, base + "_I_A", _nicA );
            added |= add( ch, ch.getConns().get( FeedType.Incremental, Feed.B ), multiplexReceiver, mdEventConsumer, sessionIdx, base + "_I_B", _nicB );
        }

        if ( _addInstrumentSessions ) {
            added |= add( ch, ch.getConns().get( FeedType.InstrumentReplay, Feed.A ), multiplexReceiver, mdEventConsumer, sessionIdx, base + "_R_A", _nicA );
            added |= add( ch, ch.getConns().get( FeedType.InstrumentReplay, Feed.B ), multiplexReceiver, mdEventConsumer, sessionIdx, base + "_R_B", _nicB );
        }
        
        if ( _addSnapshotSessions ) {
            added |= add( ch, ch.getConns().get( FeedType.Snapshot, Feed.A ), multiplexReceiver, mdEventConsumer, sessionIdx, base + "_S_A", _nicA );
            added |= add( ch, ch.getConns().get( FeedType.Snapshot, Feed.B ), multiplexReceiver, mdEventConsumer, sessionIdx, base + "_S_B", _nicB );
        }
        
        _channelToConsumer.put( channel, mdEventConsumer );

        _log.info( getComponentId() + 
                   " register sessions for channel " + ch +  
                   " with consumer "                 + mdEventConsumer.getComponentId() + 
                   ", idx="                          + sessionIdx +
                   ", addInstrumentSession="         + _addInstrumentSessions +
                   ", _addSnapshotSessions="         + _addSnapshotSessions );
        
        return added;
    }
    
    public boolean hasSessions( Integer channel ) {
        return _channelToConsumer.get( channel ) != null;
    }

    public MessageHandler getChannelConsumer( Integer channel ) {
        return _channelToConsumer.get( channel );
    }

    @SuppressWarnings( "boxing" )
    private boolean add( Channel ch, CMEConnection conn, MultiSessionThreadedReceiver multiplexReceiver, MessageHandler mh, int sessionIdx, String id, String nic ) {
        if ( conn == null || conn.getPort() == 0 ) {
            return false;
        }
        
        Integer portInt = new Integer( conn.getPort() );
        
        NonBlockingFastFixSocketSession existingSess = _portToSessionMap.get( portInt );
        
        if ( existingSess != null && conn.getProtocol() == Protocol.UDP && ((SocketConfig)existingSess.getConfig()).getPort() == conn.getPort() ) {
            join( existingSess, conn, ch.getChannelId() );
            
            _log.info( getComponentId() + 
                       " JOIN SESSION " + ch + ", id=" + id + " ADD multicast group " + conn.getIP() + 
                       " to session " + existingSess.getComponentId() +
                       " port " + conn.getPort() ); 
            
            return false;
        }
        
        _log.info( getComponentId() + " CREATE SESSION " + ch + ", id=" + id + " ADD multicast group " + conn.getIP() + " port " + conn.getPort() ); 

        NonBlockingFastFixSocketSession sess = create( conn, multiplexReceiver, mh, id, nic, ch.getChannelId() );
        
        _sessMgr.add( sess, false );
        
        _portToSessionMap.put( portInt,  sess );
        _sessions.add( sess );
        
        return true;
    }

    @SuppressWarnings( "boxing" )
    private NonBlockingFastFixSocketSession create( CMEConnection conn, MultiSessionThreadedReceiver multiplexReceiver, MessageHandler handler, String id, String nic, int channel ) {

        FastSocketConfig  config = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, conn.getPort() );
        
        config.setDirection( SessionDirection.Upstream );
        config.setDisableLoopback( _baseSocketConfig.isDisableLoopback() );
        config.setUseNIO( _baseSocketConfig.isUseNIO() );
        config.setNic( (nic == null) ? null : new ViewString(nic) );
        config.setHostname( _baseSocketConfig.getHostname() );
        
        ZString[] grps = { new ViewString( conn.getIP() ) };
        config.setMulticast( true );
        config.setMulticastGroups( grps );

        long subChannelMask = getSubChannelMask( conn );

        FastFixDecoder decoder = new CMEFastFixDecoder( id, _templateFile, subChannelMask, _trace );

        MessageRouter       inboundRouter     = new PassThruRouter( handler ); // currently each session only route to single handler 
        
        CMENonBlockingFastFixSession sess = new CMENonBlockingFastFixSession( id, 
                                                                              inboundRouter, 
                                                                              config, 
                                                                              multiplexReceiver,
                                                                              decoder );

        if ( _disableNanoStats ) {
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        } else {
            decoder.setNanoStats( true );
        }
        
        sess.setLogPojos( _enableEventPojoLogging );
        sess.setLogEvents( _logEvents );

        sess.addChannelKey( channel );
        
        return sess;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    public boolean isDisableNanoStats() {
        return _disableNanoStats;
    }
    
    public boolean isTrace() {
        return _trace;
    }

    public boolean isEnableEventPojoLogging() {
        return _enableEventPojoLogging;
    }

    public void setDisableNanoStats( boolean disableNanoStats ) {
        _disableNanoStats = disableNanoStats;
    }
    
    public void setTrace( boolean trace ) {
        _trace = trace;
    }

    public void setEnableEventPojoLogging( boolean enableEventPojoLogging ) {
        _enableEventPojoLogging = enableEventPojoLogging;
    }

    private long getSubChannelMask( CMEConnection conn ) {
        return _subChannelMask;                                 // @TODO allow subChannelMask by channel
    }

    @SuppressWarnings( { "unchecked", "boxing" } )
    private void join( NonBlockingFastFixSocketSession sess, CMEConnection conn, int channel ) {
        ((SocketConfig)sess.getConfig()).addMulticastGroup( conn.getIP() );
        ((MarketDataChannel<Integer>)sess).addChannelKey( channel );
    }
}
