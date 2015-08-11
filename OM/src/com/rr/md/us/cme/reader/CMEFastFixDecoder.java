/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.reader;

import java.util.Iterator;

import com.rr.core.codec.FixField;
import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.FastFixDecoder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.common.Dictionaries;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntMandReaderCopy;
import com.rr.core.collections.IntHashMap;
import com.rr.core.collections.IntMap;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.InstrumentLocator;
import com.rr.core.model.Message;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.rr.md.fastfix.XMLFastFixTemplateLoader;
import com.rr.md.fastfix.meta.MetaTemplate;
import com.rr.md.fastfix.meta.MetaTemplates;
import com.rr.md.fastfix.reader.FastFixToFixReader;
import com.rr.md.fastfix.template.FastFixTemplateClassRegister;
import com.rr.md.fastfix.template.TemplateClassRegister;
import com.rr.model.generated.fix.codec.MD44Decoder;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MDSnapshotFullRefreshImpl;


public final class CMEFastFixDecoder implements FastFixDecoder {

    private static final MetaTemplates meta = new MetaTemplates();
    
    private static final ErrorCode ERR_FF1 = new ErrorCode( "CMEFF01", "CME Decoding Error" );

    private static final int MD_INC_REFRESH_81 = 81;
    private static final int MD_INC_REFRESH_83 = 83;
    private static final int MD_INC_REFRESH_84 = 84;
    private static final int MD_INC_REFRESH_103 = 103;
    private static final int MD_INC_REFRESH_109 = 109;

    private static boolean _init = false;
    
    private final Logger _log = LoggerFactory.create( CMEFastFixDecoder.class );

    private final Dictionaries                  _dictionaries = new Dictionaries();
    private final IntMap<FastFixToFixReader>    _activeReaders   = new IntHashMap<FastFixToFixReader>( 200, 0.75f );
    private final IntMap<FastFixToFixReader>    _preparedReaders = new IntHashMap<FastFixToFixReader>( 200, 0.75f );

    private       boolean               _debug;
    
    private TimeZoneCalculator          _tzCalculator   = new TimeZoneCalculator();
    private final byte[]                _today          = new byte[ TimeZoneCalculator.DATE_STR_LEN ];

    private final MD44Decoder           _softDecoder    = new MD44Decoder();

    private final FastFixDecodeBuilder  _binDecodeBuilder   = new FastFixDecodeBuilder();
    private final PresenceMapReader     _pMap               = new PresenceMapReader();
    private final ReusableString        _destFixMsg         = new ReusableString();
    
    private final UIntMandReaderCopy    _templateIdReader = new UIntMandReaderCopy( "TemplateId", 0, 0 );

    private final long                  _subChannelOnMask;
    private final String                _id;

    // HAND CODED READERS
    private final MDIncRefresh_81_Reader  _mdIncRefresh_T81;
    private final MDIncRefresh_83_Reader  _mdIncRefresh_T83;
    private final MDIncRefresh_84_Reader  _mdIncRefresh_T84;
    private final MDIncRefresh_103_Reader _mdIncRefresh_T103;
    private final MDIncRefresh_109_Reader _mdIncRefresh_T109;

    private int  _templateId = 0;
    private int  _seqNum     = 0;
    private int  _lastSeqNum = 0;
    private byte _subchannel = 0;  

    private long    _receivedTS;

    // stats
    private int     _dups    = 0;
    private int     _unknown = 0;
    private int     _packets = 0;
    private int     _subChannelSkips = 0;
    private int     _handPackets;

    private boolean _logStats;

    private boolean _nextDummy;


    public CMEFastFixDecoder( String id, String templateFile, long subChannelOnMask, boolean debug ) {
        
        _id = id;
        _debug = debug;
        _tzCalculator.getDateUTC( _today );
        _subChannelOnMask = subChannelOnMask;

        initClass( templateFile );
        
        ComponentFactory cf81 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_81" );
        ComponentFactory cf83 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_83" );
        ComponentFactory cf84 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_84" );
        ComponentFactory cf103 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_103" );
        ComponentFactory cf109 = _dictionaries.getMsgTypeDictComponentFactory( "MDIncRefresh_109" );

        _mdIncRefresh_T81 = new MDIncRefresh_81_Reader( cf81, "MDIncRefresh_81", 81 );
        _mdIncRefresh_T83 = new MDIncRefresh_83_Reader( cf83, "MDIncRefresh_83", 83 );
        _mdIncRefresh_T84 = new MDIncRefresh_84_Reader( cf84, "MDIncRefresh_84", 84 );
        _mdIncRefresh_T103 = new MDIncRefresh_103_Reader( cf103, "MDIncRefresh_103", 103 );
        _mdIncRefresh_T109 = new MDIncRefresh_109_Reader( cf109, "MDIncRefresh_109", 109 );
        
        _softDecoder.setDefaultExchange( new ViewString("XCME") );
        
        Iterator<Integer> it = meta.templateIterator();
        
        while( it.hasNext() ) {
            Integer tid = it.next();
            
            preloadReader( tid.intValue() ); // force create the reader
        }
        
        ShutdownManager.instance().register( new Callback(){
            @Override
            public void shuttingDown() {
                logStats();
            }} );
    }

