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
import com.rr.core.codec.AbstractBinaryDecoder;
import com.rr.core.lang.*;
import com.rr.core.model.*;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.internal.type.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.BinaryDecodeBuilder;
import com.rr.core.codec.binary.DebugBinaryDecodeBuilder;
import com.rr.model.generated.internal.events.factory.*;
import com.rr.model.generated.internal.events.impl.*;
import com.rr.model.generated.internal.events.interfaces.*;
import com.rr.model.generated.internal.type.*;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.core.codec.binary.sbe.SBEPacketHeader;

@SuppressWarnings( "unused" )

public final class CMESimpleBinaryDecoder extends AbstractBinaryDecoder implements com.rr.core.codec.binary.sbe.SBEDecoder {

   // Attrs
    private static final short      MSG_MDIncrementalRefreshReal = 1;
    private static final short      MSG_MDIncrementalRefreshTrade = 2;
    private static final short      MSG_MDIncrementalRefreshVolume = 3;
    private static final short      MSG_MDSecurityStatus = 4;
    private static final short      MSG_MDSnapshotRefresh = 5;
    private static final short      MSG_MDIncrementalRefreshStats = 7;
    private static final short      MSG_MDIncrementalRefreshImplied = 8;
    private static final short      MSG_MDInstrumentDefinition = 9;

    private boolean _debug = false;

    private BinaryDecodeBuilder _builder;

    private       short _msgType;
    private final byte                        _protocolVersion;
    private       int                         _msgStatedLen;
    private final ViewString                  _lookup = new ViewString();
    private final ReusableString _dump  = new ReusableString(256);

    // dict var holders for conditional mappings and fields with no corresponding event entry .. useful for hooks
    private       int                         _sendingTime;
    private       int                         _timeBracket;
    private       byte                        _matchEventStartIndicator;
    private       byte                        _noMDEntries;
    private       byte                        _mDUpdateAction;
    private       byte                        _mDEntryType;
    private       int                         _securityID;
    private       int                         _rtpSeq;
    private       double                      _mDEntryPx;
    private       int                         _mDEntrySize;
    private       int                         _numberOfOrders;
    private       byte                        _mDPriceLevel;
    private       int                         _tradeID;
    private       byte                        _aggressorSide;
    private       byte                        _mDEntryTypeTrade;
    private       byte                        _mDEntryTypeVol;
    private       int                         _tradeDate;
    private       ReusableString              _securityGroup = new ReusableString(30);
    private       ReusableString              _asset = new ReusableString(30);
    private       byte                        _securityTradingStatus;
    private       byte                        _haltReason;
    private       byte                        _securityTradingEvent;
    private       byte                        _totNumReports;
    private       byte                        _securityUpdateAction;
    private       int                         _lastUpdateTime;
    private       ReusableString              _applID = new ReusableString(30);
    private       byte                        _marketSegmentID;
    private       ReusableString              _symbol = new ReusableString(30);
    private       int                         _maturityMonthYear;
    private       ReusableString              _securityType = new ReusableString(30);
    private       ReusableString              _cFICode = new ReusableString(30);
    private       ReusableString              _securitySubType = new ReusableString(30);
    private       byte                        _userDefinedInstrument;
    private       byte                        _underlyingProdcut;
    private       ReusableString              _securityExchange = new ReusableString(30);
    private       double                      _strikePrice;
    private       ReusableString              _strikeCurrency = new ReusableString(30);
    private       ReusableString              _currency = new ReusableString(30);
    private       ReusableString              _settlCurrency = new ReusableString(30);
    private       double                      _minCabPrice;
    private       double                      _priceRatio;
    private       byte                        _matchAlgorithm;
    private       int                         _minTradeVol;
    private       int                         _maxTradeVol;
    private       double                      _minPriceIncrement;
    private       double                      _minPriceIncrementAmount;
    private       double                      _displayFactor;
    private       byte                        _tickRule;
    private       byte                        _mainFraction;
    private       byte                        _subFraction;
    private       byte                        _priceDisplayFormat;
    private       byte                        _contractMultiplierUnit;
    private       byte                        _flowScheduleType;
    private       byte                        _contractMultiplier;
    private       ReusableString              _unitOfMeasure = new ReusableString(30);
    private       double                      _unitOfMeasureQty;
    private       int                         _decayQty;
    private       int                         _decayStartDate;
    private       int                         _originalContractSize;
    private       double                      _highLimitPrice;
    private       double                      _lowLimitPrice;
    private       double                      _maxPriceVariation;
    private       double                      _tradingReferencePrice;
    private       byte                        _settlPriceType;
    private       int                         _openInterestQty;
    private       int                         _clearedVolume;
    private       byte                        _noUnderlyings;
    private       byte                        _noLegs;
    private       byte                        _noMdFeedTypes;
    private       byte                        _noEvents;
    private       byte                        _noInstAttrib;
    private       byte                        _noLotTypeRules;
    private       ReusableString              _underlyingSymbol = new ReusableString(30);
    private       int                         _underlyingSecurityID;
    private       int                         _legSecurityID;
    private       byte                        _legSide;
    private       byte                        _legRatioQty;
    private       double                      _legPrice;
    private       double                      _legOptionDelta;
    private       ReusableString              _mDFeedType = new ReusableString(30);
    private       byte                        _marketDepth;
    private       byte                        _eventType;
    private       int                         _eventTime;
    private       byte                        _instAttribType;
    private       int                         _instAttribValue;
    private       byte                        _lotType;
    private       long                        _minLotSize;

   // Pools

    private final SuperPool<MDEntryImpl> _mDEntryPool = SuperpoolManager.instance().getSuperPool( MDEntryImpl.class );
    private final MDEntryFactory _mDEntryFactory = new MDEntryFactory( _mDEntryPool );

