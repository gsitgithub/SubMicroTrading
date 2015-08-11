/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.sim;

import java.io.IOException;
import java.util.List;

import com.rr.core.collections.IntHashMap;
import com.rr.core.collections.IntMap;
import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.utils.HexUtils;
import com.rr.core.utils.Utils;
import com.rr.md.us.cme.CMEFastFixSession;
import com.rr.md.us.cme.reader.CMEFastFixDecoder;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MDSnapshotFullRefreshImpl;

public class SimCMEFastFixSender implements SimClient {

    private static final Logger      _consoleLog    = LoggerFactory.console( SimCMEFastFixSender.class );

    private static class InstWrapper {
        public InstWrapper() {
        }

        int _nextBookSeqNum = 1;
    }
    
    private final Logger                _log;
    private final List<byte[]>          _templateRequests;
    private final CMEFastFixDecoder     _decoder        = new CMEFastFixDecoder( "SimFFReader", "data/cme/templates.xml", -1, false );
    private final CMEFastFixSession     _sender;
    private final IntMap<InstWrapper>   _instWrappers   = new IntHashMap<InstWrapper>( 100, 0.75f );  
    
    private volatile int                _sent = 0;
    private          int                _nextSeqNum = (int) (System.currentTimeMillis() / 10000); // 10sec potential clash
    private          int                _curIdx;
    private final    ReusableString     _binMsg    = new ReusableString();
    private final    ReusableString     _trace     = new ReusableString();

    private          int[]              _secIds    = new int[ 100 ];
    private          int[]              _seqNums   = new int[ 100 ];
    private          int                _numSecIds = 0;
    private final    EventRecycler      _recycler  = new EventRecycler();
    private          boolean            _logEvents;
    private          long               _ticks     = 0;

    public SimCMEFastFixSender( List<byte[]> templateRequests, boolean nanoTiming, CMEFastFixSession sender ) {
        super();

        _log = LoggerFactory.create( SimCMEFastFixSender.class );
        
        _templateRequests = templateRequests;
        _sender = sender;
        
        _decoder.setNanoStats( nanoTiming );
        
        _logEvents = sender.isLogEvents();
    }

    @Override
    public void dispatchEvents( int numMsgs, int batchSize, int delayMicros ) throws IOException {
        
        int waitIdx=0;
        while( ! _sender.isConnected() && ++waitIdx < 10 ) {
            Utils.delay( 100 );
        }

        Utils.delay( 100 );
        
        _ticks= 0;
        
        _consoleLog.info( "SimCMEFastFixSender about to start sending " + numMsgs );

        long startMS = System.currentTimeMillis();
        
        int errors = 0;
        
        for( int idx=0 ; idx < numMsgs ; ++idx ) {
            try {
                sendNext( idx );
            } catch( Exception e ) {
                _log.info( "Error sending msg idx=" + idx + " " + e.getMessage() );
                ++errors;
            }
            
            if ( delayMicros > 0 && (batchSize <= 1 || idx % batchSize == 0) ) {
                Utils.delayMicros( delayMicros );
            }
            
            if ( idx % 10000 == 0 ) {
                _log.info( "Sent msgs=" + idx + ", errors=" + errors );
            }
        }
        
        long durationSEC = Math.abs( System.currentTimeMillis() - startMS ) / 1000;
        
        _consoleLog.info( "SimCMEFastFixSender " + _sent + 
                          ", msgs=" + numMsgs + ", msgRate=" + (numMsgs/durationSEC) + 
                          ", ticks=" + _ticks + ", tickRate=" + (_ticks/durationSEC) );

        Utils.delay( 500 );
    }