    @Override
    public void logStats() {
        _log.info( "CMEFastFixReader STATS id=" + _id + 
                  ", handPackets=" + _handPackets + 
                  ", packets=" + _packets + 
                  ", unknown=" + _unknown + 
                  ", dups=" + _dups + 
                  ", subChannelSkips=" + _subChannelSkips );
    }

    private synchronized static void initClass( String templateFile ) {
        if ( _init == false ) {
            TemplateClassRegister reg = new FastFixTemplateClassRegister();
            XMLFastFixTemplateLoader l = new XMLFastFixTemplateLoader( templateFile );
            l.load( reg, meta );
            _init = true;
        }
    }

    @Override
    public int parseHeader( byte[] inBuffer, int inHdrLen, int bytesRead ) {
        return bytesRead;
    }

    @Override
    public Message postHeaderDecode() {
        return null;
    }

    public byte getSubchannel() {
        return _subchannel;
    }

    /**
     * decode CME fast fix message
     * 
     * @WARNING application is responsible for gap fill checking
     * 
     * builder end is invoked as CME packs 1 message only each packet
     */
    @Override
    public Message decode( byte[] msg, int offset, int maxIdx ) {
        _binDecodeBuilder.start( msg, offset, maxIdx );

        // 4 byte sequence , 1 byte channel
        
        _seqNum   = _binDecodeBuilder.decodeSeqNum();

        _subchannel  = _binDecodeBuilder.decodeChannel();

        if ( _nextDummy ) {
            _nextDummy= false;
        } else {
            if ( _seqNum == 0 ) {
                return null;        
            }
            
            if ( skipSubChannel( _subchannel ) ) {
                ++_subChannelSkips;
                return null;
            }
            
            if ( _lastSeqNum == _seqNum ) { // A and B feeds in general will be in sync so we will get lots dups
                ++_dups;
                return null;
            }
        
            _lastSeqNum = _seqNum;
        }
        
        _pMap.readMap( _binDecodeBuilder );
        
        _templateId = _templateIdReader.read( _binDecodeBuilder, _pMap ); 

        Message m = null;
        
        // HAND TEMPLATES - ENSURE SWITCH STATEMENT HAS FILLERS
        switch( _templateId ) {
        case MD_INC_REFRESH_81:
            _mdIncRefresh_T81.reset();
            ++_handPackets;
            m = _mdIncRefresh_T81.read( _binDecodeBuilder, _pMap );
            if ( _logStats ) ((MDIncRefreshImpl)m).setReceived( _receivedTS );
            break;
        case MD_INC_REFRESH_83:
            _mdIncRefresh_T83.reset();
            ++_handPackets;
            m = _mdIncRefresh_T83.read( _binDecodeBuilder, _pMap );
            if ( _logStats ) ((MDIncRefreshImpl)m).setReceived( _receivedTS );
            break;
        case MD_INC_REFRESH_84:
            _mdIncRefresh_T84.reset();
            ++_handPackets;
            m = _mdIncRefresh_T84.read( _binDecodeBuilder, _pMap );
            if ( _logStats ) ((MDIncRefreshImpl)m).setReceived( _receivedTS );
            break;
        case MD_INC_REFRESH_103:
            _mdIncRefresh_T103.reset();
            ++_handPackets;
            m = _mdIncRefresh_T103.read( _binDecodeBuilder, _pMap );
            if ( _logStats ) ((MDIncRefreshImpl)m).setReceived( _receivedTS );
            break;
        case MD_INC_REFRESH_109:
            _mdIncRefresh_T109.reset();
            ++_handPackets;
            m = _mdIncRefresh_T109.read( _binDecodeBuilder, _pMap );
            if ( _logStats ) ((MDIncRefreshImpl)m).setReceived( _receivedTS );
            break;
        case 82:                      case 85:  case 86: case 87: case 88: case 89:
        case 90:  case 91:  case 92:  case 93:  case 94: case 95: case 96: case 97: case 98: case 99: case 100: case 101: case 102:
        case 104: case 105: case 106: case 107: case 108: 
        default:
            break;
        }

        if ( m != null ) {
            _binDecodeBuilder.end(); // only one message per packet in CME
            
            return m;
        }
        
        return processsSoftTemplateDecode( _templateId, _subchannel );
    }