    private final SuperPool<MDIncRefreshImpl> _mDIncRefreshPool = SuperpoolManager.instance().getSuperPool( MDIncRefreshImpl.class );
    private final MDIncRefreshFactory _mDIncRefreshFactory = new MDIncRefreshFactory( _mDIncRefreshPool );

    private final SuperPool<SecurityStatusImpl> _securityStatusPool = SuperpoolManager.instance().getSuperPool( SecurityStatusImpl.class );
    private final SecurityStatusFactory _securityStatusFactory = new SecurityStatusFactory( _securityStatusPool );

    private final SuperPool<MDSnapEntryImpl> _mDSnapEntryPool = SuperpoolManager.instance().getSuperPool( MDSnapEntryImpl.class );
    private final MDSnapEntryFactory _mDSnapEntryFactory = new MDSnapEntryFactory( _mDSnapEntryPool );

    private final SuperPool<MDSnapshotFullRefreshImpl> _mDSnapshotFullRefreshPool = SuperpoolManager.instance().getSuperPool( MDSnapshotFullRefreshImpl.class );
    private final MDSnapshotFullRefreshFactory _mDSnapshotFullRefreshFactory = new MDSnapshotFullRefreshFactory( _mDSnapshotFullRefreshPool );

    private final SuperPool<SecDefLegImpl> _secDefLegPool = SuperpoolManager.instance().getSuperPool( SecDefLegImpl.class );
    private final SecDefLegFactory _secDefLegFactory = new SecDefLegFactory( _secDefLegPool );

    private final SuperPool<SDFeedTypeImpl> _sDFeedTypePool = SuperpoolManager.instance().getSuperPool( SDFeedTypeImpl.class );
    private final SDFeedTypeFactory _sDFeedTypeFactory = new SDFeedTypeFactory( _sDFeedTypePool );

    private final SuperPool<SecDefEventsImpl> _secDefEventsPool = SuperpoolManager.instance().getSuperPool( SecDefEventsImpl.class );
    private final SecDefEventsFactory _secDefEventsFactory = new SecDefEventsFactory( _secDefEventsPool );

    private final SuperPool<SecurityDefinitionImpl> _securityDefinitionPool = SuperpoolManager.instance().getSuperPool( SecurityDefinitionImpl.class );
    private final SecurityDefinitionFactory _securityDefinitionFactory = new SecurityDefinitionFactory( _securityDefinitionPool );


   // Constructors
    public CMESimpleBinaryDecoder() {
        super();
        setBuilder();
        _protocolVersion = (byte)'1';
    }

   // decode methods
    @Override
    protected final int getCurrentIndex() {
        return _builder.getCurrentIndex();
    }