    public void sendNext( int idx ) throws IOException {
        byte[] template = _templateRequests.get( idx % _templateRequests.size() );
        
        // 04:11:47.790 [info]  [3599652] [s#1] [t#83]  00 36 ED 24 01 C0 D3 01 5B 5A A4 23 5E 6C 66 4B 79 07 E4 09 4C 06 D3 81 AE 81 3F 43 05 B8 01 53 BD 02 3D 42 EB 08 4F DB 03 99 00 F1

        int templateId = skipToTemplate( template );
        
        if ( templateId == 0 ) return;
        
        HexUtils.hexStringToBytes( template, _curIdx, _binMsg );

        _decoder.setReceived( (Utils.nanoTime() >> 10) ); // div by 1024 ... approximation for micros
        
        Message msg = _decoder.decode( _binMsg.getBytes(), 0, _binMsg.length() );
        
        if ( msg == null ) return; 
                
        msg.setMsgSeqNum( _nextSeqNum++ );
        
        msg.setMessageHandler( _sender );

        _numSecIds = 0;
        
        int secId = 0;
        int bookSeqNum = 0;
        
        long now = Utils.nanoTime() >> 10;
        
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_MDINCREFRESH:
            MDIncRefreshImpl inc = (MDIncRefreshImpl) msg;
            inc.setSendingTime( now );
            MDEntryImpl e = (MDEntryImpl) inc.getMDEntries();
            while( e != null ) {
                secId = (int)e.getSecurityID();
                bookSeqNum = nextBookSeqNum( secId ); 
                e.setRepeatSeq( bookSeqNum );
                note( secId, bookSeqNum );
                e = e.getNext();
                ++_ticks;
            }
            break;
        case EventIds.ID_MDSNAPSHOTFULLREFRESH:
            MDSnapshotFullRefreshImpl snap = (MDSnapshotFullRefreshImpl) msg;
            secId = (int) snap.getSecurityID();
            bookSeqNum = nextBookSeqNum( secId ); 
            snap.setRptSeq( bookSeqNum );
            snap.setSendingTime( now );
            note( secId, bookSeqNum );
            break;
        }

        doSend( templateId, msg, now );
    }

    public boolean isLogEvents() {
        return _logEvents;
    }
    
    public void setLogEvents( boolean logEvents ) {
        _logEvents = logEvents;
    }

    protected void doSend( int templateId, Message msg, long now ) throws IOException {
        _sender.handleNow( msg, templateId, (byte)1 );

        ++_sent;
        
        for( int i=0 ; i < _numSecIds ; i++ ) {
            if ( _logEvents ) {
                _trace.reset(); // log bookId + "_" + tickId, nanoTS on ticks
                _trace.append( '[' ).append( templateId ).append( ']' );
                _trace.append( " bookId " ).append( _secIds[i] ).append( '_' ).append( _seqNums[i] ).append( " " ).append( now );
                _log.info( _trace );
            }
        }
        
        _recycler.recycle( msg );
    }

    private void note( int secId, int bookSeqNum ) {
        _secIds[ _numSecIds ] = secId;
        _seqNums[ _numSecIds ] = bookSeqNum; 
        ++_numSecIds;
    }

    private int nextBookSeqNum( int securityID ) {
        InstWrapper w = _instWrappers.get( securityID );
        
        if ( w == null ) {
            w = new InstWrapper();
            
            _instWrappers.put( securityID, w );
        }
        
        return w._nextBookSeqNum++;
    }

    private int skipToTemplate( byte[] template ) {
        int len = template.length;
        
        if ( len < 35 ) return 0;
        
        // 04:11:47.790 [info]  [3599652] [s#1] [t#83]  00 36 ED 24 01 C0 D3 01 5B 5A A4 23 5E 6C 66 4B 79 07 E4 09 4C 06 D3 81 AE 81 3F 43 05 B8 01 53 BD 02 3D 42 EB 08 4F DB 03 99 00 F1
        _curIdx = 22;
        
        int maxSafeIdx = len-1;
        
        while( _curIdx < maxSafeIdx ) {
            if ( template[_curIdx] == 't' && template[_curIdx+1] == '#' ) {
                break;
            }
            ++_curIdx;
        }

        if ( _curIdx == maxSafeIdx ) return 0;
        
        _curIdx += 2;
        
        int templateId = 0;
        
        while( template[_curIdx] != ']' && _curIdx < maxSafeIdx ) {
            templateId *= 10;
            templateId += (template[_curIdx++]-'0');
        }

        if ( _curIdx == maxSafeIdx ) return 0;
        
        _curIdx += 3;
        
        return templateId;
    }

    @Override
    public int getSent() {
        return _sent;
    }

    public void setSent( int sent ) {
        _sent = sent;
    }

    @Override
    public void reset() {
        _sent = 0;
    }
}
