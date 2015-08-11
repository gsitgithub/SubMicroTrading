/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.fix.codec;

import java.util.HashMap;
import java.util.Map;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.model.*;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.pool.SuperPool;
import com.rr.core.codec.FixEncoder;
import com.rr.core.codec.ReadableMDFastFixEncoder;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.model.internal.type.*;
import com.rr.model.generated.fix.model.defn.FixDictionaryMD44;
import com.rr.model.generated.internal.events.factory.*;
import com.rr.model.generated.internal.events.impl.*;
import com.rr.model.generated.internal.events.interfaces.*;
import com.rr.model.generated.internal.type.*;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.core.FullEventIds;

@SuppressWarnings( "unused" )

public final class MD44Encoder implements FixEncoder {

   // Member Vars
    private static final byte      MSG_Heartbeat = (byte)'0';
    private static final byte      MSG_Logon = (byte)'A';
    private static final byte      MSG_Logout = (byte)'5';
    private static final byte      MSG_SessionReject = (byte)'3';
    private static final byte      MSG_ResendRequest = (byte)'2';
    private static final byte      MSG_SequenceReset = (byte)'4';
    private static final byte      MSG_TestRequest = (byte)'1';
    private static final byte      MSG_TradingSessionStatus = (byte)'h';
    private static final byte      MSG_MDRequest = (byte)'V';
    private static final byte[]    MSG_MassInstrumentStateChange = "CO".getBytes();
    private static final byte      MSG_SecurityStatus = (byte)'f';
    private static final byte      MSG_SecurityDefinition = (byte)'d';
    private static final byte      MSG_MDIncRefresh = (byte)'X';
    private static final byte      MSG_MDSnapshotFullRefresh = (byte)'W';

    private final byte[]               _buf;
    private final byte                 _majorVersion;
    private final byte                 _minorVersion;
    private final com.rr.core.codec.ReadableMDFastFixEncoder _builder;

    private final ZString              _fixVersion;
    private       TimeZoneCalculator   _tzCalculator = new TimeZoneCalculator();
    private       SingleByteLookup     _sv;
    private       TwoByteLookup        _tv;
    private       MultiByteLookup      _mv;

   // Constructors
    public MD44Encoder( byte[] buf, int offset ) {
        this( FixVersion.MDFix4_4._major, FixVersion.MDFix4_4._minor, buf, offset );
    }

    public MD44Encoder( byte major, byte minor, byte[] buf, int offset ) {
        if ( buf.length < SizeType.MIN_ENCODE_BUFFER.getSize() ) {
            throw new RuntimeException( "Encode buffer too small only " + buf.length + ", min=" + SizeType.MIN_ENCODE_BUFFER.getSize() );
        }
        _buf = buf;
        _majorVersion = major;
        _minorVersion = minor;
        _builder = new com.rr.core.codec.ReadableMDFastFixEncoder( buf, offset, major, minor );
        _fixVersion   = new ViewString( "FIX." + major + "." + minor );
    }

    public MD44Encoder( byte major, byte minor, byte[] buf ) {
        this( major, minor, buf, 0 );
    }


   // encode methods

