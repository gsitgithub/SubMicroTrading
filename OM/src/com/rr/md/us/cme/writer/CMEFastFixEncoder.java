/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.writer;

import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.FastFixEncoder;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.Dictionaries;
import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntMandWriterCopy;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.rr.md.fastfix.XMLFastFixTemplateLoader;
import com.rr.md.fastfix.meta.MetaTemplates;
import com.rr.md.fastfix.template.FastFixTemplateClassRegister;
import com.rr.md.fastfix.template.TemplateClassRegister;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.interfaces.MDIncRefresh;


public final class CMEFastFixEncoder implements FastFixEncoder {

    private static final MetaTemplates meta = new MetaTemplates();

    private static final int MD_INC_REFRESH_81 = 81;
    private static final int MD_INC_REFRESH_83 = 83;
    private static final int MD_INC_REFRESH_84 = 84;
    private static final int MD_INC_REFRESH_103 = 103;
    private static final int MD_INC_REFRESH_109 = 109;

    private static boolean _init = false;
    
    private final Logger _log = LoggerFactory.create( CMEFastFixEncoder.class );

    private final Dictionaries                  _dictionaries = new Dictionaries();
    
    private TimeZoneCalculator          _tzCalculator   = new TimeZoneCalculator();
    private final byte[]                _today          = new byte[ TimeZoneCalculator.DATE_STR_LEN ];

    private final byte[]                _bufOut         = new byte[2048];

    private final FastFixBuilder        _binEncodeBuilder   = new FastFixBuilder(_bufOut, 0 ); 
    private final PresenceMapWriter     _pMap               = new PresenceMapWriter();
    private final ReusableString        _destFixMsg         = new ReusableString();
    
    private final UIntMandWriterCopy    _templateIdWriter = new UIntMandWriterCopy( "TemplateId", 0, 0 );

    private final String                _id;

    private       int                   _lastTemplateId;
    private       byte                  _lastSububChannel;

    // HAND CODED READERS
    private final MDIncRefresh_81_Writer  _mdIncRefresh_T81;
    private final MDIncRefresh_83_Writer  _mdIncRefresh_T83;
    private final MDIncRefresh_84_Writer  _mdIncRefresh_T84;
    private final MDIncRefresh_103_Writer _mdIncRefresh_T103;
    private final MDIncRefresh_109_Writer _mdIncRefresh_T109;


    // stats
    private int     _dups = 0;
    private int     _skips = 0;
    private int     _packets = 0;
    private int     _subChannelSkips = 0;
    private int     _handPackets81;
    private int     _handPackets83;
    private int     _handPackets84;
    private int     _handPackets103;
    private int     _handPackets109;

    private boolean _debug;

    private int     _lastSeqNum;


    public CMEFastFixEncoder( String id, String templateFile, boolean debug ) {
        
        _id = id;
        _tzCalculator.getDateUTC( _today );
        _debug = debug;

        initClass( templateFile );
        
        ComponentFactory cf81 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_81" );
        ComponentFactory cf83 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_83" );
        ComponentFactory cf84 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_84" );
        ComponentFactory cf103 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_103" );
        ComponentFactory cf109 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_109" );

        _mdIncRefresh_T81 = new MDIncRefresh_81_Writer( cf81, "MDIncRefresh_81", 81 );
        _mdIncRefresh_T83 = new MDIncRefresh_83_Writer( cf83, "MDIncRefresh_83", 83 );
        _mdIncRefresh_T84 = new MDIncRefresh_84_Writer( cf84, "MDIncRefresh_84", 84 );
        _mdIncRefresh_T103 = new MDIncRefresh_103_Writer( cf103, "MDIncRefresh_103", 103 );
        _mdIncRefresh_T109 = new MDIncRefresh_109_Writer( cf109, "MDIncRefresh_109", 109 );
        
        ShutdownManager.instance().register( new Callback(){
            @Override
            public void shuttingDown() {
                logStats();
            }} );
    }

    @Override
    public void logStats() {
        long totHand = _handPackets81 + _handPackets83 + _handPackets84 + _handPackets103 + _handPackets109; 

        _log.info( "CMDFastFixWriter id=" + _id + 
                  ", #81=" + _handPackets81 + 
                  ", #83=" + _handPackets83 + 
                  ", #84=" + _handPackets84 + 
                  ", #103=" + _handPackets103 + 
                  ", #109=" + _handPackets109 + 
                  ", #tot=" + totHand +
                  ", packets=" + _packets + 
                  ", skips=" + _skips + 
                  ", dups=" + _dups + 
                  ", subChannelSkips=" + _subChannelSkips );
    }