    @Override
    protected BinaryDecodeBuilder getBuilder() {
        return _builder;
    }

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
        _builder = (_debug) ? new DebugBinaryDecodeBuilder<com.rr.codec.emea.exchange.cme.sbe.SBEDecodeBuilderImpl>( _dump, new com.rr.codec.emea.exchange.cme.sbe.SBEDecodeBuilderImpl() )
                            : new com.rr.codec.emea.exchange.cme.sbe.SBEDecodeBuilderImpl();
    }

    @Override
    protected final Message doMessageDecode() {
        _builder.setMaxIdx( _maxIdx );

        switch( _msgType ) {
        case MSG_MDIncrementalRefreshReal:
            return decodeMDIncrementalRefreshReal();
        case MSG_MDIncrementalRefreshTrade:
            return decodeMDIncrementalRefreshTrade();
        case MSG_MDIncrementalRefreshVolume:
            return decodeMDIncrementalRefreshVolume();
        case MSG_MDSecurityStatus:
            return decodeMDSecurityStatus();
        case MSG_MDSnapshotRefresh:
            return decodeMDSnapshotRefresh();
        case MSG_MDIncrementalRefreshStats:
            return decodeMDIncrementalRefreshStats();
        case MSG_MDIncrementalRefreshImplied:
            return decodeMDIncrementalRefreshImplied();
        case MSG_MDInstrumentDefinition:
            return decodeMDInstrumentDefinition();
        case 6:
            break;
        }
        return null;
    }

    private final Message decodeMDIncrementalRefreshReal() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MDIncrementalRefreshReal" ).append( " : " );
        }

        final MDIncRefreshImpl msg = _mDIncRefreshFactory.get();
        final int startRootBlockIdx = _builder.getCurrentIndex();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _timeBracket = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _matchEventStartIndicator = _builder.decodeByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        msg.setNoMDEntries( _builder.decodeUByte() & 0xFF );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = _curMsgRootBlockLen - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.skip( rootBytesToSkip );

        {
             // CHECKPOINT: repeatingGroup type MDEntry
            MDEntryImpl tmpMDEntriesReal;
            MDEntryImpl lastMDEntriesReal = null;
            int counterMDEntriesReal = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesReal ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                tmpMDEntriesReal = _mDEntryFactory.get();
                if ( lastMDEntriesReal == null ) {
                    msg.setMDEntries( tmpMDEntriesReal );
                } else {
                    lastMDEntriesReal.setNext( tmpMDEntriesReal );
                }
                lastMDEntriesReal = tmpMDEntriesReal;

                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                tmpMDEntriesReal.setMdUpdateAction( transformMDUpdateAction( _builder.decodeByte() ) );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryType" ).append( " : " );
                tmpMDEntriesReal.setMdEntryType( transformMDEntryType( _builder.decodeByte() ) );

                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                tmpMDEntriesReal.setSecurityID( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                tmpMDEntriesReal.setRepeatSeq( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                tmpMDEntriesReal.setMdEntryPx( _builder.decodeDecimal() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                tmpMDEntriesReal.setMdEntrySize( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "NumberOfOrders" ).append( " : " );
                tmpMDEntriesReal.setNumberOfOrders( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDPriceLevel" ).append( " : " );
                tmpMDEntriesReal.setMdPriceLevel( _builder.decodeByte() );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 27 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.skip( bytesToSkip );
            }
        }

        _builder.end();
        return msg;
    }

    private final Message decodeMDIncrementalRefreshTrade() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MDIncrementalRefreshTrade" ).append( " : " );
        }

        final MDIncRefreshImpl msg = _mDIncRefreshFactory.get();
        final int startRootBlockIdx = _builder.getCurrentIndex();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _timeBracket = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _matchEventStartIndicator = _builder.decodeByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        msg.setNoMDEntries( _builder.decodeUByte() & 0xFF );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = _curMsgRootBlockLen - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.skip( rootBytesToSkip );

        {
             // CHECKPOINT: repeatingGroup type MDEntry
            MDEntryImpl tmpMDEntriesTrade;
            MDEntryImpl lastMDEntriesTrade = null;
            int counterMDEntriesTrade = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesTrade ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                tmpMDEntriesTrade = _mDEntryFactory.get();
                if ( lastMDEntriesTrade == null ) {
                    msg.setMDEntries( tmpMDEntriesTrade );
                } else {
                    lastMDEntriesTrade.setNext( tmpMDEntriesTrade );
                }
                lastMDEntriesTrade = tmpMDEntriesTrade;

                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                tmpMDEntriesTrade.setMdUpdateAction( transformMDUpdateAction( _builder.decodeByte() ) );

                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                tmpMDEntriesTrade.setSecurityID( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                tmpMDEntriesTrade.setRepeatSeq( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                tmpMDEntriesTrade.setMdEntryPx( _builder.decodeDecimal() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                tmpMDEntriesTrade.setMdEntrySize( _builder.decodeInt() );
                if ( _debug ) _dump.append( "\nField: " ).append( "TradeID" ).append( " : " );
                _tradeID = _builder.decodeInt();

                if ( _debug ) _dump.append( "\nField: " ).append( "NumberOfOrders" ).append( " : " );
                tmpMDEntriesTrade.setNumberOfOrders( _builder.decodeInt() );
                if ( _debug ) _dump.append( "\nField: " ).append( "AggressorSide" ).append( " : " );
                _aggressorSide = _builder.decodeByte();
                tmpMDEntriesTrade.setMdEntryType( MDEntryType.Trade );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 30 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.skip( bytesToSkip );
            }
        }

        _builder.end();
        return msg;
    }

    private final Message decodeMDIncrementalRefreshVolume() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MDIncrementalRefreshVolume" ).append( " : " );
        }

        final MDIncRefreshImpl msg = _mDIncRefreshFactory.get();
        final int startRootBlockIdx = _builder.getCurrentIndex();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _timeBracket = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _matchEventStartIndicator = _builder.decodeByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        msg.setNoMDEntries( _builder.decodeUByte() & 0xFF );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = _curMsgRootBlockLen - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.skip( rootBytesToSkip );

        {
             // CHECKPOINT: repeatingGroup type MDEntry
            MDEntryImpl tmpMDEntriesVolume;
            MDEntryImpl lastMDEntriesVolume = null;
            int counterMDEntriesVolume = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesVolume ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                tmpMDEntriesVolume = _mDEntryFactory.get();
                if ( lastMDEntriesVolume == null ) {
                    msg.setMDEntries( tmpMDEntriesVolume );
                } else {
                    lastMDEntriesVolume.setNext( tmpMDEntriesVolume );
                }
                lastMDEntriesVolume = tmpMDEntriesVolume;

                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                tmpMDEntriesVolume.setMdUpdateAction( transformMDUpdateAction( _builder.decodeByte() ) );

                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                tmpMDEntriesVolume.setSecurityID( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                tmpMDEntriesVolume.setRepeatSeq( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                tmpMDEntriesVolume.setMdEntrySize( _builder.decodeInt() );
                tmpMDEntriesVolume.setMdEntryType( MDEntryType.TradeVolume );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 13 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.skip( bytesToSkip );
            }
        }

        _builder.end();
        return msg;
    }

    private final Message decodeMDSecurityStatus() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MDSecurityStatus" ).append( " : " );
        }

        final SecurityStatusImpl msg = _securityStatusFactory.get();
        final int startRootBlockIdx = _builder.getCurrentIndex();
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _timeBracket = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "TradeDate" ).append( " : " );
        msg.setTradeDate( _builder.decodeUShort() );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityGroup" ).append( " : " );
        _builder.decodeZStringFixedWidth( _securityGroup, 6 );
        if ( _debug ) _dump.append( "\nField: " ).append( "Asset" ).append( " : " );
        _builder.decodeZStringFixedWidth( _asset, 6 );

        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
        msg.setSecurityID( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityTradingStatus" ).append( " : " );
        msg.setSecurityTradingStatus( transformSecurityTradingStatus( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "HaltReason" ).append( " : " );
        msg.setHaltReason( _builder.decodeByte() );

        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityTradingEvent" ).append( " : " );
        msg.setSecurityTradingEvent( _builder.decodeByte() );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = _curMsgRootBlockLen - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.skip( rootBytesToSkip );
        _builder.end();
        return msg;
    }

    private final Message decodeMDSnapshotRefresh() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MDSnapshotRefresh" ).append( " : " );
        }

        final MDSnapshotFullRefreshImpl msg = _mDSnapshotFullRefreshFactory.get();
        final int startRootBlockIdx = _builder.getCurrentIndex();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _timeBracket = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _matchEventStartIndicator = _builder.decodeByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        msg.setNoMDEntries( _builder.decodeUByte() & 0xFF );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = _curMsgRootBlockLen - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.skip( rootBytesToSkip );

        {
             // CHECKPOINT: repeatingGroup type MDSnapEntry
            MDSnapEntryImpl tmpMDEntriesSnap;
            MDSnapEntryImpl lastMDEntriesSnap = null;
            int counterMDEntriesSnap = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesSnap ; ++i ) { 
                tmpMDEntriesSnap = _mDSnapEntryFactory.get();
                if ( lastMDEntriesSnap == null ) {
                    msg.setMDEntries( tmpMDEntriesSnap );
                } else {
                    lastMDEntriesSnap.setNext( tmpMDEntriesSnap );
                }
                lastMDEntriesSnap = tmpMDEntriesSnap;

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryType" ).append( " : " );
                tmpMDEntriesSnap.setMdEntryType( transformMDEntryType( _builder.decodeByte() ) );
                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                _securityID = _builder.decodeInt();
                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                _rtpSeq = _builder.decodeInt();

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                tmpMDEntriesSnap.setMdEntryPx( _builder.decodeDecimal() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                tmpMDEntriesSnap.setMdEntrySize( _builder.decodeInt() );
                if ( _debug ) _dump.append( "\nField: " ).append( "NumberOfOrders" ).append( " : " );
                _numberOfOrders = _builder.decodeInt();

                if ( _debug ) _dump.append( "\nField: " ).append( "MDPriceLevel" ).append( " : " );
                tmpMDEntriesSnap.setMdPriceLevel( _builder.decodeByte() );

                if ( _debug ) _dump.append( "\nField: " ).append( "fillerEntry" ).append( " : " );
                _builder.skip( 27 );    // skip OpenCloseSettlFlag(1) + TradingReferenceDate(2) + HighLimitPrice(8) + LowLimitPrice(8) + MaxPriceVariation(8)
            }
        }

        _builder.end();
        return msg;
    }

    private final Message decodeMDIncrementalRefreshStats() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MDIncrementalRefreshStats" ).append( " : " );
        }

        final MDIncRefreshImpl msg = _mDIncRefreshFactory.get();
        final int startRootBlockIdx = _builder.getCurrentIndex();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _timeBracket = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _matchEventStartIndicator = _builder.decodeByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        msg.setNoMDEntries( _builder.decodeUByte() & 0xFF );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = _curMsgRootBlockLen - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.skip( rootBytesToSkip );

        {
             // CHECKPOINT: repeatingGroup type MDEntry
            MDEntryImpl tmpMDEntriesStats;
            MDEntryImpl lastMDEntriesStats = null;
            int counterMDEntriesStats = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesStats ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                tmpMDEntriesStats = _mDEntryFactory.get();
                if ( lastMDEntriesStats == null ) {
                    msg.setMDEntries( tmpMDEntriesStats );
                } else {
                    lastMDEntriesStats.setNext( tmpMDEntriesStats );
                }
                lastMDEntriesStats = tmpMDEntriesStats;

                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                tmpMDEntriesStats.setMdUpdateAction( transformMDUpdateAction( _builder.decodeByte() ) );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryType" ).append( " : " );
                tmpMDEntriesStats.setMdEntryType( transformMDEntryType( _builder.decodeByte() ) );

                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                tmpMDEntriesStats.setSecurityID( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                tmpMDEntriesStats.setRepeatSeq( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                tmpMDEntriesStats.setMdEntryPx( _builder.decodeDecimal() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                tmpMDEntriesStats.setMdEntrySize( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "fillerEntry" ).append( " : " );
                _builder.skip( 27 );    // skip OpenCloseSettlFlag(1) + TradingReferenceDate(2) + HighLimitPrice(8) + LowLimitPrice(8) + MaxPriceVariation(8)

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 49 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.skip( bytesToSkip );
            }
        }

        _builder.end();
        return msg;
    }

    private final Message decodeMDIncrementalRefreshImplied() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MDIncrementalRefreshImplied" ).append( " : " );
        }

        final MDIncRefreshImpl msg = _mDIncRefreshFactory.get();
        final int startRootBlockIdx = _builder.getCurrentIndex();

        if ( _debug ) _dump.append( "\nField: " ).append( "sendingTime" ).append( " : " );
        msg.setSendingTime( _builder.decodeTimestampUTC() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TimeBracket" ).append( " : " );
        _timeBracket = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchEventStartIndicator" ).append( " : " );
        _matchEventStartIndicator = _builder.decodeByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "noMDEntries" ).append( " : " );
        msg.setNoMDEntries( _builder.decodeUByte() & 0xFF );

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = _curMsgRootBlockLen - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.skip( rootBytesToSkip );

        {
             // CHECKPOINT: repeatingGroup type MDEntry
            MDEntryImpl tmpMDEntriesImplied;
            MDEntryImpl lastMDEntriesImplied = null;
            int counterMDEntriesImplied = msg.getNoMDEntries();
            for( int i=0 ; i < counterMDEntriesImplied ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                tmpMDEntriesImplied = _mDEntryFactory.get();
                if ( lastMDEntriesImplied == null ) {
                    msg.setMDEntries( tmpMDEntriesImplied );
                } else {
                    lastMDEntriesImplied.setNext( tmpMDEntriesImplied );
                }
                lastMDEntriesImplied = tmpMDEntriesImplied;

                if ( _debug ) _dump.append( "\nField: " ).append( "MDUpdateAction" ).append( " : " );
                tmpMDEntriesImplied.setMdUpdateAction( transformMDUpdateAction( _builder.decodeByte() ) );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryType" ).append( " : " );
                tmpMDEntriesImplied.setMdEntryType( transformMDEntryType( _builder.decodeByte() ) );

                if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
                tmpMDEntriesImplied.setSecurityID( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "RtpSeq" ).append( " : " );
                tmpMDEntriesImplied.setRepeatSeq( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntryPx" ).append( " : " );
                tmpMDEntriesImplied.setMdEntryPx( _builder.decodeDecimal() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDEntrySize" ).append( " : " );
                tmpMDEntriesImplied.setMdEntrySize( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "MDPriceLevel" ).append( " : " );
                tmpMDEntriesImplied.setMdPriceLevel( _builder.decodeByte() );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 23 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.skip( bytesToSkip );
            }
        }

        _builder.end();
        return msg;
    }

    private final Message decodeMDInstrumentDefinition() {
        if ( _debug ) {
            _dump.append( "\nKnown Message : " ).append( "MDInstrumentDefinition" ).append( " : " );
        }

        final SecurityDefinitionImpl msg = _securityDefinitionFactory.get();
        final int startRootBlockIdx = _builder.getCurrentIndex();

        if ( _debug ) _dump.append( "\nField: " ).append( "TotNumReports" ).append( " : " );
        msg.setTotNumReports( _builder.decodeUByte() & 0xFF );

        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityUpdateAction" ).append( " : " );
        msg.setSecurityUpdateAction( SecurityUpdateAction.getVal( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "LastUpdateTime" ).append( " : " );
        msg.setLastUpdateTime( _builder.decodeTimestampUTC() );

        if ( _debug ) _dump.append( "\nField: " ).append( "ApplID" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getApplIDForUpdate(), 5 );
        if ( _debug ) _dump.append( "\nField: " ).append( "MarketSegmentID" ).append( " : " );
        _marketSegmentID = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "Symbol" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getSymbolForUpdate(), 20 );

        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityID" ).append( " : " );
        msg.setSecurityID( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "MaturityMonthYear" ).append( " : " );
        msg.setMaturityMonthYear( _builder.decodeUShort() );

        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityGroup" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getSecurityGroupForUpdate(), 6 );
        if ( _debug ) _dump.append( "\nField: " ).append( "Asset" ).append( " : " );
        _builder.decodeZStringFixedWidth( _asset, 6 );

        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityType" ).append( " : " );
        msg.setSecurityType( SecurityType.getVal( _binaryMsg, _builder.getCurrentIndex(), 6) );
        _builder.skip( 6);

        if ( _debug ) _dump.append( "\nField: " ).append( "CFICode" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getCFICodeForUpdate(), 6 );
        if ( _debug ) _dump.append( "\nField: " ).append( "SecuritySubType" ).append( " : " );
        _builder.decodeZStringFixedWidth( _securitySubType, 5 );
        if ( _debug ) _dump.append( "\nField: " ).append( "UserDefinedInstrument" ).append( " : " );
        _userDefinedInstrument = _builder.decodeChar();

        if ( _debug ) _dump.append( "\nField: " ).append( "UnderlyingProdcut" ).append( " : " );
        _builder.decodeString( msg.getUnderlyingProductForUpdate() );

        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityExchange" ).append( " : " );
        _builder.decodeZStringFixedWidth( msg.getSecurityExchangeForUpdate(), 4 );

        if ( _debug ) _dump.append( "\nField: " ).append( "SecurityTradingStatus" ).append( " : " );
        msg.setSecurityTradingStatus( transformSecurityTradingStatus( _builder.decodeByte() ) );

        if ( _debug ) _dump.append( "\nField: " ).append( "StrikePrice" ).append( " : " );
        msg.setStrikePrice( _builder.decodeDecimal() );

        if ( _debug ) _dump.append( "\nField: " ).append( "StrikeCurrency" ).append( " : " );
        msg.setStrikeCurrency( Currency.getVal( _binaryMsg, _builder.getCurrentIndex(), 3) );
        _builder.skip( 3);

        if ( _debug ) _dump.append( "\nField: " ).append( "Currency" ).append( " : " );
        msg.setCurrency( Currency.getVal( _binaryMsg, _builder.getCurrentIndex(), 3) );
        _builder.skip( 3);

        if ( _debug ) _dump.append( "\nField: " ).append( "SettlCurrency" ).append( " : " );
        msg.setSettlCurrency( Currency.getVal( _binaryMsg, _builder.getCurrentIndex(), 3) );
        _builder.skip( 3);
        if ( _debug ) _dump.append( "\nField: " ).append( "MinCabPrice" ).append( " : " );
        _minCabPrice = _builder.decodePrice();

        if ( _debug ) _dump.append( "\nField: " ).append( "PriceRatio" ).append( " : " );
        msg.setPriceRatio( _builder.decodeDecimal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "MatchAlgorithm" ).append( " : " );
        _matchAlgorithm = _builder.decodeChar();

        if ( _debug ) _dump.append( "\nField: " ).append( "MinTradeVol" ).append( " : " );
        msg.setMinTradeVol( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "MaxTradeVol" ).append( " : " );
        msg.setMaxTradeVol( _builder.decodeInt() );

        if ( _debug ) _dump.append( "\nField: " ).append( "MinPriceIncrement" ).append( " : " );
        msg.setMinPriceIncrement( _builder.decodeDecimal() );

        if ( _debug ) _dump.append( "\nField: " ).append( "MinPriceIncrementAmount" ).append( " : " );
        msg.setMinPriceIncrementAmount( _builder.decodeDecimal() );

        if ( _debug ) _dump.append( "\nField: " ).append( "DisplayFactor" ).append( " : " );
        msg.setDisplayFactor( _builder.decodeDecimal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "TickRule" ).append( " : " );
        _tickRule = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "MainFraction" ).append( " : " );
        _mainFraction = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "SubFraction" ).append( " : " );
        _subFraction = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "PriceDisplayFormat" ).append( " : " );
        _priceDisplayFormat = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "ContractMultiplierUnit" ).append( " : " );
        msg.setContractMultiplierType( _builder.decodeUByte() & 0xFF );
        if ( _debug ) _dump.append( "\nField: " ).append( "FlowScheduleType" ).append( " : " );
        _flowScheduleType = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "ContractMultiplier" ).append( " : " );
        msg.setContractMultiplier( _builder.decodeUByte() & 0xFF );
        if ( _debug ) _dump.append( "\nField: " ).append( "UnitOfMeasure" ).append( " : " );
        _builder.decodeZStringFixedWidth( _unitOfMeasure, 30 );
        if ( _debug ) _dump.append( "\nField: " ).append( "UnitOfMeasureQty" ).append( " : " );
        _unitOfMeasureQty = _builder.decodePrice();
        if ( _debug ) _dump.append( "\nField: " ).append( "DecayQty" ).append( " : " );
        _decayQty = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "DecayStartDate" ).append( " : " );
        _decayStartDate = _builder.decodeUShort();
        if ( _debug ) _dump.append( "\nField: " ).append( "OriginalContractSize" ).append( " : " );
        _originalContractSize = _builder.decodeInt();

        if ( _debug ) _dump.append( "\nField: " ).append( "HighLimitPrice" ).append( " : " );
        msg.setHighLimitPx( _builder.decodeDecimal() );

        if ( _debug ) _dump.append( "\nField: " ).append( "LowLimitPrice" ).append( " : " );
        msg.setLowLimitPx( _builder.decodeDecimal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "MaxPriceVariation" ).append( " : " );
        _maxPriceVariation = _builder.decodePrice();

        if ( _debug ) _dump.append( "\nField: " ).append( "TradingReferencePrice" ).append( " : " );
        msg.setTradingReferencePrice( _builder.decodeDecimal() );
        if ( _debug ) _dump.append( "\nField: " ).append( "SettlPriceType" ).append( " : " );
        _settlPriceType = _builder.decodeByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "OpenInterestQty" ).append( " : " );
        msg.setOpenInterestQty( _builder.decodeInt() );
        if ( _debug ) _dump.append( "\nField: " ).append( "ClearedVolume" ).append( " : " );
        _clearedVolume = _builder.decodeInt();
        if ( _debug ) _dump.append( "\nField: " ).append( "NoUnderlyings" ).append( " : " );
        _noUnderlyings = _builder.decodeUByte();

        if ( _debug ) _dump.append( "\nField: " ).append( "NoLegs" ).append( " : " );
        msg.setNoLegs( _builder.decodeUByte() & 0xFF );

        if ( _debug ) _dump.append( "\nField: " ).append( "NoMdFeedTypes" ).append( " : " );
        msg.setNoSDFeedTypes( _builder.decodeUByte() & 0xFF );

        if ( _debug ) _dump.append( "\nField: " ).append( "NoEvents" ).append( " : " );
        msg.setNoEvents( _builder.decodeUByte() & 0xFF );
        if ( _debug ) _dump.append( "\nField: " ).append( "NoInstAttrib" ).append( " : " );
        _noInstAttrib = _builder.decodeUByte();
        if ( _debug ) _dump.append( "\nField: " ).append( "NoLotTypeRules" ).append( " : " );
        _noLotTypeRules = _builder.decodeUByte();

        final int endRootBlockIdx = _builder.getCurrentIndex();
        final int rootBytesToSkip = _curMsgRootBlockLen - (endRootBlockIdx - startRootBlockIdx);
        if ( rootBytesToSkip > 0 ) _builder.skip( rootBytesToSkip );

        {
            final int bytesToSkip = _noUnderlyings * 24;
            _builder.skip( bytesToSkip ); // SKIPPING repeating group : UnderlyingsGrp
        }


        {
             // CHECKPOINT: repeatingGroup type SecDefLeg
            SecDefLegImpl tmpLegsGrp;
            SecDefLegImpl lastLegsGrp = null;
            int counterLegsGrp = msg.getNoLegs();
            for( int i=0 ; i < counterLegsGrp ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                tmpLegsGrp = _secDefLegFactory.get();
                if ( lastLegsGrp == null ) {
                    msg.setLegs( tmpLegsGrp );
                } else {
                    lastLegsGrp.setNext( tmpLegsGrp );
                }
                lastLegsGrp = tmpLegsGrp;

                if ( _debug ) _dump.append( "\nField: " ).append( "LegSecurityID" ).append( " : " );
                tmpLegsGrp.setLegSecurityID( _builder.decodeInt() );

                if ( _debug ) _dump.append( "\nField: " ).append( "LegSide" ).append( " : " );
                tmpLegsGrp.setLegSide( transformSide( _builder.decodeByte() ) );

                if ( _debug ) _dump.append( "\nField: " ).append( "LegRatioQty" ).append( " : " );
                tmpLegsGrp.setLegRatioQty( _builder.decodeByte() );
                if ( _debug ) _dump.append( "\nField: " ).append( "LegPrice" ).append( " : " );
                _legPrice = _builder.decodePrice();
                if ( _debug ) _dump.append( "\nField: " ).append( "LegOptionDelta" ).append( " : " );
                _legOptionDelta = _builder.decodePrice();

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 22 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.skip( bytesToSkip );
            }
        }


        {
             // CHECKPOINT: repeatingGroup type SDFeedType
            SDFeedTypeImpl tmpMdFeedTypesGrp;
            SDFeedTypeImpl lastMdFeedTypesGrp = null;
            int counterMdFeedTypesGrp = msg.getNoSDFeedTypes();
            for( int i=0 ; i < counterMdFeedTypesGrp ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                tmpMdFeedTypesGrp = _sDFeedTypeFactory.get();
                if ( lastMdFeedTypesGrp == null ) {
                    msg.setSDFeedTypes( tmpMdFeedTypesGrp );
                } else {
                    lastMdFeedTypesGrp.setNext( tmpMdFeedTypesGrp );
                }
                lastMdFeedTypesGrp = tmpMdFeedTypesGrp;

                if ( _debug ) _dump.append( "\nField: " ).append( "MDFeedType" ).append( " : " );
                _builder.decodeZStringFixedWidth( tmpMdFeedTypesGrp.getFeedTypeForUpdate(), 3 );

                if ( _debug ) _dump.append( "\nField: " ).append( "MarketDepth" ).append( " : " );
                tmpMdFeedTypesGrp.setMarketDepth( _builder.decodeByte() );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 4 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.skip( bytesToSkip );
            }
        }


        {
             // CHECKPOINT: repeatingGroup type SecDefEvents
            SecDefEventsImpl tmpEventsGrp;
            SecDefEventsImpl lastEventsGrp = null;
            int counterEventsGrp = msg.getNoEvents();
            for( int i=0 ; i < counterEventsGrp ; ++i ) { 
                final int startBlockIdx = _builder.getCurrentIndex();
                tmpEventsGrp = _secDefEventsFactory.get();
                if ( lastEventsGrp == null ) {
                    msg.setEvents( tmpEventsGrp );
                } else {
                    lastEventsGrp.setNext( tmpEventsGrp );
                }
                lastEventsGrp = tmpEventsGrp;

                if ( _debug ) _dump.append( "\nField: " ).append( "EventType" ).append( " : " );
                tmpEventsGrp.setEventType( _builder.decodeByte() );

                if ( _debug ) _dump.append( "\nField: " ).append( "EventTime" ).append( " : " );
                tmpEventsGrp.setEventTime( _builder.decodeTimestampUTC() );

                final int endBlockIdx = _builder.getCurrentIndex();
                final int bytesToSkip = 9 - (endBlockIdx - startBlockIdx);
                if ( bytesToSkip > 0 ) _builder.skip( bytesToSkip );
            }
        }


        {
            final int bytesToSkip = _noInstAttrib * 5;
            _builder.skip( bytesToSkip ); // SKIPPING repeating group : InstAttribGrp
        }


        {
            final int bytesToSkip = _noLotTypeRules * 9;
            _builder.skip( bytesToSkip ); // SKIPPING repeating group : LotTypeRulesGrp
        }

        _builder.end();
        return msg;
    }


   // transform methods
    private static final SecurityTradingStatus[] _securityTradingStatusMap = new SecurityTradingStatus[26];
    private static final int    _securityTradingStatusIndexOffset = 2;
    static {
        for ( int i=0 ; i < _securityTradingStatusMap.length ; i++ ) {
             _securityTradingStatusMap[i] = SecurityTradingStatus.Unknown;
        }
         _securityTradingStatusMap[ (byte)0x02 - _securityTradingStatusIndexOffset ] = SecurityTradingStatus.TradingHalt;
         _securityTradingStatusMap[ (byte)0x04 - _securityTradingStatusIndexOffset ] = SecurityTradingStatus.NoOpenNoResume;
         _securityTradingStatusMap[ (byte)0x0E - _securityTradingStatusIndexOffset ] = SecurityTradingStatus.NewPriceIndication;
         _securityTradingStatusMap[ (byte)0x11 - _securityTradingStatusIndexOffset ] = SecurityTradingStatus.StartOfTradingSession;
         _securityTradingStatusMap[ (byte)0x12 - _securityTradingStatusIndexOffset ] = SecurityTradingStatus.EndOfSessionTradingUnavailable;
         _securityTradingStatusMap[ (byte)0x14 - _securityTradingStatusIndexOffset ] = SecurityTradingStatus.Invalid;
         _securityTradingStatusMap[ (byte)0x15 - _securityTradingStatusIndexOffset ] = SecurityTradingStatus.PreOpen;
         _securityTradingStatusMap[ (byte)0x1A - _securityTradingStatusIndexOffset ] = SecurityTradingStatus.NoOpenNoResume;
    }

    private SecurityTradingStatus transformSecurityTradingStatus( byte extVal ) {
        final int arrIdx = extVal - _securityTradingStatusIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _securityTradingStatusMap.length ) {
            return SecurityTradingStatus.Unknown;
        }
        SecurityTradingStatus intVal = _securityTradingStatusMap[ arrIdx ];
        return intVal;
    }

    private static final Side[] _sideMap = new Side[3];
    private static final int    _sideIndexOffset = 1;
    static {
        for ( int i=0 ; i < _sideMap.length ; i++ ) {
             _sideMap[i] = null;
        }
         _sideMap[ (byte)0x01 - _sideIndexOffset ] = Side.Buy;
         _sideMap[ (byte)0x02 - _sideIndexOffset ] = Side.Sell;
    }

    private Side transformSide( byte extVal ) {
        final int arrIdx = extVal - _sideIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _sideMap.length ) {
            throw new RuntimeDecodingException( " unsupported decoding on Side for value " + (char)extVal );
        }
        Side intVal = _sideMap[ arrIdx ];
        if ( intVal == null ) {
            throw new RuntimeDecodingException( " unsupported decoding on Side for value " + (char)extVal );
        }
        return intVal;
    }

    private static final MDUpdateAction[] _mDUpdateActionMap = new MDUpdateAction[7];
    private static final int    _mDUpdateActionIndexOffset = 0;
    static {
        for ( int i=0 ; i < _mDUpdateActionMap.length ; i++ ) {
             _mDUpdateActionMap[i] = MDUpdateAction.Unknown;
        }
         _mDUpdateActionMap[ (byte)0x00 - _mDUpdateActionIndexOffset ] = MDUpdateAction.New;
         _mDUpdateActionMap[ (byte)0x01 - _mDUpdateActionIndexOffset ] = MDUpdateAction.Change;
         _mDUpdateActionMap[ (byte)0x02 - _mDUpdateActionIndexOffset ] = MDUpdateAction.Delete;
         _mDUpdateActionMap[ (byte)0x03 - _mDUpdateActionIndexOffset ] = MDUpdateAction.DeleteThru;
         _mDUpdateActionMap[ (byte)0x04 - _mDUpdateActionIndexOffset ] = MDUpdateAction.DeleteFrom;
         _mDUpdateActionMap[ (byte)0x05 - _mDUpdateActionIndexOffset ] = MDUpdateAction.Overlay;
    }

    private MDUpdateAction transformMDUpdateAction( byte extVal ) {
        final int arrIdx = extVal - _mDUpdateActionIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _mDUpdateActionMap.length ) {
            return MDUpdateAction.Unknown;
        }
        MDUpdateAction intVal = _mDUpdateActionMap[ arrIdx ];
        return intVal;
    }

    private static final MDEntryType[] _mDEntryTypeMap = new MDEntryType[103];
    private static final int    _mDEntryTypeIndexOffset = 0;
    static {
        for ( int i=0 ; i < _mDEntryTypeMap.length ; i++ ) {
             _mDEntryTypeMap[i] = MDEntryType.Unknown;
        }
         _mDEntryTypeMap[ (byte)0x00 - _mDEntryTypeIndexOffset ] = MDEntryType.Bid;
         _mDEntryTypeMap[ (byte)0x01 - _mDEntryTypeIndexOffset ] = MDEntryType.Offer;
         _mDEntryTypeMap[ (byte)0x02 - _mDEntryTypeIndexOffset ] = MDEntryType.Trade;
         _mDEntryTypeMap[ (byte)0x04 - _mDEntryTypeIndexOffset ] = MDEntryType.OpeningPrice;
         _mDEntryTypeMap[ (byte)0x06 - _mDEntryTypeIndexOffset ] = MDEntryType.SettlementPrice;
         _mDEntryTypeMap[ (byte)0x07 - _mDEntryTypeIndexOffset ] = MDEntryType.TradingSessionHighPrice;
         _mDEntryTypeMap[ (byte)0x08 - _mDEntryTypeIndexOffset ] = MDEntryType.TradingSessionLowPrice;
         _mDEntryTypeMap[ (byte)0x42 - _mDEntryTypeIndexOffset ] = MDEntryType.TradeVolume;
         _mDEntryTypeMap[ (byte)0x43 - _mDEntryTypeIndexOffset ] = MDEntryType.OpenInterest;
         _mDEntryTypeMap[ (byte)0x45 - _mDEntryTypeIndexOffset ] = MDEntryType.SimulatedSellPrice;
         _mDEntryTypeMap[ (byte)0x46 - _mDEntryTypeIndexOffset ] = MDEntryType.SimulatedBuy;
         _mDEntryTypeMap[ (byte)0x4A - _mDEntryTypeIndexOffset ] = MDEntryType.EmptyBook;
         _mDEntryTypeMap[ (byte)0x4E - _mDEntryTypeIndexOffset ] = MDEntryType.SessionHighBid;
         _mDEntryTypeMap[ (byte)0x4F - _mDEntryTypeIndexOffset ] = MDEntryType.SessionLowOffer;
         _mDEntryTypeMap[ (byte)0x57 - _mDEntryTypeIndexOffset ] = MDEntryType.FixingPrice;
         _mDEntryTypeMap[ (byte)0x65 - _mDEntryTypeIndexOffset ] = MDEntryType.TradeVolume;
    }

    private MDEntryType transformMDEntryType( byte extVal ) {
        final int arrIdx = extVal - _mDEntryTypeIndexOffset;
        if ( arrIdx < 0 || arrIdx >= _mDEntryTypeMap.length ) {
            return MDEntryType.Unknown;
        }
        MDEntryType intVal = _mDEntryTypeMap[ arrIdx ];
        return intVal;
    }

    private       int _curMsgRootBlockLen;    private       int _schemaId;    private       int _curMsgSchemaVersion;    private final ReusableString        _destFixMsg         = new ReusableString();    private       int                   _subMsgId;    private       int                   _packetSeqNum;    public void decodeStartPacket( byte[] msg, int offset, int maxIdx, SBEPacketHeader h ) {        _builder.start( msg, offset, maxIdx );        h._packetSeqNum  =  _packetSeqNum  = _builder.decodeUInt();        h._sendTimeNanos = _builder.decodeULong();        _subMsgId = 0;    }    @Override    public final int parseHeader( final byte[] msg, final int offset, final int bytesRead ) {               _binaryMsg = msg;        _maxIdx = bytesRead + offset; // temp assign maxIdx to last data bytes in buffer        _offset = offset;        _builder.start( msg, offset, _maxIdx );                if ( bytesRead < 8 ) {            ReusableString copy = TLC.instance().getString();            if ( bytesRead == 0 )  {                copy.setValue( "{empty}" );            } else{                copy.setValue( msg, offset, bytesRead );            }            throw new RuntimeDecodingException( "SBE Messsage too small, len=" + bytesRead, copy );        } else if ( msg.length < _maxIdx ){            throwDecodeException( "Buffer too small for specified bytesRead=" + bytesRead + ",offset=" + offset + ", bufLen=" + msg.length );        }                _msgStatedLen        = _builder.decodeUShort();        _curMsgRootBlockLen  = _builder.decodeUShort();        _msgType             = _builder.decodeUShort(); // actually the templateId        _schemaId            = _builder.decodeUShort();         _curMsgSchemaVersion = _builder.decodeUShort();                 _maxIdx = _msgStatedLen + _offset;  // correctly assign maxIdx as last bytes of current message        if ( _maxIdx > _binaryMsg.length )  _maxIdx  = _binaryMsg.length;                ++_subMsgId;                return _msgStatedLen;    }    @Override    public void logLastMsg() {        _destFixMsg.reset();        _destFixMsg.append( "IN  [" ).append( _packetSeqNum ).append( "] [idx#" ).append( _subMsgId ).append( "] [t#" ).append( _schemaId ).append( "] ");        _destFixMsg.appendHEX( _builder.getBuffer(), _builder.getOffset(), _builder.getMaxIdx() );        _log.info( _destFixMsg );    }    @Override    public int getCurrentOffset() {        return _builder.getCurrentIndex();    }}