    @Override
    public final void encode( final Message msg ) {
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_HEARTBEAT:
            encodeHeartbeat( (Heartbeat) msg );
            break;
        case EventIds.ID_LOGON:
            encodeLogon( (Logon) msg );
            break;
        case EventIds.ID_LOGOUT:
            encodeLogout( (Logout) msg );
            break;
        case EventIds.ID_SESSIONREJECT:
            encodeSessionReject( (SessionReject) msg );
            break;
        case EventIds.ID_RESENDREQUEST:
            encodeResendRequest( (ResendRequest) msg );
            break;
        case EventIds.ID_SEQUENCERESET:
            encodeSequenceReset( (SequenceReset) msg );
            break;
        case EventIds.ID_TESTREQUEST:
            encodeTestRequest( (TestRequest) msg );
            break;
        case EventIds.ID_TRADINGSESSIONSTATUS:
            encodeTradingSessionStatus( (TradingSessionStatus) msg );
            break;
        case EventIds.ID_MDREQUEST:
            encodeMDRequest( (MDRequest) msg );
            break;
        case EventIds.ID_MASSINSTRUMENTSTATECHANGE:
            encodeMassInstrumentStateChange( (MassInstrumentStateChange) msg );
            break;
        case EventIds.ID_SECURITYSTATUS:
            encodeSecurityStatus( (SecurityStatus) msg );
            break;
        case EventIds.ID_SECURITYDEFINITION:
            encodeSecurityDefinition( (SecurityDefinition) msg );
            break;
        case EventIds.ID_MDINCREFRESH:
            encodeMDIncRefresh( (MDIncRefresh) msg );
            break;
        case EventIds.ID_MDSNAPSHOTFULLREFRESH:
            encodeMDSnapshotFullRefresh( (MDSnapshotFullRefresh) msg );
            break;
        case 26:
        case 29:
        case 31:
        case 32:
        case 33:
        case 34:
        case 35:
        case 36:
        case 37:
        case 38:
        case 39:
        case 40:
        case 41:
        case 42:
        case 43:
        case 44:
        case 45:
        case 46:
        case 47:
        case 48:
        case 49:
        case 50:
        case 51:
        case 52:
        case 53:
        case 54:
        case 55:
        case 56:
        case 57:
        case 58:
        case 59:
        case 60:
        case 61:
        case 62:
        case 63:
        case 64:
        case 65:
        case 66:
        case 67:
        case 68:
        case 70:
        case 71:
        case 72:
        case 73:
        case 74:
        case 75:
        case 76:
        case 77:
        case 78:
        case 82:
        case 83:
        default:
            _builder.start();
            break;
        }
    }

    @Override public final int getLength() { return _builder.getLength(); }
    @Override public final int getOffset() { return _builder.getOffset(); }


    public final void encodeHeartbeat( final Heartbeat msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Heartbeat );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeUTCTimestamp( FixDictionaryMD44.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionaryMD44.testReqID, msg.getTestReqID() );        // tag112
        _builder.encodeEnvelope();
    }

    public final void encodeLogon( final Logon msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Logon );
        _builder.encodeString( FixDictionaryMD44.SenderCompID, msg.getSenderCompId() );        // tag49
        _builder.encodeString( FixDictionaryMD44.TargetCompID, msg.getTargetCompId() );        // tag56
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeUTCTimestamp( FixDictionaryMD44.SendingTime, now );        // tag52
        final EncryptMethod tEncryptMethod = msg.getEncryptMethod();
        if ( tEncryptMethod != null ) _builder.encodeByte( FixDictionaryMD44.EncryptMethod, tEncryptMethod.getVal() );        // tag98
        _builder.encodeInt( FixDictionaryMD44.heartBtInt, msg.getHeartBtInt() );        // tag108
        _builder.encodeInt( FixDictionaryMD44.RawDataLen, msg.getRawDataLen() );        // tag95
        _builder.encodeString( FixDictionaryMD44.RawData, msg.getRawData() );        // tag96
        _builder.encodeBool( FixDictionaryMD44.ResetSeqNumFlag, msg.getResetSeqNumFlag() );        // tag141
        _builder.encodeInt( FixDictionaryMD44.NextExpectedMsgSeqNum, msg.getNextExpectedMsgSeqNum() );        // tag789
        _builder.encodeEnvelope();
    }

    public final void encodeLogout( final Logout msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_Logout );
        _builder.encodeString( FixDictionaryMD44.SenderCompID, msg.getSenderCompId() );        // tag49
        _builder.encodeString( FixDictionaryMD44.TargetCompID, msg.getTargetCompId() );        // tag56
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeUTCTimestamp( FixDictionaryMD44.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionaryMD44.Text, msg.getText() );        // tag58
        _builder.encodeEnvelope();
    }

    public final void encodeSessionReject( final SessionReject msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_SessionReject );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeUTCTimestamp( FixDictionaryMD44.SendingTime, now );        // tag52
        _builder.encodeInt( FixDictionaryMD44.RefSeqNum, msg.getRefSeqNum() );        // tag45
        _builder.encodeInt( FixDictionaryMD44.RefTagID, msg.getRefTagID() );        // tag371
        _builder.encodeString( FixDictionaryMD44.RefMsgType, msg.getRefMsgType() );        // tag372
        final SessionRejectReason tSessionRejectReason = msg.getSessionRejectReason();
        if ( tSessionRejectReason != null ) _builder.encodeTwoByte( FixDictionaryMD44.SessionRejectReason, tSessionRejectReason.getVal() );        // tag373
        _builder.encodeString( FixDictionaryMD44.Text, msg.getText() );        // tag58
        _builder.encodeEnvelope();
    }

    public final void encodeResendRequest( final ResendRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_ResendRequest );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeUTCTimestamp( FixDictionaryMD44.SendingTime, now );        // tag52
        _builder.encodeInt( FixDictionaryMD44.BeginSeqNo, msg.getBeginSeqNo() );        // tag7
        _builder.encodeInt( FixDictionaryMD44.EndSeqNo, msg.getEndSeqNo() );        // tag16
        _builder.encodeEnvelope();
    }

    public final void encodeSequenceReset( final SequenceReset msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_SequenceReset );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeUTCTimestamp( FixDictionaryMD44.SendingTime, now );        // tag52
        _builder.encodeBool( FixDictionaryMD44.GapFillFlag, msg.getGapFillFlag() );        // tag123
        _builder.encodeInt( FixDictionaryMD44.NewSeqNo, msg.getNewSeqNo() );        // tag36
        _builder.encodeEnvelope();
    }

    public final void encodeTestRequest( final TestRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_TestRequest );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeUTCTimestamp( FixDictionaryMD44.SendingTime, now );        // tag52
        _builder.encodeString( FixDictionaryMD44.testReqID, msg.getTestReqID() );        // tag112
        _builder.encodeEnvelope();
    }

    public final void encodeTradingSessionStatus( final TradingSessionStatus msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_TradingSessionStatus );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeUTCTimestamp( FixDictionaryMD44.SendingTime, now );        // tag52
        _builder.encodeInt( FixDictionaryMD44.marketSegmentId, msg.getMarketSegmentID() );        // tag1300
        final TradingSessionID tTradingSessionID = msg.getTradingSessionID();
        if ( tTradingSessionID != null ) _builder.encodeByte( FixDictionaryMD44.TradingSessionID, tTradingSessionID.getVal() );        // tag336
        final TradingSessionSubID tTradingSessionSubID = msg.getTradingSessionSubID();
        if ( tTradingSessionSubID != null ) _builder.encodeByte( FixDictionaryMD44.TradingSessionSubID, tTradingSessionSubID.getVal() );        // tag625
        final TradSesStatus tTradSesStatus = msg.getTradSesStatus();
        if ( tTradSesStatus != null ) _builder.encodeByte( FixDictionaryMD44.TradSesStatus, tTradSesStatus.getVal() );        // tag340
        _builder.encodeUTCTimestamp( FixDictionaryMD44.TransactTime, now );        // tag60
        _builder.encodeEnvelope();
    }

    public final void encodeMDRequest( final MDRequest msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_MDRequest );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeString( FixDictionaryMD44.mdReqId, msg.getMdReqId() );        // tag262
        final SubsReqType tSubsReqType = msg.getSubsReqType();
        if ( tSubsReqType != null ) _builder.encodeByte( FixDictionaryMD44.subsReqType, tSubsReqType.getVal() );        // tag263
        _builder.encodeInt( FixDictionaryMD44.marketDepth, msg.getMarketDepth() );        // tag264
        _builder.encodeInt( FixDictionaryMD44.numRelatedSym, msg.getNumRelatedSym() );        // tag146
        _builder.encodeEnvelope();
    }

    public final void encodeMassInstrumentStateChange( final MassInstrumentStateChange msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeTwoByte( 35, MSG_MassInstrumentStateChange );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeUTCTimestamp( FixDictionaryMD44.SendingTime, now );        // tag52
        _builder.encodeInt( FixDictionaryMD44.marketSegmentId, msg.getMarketSegmentID() );        // tag1300
        _builder.encodeInt( FixDictionaryMD44.instrumentScopeProductComplex, msg.getInstrumentScopeProductComplex() );        // tag1544
        _builder.encodeInt( FixDictionaryMD44.securityMassTradingStatus, msg.getSecurityMassTradingStatus() );        // tag1679
        _builder.encodeUTCTimestamp( FixDictionaryMD44.TransactTime, now );        // tag60
        _builder.encodeInt( FixDictionaryMD44.numRelatedSym, msg.getNumRelatedSym() );        // tag146
        _builder.encodeEnvelope();
    }

    public final void encodeSecurityStatus( final SecurityStatus msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_SecurityStatus );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionaryMD44.securityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeLong( FixDictionaryMD44.securityID, msg.getSecurityID() );        // tag48
        _builder.encodeInt( FixDictionaryMD44.TradeDate, msg.getTradeDate() );        // tag75
        _builder.encodePrice( FixDictionaryMD44.highPx, msg.getHighPx() );        // tag332
        _builder.encodePrice( FixDictionaryMD44.lowPx, msg.getLowPx() );        // tag333
        final SecurityTradingStatus tSecurityTradingStatus = msg.getSecurityTradingStatus();
        if ( tSecurityTradingStatus != null ) _builder.encodeTwoByte( FixDictionaryMD44.securityTradingStatus, tSecurityTradingStatus.getVal() );        // tag326
        _builder.encodeInt( FixDictionaryMD44.haltReason, msg.getHaltReason() );        // tag327
        _builder.encodeInt( FixDictionaryMD44.SecurityTradingEvent, msg.getSecurityTradingEvent() );        // tag1174
        _builder.encodeString( FixDictionaryMD44.Symbol, msg.getSymbol() );        // tag55
        _builder.encodeEnvelope();
    }

    public final void encodeSecurityDefinition( final SecurityDefinition msg ) {
        final int now = _tzCalculator.getNowUTC();
        _builder.start();
        _builder.encodeByte( 35, MSG_SecurityDefinition );
            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;
            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;
        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34
        _builder.encodeInt( FixDictionaryMD44.totNumReports, msg.getTotNumReports() );        // tag911
        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();
        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionaryMD44.securityIDSource, tSecurityIDSource.getVal() );        // tag22
        _builder.encodeLong( FixDictionaryMD44.securityID, msg.getSecurityID() );        // tag48
        _builder.encodeString( FixDictionaryMD44.Symbol, msg.getSymbol() );        // tag55
        final SecurityType tSecurityType = msg.getSecurityType();
        if ( tSecurityType != null ) _builder.encodeBytes( FixDictionaryMD44.securityType, tSecurityType.getVal() );        // tag167
        _builder.encodeInt( FixDictionaryMD44.noEvents, msg.getNoEvents() );        // tag864
        final SecurityUpdateAction tSecurityUpdateAction = msg.getSecurityUpdateAction();
        if ( tSecurityUpdateAction != null ) _builder.encodeByte( FixDictionaryMD44.SecurityUpdateAction, tSecurityUpdateAction.getVal() );        // tag980
        _builder.encodeInt( FixDictionaryMD44.noLegs, msg.getNoLegs() );        // tag555
        _builder.encodePrice( FixDictionaryMD44.tradingReferencePrice, msg.getTradingReferencePrice() );        // tag1150
        _builder.encodePrice( FixDictionaryMD44.highLimitPx, msg.getHighLimitPx() );        // tag1149
        _builder.encodePrice( FixDictionaryMD44.lowLimitPx, msg.getLowLimitPx() );        // tag1148
        _builder.encodePrice( FixDictionaryMD44.minPriceIncrement, msg.getMinPriceIncrement() );        // tag969
        _builder.encodePrice( FixDictionaryMD44.minPriceIncrementAmount, msg.getMinPriceIncrementAmount() );        // tag1146
        _builder.encodeString( FixDictionaryMD44.securityGroup, msg.getSecurityGroup() );        // tag1151
        _builder.encodeString( FixDictionaryMD44.securityDesc, msg.getSecurityDesc() );        // tag107
        _builder.encodeString( FixDictionaryMD44.CFICode, msg.getCFICode() );        // tag461
        _builder.encodeString( FixDictionaryMD44.underlyingProduct, msg.getUnderlyingProduct() );        // tag462
        _builder.encodeString( FixDictionaryMD44.securityExchange, msg.getSecurityExchange() );        // tag207
        _builder.encodeInt( FixDictionaryMD44.noSecurityAltID, msg.getNoSecurityAltID() );        // tag454
        _builder.encodePrice( FixDictionaryMD44.strikePrice, msg.getStrikePrice() );        // tag202
        final Currency tStrikeCurrency = msg.getStrikeCurrency();
        if ( tStrikeCurrency != null ) _builder.encodeBytes( FixDictionaryMD44.strikeCurrency, tStrikeCurrency.getVal() );        // tag947
        final Currency tCurrency = msg.getCurrency();
        if ( tCurrency != null ) _builder.encodeBytes( FixDictionaryMD44.Currency, tCurrency.getVal() );        // tag15
        final Currency tSettlCurrency = msg.getSettlCurrency();
        if ( tSettlCurrency != null ) _builder.encodeBytes( FixDictionaryMD44.settlCurrency, tSettlCurrency.getVal() );        // tag120
        _builder.encodeLong( FixDictionaryMD44.minTradeVol, msg.getMinTradeVol() );        // tag562
        _builder.encodeLong( FixDictionaryMD44.maxTradeVol, msg.getMaxTradeVol() );        // tag1140
        _builder.encodeInt( FixDictionaryMD44.noSDFeedTypes, msg.getNoSDFeedTypes() );        // tag1141
        _builder.encodeLong( FixDictionaryMD44.maturityMonthYear, msg.getMaturityMonthYear() );        // tag200
        _builder.encodeLong( FixDictionaryMD44.lastUpdateTime, msg.getLastUpdateTime() );        // tag779
        _builder.encodeString( FixDictionaryMD44.applID, msg.getApplID() );        // tag1180
        _builder.encodePrice( FixDictionaryMD44.displayFactor, msg.getDisplayFactor() );        // tag9787
        _builder.encodePrice( FixDictionaryMD44.priceRatio, msg.getPriceRatio() );        // tag5770
        _builder.encodeInt( FixDictionaryMD44.contractMultiplierType, msg.getContractMultiplierType() );        // tag1435
        _builder.encodeInt( FixDictionaryMD44.contractMultiplier, msg.getContractMultiplier() );        // tag231
        _builder.encodeInt( FixDictionaryMD44.openInterestQty, msg.getOpenInterestQty() );        // tag5792
        _builder.encodeInt( FixDictionaryMD44.tradingReferenceDate, msg.getTradingReferenceDate() );        // tag5796
        _builder.encodeInt( FixDictionaryMD44.minQty, msg.getMinQty() );        // tag110
        _builder.encodePrice( FixDictionaryMD44.pricePrecision, msg.getPricePrecision() );        // tag1200
        _builder.encodeEnvelope();
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

        /**     * PostPend  Common Encoder File     *     * expected to contain methods used in hooks from model     */         @Override    public void setNanoStats( boolean nanoTiming ) {        _nanoStats = nanoTiming;    }    private       boolean         _nanoStats    =  true;             private       int             _idx          = 1;        private final ClientCancelRejectFactory _canRejFactory   = SuperpoolManager.instance().getFactory( ClientCancelRejectFactory.class, ClientCancelRejectImpl.class );    private final ClientRejectedFactory     _rejectedFactory = SuperpoolManager.instance().getFactory( ClientRejectedFactory.class,     ClientRejectedImpl.class );     public static final ZString ENCODE_REJ              = new ViewString( "ERJ" );    public static final ZString NONE                    = new ViewString( "NON" );    @Override    public Message unableToSend( Message msg, ZString errMsg ) {        switch( msg.getReusableType().getSubId() ) {        case EventIds.ID_NEWORDERSINGLE:            return rejectNewOrderSingle( (NewOrderSingle) msg, errMsg );        case EventIds.ID_NEWORDERACK:            break;        case EventIds.ID_TRADENEW:            break;        case EventIds.ID_CANCELREPLACEREQUEST:            return rejectCancelReplaceRequest( (CancelReplaceRequest) msg, errMsg );        case EventIds.ID_CANCELREQUEST:            return rejectCancelRequest( (CancelRequest) msg, errMsg );        }                return null;    }    private Message rejectNewOrderSingle( NewOrderSingle nos, ZString errMsg ) {        final ClientRejectedImpl reject = _rejectedFactory.get();        reject.setSrcEvent( nos );        reject.getExecIdForUpdate().copy( ENCODE_REJ ).append( nos.getClOrdId() ).append( ++_idx );        reject.getOrderIdForUpdate().setValue( NONE );        reject.setOrdRejReason( OrdRejReason.Other );        reject.getTextForUpdate().setValue( errMsg );        reject.setOrdStatus( OrdStatus.Rejected );        reject.setExecType( ExecType.Rejected );        reject.setCumQty( 0 );        reject.setAvgPx( 0.0 );        reject.setMessageHandler( nos.getMessageHandler() );        return reject;    }    private Message rejectCancelReplaceRequest( CancelReplaceRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelReplace );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private Message rejectCancelRequest( CancelRequest msg, ZString errMsg ) {        final ClientCancelRejectImpl reject = _canRejFactory.get();                reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );        reject.getOrderIdForUpdate().    setValue( NONE );        reject.getTextForUpdate().       setValue( errMsg );        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelRequest );        reject.setCxlRejReason(     CxlRejReason.Other );        reject.setOrdStatus(        OrdStatus.Unknown );        return reject;    }    private static final byte[] STATS       = "     [".getBytes();    private static final byte   STAT_DELIM  = ',';    private static final byte   STAT_END    = ']';        private       ReusableString  _senderCompId      = new ReusableString();    private       ReusableString  _senderSubId       = new ReusableString();    private       ReusableString  _senderLocationId  = new ReusableString();    private       ReusableString  _targetCompId      = new ReusableString();    private       ReusableString  _targetSubId       = new ReusableString();        @Override    public void setSenderCompId( ZString senderCompId ) {        _senderCompId.copy( senderCompId );    }    @Override    public void setSenderSubId( ZString senderSubId ) {        _senderSubId.copy( senderSubId );    }    @Override    public void setSenderLocationId( ZString senderLocationId ) {        _senderLocationId.copy( senderLocationId );    }    @Override    public void setTargetCompId( ZString targetId ) {        _targetCompId.copy( targetId );    }    @Override    public void setTargetSubId( ZString targetSubId ) {        _targetSubId.copy( targetSubId );    }    public void setCompIds( String senderCompId, String senderSubId,  String targetCompId, String targetSubId ) {        setSenderCompId(     new ReusableString( senderCompId ) );        setSenderSubId(      new ReusableString( senderSubId  ) );        setTargetCompId(     new ReusableString( targetCompId ) );        setTargetSubId(      new ReusableString( targetSubId  ) );    }         @Override    public void addStats( final ReusableString outBuf, final Message msg, final long msgSent ) {                if ( msg.getReusableType().getSubId() == EventIds.ID_NEWORDERSINGLE ) {            final NewOrderSingle nos = (NewOrderSingle) msg;            final long tickIn   = nos.getOrderReceived();            final long microTickToTrade = (msgSent - tickIn) / 1000;                        outBuf.append( STATS ).append( microTickToTrade ).append( STAT_END );        }    }/* * HANDCODED ENCODER METHDOS */      public final void encodeMDIncRefresh( final MDIncRefresh msg ) {        final int now = _tzCalculator.getNowUTC();        _builder.startNoLenOrChecksum();        _builder.encodeByte( 35, MSG_MDIncRefresh );            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34        writeDateTime( FixDictionaryMD44.SendingTime, msg.getSendingTime() );          // tag52                _builder.encodeInt( FixDictionaryMD44.noMDEntries, msg.getNoMDEntries() );        // tag268        MDEntryImpl curTick = (MDEntryImpl) msg.getMDEntries();                while( curTick != null ) {            _builder.encodeByte(  FixDictionaryMD44.mdUpdateAction, curTick.getMdUpdateAction().getVal() );  // tag279            _builder.encodeInt(   FixDictionaryMD44.mdPriceLevel,   curTick.getMdPriceLevel() );            // tag1023            _builder.encodeByte(  FixDictionaryMD44.mdEntryType,    curTick.getMdEntryType().getVal() );  // tag269            _builder.encodeInt(   FixDictionaryMD44.mdEntryTime,       curTick.getMdEntryTime() );           // tag273            _builder.encodeByte(  FixDictionaryMD44.securityIDSource,  curTick.getSecurityIDSource().getVal() );      // tag22            _builder.encodeLong(   FixDictionaryMD44.securityID,        curTick.getSecurityID() );            // tag48            _builder.encodeInt(   FixDictionaryMD44.repeatSeq,        curTick.getRepeatSeq() );            // tag83            _builder.encodePrice( FixDictionaryMD44.mdEntryPx,      curTick.getMdEntryPx() );             // tag270            _builder.encodeInt(   FixDictionaryMD44.mdEntrySize,    curTick.getMdEntrySize() );           // tag271                        _builder.encodeInt(   FixDictionaryMD44.numberOfOrders,    curTick.getNumberOfOrders() );                               // tag346                        final TradingSessionID tradingSessionId = curTick.getTradingSessionID();            if ( tradingSessionId != null ) _builder.encodeByte( FixDictionaryMD44.TradingSessionID, tradingSessionId.getVal() );  // tag336;                        curTick = curTick.getNext();        }        _builder.encodeEnvelopeNoLenOrChecksum();    }        final ReusableString _tmpTime = new ReusableString(22);    final ReusableString _tmpTime2 = new ReusableString(22);        private void writeDateTime( int tag, long dateTime ) {        _tmpTime.reset();        _tmpTime.append( dateTime );        _tmpTime2.reset();                _tmpTime2.append( _tmpTime, 0, 8 );        _tmpTime2.append( '-' );        _tmpTime2.append( _tmpTime, 8, 2 );        _tmpTime2.append( ':' );        _tmpTime2.append( _tmpTime, 10, 2 );        _tmpTime2.append( ':' );        _tmpTime2.append( _tmpTime, 12, 2 );        _tmpTime2.append( '.' );        _tmpTime2.append( _tmpTime, 14, 3 );        _builder.encodeString( tag, _tmpTime2 );    }    public final void encodeMDSnapshotFullRefresh( final MDSnapshotFullRefresh msg ) {        _builder.start();        _builder.encodeByte( 35, MSG_MDSnapshotFullRefresh );            _builder.encodeString( FixDictionaryMD44.SenderCompID, _senderCompId ); // tag49;            _builder.encodeString( FixDictionaryMD44.TargetCompID, _targetCompId ); // tag56;        _builder.encodeInt( FixDictionaryMD44.MsgSeqNum, msg.getMsgSeqNum() );        // tag34        writeDateTime( FixDictionaryMD44.SendingTime, msg.getSendingTime() );          // tag52        _builder.encodeInt( FixDictionaryMD44.lastMsgSeqNumProcessed, msg.getLastMsgSeqNumProcessed() );        // tag369        _builder.encodeInt( FixDictionaryMD44.totNumReports, msg.getTotNumReports() );        // tag911        _builder.encodeInt( FixDictionaryMD44.mdBookType, msg.getMdBookType() );        // tag1021        final SecurityIDSource tSecurityIDSource = msg.getSecurityIDSource();        if ( tSecurityIDSource != null ) _builder.encodeByte( FixDictionaryMD44.securityIDSource, tSecurityIDSource.getVal() );        // tag22        _builder.encodeLong( FixDictionaryMD44.securityID, msg.getSecurityID() );        // tag48        _builder.encodeInt( FixDictionaryMD44.mdSecurityTradingStatus, msg.getMdSecurityTradingStatus() );        // tag1682        _builder.encodeInt( FixDictionaryMD44.noMDEntries, msg.getNoMDEntries() );        // tag268        MDSnapEntryImpl curSnapEntry = (MDSnapEntryImpl) msg.getMDEntries();                while( curSnapEntry != null ) {            _builder.encodeByte(  FixDictionaryMD44.mdEntryType,    curSnapEntry.getMdEntryType().getVal() );  // tag269            _builder.encodePrice( FixDictionaryMD44.mdEntryPx,      curSnapEntry.getMdEntryPx() );             // tag270            _builder.encodeInt(   FixDictionaryMD44.mdEntrySize,    curSnapEntry.getMdEntrySize() );           // tag271            _builder.encodeInt(   FixDictionaryMD44.mdEntryTime,    curSnapEntry.getMdEntryTime() );           // tag273            _builder.encodeByte(  FixDictionaryMD44.tickDirection,  curSnapEntry.getTickDirection().getVal() ); // tag274            _builder.encodeInt(   FixDictionaryMD44.tradeVolume,    curSnapEntry.getTradeVolume() );            // tag1020            _builder.encodeInt(  FixDictionaryMD44.mdPriceLevel,   curSnapEntry.getMdPriceLevel() );           // tag1023                        curSnapEntry = curSnapEntry.getNext();        }        _builder.encodeEnvelope();    }         }
