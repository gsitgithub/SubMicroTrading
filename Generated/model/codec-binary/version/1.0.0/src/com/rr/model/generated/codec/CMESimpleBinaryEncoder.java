/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.codec;

import java.util.HashMap;
import java.util.Map;
import com.rr.core.lang.*;
import com.rr.core.model.*;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.pool.SuperPool;
import com.rr.core.codec.BinaryEncoder;
import com.rr.codec.emea.exchange.cme.sbe.SBEEncodeBuilderImpl;
import com.rr.core.codec.binary.BinaryEncodeBuilder;
import com.rr.core.codec.binary.DebugBinaryEncodeBuilder;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.model.internal.type.*;
import com.rr.model.generated.internal.events.factory.*;
import com.rr.model.generated.internal.events.impl.*;
import com.rr.model.generated.internal.events.interfaces.*;
import com.rr.model.generated.internal.type.*;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.core.FullEventIds;
import com.rr.core.codec.binary.sbe.SBEPacketHeader;

@SuppressWarnings( {"unused", "cast"} )

public final class CMESimpleBinaryEncoder implements com.rr.core.codec.binary.sbe.SBEEncoder {

   // Member Vars
    private static final int      MSG_MDIncrementalRefreshReal = 1;
    private static final int      MSG_MDIncrementalRefreshTrade = 2;
    private static final int      MSG_MDIncrementalRefreshVolume = 3;
    private static final int      MSG_MDSecurityStatus = 4;
    private static final int      MSG_MDSnapshotRefresh = 5;
    private static final int      MSG_MDIncrementalRefreshStats = 7;
    private static final int      MSG_MDIncrementalRefreshImplied = 8;
    private static final int      MSG_MDInstrumentDefinition = 9;

    private static final byte      DEFAULT_Side = 0x00;
    private static final byte      DEFAULT_MDUpdateAction = 0x00;
    private static final byte      DEFAULT_MDEntryType = 0x00;

    private final byte[]                  _buf;
    private final int                     _offset;
    private final ZString                 _binaryVersion;

    private BinaryEncodeBuilder     _builder;

    private       TimeZoneCalculator      _tzCalculator = new TimeZoneCalculator();
    private       SingleByteLookup        _sv;
    private       TwoByteLookup           _tv;
    private       MultiByteLookup         _mv;
    private final ReusableString          _dump  = new ReusableString(256);

    private boolean                 _debug = false;

   // Constructors
    public CMESimpleBinaryEncoder( byte[] buf, int offset ) {
        if ( buf.length < SizeType.MIN_ENCODE_BUFFER.getSize() ) {
            throw new RuntimeException( "Encode buffer too small only " + buf.length + ", min=" + SizeType.MIN_ENCODE_BUFFER.getSize() );
        }
        _buf = buf;
        _offset = offset;
        _binaryVersion   = new ViewString( "1");
        setBuilder();
    }


   // encode methods