    private synchronized static void initClass( String templateFile ) {
        if ( templateFile != null ) {
            if ( _init == false ) {
                TemplateClassRegister reg = new FastFixTemplateClassRegister();
                XMLFastFixTemplateLoader l = new XMLFastFixTemplateLoader( templateFile );
                l.load( reg, meta );
                _init = true;
            }
        }
    }

    @Override
    public void encode( Message msg ) {
        if ( msg instanceof MDIncRefresh ) {
            encode( msg, 81, (byte) 0 ); 
        } else {
            throw new RuntimeEncodingException( "Unsupported message type " + msg.getClass().getName() );
        }
    }
    
    public void encode( final Message msg, final int templateId, final byte subChannel ) {
        
        _lastTemplateId = templateId;
        _lastSububChannel = subChannel;
        
        // 4 byte sequence , 1 byte channel
        
        _binEncodeBuilder.start();

        _lastSeqNum = msg.getMsgSeqNum();
        _binEncodeBuilder.encodeSeqNum( _lastSeqNum );
        _binEncodeBuilder.encodeChannel( subChannel );
        
        _pMap.set( _binEncodeBuilder, 5, 1 );
        
        _templateIdWriter.reset();
        
        _templateIdWriter.write( _binEncodeBuilder, _pMap, templateId ); 

        // HAND TEMPLATES - ENSURE SWITCH STATEMENT HAS FILLERS
        switch( templateId ) {
        case MD_INC_REFRESH_81:
            _mdIncRefresh_T81.reset();
            ++_handPackets81;
            _mdIncRefresh_T81.write( _binEncodeBuilder, _pMap, (MDIncRefreshImpl) msg );
            break;
        case MD_INC_REFRESH_83:
            _mdIncRefresh_T83.reset();
            ++_handPackets83;
            _mdIncRefresh_T83.write( _binEncodeBuilder, _pMap, (MDIncRefreshImpl) msg );
            break;
        case MD_INC_REFRESH_84:
            _mdIncRefresh_T84.reset();
            ++_handPackets84;
            _mdIncRefresh_T84.write( _binEncodeBuilder, _pMap, (MDIncRefreshImpl) msg );
            break;
        case MD_INC_REFRESH_103:
            _mdIncRefresh_T103.reset();
            ++_handPackets103;
            _mdIncRefresh_T103.write( _binEncodeBuilder, _pMap, (MDIncRefreshImpl) msg );
            break;
        case MD_INC_REFRESH_109:
            _mdIncRefresh_T109.reset();
            ++_handPackets109;
            _mdIncRefresh_T109.write( _binEncodeBuilder, _pMap, (MDIncRefreshImpl) msg );
            break;
        case 82:                      case 85:  case 86: case 87: case 88: case 89:
        case 90:  case 91:  case 92:  case 93:  case 94: case 95: case 96: case 97: case 98: case 99: case 100: case 101: case 102:
        case 104: case 105: case 106: case 107: case 108: 
        default:
            throw new RuntimeEncodingException( "Unsupported templateId " + templateId );
        }
        
        _pMap.end();
        _binEncodeBuilder.end();
    }

    @Override
    public void logLastMsg() {
        if ( _debug ) {
            _destFixMsg.reset();
            _destFixMsg.append( "OUT [" ).append( _lastSeqNum ).append( "] [s#" ).append( (int)_lastSububChannel ).append( "] [t#" ).append( _lastTemplateId ).append( "] ");
            _destFixMsg.appendHEX( _bufOut, 0, _binEncodeBuilder.getLength() );
            
            _log.info( _destFixMsg );
        }
    }
    
    @Override
    public void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        _tzCalculator= calc;
    }

    @Override public void setNanoStats( boolean nanoTiming )                            { /* nothing */ }

    @Override
    public int getOffset() {
        return _binEncodeBuilder.getOffset();
    }

    @Override
    public int getLength() {
        return _binEncodeBuilder.getLength();
    }

    @Override
    public Message unableToSend( Message msg, ZString errMsg ) {
        return null;
    }

    @Override
    public byte[] getBytes() {
        return _binEncodeBuilder.getBuffer();
    }

    @Override
    public void addStats( ReusableString outBuf, Message msg, long time ) {
        // nothing
    }

    @Override
    public boolean isDebug() {
        return _debug;
    }

    @Override
    public void setDebug( boolean debug ) {
        _debug = debug;
    }
}
