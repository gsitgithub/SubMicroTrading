/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.builder;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.rr.core.codec.binary.fastfix.NonBlockingFastFixSocketSession;
import com.rr.core.component.SMTInitialisableComponent;
import com.rr.core.component.SMTStartContext;
import com.rr.core.model.MessageHandler;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.md.us.cme.CMEConfig;
import com.rr.md.us.cme.CMEConfig.Channel;
import com.rr.om.session.SessionManager;


/**
 * CMESessionBuilder - create sessions and load balance across multiplex receivers as well as md event handlers
 * 
 * @TODO allow slow and fast market multiplexers .. fast spin .... use fast only for incremental updates
 *
 * @author Richard Rose
 */
public class CMERoundRobinSessionBuilder extends BaseCMEFastFixSessionBuilder implements SMTInitialisableComponent {

    private final MultiSessionThreadedReceiver[]    _multiplexReceivers;
    private final MessageHandler[]                  _handlers;
    private final int[]                             _channels;
    private       int                               _next = 0;
    
    /**
     * 
     * @param cfg
     * @param channelList
     * @param sessMgr - session manager to register sessions with
     * @param handlers - consumers of the market data events
     * @param multiplexReceivers -  list of receivers which give a timeslice to try and read from the MD session. 
     *                              Multiplex receivers are themselves shared (multipexed) across Control threads
     * @param baseSocketConfig
     * @param disableNanoStats
     * @param enableEventPojoLogging
     * @param debug
     */
    public CMERoundRobinSessionBuilder( String                             id,
                                        CMEConfig                          cfg,
                                        String                             channelList, 
                                        SessionManager                     sessMgr, 
                                        MessageHandler[]                   handlers,
                                        MultiSessionThreadedReceiver[]     multiplexReceivers, 
                                        SocketConfig                       baseSocketConfig,
                                        String                             nics ) {

        super( id, cfg, sessMgr, baseSocketConfig, nics );
        
        _handlers               = handlers;
        _multiplexReceivers     = multiplexReceivers;
        
        _channels               = channelsParse( channelList );
    }

    public synchronized List<NonBlockingFastFixSocketSession> create() {
        if ( _handlers.length <= 0 ) {
            throw new SMTRuntimeException( "CMESessionBuilder requires at least one handler for market data sessions" );
        }
        
        for( int channel : _channels ) {

            @SuppressWarnings( "boxing" )
            Channel ch = _cfg.get( channel );
            
            if ( ch == null )  {
                throw new SMTRuntimeException( "CME channel is not recognised " + channel ); // shouldnt be possible
            }
        
            MultiSessionThreadedReceiver receiver = _multiplexReceivers[ _next % _multiplexReceivers.length ];
            MessageHandler mh = _handlers[ _next % _handlers.length ];

            if ( ensureChannelsExist( receiver, mh, ch, _next ) ) {
                ++_next;
            }
        }
        
        return _sessions;
    }

    @Override
    public synchronized void register( Integer channelKey, String pipeLineId, MessageHandler consumer ) {

        Channel ch = _cfg.get( channelKey );
        
        if ( ch == null )  {
            throw new SMTRuntimeException( "CME channel is not recognised " + channelKey ); 
        }
    
        MultiSessionThreadedReceiver receiver = _multiplexReceivers[ _next % _multiplexReceivers.length ];

        if ( ensureChannelsExist( receiver, consumer, ch, _next ) ) {
            ++_next;
        }
    }

    private int[] channelsParse( String channelList ) {
        
        if ( channelList.trim().equalsIgnoreCase( "ALL" ) ) {
            Iterator<Channel> it = _cfg.getChannelIterator();
            
            Set<Integer> channelSet = new HashSet<Integer>();
            
            while( it.hasNext() ) {
                Channel ch = it.next();
                channelSet.add( ch.getChannelId() );
            }
            
            int[] channels = new int[ channelSet.size() ];
            
            int next = 0;
            
            for( Integer ch : channelSet ) {
                channels[ next++ ] = ch.intValue();
            }
            
            return channels;
        } 
        
        String[] channelZ = channelList.split( "," );

        int[] channels = new int[ channelZ.length ];
        
        int nxt=0;
        
        for( String channel : channelZ ) {
            channels[ nxt++ ] = Integer.parseInt( channel.trim() );
        }
        
        return channels;
    }

    @Override
    public void init( SMTStartContext ctx ) {
        // nothing
    }

    @Override
    public void prepare() {
        create();
    }
}