    @Override
    public final void encode( final Message msg ) {
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_MDINCREFRESH:
            encodeMDIncrementalRefreshReal( (MDIncRefresh) msg );
            break;
        case EventIds.ID_SECURITYSTATUS:
            encodeMDSecurityStatus( (SecurityStatus) msg );
            break;
        case EventIds.ID_MDSNAPSHOTFULLREFRESH:
            encodeMDSnapshotRefresh( (MDSnapshotFullRefresh) msg );
            break;
        case EventIds.ID_SECURITYDEFINITION:
            encodeMDInstrumentDefinition( (SecurityDefinition) msg );
            break;
        case 82:
        case 83:
            _builder.start();
            break;
        default:
            _builder.start();
            break;
        }
    }

    @Override public final int getLength() { return _builder.getLength(); }
    @Override public final int getOffset() { return _builder.getOffset(); }

    @Override
    public boolean isDebug() {
        return _debug;
    }

    @Override
    public void setDebug( boolean isDebugOn ) {
        _debug = isDebugOn;
        setBuilder();
    }

    private void setBuilder() {
        _builder = (_debug) ? new DebugBinaryEncodeBuilder<com.rr.codec.emea.exchange.cme.sbe.SBEEncodeBuilderImpl>( _dump, new com.rr.codec.emea.exchange.cme.sbe.SBEEncodeBuilderImpl( _buf, _offset, _binaryVersion ) )
                            : new com.rr.codec.emea.exchange.cme.sbe.SBEEncodeBuilderImpl( _buf, _offset, _binaryVersion );
    }


    public final void encodeMDIncrementalRefreshReal( final MDIncRefresh msg ) {
        final int startRootBlockIdx = _builder.getCurrentIndex();
        final int now = _tzCalculator.getNowUTC();
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MDIncrementalRefreshReal" ).append( "  rootBlockLen=" ).append( "14" ).append( "  eventType=" ).append( "MDIncRefresh" ).append( " : " );
        ((SBEEncodeBuilderImpl)_builder).setNextBlockLen( (short)14 );
        _builder.start( MSG_MDIncrementalRefreshReal );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( (int)msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // TimeBracket
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _builder.encodeByte( Constants.UNSET_BYTE );    // MatchEventStartIndicator
        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getNoMDEntries() );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = 14 - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.encodeFiller( rootBytesToSkip );
        if ( _debug ) _dump.append( "\nField: " ).append( "MDEntriesReal" ).append( " : " );

        {
            MDEntryImpl tmpMDEntriesReal = (MDEntryImpl)msg.getMDEntries();
            int counterMDEntriesReal = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesReal ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                _builder.encodeByte( transformMDUpdateAction( tmpMDEntriesReal.getMdUpdateAction() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryType" ).append( " : " );
                _builder.encodeByte( transformMDEntryType( tmpMDEntriesReal.getMdEntryType() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesReal.getSecurityID() );
                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesReal.getRepeatSeq() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                _builder.encodeDecimal( tmpMDEntriesReal.getMdEntryPx() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesReal.getMdEntrySize() );
                if ( _debug ) _dump.append( "\nField: " ).append( "NumberOfOrders" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesReal.getNumberOfOrders() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDPriceLevel" ).append( " : " );
                _builder.encodeByte( (byte)tmpMDEntriesReal.getMdPriceLevel() );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 27 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.encodeFiller( bytesToSkip );
                tmpMDEntriesReal = tmpMDEntriesReal.getNext();
            }
        }


        _builder.end();
    }

    public final void encodeMDIncrementalRefreshTrade( final MDIncRefresh msg ) {
        final int startRootBlockIdx = _builder.getCurrentIndex();
        final int now = _tzCalculator.getNowUTC();
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MDIncrementalRefreshTrade" ).append( "  rootBlockLen=" ).append( "14" ).append( "  eventType=" ).append( "MDIncRefresh" ).append( " : " );
        ((SBEEncodeBuilderImpl)_builder).setNextBlockLen( (short)14 );
        _builder.start( MSG_MDIncrementalRefreshTrade );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( (int)msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // TimeBracket
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _builder.encodeByte( Constants.UNSET_BYTE );    // MatchEventStartIndicator
        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getNoMDEntries() );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = 14 - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.encodeFiller( rootBytesToSkip );
        if ( _debug ) _dump.append( "\nField: " ).append( "MDEntriesTrade" ).append( " : " );

        {
            MDEntryImpl tmpMDEntriesTrade = (MDEntryImpl)msg.getMDEntries();
            int counterMDEntriesTrade = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesTrade ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                _builder.encodeByte( transformMDUpdateAction( tmpMDEntriesTrade.getMdUpdateAction() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesTrade.getSecurityID() );
                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesTrade.getRepeatSeq() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                _builder.encodeDecimal( tmpMDEntriesTrade.getMdEntryPx() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesTrade.getMdEntrySize() );
                if ( _debug ) _dump.append( "\nField: " ).append( "TradeID" ).append( " : " );
                _builder.encodeInt( Constants.UNSET_INT );    // TradeID
                if ( _debug ) _dump.append( "\nField: " ).append( "NumberOfOrders" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesTrade.getNumberOfOrders() );
                if ( _debug ) _dump.append( "\nField: " ).append( "AggressorSide" ).append( " : " );
                _builder.encodeByte( Constants.UNSET_BYTE );    // AggressorSide
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryTypeTrade" ).append( " : " );
                final MDEntryType tMdEntryTypeBase = tmpMDEntriesTrade.getMdEntryType();
                final byte tMdEntryType = ( tMdEntryTypeBase == null ) ? Constants.UNSET_BYTE : transformMDEntryType( tMdEntryTypeBase );
                _builder.encodeByte( tMdEntryType );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 30 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.encodeFiller( bytesToSkip );
                tmpMDEntriesTrade = tmpMDEntriesTrade.getNext();
            }
        }


        _builder.end();
    }

    public final void encodeMDIncrementalRefreshVolume( final MDIncRefresh msg ) {
        final int startRootBlockIdx = _builder.getCurrentIndex();
        final int now = _tzCalculator.getNowUTC();
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MDIncrementalRefreshVolume" ).append( "  rootBlockLen=" ).append( "14" ).append( "  eventType=" ).append( "MDIncRefresh" ).append( " : " );
        ((SBEEncodeBuilderImpl)_builder).setNextBlockLen( (short)14 );
        _builder.start( MSG_MDIncrementalRefreshVolume );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( (int)msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // TimeBracket
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _builder.encodeByte( Constants.UNSET_BYTE );    // MatchEventStartIndicator
        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getNoMDEntries() );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = 14 - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.encodeFiller( rootBytesToSkip );
        if ( _debug ) _dump.append( "\nField: " ).append( "MDEntriesVolume" ).append( " : " );

        {
            MDEntryImpl tmpMDEntriesVolume = (MDEntryImpl)msg.getMDEntries();
            int counterMDEntriesVolume = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesVolume ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                _builder.encodeByte( transformMDUpdateAction( tmpMDEntriesVolume.getMdUpdateAction() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesVolume.getSecurityID() );
                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesVolume.getRepeatSeq() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesVolume.getMdEntrySize() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryTypeVol" ).append( " : " );
                final MDEntryType tMdEntryTypeBase = tmpMDEntriesVolume.getMdEntryType();
                final byte tMdEntryType = ( tMdEntryTypeBase == null ) ? Constants.UNSET_BYTE : transformMDEntryType( tMdEntryTypeBase );
                _builder.encodeByte( tMdEntryType );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 13 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.encodeFiller( bytesToSkip );
                tmpMDEntriesVolume = tmpMDEntriesVolume.getNext();
            }
        }


        _builder.end();
    }

    public final void encodeMDSecurityStatus( final SecurityStatus msg ) {
        final int startRootBlockIdx = _builder.getCurrentIndex();
        final int now = _tzCalculator.getNowUTC();
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MDSecurityStatus" ).append( "  rootBlockLen=" ).append( "29" ).append( "  eventType=" ).append( "SecurityStatus" ).append( " : " );
        ((SBEEncodeBuilderImpl)_builder).setNextBlockLen( (short)29 );
        _builder.start( MSG_MDSecurityStatus );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // TimeBracket
        if ( _debug ) _dump.append( "\nField: " ).append( "TradeDate" ).append( " : " );
        _builder.encodeUShort( (short)msg.getTradeDate() );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityGroup" ).append( " : " );
        _builder.encodeFiller( 6 );    // SecurityGroup
        if ( _debug ) _dump.append( "\nField: " ).append( "Asset" ).append( " : " );
        _builder.encodeFiller( 6 );    // Asset
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
        _builder.encodeInt( (int)msg.getSecurityID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityTradingStatus" ).append( " : " );
        final SecurityTradingStatus tSecurityTradingStatusBase = msg.getSecurityTradingStatus();
        final ViewString tSecurityTradingStatus = transformSecurityTradingStatus( msg.getSecurityTradingStatus() );
        if ( tSecurityTradingStatus != null ) _builder.encodeString( tSecurityTradingStatus );
        if ( _debug ) _dump.append( "\nField: " ).append( "HaltReason" ).append( " : " );
        _builder.encodeByte( (byte)msg.getHaltReason() );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityTradingEvent" ).append( " : " );
        _builder.encodeByte( (byte)msg.getSecurityTradingEvent() );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = 29 - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.encodeFiller( rootBytesToSkip );
        _builder.end();
    }

    public final void encodeMDSnapshotRefresh( final MDSnapshotFullRefresh msg ) {
        final int startRootBlockIdx = _builder.getCurrentIndex();
        final int now = _tzCalculator.getNowUTC();
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MDSnapshotRefresh" ).append( "  rootBlockLen=" ).append( "14" ).append( "  eventType=" ).append( "MDSnapshotFullRefresh" ).append( " : " );
        ((SBEEncodeBuilderImpl)_builder).setNextBlockLen( (short)14 );
        _builder.start( MSG_MDSnapshotRefresh );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( (int)msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // TimeBracket
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _builder.encodeByte( Constants.UNSET_BYTE );    // MatchEventStartIndicator
        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getNoMDEntries() );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = 14 - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.encodeFiller( rootBytesToSkip );
        if ( _debug ) _dump.append( "\nField: " ).append( "MDEntriesSnap" ).append( " : " );

        {
            MDSnapEntryImpl tmpMDEntriesSnap = (MDSnapEntryImpl)msg.getMDEntries();
            int counterMDEntriesSnap = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesSnap ; ++i ) { 
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryType" ).append( " : " );
                _builder.encodeByte( transformMDEntryType( tmpMDEntriesSnap.getMdEntryType() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                _builder.encodeInt( Constants.UNSET_INT );    // SecurityID
                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                _builder.encodeInt( Constants.UNSET_INT );    // RtpSeq
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                _builder.encodeDecimal( tmpMDEntriesSnap.getMdEntryPx() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesSnap.getMdEntrySize() );
                if ( _debug ) _dump.append( "\nField: " ).append( "NumberOfOrders" ).append( " : " );
                _builder.encodeInt( Constants.UNSET_INT );    // NumberOfOrders
                if ( _debug ) _dump.append( "\nField: " ).append( "MDPriceLevel" ).append( " : " );
                _builder.encodeByte( (byte)tmpMDEntriesSnap.getMdPriceLevel() );
                if ( _debug ) _dump.append( "\nField: " ).append( "fillerEntry" ).append( " : " );
                _builder.encodeFiller( 27 );
                tmpMDEntriesSnap = tmpMDEntriesSnap.getNext();
            }
        }


        _builder.end();
    }

    public final void encodeMDIncrementalRefreshStats( final MDIncRefresh msg ) {
        final int startRootBlockIdx = _builder.getCurrentIndex();
        final int now = _tzCalculator.getNowUTC();
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MDIncrementalRefreshStats" ).append( "  rootBlockLen=" ).append( "14" ).append( "  eventType=" ).append( "MDIncRefresh" ).append( " : " );
        ((SBEEncodeBuilderImpl)_builder).setNextBlockLen( (short)14 );
        _builder.start( MSG_MDIncrementalRefreshStats );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( (int)msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // TimeBracket
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _builder.encodeByte( Constants.UNSET_BYTE );    // MatchEventStartIndicator
        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getNoMDEntries() );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = 14 - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.encodeFiller( rootBytesToSkip );
        if ( _debug ) _dump.append( "\nField: " ).append( "MDEntriesStats" ).append( " : " );

        {
            MDEntryImpl tmpMDEntriesStats = (MDEntryImpl)msg.getMDEntries();
            int counterMDEntriesStats = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesStats ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                _builder.encodeByte( transformMDUpdateAction( tmpMDEntriesStats.getMdUpdateAction() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryType" ).append( " : " );
                _builder.encodeByte( transformMDEntryType( tmpMDEntriesStats.getMdEntryType() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesStats.getSecurityID() );
                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesStats.getRepeatSeq() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                _builder.encodeDecimal( tmpMDEntriesStats.getMdEntryPx() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesStats.getMdEntrySize() );
                if ( _debug ) _dump.append( "\nField: " ).append( "fillerEntry" ).append( " : " );
                _builder.encodeFiller( 27 );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 49 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.encodeFiller( bytesToSkip );
                tmpMDEntriesStats = tmpMDEntriesStats.getNext();
            }
        }


        _builder.end();
    }

    public final void encodeMDIncrementalRefreshImplied( final MDIncRefresh msg ) {
        final int startRootBlockIdx = _builder.getCurrentIndex();
        final int now = _tzCalculator.getNowUTC();
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MDIncrementalRefreshImplied" ).append( "  rootBlockLen=" ).append( "14" ).append( "  eventType=" ).append( "MDIncRefresh" ).append( " : " );
        ((SBEEncodeBuilderImpl)_builder).setNextBlockLen( (short)14 );
        _builder.start( MSG_MDIncrementalRefreshImplied );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        _builder.encodeTimestampUTC( (int)msg.getSendingTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // TimeBracket
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _builder.encodeByte( Constants.UNSET_BYTE );    // MatchEventStartIndicator
        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getNoMDEntries() );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = 14 - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.encodeFiller( rootBytesToSkip );
        if ( _debug ) _dump.append( "\nField: " ).append( "MDEntriesImplied" ).append( " : " );

        {
            MDEntryImpl tmpMDEntriesImplied = (MDEntryImpl)msg.getMDEntries();
            int counterMDEntriesImplied = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesImplied ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                _builder.encodeByte( transformMDUpdateAction( tmpMDEntriesImplied.getMdUpdateAction() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryType" ).append( " : " );
                _builder.encodeByte( transformMDEntryType( tmpMDEntriesImplied.getMdEntryType() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesImplied.getSecurityID() );
                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesImplied.getRepeatSeq() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                _builder.encodeDecimal( tmpMDEntriesImplied.getMdEntryPx() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                _builder.encodeInt( (int)tmpMDEntriesImplied.getMdEntrySize() );
                if ( _debug ) _dump.append( "\nField: " ).append( "MDPriceLevel" ).append( " : " );
                _builder.encodeByte( (byte)tmpMDEntriesImplied.getMdPriceLevel() );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 23 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.encodeFiller( bytesToSkip );
                tmpMDEntriesImplied = tmpMDEntriesImplied.getNext();
            }
        }


        _builder.end();
    }

    public final void encodeMDInstrumentDefinition( final SecurityDefinition msg ) {
        final int startRootBlockIdx = _builder.getCurrentIndex();
        final int now = _tzCalculator.getNowUTC();
        if ( _debug ) {
            _dump.append( "  encodeMap=" ).append( "MDInstrumentDefinition" ).append( "  rootBlockLen=" ).append( "246" ).append( "  eventType=" ).append( "SecurityDefinition" ).append( " : " );
        ((SBEEncodeBuilderImpl)_builder).setNextBlockLen( (short)246 );
        _builder.start( MSG_MDInstrumentDefinition );
        }

        if ( _debug ) _dump.append( "\nField: " ).append( "TotNumReports" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getTotNumReports() );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityUpdateAction" ).append( " : " );
        final SecurityUpdateAction tSecurityUpdateAction = msg.getSecurityUpdateAction();
        final byte tSecurityUpdateActionBytes = ( tSecurityUpdateAction != null ) ? tSecurityUpdateAction.getVal() : 0x00;
        _builder.encodeByte( tSecurityUpdateActionBytes );
        if ( _debug ) _dump.append( "\nField: " ).append( "LastUpdateTime" ).append( " : " );
        _builder.encodeTimestampUTC( (int)msg.getLastUpdateTime() );
        if ( _debug ) _dump.append( "\nField: " ).append( "ApplID" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getApplID(), 5 );
        if ( _debug ) _dump.append( "\nField: " ).append( "MarketSegmentID" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // MarketSegmentID
        if ( _debug ) _dump.append( "\nField: " ).append( "Symbol" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getSymbol(), 20 );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
        _builder.encodeInt( (int)msg.getSecurityID() );
        if ( _debug ) _dump.append( "\nField: " ).append( "MaturityMonthYear" ).append( " : " );
        _builder.encodeUShort( (short)msg.getMaturityMonthYear() );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityGroup" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getSecurityGroup(), 6 );
        if ( _debug ) _dump.append( "\nField: " ).append( "Asset" ).append( " : " );
        _builder.encodeFiller( 6 );    // Asset
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityType" ).append( " : " );
        final SecurityType tSecurityType = msg.getSecurityType();
        final byte[] tSecurityTypeBytes = ( tSecurityType != null ) ? tSecurityType.getVal() : null;
        _builder.encodeZStringFixedWidth( tSecurityTypeBytes, 0, 6 );
        if ( _debug ) _dump.append( "\nField: " ).append( "CFICode" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getCFICode(), 6 );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecuritySubType" ).append( " : " );
        _builder.encodeFiller( 5 );    // SecuritySubType
        if ( _debug ) _dump.append( "\nField: " ).append( "UserDefinedInstrument" ).append( " : " );
        _builder.encodeByte( Constants.UNSET_BYTE );    // UserDefinedInstrument
        if ( _debug ) _dump.append( "\nField: " ).append( "UnderlyingProdcut" ).append( " : " );
        _builder.encodeString( msg.getUnderlyingProduct(), 1 );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityExchange" ).append( " : " );
        _builder.encodeZStringFixedWidth( msg.getSecurityExchange(), 4 );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityTradingStatus" ).append( " : " );
        final SecurityTradingStatus tSecurityTradingStatusBase = msg.getSecurityTradingStatus();
        final ViewString tSecurityTradingStatus = transformSecurityTradingStatus( msg.getSecurityTradingStatus() );
        if ( tSecurityTradingStatus != null ) _builder.encodeString( tSecurityTradingStatus );
        if ( _debug ) _dump.append( "\nField: " ).append( "StrikePrice" ).append( " : " );
        _builder.encodeDecimal( msg.getStrikePrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "StrikeCurrency" ).append( " : " );
        final Currency tStrikeCurrency = msg.getStrikeCurrency();
        final byte[] tStrikeCurrencyBytes = ( tStrikeCurrency != null ) ? tStrikeCurrency.getVal() : null;
        _builder.encodeZStringFixedWidth( tStrikeCurrencyBytes, 0, 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "Currency" ).append( " : " );
        final Currency tCurrency = msg.getCurrency();
        final byte[] tCurrencyBytes = ( tCurrency != null ) ? tCurrency.getVal() : null;
        _builder.encodeZStringFixedWidth( tCurrencyBytes, 0, 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "SettlCurrency" ).append( " : " );
        final Currency tSettlCurrency = msg.getSettlCurrency();
        final byte[] tSettlCurrencyBytes = ( tSettlCurrency != null ) ? tSettlCurrency.getVal() : null;
        _builder.encodeZStringFixedWidth( tSettlCurrencyBytes, 0, 3 );
        if ( _debug ) _dump.append( "\nField: " ).append( "MinCabPrice" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // MinCabPrice
        if ( _debug ) _dump.append( "\nField: " ).append( "PriceRatio" ).append( " : " );
        _builder.encodeDecimal( msg.getPriceRatio() );
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchAlgorithm" ).append( " : " );
        _builder.encodeByte( Constants.UNSET_BYTE );    // MatchAlgorithm
        if ( _debug ) _dump.append( "\nField: " ).append( "MinTradeVol" ).append( " : " );
        _builder.encodeInt( (int)msg.getMinTradeVol() );
        if ( _debug ) _dump.append( "\nField: " ).append( "MaxTradeVol" ).append( " : " );
        _builder.encodeInt( (int)msg.getMaxTradeVol() );
        if ( _debug ) _dump.append( "\nField: " ).append( "MinPriceIncrement" ).append( " : " );
        _builder.encodeDecimal( msg.getMinPriceIncrement() );
        if ( _debug ) _dump.append( "\nField: " ).append( "MinPriceIncrementAmount" ).append( " : " );
        _builder.encodeDecimal( msg.getMinPriceIncrementAmount() );
        if ( _debug ) _dump.append( "\nField: " ).append( "DisplayFactor" ).append( " : " );
        _builder.encodeDecimal( msg.getDisplayFactor() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TickRule" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // TickRule
        if ( _debug ) _dump.append( "\nField: " ).append( "MainFraction" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // MainFraction
        if ( _debug ) _dump.append( "\nField: " ).append( "SubFraction" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // SubFraction
        if ( _debug ) _dump.append( "\nField: " ).append( "PriceDisplayFormat" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // PriceDisplayFormat
        if ( _debug ) _dump.append( "\nField: " ).append( "ContractMultiplierUnit" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getContractMultiplierType() );
        if ( _debug ) _dump.append( "\nField: " ).append( "FlowScheduleType" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // FlowScheduleType
        if ( _debug ) _dump.append( "\nField: " ).append( "ContractMultiplier" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getContractMultiplier() );
        if ( _debug ) _dump.append( "\nField: " ).append( "UnitOfMeasure" ).append( " : " );
        _builder.encodeFiller( 30 );    // UnitOfMeasure
        if ( _debug ) _dump.append( "\nField: " ).append( "UnitOfMeasureQty" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // UnitOfMeasureQty
        if ( _debug ) _dump.append( "\nField: " ).append( "DecayQty" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // DecayQty
        if ( _debug ) _dump.append( "\nField: " ).append( "DecayStartDate" ).append( " : " );
        _builder.encodeUShort( Constants.UNSET_SHORT );    // DecayStartDate
        if ( _debug ) _dump.append( "\nField: " ).append( "OriginalContractSize" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // OriginalContractSize
        if ( _debug ) _dump.append( "\nField: " ).append( "HighLimitPrice" ).append( " : " );
        _builder.encodeDecimal( msg.getHighLimitPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "LowLimitPrice" ).append( " : " );
        _builder.encodeDecimal( msg.getLowLimitPx() );
        if ( _debug ) _dump.append( "\nField: " ).append( "MaxPriceVariation" ).append( " : " );
        _builder.encodePrice( Constants.UNSET_DOUBLE );    // MaxPriceVariation
        if ( _debug ) _dump.append( "\nField: " ).append( "TradingReferencePrice" ).append( " : " );
        _builder.encodeDecimal( msg.getTradingReferencePrice() );
        if ( _debug ) _dump.append( "\nField: " ).append( "SettlPriceType" ).append( " : " );
        _builder.encodeByte( Constants.UNSET_BYTE );    // SettlPriceType
        if ( _debug ) _dump.append( "\nField: " ).append( "OpenInterestQty" ).append( " : " );
        _builder.encodeInt( (int)msg.getOpenInterestQty() );
        if ( _debug ) _dump.append( "\nField: " ).append( "ClearedVolume" ).append( " : " );
        _builder.encodeInt( Constants.UNSET_INT );    // ClearedVolume
        if ( _debug ) _dump.append( "\nField: " ).append( "NoUnderlyings" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // NoUnderlyings
        if ( _debug ) _dump.append( "\nField: " ).append( "NoLegs" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getNoLegs() );
        if ( _debug ) _dump.append( "\nField: " ).append( "NoMdFeedTypes" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getNoSDFeedTypes() );
        if ( _debug ) _dump.append( "\nField: " ).append( "NoEvents" ).append( " : " );
        _builder.encodeUByte( (byte)msg.getNoEvents() );
        if ( _debug ) _dump.append( "\nField: " ).append( "NoInstAttrib" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // NoInstAttrib
        if ( _debug ) _dump.append( "\nField: " ).append( "NoLotTypeRules" ).append( " : " );
        _builder.encodeUByte( Constants.UNSET_BYTE );    // NoLotTypeRules

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = 246 - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.encodeFiller( rootBytesToSkip );
        if ( _debug ) _dump.append( "\nField: " ).append( "UnderlyingsGrp" ).append( " : " );

        if ( _debug ) _dump.append( "\nField: " ).append( "LegsGrp" ).append( " : " );

        {
            SecDefLegImpl tmpLegsGrp = (SecDefLegImpl)msg.getLegs();
            int counterLegsGrp = msg.getNoLegs();
            for( int i=0 ; i < counterLegsGrp ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                if ( _debug ) _dump.append( "\nField: " ).append( "LegSecurityID" ).append( " : " );
                _builder.encodeInt( (int)tmpLegsGrp.getLegSecurityID() );
                if ( _debug ) _dump.append( "\nField: " ).append( "LegSide" ).append( " : " );
                final Side tLegSideBase = tmpLegsGrp.getLegSide();
                final byte tLegSide = ( tLegSideBase == null ) ? Constants.UNSET_BYTE : transformSide( tLegSideBase );
                _builder.encodeByte( tLegSide );
                if ( _debug ) _dump.append( "\nField: " ).append( "LegRatioQty" ).append( " : " );
                _builder.encodeByte( (byte)tmpLegsGrp.getLegRatioQty() );
                if ( _debug ) _dump.append( "\nField: " ).append( "LegPrice" ).append( " : " );
                _builder.encodePrice( Constants.UNSET_DOUBLE );    // LegPrice
                if ( _debug ) _dump.append( "\nField: " ).append( "LegOptionDelta" ).append( " : " );
                _builder.encodePrice( Constants.UNSET_DOUBLE );    // LegOptionDelta

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 22 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.encodeFiller( bytesToSkip );
                tmpLegsGrp = tmpLegsGrp.getNext();
            }
        }


        if ( _debug ) _dump.append( "\nField: " ).append( "MdFeedTypesGrp" ).append( " : " );

        {
            SDFeedTypeImpl tmpMdFeedTypesGrp = (SDFeedTypeImpl)msg.getSDFeedTypes();
            int counterMdFeedTypesGrp = msg.getNoSDFeedTypes();
            for( int i=0 ; i < counterMdFeedTypesGrp ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                if ( _debug ) _dump.append( "\nField: " ).append( "MDFeedType" ).append( " : " );
                _builder.encodeZStringFixedWidth( tmpMdFeedTypesGrp.getFeedType(), 3 );
                if ( _debug ) _dump.append( "\nField: " ).append( "MarketDepth" ).append( " : " );
                _builder.encodeByte( (byte)tmpMdFeedTypesGrp.getMarketDepth() );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 4 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.encodeFiller( bytesToSkip );
                tmpMdFeedTypesGrp = tmpMdFeedTypesGrp.getNext();
            }
        }


        if ( _debug ) _dump.append( "\nField: " ).append( "EventsGrp" ).append( " : " );

        {
            SecDefEventsImpl tmpEventsGrp = (SecDefEventsImpl)msg.getEvents();
            int counterEventsGrp = msg.getNoEvents();
            for( int i=0 ; i < counterEventsGrp ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                if ( _debug ) _dump.append( "\nField: " ).append( "EventType" ).append( " : " );
                _builder.encodeByte( (byte)tmpEventsGrp.getEventType() );
                if ( _debug ) _dump.append( "\nField: " ).append( "EventTime" ).append( " : " );
                _builder.encodeTimestampUTC( (int)tmpEventsGrp.getEventTime() );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 9 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.encodeFiller( bytesToSkip );
                tmpEventsGrp = tmpEventsGrp.getNext();
            }
        }


        if ( _debug ) _dump.append( "\nField: " ).append( "InstAttribGrp" ).append( " : " );

        if ( _debug ) _dump.append( "\nField: " ).append( "LotTypeRulesGrp" ).append( " : " );

        _builder.end();
    }
    @Override
    public final byte[] getBytes() {
        return _buf;
    }

    @Override
    public final void setTimeZoneCalculator( final TimeZoneCalculator calc ) {
        _tzCalculator = calc;
        _builder.setTimeZoneCalculator( calc );
    }

    private static final Map<SecurityTradingStatus, ViewString> _securityTradingStatusMap = new HashMap<SecurityTradingStatus, ViewString>( 28 );
    private static final ViewString _securityTradingStatusDefault = new ViewString( "SecurityTradingStatus.Unknown" );

    static {
         _securityTradingStatusMap.put( SecurityTradingStatus.getVal( "2".getBytes() ), StringFactory.hexToViewString( "0x02" ) );
         _securityTradingStatusMap.put( SecurityTradingStatus.getVal( "4".getBytes() ), StringFactory.hexToViewString( "0x1A" ) );
         _securityTradingStatusMap.put( SecurityTradingStatus.getVal( "15".getBytes() ), StringFactory.hexToViewString( "0x0E" ) );
         _securityTradingStatusMap.put( SecurityTradingStatus.getVal( "17".getBytes() ), StringFactory.hexToViewString( "0x11" ) );
         _securityTradingStatusMap.put( SecurityTradingStatus.getVal( "18".getBytes() ), StringFactory.hexToViewString( "0x12" ) );
         _securityTradingStatusMap.put( SecurityTradingStatus.getVal( "20".getBytes() ), StringFactory.hexToViewString( "0x14" ) );
         _securityTradingStatusMap.put( SecurityTradingStatus.getVal( "21".getBytes() ), StringFactory.hexToViewString( "0x15" ) );
    }

    private ViewString transformSecurityTradingStatus( SecurityTradingStatus intVal ) {
        ViewString extVal = _securityTradingStatusMap.get( intVal );
        if ( extVal == null ) {
            return _securityTradingStatusDefault;
        }
        return extVal;
    }

    private byte transformSide( Side val ) {
        switch( val ) {
        case Buy:  // Buy
            return (byte)0x01;
        case Sell:  // Sell
            return (byte)0x02;
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on Side for value " + val );
    }

    private byte transformMDUpdateAction( MDUpdateAction val ) {
        switch( val ) {
        case New:  // New
            return (byte)0x00;
        case Change:  // Change
            return (byte)0x01;
        case Delete:  // Delete
            return (byte)0x02;
        case DeleteThru:  // Delete Thru
            return (byte)0x03;
        case DeleteFrom:  // Delete From
            return (byte)0x04;
        case Overlay:  // Overlay
            return (byte)0x05;
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on MDUpdateAction for value " + val );
    }

    private byte transformMDEntryType( MDEntryType val ) {
        switch( val ) {
        case Bid:  // Bid
            return (byte)0x00;
        case Offer:  // Offer
            return (byte)0x01;
        case Trade:  // Trade
            return (byte)0x02;
        case OpeningPrice:  // Opening Price
            return (byte)0x04;
        case SettlementPrice:  // Settlement Price
            return (byte)0x06;
        case TradingSessionHighPrice:  // Trading Session High Price
            return (byte)0x07;
        case TradingSessionLowPrice:  // Trading Session Low Price
            return (byte)0x08;
        case TradeVolume:  // Event Summary
            return (byte)0x65;
        case OpenInterest:  // Open Interest
            return (byte)0x43;
        case SimulatedSellPrice:  // Implied Bid
            return (byte)0x45;
        case SimulatedBuy:  // Implied Offer
            return (byte)0x46;
        case EmptyBook:  // Empty Book
            return (byte)0x4A;
        case SessionHighBid:  // Session High Bid
            return (byte)0x4E;
        case SessionLowOffer:  // Session Low Offer
            return (byte)0x4F;
        case FixingPrice:  // Fixing Price
            return (byte)0x57;
        default:
            break;
        }
        throw new RuntimeEncodingException( " unsupported encoding on MDEntryType for value " + val );
    }

    /**     * PostPend  Common Encoder File     *     * expected to contain methods used in hooks from model     */         @Override    public void setNanoStats( boolean nanoTiming ) {        _nanoStats = nanoTiming;    }    private       boolean         _nanoStats    =  true;             private       int             _idx          = 1;        private final ClientCancelRejectFactory _canRejFactory   = SuperpoolManager.instance().getFactory( ClientCancelRejectFactory.class, ClientCancelRejectImpl.class );    private final ClientRejectedFactory     _rejectedFactory = SuperpoolManager.instance().getFactory( ClientRejectedFactory.class,     ClientRejectedImpl.class );     public static final ZString ENCODE_REJ              = new ViewString( "ERJ" );    public static final ZString NONE                    = new ViewString( "NON" );    @Override    public Message unableToSend( Message msg, ZString errMsg ) {        switch( msg.getReusableType().getSubId() ) {        case EventIds.ID_NEWORDERSINGLE:            return rejectNewOrderSingle( (NewOrderSingle) msg, errMsg );        case EventIds.ID_NEWORDERACK:            break;        case EventIds.ID_TRADENEW:            break;        case EventIds.ID_CANCELREPLACEREQUEST:            return rejectCancelReplaceRequest( (CancelReplaceRequest) msg, errMsg );        case EventIds.ID_CANCELREQUEST:            return rejectCancelRequest( (CancelRequest) msg, errMsg );        }                return null;    }    private Message rejectNewOrderSingle( NewOrderSingle nos, ZString errMsg ) {        final ClientRejectedImpl reject = _rejectedFactory.get();        reject.setSrcEvent( nos );        reject.getExecIdForUpdate().copy( ENCODE_REJ ).append( nos.getClOrdId() ).append( ++_idx );        reject.getOrderIdForUpdate().setValue( NONE );        reject.setOrdRejReason( OrdRejReason.Other );        reject.getTextForUpdate().setValue( errMsg );        reject.setOrdStatus( OrdStatus.Rejected );        reject.setExecType( ExecType.Rejected );        reject.setCumQty( 0 );        reject.setAvgPx( 0.0 );        reject.setMessageHandler( nos.getMessageHandler() );        return reject;    }    private Message rejectCancelReplaceRequest( CancelReplaceRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelReplace );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private Message rejectCancelRequest( CancelRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelRequest );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private static final byte[] STATS       = "     [".getBytes();    private static final byte   STAT_DELIM  = ',';    private static final byte   STAT_END    = ']';    public void addStats( ReusableString outBuf, Message msg, long time ) { /* nothing */ }    public void logStats() { /* nothing */ }    public void logLastMsg() { /* nothing */ }    @Override    public void encodeStartPacket( final SBEPacketHeader h ) {        if ( _debug ) _dump.append( "\nField: " ).append( "packetSeqNum" ).append( " : " );        _builder.encodeUInt( h._packetSeqNum );        if ( _debug ) _dump.append( "\nField: " ).append( "sendTimeNanos" ).append( " : " );        _builder.encodeULong( h._sendTimeNanos );    }}