    private void logBinary( Logger log, int templateId, byte subchannel ) {
        _destFixMsg.reset();
        _destFixMsg.append( "IN  [" ).append( _seqNum ).append( "] [s#" ).append( (int)subchannel ).append( "] [t#" ).append( templateId ).append( "] ");
        _destFixMsg.appendHEX( _binDecodeBuilder.getBuffer(), _binDecodeBuilder.getOffset(), _binDecodeBuilder.getMaxIdx() );

        log.info( _destFixMsg );
    }

    private Message processsSoftTemplateDecode( int templateId, byte subchannel ) {

        _destFixMsg.reset();
        
        FastFixToFixReader reader = getReader( templateId );

        if ( reader != null ) {
            reader.reset();

            reader.read( _binDecodeBuilder, _pMap, _destFixMsg );
            
            _binDecodeBuilder.end(); // only one message per packet in CME
            
            ++_packets;

            if ( _debug ) {
                _log.info( _destFixMsg ); // log the intermediate readable fix message
            }
            
            Message m = _softDecoder.decode( _destFixMsg.getBytes(), 0, _destFixMsg.length() );

            if ( _logStats ) {
                switch( m.getReusableType().getSubId() ) {
                case EventIds.ID_MDINCREFRESH:
                    MDIncRefreshImpl inc = (MDIncRefreshImpl) m;
                    inc.setReceived( _receivedTS );
                    break;
                case EventIds.ID_MDSNAPSHOTFULLREFRESH:
                    MDSnapshotFullRefreshImpl snap = (MDSnapshotFullRefreshImpl) m;
                    snap.setReceived( _receivedTS );
                    break;
                }
            }
            
            return m;
        } 

        if ( _debug ) {
            _destFixMsg.append( " SKIPPING templateId=" ).append( templateId );
            _log.info( _destFixMsg );
        }
        
        ++_unknown;
        
        return null;
    }

    private boolean skipSubChannel( byte subChannel ) {
        long bit = 1 << subChannel;
        
        return (_subChannelOnMask & bit) == 0;
    }

    @Override
    public void setReceived( long nanos ) {
        _receivedTS = nanos;
    }

    @Override
    public long getReceived() {
        return _receivedTS;
    }

    @Override
    public void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        _tzCalculator= calc;
    }

    private FastFixToFixReader preloadReader( int templateId ) {
        MetaTemplate mt = meta.getTemplate( templateId );
        if ( mt == null ) return null;
        
        FastFixToFixReader reader = _preparedReaders.get( templateId );
        
        if ( reader == null ) {
            reader = new FastFixToFixReader( mt, "T" + templateId, templateId, FixField.FIELD_DELIMITER );
            
           _preparedReaders.put( templateId, reader );
           
           reader.init( _dictionaries.getMsgTypeDictComponentFactory( mt.getDictionaryId() ) );
        }

        return reader;
    }

    private FastFixToFixReader getReader( int templateId ) {
        MetaTemplate mt = meta.getTemplate( templateId );
        if ( mt == null ) return null;
        
        FastFixToFixReader reader = _activeReaders.get( templateId );
        
        if ( reader == null ) {
            reader = preloadReader( templateId );

            _log.warn( "CMEFastFixReader using softTemplate " + templateId );
            
           _activeReaders.put( templateId, reader );
        }
        
        return reader;
    }

    @Override public void setNanoStats( boolean nanoTiming )                            { 
        _logStats = nanoTiming; 
    }
    
    @Override public void setInstrumentLocator( InstrumentLocator instrumentLocator )   { /* nothing */ }
    @Override public InstrumentLocator getInstrumentLocator()                           { return null; }
    @Override public void setClientProfile( ClientProfile client )                      { /* nothing */ }
    @Override public ResyncCode resync( byte[] fixMsg, int offset, int maxIdx )         { return null; }
    @Override public int getSkipBytes()                                                 { return 0; }

    @Override
    public boolean isDebug() {
        return _debug;
    }

    @Override
    public void setDebug( boolean isDebugOn ) {
        _debug = isDebugOn;
    }

    public void logError( Logger log, ReusableString errMsg, Exception e ) {
        errMsg.reset();
        errMsg.append( "DECODING EXCEPTION, templateId=" ).append( _templateId ).append( " : ").append( e.getMessage() );
        log.error( ERR_FF1, errMsg, e );
        logBinary( log, _templateId, _subchannel );
    }

    @Override
    public int getLength() {
        return _binDecodeBuilder.getLength();
    }

    @Override
    public void logLastMsg() {
        logBinary( _log, _templateId, _subchannel );
    }

    public int getLastSeqNum() {
        return _lastSeqNum;
    }
    
    @Override
    public void setNextDummy() {
        _nextDummy = true;
    }

}
