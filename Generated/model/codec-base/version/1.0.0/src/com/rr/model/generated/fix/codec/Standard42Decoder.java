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
import com.rr.core.codec.AbstractFixDecoder42;
import com.rr.core.codec.FixField;
import com.rr.core.lang.*;
import com.rr.core.model.*;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.internal.type.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.utils.StringUtils;
import com.rr.model.generated.fix.model.defn.FixDictionary42;
import com.rr.model.generated.internal.events.factory.*;
import com.rr.model.generated.internal.events.impl.*;
import com.rr.model.generated.internal.events.interfaces.*;
import com.rr.model.generated.internal.type.*;
import com.rr.model.generated.internal.core.SizeType;

@SuppressWarnings( "unused" )

public final class Standard42Decoder extends AbstractFixDecoder42 {

   // Attrs
    ExecTransType _execTransType = null;    // Tag 20
    OrdStatus _ordStatus = null;    // Tag 39
    ExecType _execType = null;    // Tag 150
    Side _side = null;    // Tag 54
    MultiLegReportingType _multiLegReportingType = null;    // Tag 442
    ExecRestatementReason _execRestatementReason = null;    // Tag 378

   // exec rpt only populated after all fields processed
   // only generate vars that are required

   // write String start and length vars required for ExecRpts

    int _clOrdIdStart = 0;    // tag 11
    int _clOrdIdLen = 0;    // tag 11
    int _execIdStart = 0;    // tag 17
    int _execIdLen = 0;    // tag 17
    int _orderIdStart = 0;    // tag 37
    int _orderIdLen = 0;    // tag 37
    int _lastMktStart = 0;    // tag 30
    int _lastMktLen = 0;    // tag 30
    int _securityDescStart = 0;    // tag 107
    int _securityDescLen = 0;    // tag 107
    int _textStart = 0;    // tag 58
    int _textLen = 0;    // tag 58
    int _origClOrdIdStart = 0;    // tag 41
    int _origClOrdIdLen = 0;    // tag 41
    int _execRefIDStart = 0;    // tag 19
    int _execRefIDLen = 0;    // tag 19

   // write value holders

    int _msgSeqNum = Constants.UNSET_INT;    // tag 34
    boolean _possDupFlag = false;    // tag 43
    double _avgPx = Constants.UNSET_DOUBLE;    // tag 6
    int _cumQty = Constants.UNSET_INT;    // tag 14
    int _leavesQty = Constants.UNSET_INT;    // tag 151
    int _lastQty = Constants.UNSET_INT;    // tag 32
    double _lastPx = Constants.UNSET_DOUBLE;    // tag 31
    private final ViewString                  _lookup = new ViewString();

   // Pools

    private final SuperPool<ClientNewOrderSingleImpl> _newOrderSinglePool = SuperpoolManager.instance().getSuperPool( ClientNewOrderSingleImpl.class );
    private final ClientNewOrderSingleFactory _newOrderSingleFactory = new ClientNewOrderSingleFactory( _newOrderSinglePool );

    private final SuperPool<ClientCancelReplaceRequestImpl> _cancelReplaceRequestPool = SuperpoolManager.instance().getSuperPool( ClientCancelReplaceRequestImpl.class );
    private final ClientCancelReplaceRequestFactory _cancelReplaceRequestFactory = new ClientCancelReplaceRequestFactory( _cancelReplaceRequestPool );

    private final SuperPool<ClientCancelRequestImpl> _cancelRequestPool = SuperpoolManager.instance().getSuperPool( ClientCancelRequestImpl.class );
    private final ClientCancelRequestFactory _cancelRequestFactory = new ClientCancelRequestFactory( _cancelRequestPool );

    private final SuperPool<MarketCancelRejectImpl> _cancelRejectPool = SuperpoolManager.instance().getSuperPool( MarketCancelRejectImpl.class );
    private final MarketCancelRejectFactory _cancelRejectFactory = new MarketCancelRejectFactory( _cancelRejectPool );

    private final SuperPool<MarketNewOrderAckImpl> _newOrderAckPool = SuperpoolManager.instance().getSuperPool( MarketNewOrderAckImpl.class );
    private final MarketNewOrderAckFactory _newOrderAckFactory = new MarketNewOrderAckFactory( _newOrderAckPool );

    private final SuperPool<MarketTradeNewImpl> _tradeNewPool = SuperpoolManager.instance().getSuperPool( MarketTradeNewImpl.class );
    private final MarketTradeNewFactory _tradeNewFactory = new MarketTradeNewFactory( _tradeNewPool );

    private final SuperPool<MarketRejectedImpl> _rejectedPool = SuperpoolManager.instance().getSuperPool( MarketRejectedImpl.class );
    private final MarketRejectedFactory _rejectedFactory = new MarketRejectedFactory( _rejectedPool );

    private final SuperPool<MarketCancelledImpl> _cancelledPool = SuperpoolManager.instance().getSuperPool( MarketCancelledImpl.class );
    private final MarketCancelledFactory _cancelledFactory = new MarketCancelledFactory( _cancelledPool );

    private final SuperPool<MarketReplacedImpl> _replacedPool = SuperpoolManager.instance().getSuperPool( MarketReplacedImpl.class );
    private final MarketReplacedFactory _replacedFactory = new MarketReplacedFactory( _replacedPool );

    private final SuperPool<MarketDoneForDayImpl> _doneForDayPool = SuperpoolManager.instance().getSuperPool( MarketDoneForDayImpl.class );
    private final MarketDoneForDayFactory _doneForDayFactory = new MarketDoneForDayFactory( _doneForDayPool );

    private final SuperPool<MarketStoppedImpl> _stoppedPool = SuperpoolManager.instance().getSuperPool( MarketStoppedImpl.class );
    private final MarketStoppedFactory _stoppedFactory = new MarketStoppedFactory( _stoppedPool );

    private final SuperPool<MarketExpiredImpl> _expiredPool = SuperpoolManager.instance().getSuperPool( MarketExpiredImpl.class );
    private final MarketExpiredFactory _expiredFactory = new MarketExpiredFactory( _expiredPool );

    private final SuperPool<MarketSuspendedImpl> _suspendedPool = SuperpoolManager.instance().getSuperPool( MarketSuspendedImpl.class );
    private final MarketSuspendedFactory _suspendedFactory = new MarketSuspendedFactory( _suspendedPool );

    private final SuperPool<MarketRestatedImpl> _restatedPool = SuperpoolManager.instance().getSuperPool( MarketRestatedImpl.class );
    private final MarketRestatedFactory _restatedFactory = new MarketRestatedFactory( _restatedPool );

    private final SuperPool<MarketTradeCorrectImpl> _tradeCorrectPool = SuperpoolManager.instance().getSuperPool( MarketTradeCorrectImpl.class );
    private final MarketTradeCorrectFactory _tradeCorrectFactory = new MarketTradeCorrectFactory( _tradeCorrectPool );

    private final SuperPool<MarketTradeCancelImpl> _tradeCancelPool = SuperpoolManager.instance().getSuperPool( MarketTradeCancelImpl.class );
    private final MarketTradeCancelFactory _tradeCancelFactory = new MarketTradeCancelFactory( _tradeCancelPool );

    private final SuperPool<MarketOrderStatusImpl> _orderStatusPool = SuperpoolManager.instance().getSuperPool( MarketOrderStatusImpl.class );
    private final MarketOrderStatusFactory _orderStatusFactory = new MarketOrderStatusFactory( _orderStatusPool );

    private final SuperPool<HeartbeatImpl> _heartbeatPool = SuperpoolManager.instance().getSuperPool( HeartbeatImpl.class );
    private final HeartbeatFactory _heartbeatFactory = new HeartbeatFactory( _heartbeatPool );

    private final SuperPool<LogonImpl> _logonPool = SuperpoolManager.instance().getSuperPool( LogonImpl.class );
    private final LogonFactory _logonFactory = new LogonFactory( _logonPool );

    private final SuperPool<LogoutImpl> _logoutPool = SuperpoolManager.instance().getSuperPool( LogoutImpl.class );
    private final LogoutFactory _logoutFactory = new LogoutFactory( _logoutPool );

    private final SuperPool<SessionRejectImpl> _sessionRejectPool = SuperpoolManager.instance().getSuperPool( SessionRejectImpl.class );
    private final SessionRejectFactory _sessionRejectFactory = new SessionRejectFactory( _sessionRejectPool );

    private final SuperPool<ResendRequestImpl> _resendRequestPool = SuperpoolManager.instance().getSuperPool( ResendRequestImpl.class );
    private final ResendRequestFactory _resendRequestFactory = new ResendRequestFactory( _resendRequestPool );

    private final SuperPool<SequenceResetImpl> _sequenceResetPool = SuperpoolManager.instance().getSuperPool( SequenceResetImpl.class );
    private final SequenceResetFactory _sequenceResetFactory = new SequenceResetFactory( _sequenceResetPool );

    private final SuperPool<TestRequestImpl> _testRequestPool = SuperpoolManager.instance().getSuperPool( TestRequestImpl.class );
    private final TestRequestFactory _testRequestFactory = new TestRequestFactory( _testRequestPool );


   // Constructors
    public Standard42Decoder() {
        this( FixVersion.Fix4_2._major, FixVersion.Fix4_2._minor );
    }

    public Standard42Decoder( byte major, byte minor ) {
        super( major, minor );
    }

   // decode methods

    @Override
    protected final Message doMessageDecode() {
        // get message type field
        if ( _fixMsg[_idx] != '3' || _fixMsg[_idx+1] != '5' || _fixMsg[_idx+2] != '=' )
            throwDecodeException( "Fix Messsage missing message type" );
        _idx += 3;

        byte msgType = _fixMsg[ _idx ];
        switch( msgType ) {
        case '8':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeExecReport();
        case 'D':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeNewOrderSingle();
        case 'G':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeCancelReplaceRequest();
        case 'F':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeCancelRequest();
        case '9':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeCancelReject();
        case '0':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeHeartbeat();
        case 'A':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeLogon();
        case '5':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeLogout();
        case '3':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeSessionReject();
        case '2':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeResendRequest();
        case '4':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeSequenceReset();
        case '1':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeTestRequest();
        case '6':
        case '7':
        case ':':
        case ';':
        case '<':
        case '=':
        case '>':
        case '?':
        case '@':
        case 'B':
        case 'C':
        case 'E':
            break;
        }
        _idx += 2;
        throwDecodeException( "Unsupported fix message type " + msgType );
        return null;
    }


    public final Message decodeExecReport() {
        _execTransType = null;    // Tag 20
        _ordStatus = null;    // Tag 39
        _execType = null;    // Tag 150
        _side = null;    // Tag 54
        _multiLegReportingType = null;    // Tag 442
        _execRestatementReason = null;    // Tag 378
        _clOrdIdStart = 0;    // tag 11
        _clOrdIdLen = 0;    // tag 11
        _execIdStart = 0;    // tag 17
        _execIdLen = 0;    // tag 17
        _orderIdStart = 0;    // tag 37
        _orderIdLen = 0;    // tag 37
        _lastMktStart = 0;    // tag 30
        _lastMktLen = 0;    // tag 30
        _securityDescStart = 0;    // tag 107
        _securityDescLen = 0;    // tag 107
        _textStart = 0;    // tag 58
        _textLen = 0;    // tag 58
        _origClOrdIdStart = 0;    // tag 41
        _origClOrdIdLen = 0;    // tag 41
        _execRefIDStart = 0;    // tag 19
        _execRefIDLen = 0;    // tag 19
        _msgSeqNum = Constants.UNSET_INT;    // tag 34
        _possDupFlag = false;    // tag 43
        _avgPx = Constants.UNSET_DOUBLE;    // tag 6
        _cumQty = Constants.UNSET_INT;    // tag 14
        _leavesQty = Constants.UNSET_INT;    // tag 151
        _lastQty = Constants.UNSET_INT;    // tag 32
        _lastPx = Constants.UNSET_DOUBLE;    // tag 31
        int start;
        int valLen;
        
        _tag = getTag();
        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.AvgPx:         // tag6
                _avgPx = getDoubleVal();
                break;
            case FixDictionary42.ClOrdId:         // tag11
                _clOrdIdStart = _idx;
                _clOrdIdLen = getValLength();
                break;
            case FixDictionary42.CumQty:         // tag14
                _cumQty = getIntVal();
                break;
            case FixDictionary42.ExecID:         // tag17
                _execIdStart = _idx;
                _execIdLen = getValLength();
                break;
            case FixDictionary42.ExecRefID:         // tag19
                _execRefIDStart = _idx;
                _execRefIDLen = getValLength();
                break;
            case FixDictionary42.ExecTransType:         // tag20
                 _execTransType = ExecTransType.getVal( _fixMsg[_idx++] );
                break;
            case FixDictionary42.LastMkt:         // tag30
                _lastMktStart = _idx;
                _lastMktLen = getValLength();
                break;
            case FixDictionary42.LastPx:         // tag31
                _lastPx = getDoubleVal();
                break;
            case FixDictionary42.LastQty:         // tag32
                _lastQty = getIntVal();
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                _msgSeqNum = getIntVal();
                break;
            case FixDictionary42.OrderId:         // tag37
                _orderIdStart = _idx;
                _orderIdLen = getValLength();
                break;
            case FixDictionary42.OrdStatus:         // tag39
                 _ordStatus = OrdStatus.getVal( _fixMsg[_idx++] );
                break;
            case FixDictionary42.OrigClOrdId:         // tag41
                _origClOrdIdStart = _idx;
                _origClOrdIdLen = getValLength();
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                _possDupFlag = ((_fixMsg[_idx++]=='Y') ? true : false);
                break;
            case FixDictionary42.Side:         // tag54
                 _side = Side.getVal( _fixMsg[_idx++] );
                break;
            case FixDictionary42.SrcLinkId:         // tag58
                _textStart = _idx;
                _textLen = getValLength();
                break;
            case FixDictionary42.Account:         // tag1
            case 2:         // tag2
            case 3:         // tag3
            case 4:         // tag4
            case 5:         // tag5
            case FixDictionary42.BeginSeqNo:         // tag7
            case FixDictionary42.BeginString:         // tag8
            case FixDictionary42.BodyLength:         // tag9
            case FixDictionary42.CheckSum:         // tag10
            case 12:         // tag12
            case 13:         // tag13
            case FixDictionary42.Currency:         // tag15
            case FixDictionary42.EndSeqNo:         // tag16
            case FixDictionary42.ExecInst:         // tag18
            case FixDictionary42.HandlInst:         // tag21
            case FixDictionary42.SecurityIDSource:         // tag22
            case 23:         // tag23
            case 24:         // tag24
            case 25:         // tag25
            case 26:         // tag26
            case 27:         // tag27
            case 28:         // tag28
            case 29:         // tag29
            case 33:         // tag33
            case FixDictionary42.MsgType:         // tag35
            case FixDictionary42.NewSeqNo:         // tag36
            case FixDictionary42.OrderQty:         // tag38
            case FixDictionary42.OrdType:         // tag40
            case 42:         // tag42
            case FixDictionary42.Price:         // tag44
            case FixDictionary42.RefSeqNum:         // tag45
            case 46:         // tag46
            case FixDictionary42.Rule80A:         // tag47
            case FixDictionary42.SecurityID:         // tag48
            case FixDictionary42.SenderCompID:         // tag49
            case FixDictionary42.SenderSubID:         // tag50
            case 51:         // tag51
            case FixDictionary42.SendingTime:         // tag52
            case 53:         // tag53
            case FixDictionary42.Symbol:         // tag55
            case FixDictionary42.TargetCompID:         // tag56
            case FixDictionary42.TargetSubID:         // tag57
            case FixDictionary42.TimeInForce:         // tag59
            case FixDictionary42.TransactTime:         // tag60
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.ExecType:         // tag150
                     _execType = ExecType.getVal( _fixMsg[_idx++] );
                    break;
                case FixDictionary42.LeavesQty:         // tag151
                    _leavesQty = getIntVal();
                    break;
                case FixDictionary42.SecurityDesc:         // tag107
                    _securityDescStart = _idx;
                    _securityDescLen = getValLength();
                    break;
                case FixDictionary42.MultiLegReportingType:         // tag442
                     _multiLegReportingType = MultiLegReportingType.getVal( _fixMsg[_idx++] );
                    break;
                case FixDictionary42.ExecRestatementReason:         // tag378
                    start = _idx;
                    valLen = getValLength();
                     _execRestatementReason = ExecRestatementReason.getVal( _fixMsg, start, valLen );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }

            _idx++; // past delimiter
            _tag = getTag();
        }
        preExecMessageDetermination();

        if ( _ordStatus == null || _execType == null ){
            throwDecodeException( "Execution report missing order or exec status " );
        }
        switch( _execType.getID() ){
        case ManualTypeIds.EXECTYPE_NEW:
            return populateExecRptNew();
        case ManualTypeIds.EXECTYPE_PARTIALFILL:
            return populateExecRptPartialFill();
        case ManualTypeIds.EXECTYPE_FILL:
            return populateExecRptFill();
        case ManualTypeIds.EXECTYPE_DONEFORDAY:
            return populateExecRptDoneForDay();
        case ManualTypeIds.EXECTYPE_CANCELED:
            return populateExecRptCanceled();
        case ManualTypeIds.EXECTYPE_REPLACED:
            return populateExecRptReplaced();
        case ManualTypeIds.EXECTYPE_PENDINGCANCEL:
            return populateExecRptPendingCancel();
        case ManualTypeIds.EXECTYPE_STOPPED:
            return populateExecRptStopped();
        case ManualTypeIds.EXECTYPE_REJECTED:
            return populateExecRptRejected();
        case ManualTypeIds.EXECTYPE_SUSPENDED:
            return populateExecRptSuspended();
        case ManualTypeIds.EXECTYPE_PENDINGNEW:
            return populateExecRptPendingNew();
        case ManualTypeIds.EXECTYPE_CALCULATED:
            return populateExecRptCalculated();
        case ManualTypeIds.EXECTYPE_EXPIRED:
            return populateExecRptExpired();
        case ManualTypeIds.EXECTYPE_RESTATED:
            return populateExecRptRestated();
        case ManualTypeIds.EXECTYPE_PENDINGREPLACE:
            return populateExecRptPendingReplace();
        case ManualTypeIds.EXECTYPE_TRADE:
            return populateExecRptTrade();
        case ManualTypeIds.EXECTYPE_TRADECORRECT:
            return populateExecRptTradeCorrect();
        case ManualTypeIds.EXECTYPE_TRADECANCEL:
            return populateExecRptTradeCancel();
        case ManualTypeIds.EXECTYPE_ORDERSTATUS:
            return populateExecRptOrderStatus();
        case ManualTypeIds.EXECTYPE_UNKNOWN:
            return populateExecRptUnknown();
        }
        throwDecodeException( "ExecRpt type " + _execType + " not supported" );
        return null;
    }

    public final Message populateExecRptNew() {
        MarketNewOrderAckImpl msg = _newOrderAckFactory.get();
        if ( _nanoStats ) msg.setAckReceived( _received );
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        // ignore 32;
        // ignore 31;
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        return msg;
    }

    public final Message populateExecRptTrade() {
        MarketTradeNewImpl msg = _tradeNewFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        msg.setLastQty( _lastQty );
        msg.setLastPx( _lastPx );
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        if ( _lastMktLen > 0 ) msg.setLastMkt( _fixMsg, _lastMktStart, _lastMktLen );
        if ( _securityDescLen > 0 ) msg.setSecurityDesc( _fixMsg, _securityDescStart, _securityDescLen );
        msg.setMultiLegReportingType( _multiLegReportingType );
        return msg;
    }

    public final Message populateExecRptRejected() {
        MarketRejectedImpl msg = _rejectedFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        // ignore 32;
        // ignore 31;
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        if ( _textLen > 0 ) msg.setText( _fixMsg, _textStart, _textLen );
        return msg;
    }

    public final Message populateExecRptReplaced() {
        MarketReplacedImpl msg = _replacedFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        if ( _origClOrdIdLen > 0 ) msg.setOrigClOrdId( _fixMsg, _origClOrdIdStart, _origClOrdIdLen );
        return msg;
    }

    public final Message populateExecRptCanceled() {
        MarketCancelledImpl msg = _cancelledFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        // ignore 32;
        // ignore 31;
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        if ( _origClOrdIdLen > 0 ) msg.setOrigClOrdId( _fixMsg, _origClOrdIdStart, _origClOrdIdLen );
        return msg;
    }

    public final Message populateExecRptDoneForDay() {
        MarketDoneForDayImpl msg = _doneForDayFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        // ignore 32;
        // ignore 31;
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        return msg;
    }

    public final Message populateExecRptPendingCancel() {
        return null; // flagged in model with IGNORE
    }

    public final Message populateExecRptStopped() {
        MarketStoppedImpl msg = _stoppedFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        // ignore 32;
        // ignore 31;
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        return msg;
    }

    public final Message populateExecRptSuspended() {
        MarketSuspendedImpl msg = _suspendedFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        // ignore 32;
        // ignore 31;
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        return msg;
    }

    public final Message populateExecRptPendingNew() {
        return null; // flagged in model with IGNORE
    }

    public final Message populateExecRptCalculated() {
        return null; // flagged in model with IGNORE
    }

    public final Message populateExecRptExpired() {
        MarketExpiredImpl msg = _expiredFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        // ignore 32;
        // ignore 31;
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        return msg;
    }

    public final Message populateExecRptRestated() {
        MarketRestatedImpl msg = _restatedFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        msg.setExecRestatementReason( _execRestatementReason );
        return msg;
    }

    public final Message populateExecRptPendingReplace() {
        return null; // flagged in model with IGNORE
    }

    public final Message populateExecRptTradeCorrect() {
        MarketTradeCorrectImpl msg = _tradeCorrectFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        msg.setLastQty( _lastQty );
        msg.setLastPx( _lastPx );
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        if ( _lastMktLen > 0 ) msg.setLastMkt( _fixMsg, _lastMktStart, _lastMktLen );
        if ( _securityDescLen > 0 ) msg.setSecurityDesc( _fixMsg, _securityDescStart, _securityDescLen );
        msg.setMultiLegReportingType( _multiLegReportingType );
        if ( _execRefIDLen > 0 ) msg.setExecRefID( _fixMsg, _execRefIDStart, _execRefIDLen );
        return msg;
    }

    public final Message populateExecRptTradeCancel() {
        MarketTradeCancelImpl msg = _tradeCancelFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        // ignore 32;
        // ignore 31;
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        if ( _lastMktLen > 0 ) msg.setLastMkt( _fixMsg, _lastMktStart, _lastMktLen );
        if ( _securityDescLen > 0 ) msg.setSecurityDesc( _fixMsg, _securityDescStart, _securityDescLen );
        msg.setMultiLegReportingType( _multiLegReportingType );
        if ( _execRefIDLen > 0 ) msg.setExecRefID( _fixMsg, _execRefIDStart, _execRefIDLen );
        return msg;
    }

    public final Message populateExecRptOrderStatus() {
        MarketOrderStatusImpl msg = _orderStatusFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        // ignore 32;
        // ignore 31;
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        return msg;
    }

    public final Message populateExecRptPartialFill() {
        MarketTradeNewImpl msg = _tradeNewFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        msg.setLastQty( _lastQty );
        msg.setLastPx( _lastPx );
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        if ( _lastMktLen > 0 ) msg.setLastMkt( _fixMsg, _lastMktStart, _lastMktLen );
        if ( _securityDescLen > 0 ) msg.setSecurityDesc( _fixMsg, _securityDescStart, _securityDescLen );
        msg.setMultiLegReportingType( _multiLegReportingType );
        return msg;
    }

    public final Message populateExecRptFill() {
        MarketTradeNewImpl msg = _tradeNewFactory.get();
        int start;
        int valLen;
        execCheckSenderCompID();
        execCheckTargetCompID();
        msg.setMsgSeqNum( _msgSeqNum );
        execCheckSenderSubID();
        execCheckTargetSubID();
        msg.setPossDupFlag( _possDupFlag );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        if ( _execIdLen > 0 ) msg.setExecId( _fixMsg, _execIdStart, _execIdLen );
        // ignore 20;
        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        msg.setOrdStatus( _ordStatus );
        msg.setExecType( _execType );
        msg.setLastQty( _lastQty );
        msg.setLastPx( _lastPx );
        msg.setSide( _side );
        msg.setAvgPx( _avgPx );
        msg.setCumQty( _cumQty );
        msg.setLeavesQty( _leavesQty );
        if ( _lastMktLen > 0 ) msg.setLastMkt( _fixMsg, _lastMktStart, _lastMktLen );
        if ( _securityDescLen > 0 ) msg.setSecurityDesc( _fixMsg, _securityDescStart, _securityDescLen );
        msg.setMultiLegReportingType( _multiLegReportingType );
        return msg;
    }

    public final Message populateExecRptUnknown() {
        throwDecodeException( "ExecRpt type Unknown not supported" );
        return null;
    }

    public final Message decodeNewOrderSingle() {
        final ClientNewOrderSingleImpl msg = _newOrderSingleFactory.get();
        if ( _nanoStats ) msg.setOrderReceived( getReceived() );
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.Account:         // tag1
                start = _idx - _offset;
                valLen = getValLength();
                msg.setAccount( start, valLen );
                break;
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.ClOrdId:         // tag11
                start = _idx - _offset;
                valLen = getValLength();
                msg.setClOrdId( start, valLen );
                break;
            case FixDictionary42.Currency:         // tag15
                start = _idx;
                valLen = getValLength();
                msg.setCurrency( Currency.getVal( _fixMsg, start, valLen ) );
                break;
            case FixDictionary42.HandlInst:         // tag21
                msg.setHandlInst( HandlInst.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.SecurityIDSource:         // tag22
                msg.setSecurityIDSource( SecurityIDSource.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.OrderQty:         // tag38
                msg.setOrderQty( getIntVal() );
                break;
            case FixDictionary42.OrdType:         // tag40
                msg.setOrdType( OrdType.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.Price:         // tag44
                msg.setPrice( getDoubleVal() );
                break;
            case FixDictionary42.Rule80A:         // tag47
                msg.setOrderCapacity( decodeRule47A( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.SecurityID:         // tag48
                start = _idx - _offset;
                valLen = getValLength();
                msg.setSecurityId( start, valLen );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                decodeSenderCompID();
                break;
            case FixDictionary42.SenderSubID:         // tag50
                decodeSenderSubID();
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.Side:         // tag54
                msg.setSide( Side.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.Symbol:         // tag55
                start = _idx - _offset;
                valLen = getValLength();
                msg.setSymbol( start, valLen );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                decodeTargetCompID();
                break;
            case FixDictionary42.TargetSubID:         // tag57
                decodeTargetSubID();
                break;
            case FixDictionary42.SrcLinkId:         // tag58
                start = _idx - _offset;
                valLen = getValLength();
                msg.setSrcLinkId( start, valLen );
                break;
            case FixDictionary42.TimeInForce:         // tag59
                msg.setTimeInForce( TimeInForce.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.TransactTime:         // tag60
                msg.setTransactTime( getMSFromStartDayUTC() );
                break;
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrderId:         // SKIP tag37
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case FixDictionary42.OrigClOrdId:         // SKIP tag41
            case 42:         // SKIP tag42
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.ExDest:         // tag100
                    start = _idx - _offset;
                    valLen = getValLength();
                    msg.setExDest( start, valLen );
                    break;
                case FixDictionary42.SecurityExchange:         // tag207
                    start = _idx - _offset;
                    valLen = getValLength();
                    msg.setSecurityExchange( start, valLen );
                    break;
                case FixDictionary42.BookingType:         // tag775
                    msg.setBookingType( BookingType.getVal( _fixMsg[_idx++] ) );
                    break;
                case FixDictionary42.PegOffsetValue:         // tag211
                    getValLength();
                    throwDecodeException( "Tag 211 not supported in NewOrderSingle" );
                    break;
                case 210:         // tag210
                    getValLength();
                    throwDecodeException( "Tag 210 not supported in NewOrderSingle" );
                    break;
                case 111:         // tag111
                    getValLength();
                    throwDecodeException( "Tag 111 not supported in NewOrderSingle" );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        if ( _idx > SizeType.VIEW_NOS_BUFFER.getSize() ) {
            throw new RuntimeDecodingException( "NewOrderSingleMessage too big " + _idx + ", max=" + SizeType.VIEW_NOS_BUFFER.getSize() );
        }
        msg.setViewBuf( _fixMsg, _offset, _idx );

        enrich( msg );
        return msg;
    }

    public final Message decodeCancelReplaceRequest() {
        final ClientCancelReplaceRequestImpl msg = _cancelReplaceRequestFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.Account:         // tag1
                start = _idx - _offset;
                valLen = getValLength();
                msg.setAccount( start, valLen );
                break;
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.ClOrdId:         // tag11
                start = _idx;
                valLen = getValLength();
                msg.setClOrdId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.Currency:         // tag15
                start = _idx;
                valLen = getValLength();
                msg.setCurrency( Currency.getVal( _fixMsg, start, valLen ) );
                break;
            case FixDictionary42.HandlInst:         // tag21
                msg.setHandlInst( HandlInst.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.SecurityIDSource:         // tag22
                msg.setSecurityIDSource( SecurityIDSource.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.OrderId:         // tag37
                start = _idx;
                valLen = getValLength();
                msg.setOrderId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.OrderQty:         // tag38
                msg.setOrderQty( getIntVal() );
                break;
            case FixDictionary42.OrdType:         // tag40
                msg.setOrdType( OrdType.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.OrigClOrdId:         // tag41
                start = _idx;
                valLen = getValLength();
                msg.setOrigClOrdId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.Price:         // tag44
                msg.setPrice( getDoubleVal() );
                break;
            case FixDictionary42.Rule80A:         // tag47
                msg.setOrderCapacity( decodeRule47A( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.SecurityID:         // tag48
                start = _idx - _offset;
                valLen = getValLength();
                msg.setSecurityId( start, valLen );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                decodeSenderCompID();
                break;
            case FixDictionary42.SenderSubID:         // tag50
                decodeSenderSubID();
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.Side:         // tag54
                msg.setSide( Side.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.Symbol:         // tag55
                start = _idx - _offset;
                valLen = getValLength();
                msg.setSymbol( start, valLen );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                decodeTargetCompID();
                break;
            case FixDictionary42.TargetSubID:         // tag57
                decodeTargetSubID();
                break;
            case FixDictionary42.SrcLinkId:         // tag58
                start = _idx - _offset;
                valLen = getValLength();
                msg.setSrcLinkId( start, valLen );
                break;
            case FixDictionary42.TimeInForce:         // tag59
                msg.setTimeInForce( TimeInForce.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.TransactTime:         // tag60
                msg.setTransactTime( getMSFromStartDayUTC() );
                break;
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case 42:         // SKIP tag42
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.ExDest:         // tag100
                    start = _idx - _offset;
                    valLen = getValLength();
                    msg.setExDest( start, valLen );
                    break;
                case FixDictionary42.SecurityExchange:         // tag207
                    start = _idx - _offset;
                    valLen = getValLength();
                    msg.setSecurityExchange( start, valLen );
                    break;
                case FixDictionary42.BookingType:         // tag775
                    msg.setBookingType( BookingType.getVal( _fixMsg[_idx++] ) );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        if ( _idx > SizeType.VIEW_NOS_BUFFER.getSize() ) {
            throw new RuntimeDecodingException( "CancelReplaceRequestMessage too big " + _idx + ", max=" + SizeType.VIEW_NOS_BUFFER.getSize() );
        }
        msg.setViewBuf( _fixMsg, _offset, _idx );

        enrich( msg );
        return msg;
    }

    public final Message decodeCancelRequest() {
        final ClientCancelRequestImpl msg = _cancelRequestFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.Account:         // tag1
                start = _idx - _offset;
                valLen = getValLength();
                msg.setAccount( start, valLen );
                break;
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.ClOrdId:         // tag11
                start = _idx;
                valLen = getValLength();
                msg.setClOrdId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.Currency:         // tag15
                start = _idx;
                valLen = getValLength();
                msg.setCurrency( Currency.getVal( _fixMsg, start, valLen ) );
                break;
            case FixDictionary42.SecurityIDSource:         // tag22
                msg.setSecurityIDSource( SecurityIDSource.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.OrderId:         // tag37
                start = _idx;
                valLen = getValLength();
                msg.setOrderId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.OrigClOrdId:         // tag41
                start = _idx;
                valLen = getValLength();
                msg.setOrigClOrdId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.SecurityID:         // tag48
                start = _idx - _offset;
                valLen = getValLength();
                msg.setSecurityId( start, valLen );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                decodeSenderCompID();
                break;
            case FixDictionary42.SenderSubID:         // tag50
                decodeSenderSubID();
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.Symbol:         // tag55
                start = _idx - _offset;
                valLen = getValLength();
                msg.setSymbol( start, valLen );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                decodeTargetCompID();
                break;
            case FixDictionary42.TargetSubID:         // tag57
                decodeTargetSubID();
                break;
            case FixDictionary42.SrcLinkId:         // tag58
                start = _idx;
                valLen = getValLength();
                msg.setSrcLinkId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.TransactTime:         // tag60
                msg.setTransactTime( getMSFromStartDayUTC() );
                break;
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case FixDictionary42.HandlInst:         // SKIP tag21
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrderQty:         // SKIP tag38
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case FixDictionary42.OrdType:         // SKIP tag40
            case 42:         // SKIP tag42
            case FixDictionary42.Price:         // SKIP tag44
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case FixDictionary42.Rule80A:         // SKIP tag47
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
            case FixDictionary42.Side:         // SKIP tag54
            case FixDictionary42.TimeInForce:         // SKIP tag59
                getValLength();
                break;
            default:
                getValLength();
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        if ( _idx > SizeType.VIEW_NOS_BUFFER.getSize() ) {
            throw new RuntimeDecodingException( "CancelRequestMessage too big " + _idx + ", max=" + SizeType.VIEW_NOS_BUFFER.getSize() );
        }
        msg.setViewBuf( _fixMsg, _offset, _idx );

        enrich( msg );
        return msg;
    }

    public final Message decodeCancelReject() {
        final MarketCancelRejectImpl msg = _cancelRejectFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.ClOrdId:         // tag11
                start = _idx;
                valLen = getValLength();
                msg.setClOrdId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.OrderId:         // tag37
                start = _idx;
                valLen = getValLength();
                msg.setOrderId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.OrdStatus:         // tag39
                msg.setOrdStatus( OrdStatus.getVal( _fixMsg[_idx++] ) );
                break;
            case FixDictionary42.OrigClOrdId:         // tag41
                start = _idx;
                valLen = getValLength();
                msg.setOrigClOrdId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                decodeSenderCompID();
                break;
            case FixDictionary42.SenderSubID:         // tag50
                decodeSenderSubID();
                break;
            case FixDictionary42.TargetCompID:         // tag56
                decodeTargetCompID();
                break;
            case FixDictionary42.TargetSubID:         // tag57
                decodeTargetSubID();
                break;
            case FixDictionary42.SrcLinkId:         // tag58
                start = _idx;
                valLen = getValLength();
                msg.setText( _fixMsg, start, valLen );
                break;
            case FixDictionary42.Account:         // SKIP tag1
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case FixDictionary42.CheckSum:         // SKIP tag10
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.Currency:         // SKIP tag15
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case FixDictionary42.HandlInst:         // SKIP tag21
            case FixDictionary42.SecurityIDSource:         // SKIP tag22
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrderQty:         // SKIP tag38
            case FixDictionary42.OrdType:         // SKIP tag40
            case 42:         // SKIP tag42
            case FixDictionary42.Price:         // SKIP tag44
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case FixDictionary42.Rule80A:         // SKIP tag47
            case FixDictionary42.SecurityID:         // SKIP tag48
            case 51:         // SKIP tag51
            case FixDictionary42.SendingTime:         // SKIP tag52
            case 53:         // SKIP tag53
            case FixDictionary42.Side:         // SKIP tag54
            case FixDictionary42.Symbol:         // SKIP tag55
            case FixDictionary42.TimeInForce:         // SKIP tag59
            case FixDictionary42.TransactTime:         // SKIP tag60
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.CxlRejReason:         // tag102
                    start = _idx;
                    valLen = getValLength();
                    msg.setCxlRejReason( CxlRejReason.getVal( _fixMsg, start, valLen ) );
                    break;
                case FixDictionary42.CxlRejResponseTo:         // tag434
                    msg.setCxlRejResponseTo( CxlRejResponseTo.getVal( _fixMsg[_idx++] ) );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        return msg;
    }

    public final Message decodeHeartbeat() {
        final HeartbeatImpl msg = _heartbeatFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                getValLength();
                break;
            case FixDictionary42.SenderSubID:         // tag50
                getValLength();
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                getValLength();
                break;
            case FixDictionary42.TargetSubID:         // tag57
                getValLength();
                break;
            case FixDictionary42.Account:         // SKIP tag1
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case FixDictionary42.ClOrdId:         // SKIP tag11
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.Currency:         // SKIP tag15
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case FixDictionary42.HandlInst:         // SKIP tag21
            case FixDictionary42.SecurityIDSource:         // SKIP tag22
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrderId:         // SKIP tag37
            case FixDictionary42.OrderQty:         // SKIP tag38
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case FixDictionary42.OrdType:         // SKIP tag40
            case FixDictionary42.OrigClOrdId:         // SKIP tag41
            case 42:         // SKIP tag42
            case FixDictionary42.Price:         // SKIP tag44
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case FixDictionary42.Rule80A:         // SKIP tag47
            case FixDictionary42.SecurityID:         // SKIP tag48
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
            case FixDictionary42.Side:         // SKIP tag54
            case FixDictionary42.Symbol:         // SKIP tag55
            case FixDictionary42.SrcLinkId:         // SKIP tag58
            case FixDictionary42.TimeInForce:         // SKIP tag59
            case FixDictionary42.TransactTime:         // SKIP tag60
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.testReqID:         // tag112
                    start = _idx;
                    valLen = getValLength();
                    msg.setTestReqID( _fixMsg, start, valLen );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        return msg;
    }

    public final Message decodeLogon() {
        final LogonImpl msg = _logonFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                start = _idx;
                valLen = getValLength();
                msg.setSenderCompId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.SenderSubID:         // tag50
                start = _idx;
                valLen = getValLength();
                msg.setSenderSubId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                start = _idx;
                valLen = getValLength();
                msg.setTargetCompId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.TargetSubID:         // tag57
                start = _idx;
                valLen = getValLength();
                msg.setTargetSubId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.Account:         // SKIP tag1
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case FixDictionary42.ClOrdId:         // SKIP tag11
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.Currency:         // SKIP tag15
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case FixDictionary42.HandlInst:         // SKIP tag21
            case FixDictionary42.SecurityIDSource:         // SKIP tag22
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrderId:         // SKIP tag37
            case FixDictionary42.OrderQty:         // SKIP tag38
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case FixDictionary42.OrdType:         // SKIP tag40
            case FixDictionary42.OrigClOrdId:         // SKIP tag41
            case 42:         // SKIP tag42
            case FixDictionary42.Price:         // SKIP tag44
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case FixDictionary42.Rule80A:         // SKIP tag47
            case FixDictionary42.SecurityID:         // SKIP tag48
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
            case FixDictionary42.Side:         // SKIP tag54
            case FixDictionary42.Symbol:         // SKIP tag55
            case FixDictionary42.SrcLinkId:         // SKIP tag58
            case FixDictionary42.TimeInForce:         // SKIP tag59
            case FixDictionary42.TransactTime:         // SKIP tag60
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.EncryptMethod:         // tag98
                    msg.setEncryptMethod( EncryptMethod.getVal( _fixMsg[_idx++] ) );
                    break;
                case FixDictionary42.heartBtInt:         // tag108
                    msg.setHeartBtInt( getIntVal() );
                    break;
                case FixDictionary42.RawDataLen:         // tag95
                    msg.setRawDataLen( getIntVal() );
                    break;
                case FixDictionary42.RawData:         // tag96
                    start = _idx;
                    valLen = getValLength();
                    msg.setRawData( _fixMsg, start, valLen );
                    break;
                case FixDictionary42.ResetSeqNumFlag:         // tag141
                    msg.setResetSeqNumFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                    break;
                case FixDictionary42.NextExpectedMsgSeqNum:         // tag789
                    msg.setNextExpectedMsgSeqNum( getIntVal() );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        return msg;
    }

    public final Message decodeLogout() {
        final LogoutImpl msg = _logoutFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                start = _idx;
                valLen = getValLength();
                msg.setSenderCompId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.SenderSubID:         // tag50
                start = _idx;
                valLen = getValLength();
                msg.setSenderSubId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                start = _idx;
                valLen = getValLength();
                msg.setTargetCompId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.TargetSubID:         // tag57
                start = _idx;
                valLen = getValLength();
                msg.setTargetSubId( _fixMsg, start, valLen );
                break;
            case FixDictionary42.Account:         // SKIP tag1
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case FixDictionary42.ClOrdId:         // SKIP tag11
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.Currency:         // SKIP tag15
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case FixDictionary42.HandlInst:         // SKIP tag21
            case FixDictionary42.SecurityIDSource:         // SKIP tag22
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrderId:         // SKIP tag37
            case FixDictionary42.OrderQty:         // SKIP tag38
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case FixDictionary42.OrdType:         // SKIP tag40
            case FixDictionary42.OrigClOrdId:         // SKIP tag41
            case 42:         // SKIP tag42
            case FixDictionary42.Price:         // SKIP tag44
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case FixDictionary42.Rule80A:         // SKIP tag47
            case FixDictionary42.SecurityID:         // SKIP tag48
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
            case FixDictionary42.Side:         // SKIP tag54
            case FixDictionary42.Symbol:         // SKIP tag55
            case FixDictionary42.SrcLinkId:         // SKIP tag58
            case FixDictionary42.TimeInForce:         // SKIP tag59
            case FixDictionary42.TransactTime:         // SKIP tag60
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.lastMsgSeqNumProcessed:         // tag369
                    msg.setLastMsgSeqNumProcessed( getIntVal() );
                    break;
                case FixDictionary42.NextExpectedMsgSeqNum:         // tag789
                    msg.setNextExpectedMsgSeqNum( getIntVal() );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        return msg;
    }

    public final Message decodeSessionReject() {
        final SessionRejectImpl msg = _sessionRejectFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.RefSeqNum:         // tag45
                msg.setRefSeqNum( getIntVal() );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                getValLength();
                break;
            case FixDictionary42.SenderSubID:         // tag50
                getValLength();
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                getValLength();
                break;
            case FixDictionary42.TargetSubID:         // tag57
                getValLength();
                break;
            case FixDictionary42.SrcLinkId:         // tag58
                start = _idx;
                valLen = getValLength();
                msg.setText( _fixMsg, start, valLen );
                break;
            case FixDictionary42.Account:         // SKIP tag1
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case FixDictionary42.ClOrdId:         // SKIP tag11
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.Currency:         // SKIP tag15
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case FixDictionary42.HandlInst:         // SKIP tag21
            case FixDictionary42.SecurityIDSource:         // SKIP tag22
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrderId:         // SKIP tag37
            case FixDictionary42.OrderQty:         // SKIP tag38
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case FixDictionary42.OrdType:         // SKIP tag40
            case FixDictionary42.OrigClOrdId:         // SKIP tag41
            case 42:         // SKIP tag42
            case FixDictionary42.Price:         // SKIP tag44
            case 46:         // SKIP tag46
            case FixDictionary42.Rule80A:         // SKIP tag47
            case FixDictionary42.SecurityID:         // SKIP tag48
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
            case FixDictionary42.Side:         // SKIP tag54
            case FixDictionary42.Symbol:         // SKIP tag55
            case FixDictionary42.TimeInForce:         // SKIP tag59
            case FixDictionary42.TransactTime:         // SKIP tag60
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.RefTagID:         // tag371
                    msg.setRefTagID( getIntVal() );
                    break;
                case FixDictionary42.RefMsgType:         // tag372
                    start = _idx;
                    valLen = getValLength();
                    msg.setRefMsgType( _fixMsg, start, valLen );
                    break;
                case FixDictionary42.SessionRejectReason:         // tag373
                    start = _idx;
                    valLen = getValLength();
                    msg.setSessionRejectReason( SessionRejectReason.getVal( _fixMsg, start, valLen ) );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        return msg;
    }

    public final Message decodeResendRequest() {
        final ResendRequestImpl msg = _resendRequestFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.BeginSeqNo:         // tag7
                msg.setBeginSeqNo( getIntVal() );
                break;
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.EndSeqNo:         // tag16
                msg.setEndSeqNo( getIntVal() );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                getValLength();
                break;
            case FixDictionary42.SenderSubID:         // tag50
                getValLength();
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                getValLength();
                break;
            case FixDictionary42.TargetSubID:         // tag57
                getValLength();
                break;
            case FixDictionary42.Account:         // SKIP tag1
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case FixDictionary42.ClOrdId:         // SKIP tag11
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.Currency:         // SKIP tag15
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case FixDictionary42.HandlInst:         // SKIP tag21
            case FixDictionary42.SecurityIDSource:         // SKIP tag22
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrderId:         // SKIP tag37
            case FixDictionary42.OrderQty:         // SKIP tag38
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case FixDictionary42.OrdType:         // SKIP tag40
            case FixDictionary42.OrigClOrdId:         // SKIP tag41
            case 42:         // SKIP tag42
            case FixDictionary42.Price:         // SKIP tag44
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case FixDictionary42.Rule80A:         // SKIP tag47
            case FixDictionary42.SecurityID:         // SKIP tag48
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
            case FixDictionary42.Side:         // SKIP tag54
            case FixDictionary42.Symbol:         // SKIP tag55
            case FixDictionary42.SrcLinkId:         // SKIP tag58
            case FixDictionary42.TimeInForce:         // SKIP tag59
            case FixDictionary42.TransactTime:         // SKIP tag60
                getValLength();
                break;
            default:
                getValLength();
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        return msg;
    }

    public final Message decodeSequenceReset() {
        final SequenceResetImpl msg = _sequenceResetFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.NewSeqNo:         // tag36
                msg.setNewSeqNo( getIntVal() );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                getValLength();
                break;
            case FixDictionary42.SenderSubID:         // tag50
                getValLength();
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                getValLength();
                break;
            case FixDictionary42.TargetSubID:         // tag57
                getValLength();
                break;
            case FixDictionary42.Account:         // SKIP tag1
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case FixDictionary42.ClOrdId:         // SKIP tag11
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.Currency:         // SKIP tag15
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case FixDictionary42.HandlInst:         // SKIP tag21
            case FixDictionary42.SecurityIDSource:         // SKIP tag22
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.OrderId:         // SKIP tag37
            case FixDictionary42.OrderQty:         // SKIP tag38
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case FixDictionary42.OrdType:         // SKIP tag40
            case FixDictionary42.OrigClOrdId:         // SKIP tag41
            case 42:         // SKIP tag42
            case FixDictionary42.Price:         // SKIP tag44
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case FixDictionary42.Rule80A:         // SKIP tag47
            case FixDictionary42.SecurityID:         // SKIP tag48
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
            case FixDictionary42.Side:         // SKIP tag54
            case FixDictionary42.Symbol:         // SKIP tag55
            case FixDictionary42.SrcLinkId:         // SKIP tag58
            case FixDictionary42.TimeInForce:         // SKIP tag59
            case FixDictionary42.TransactTime:         // SKIP tag60
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.GapFillFlag:         // tag123
                    msg.setGapFillFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        return msg;
    }

    public final Message decodeTestRequest() {
        final TestRequestImpl msg = _testRequestFactory.get();
        _tag = getTag();

        int start;
        int valLen;

        while( _tag != 0 ) {
            switch( _tag ) {
            case FixDictionary42.CheckSum:         // tag10
                validateChecksum( getIntVal() );
                break;
            case FixDictionary42.MsgSeqNum:         // tag34
                msg.setMsgSeqNum( getIntVal() );
                break;
            case FixDictionary42.PossDupFlag:         // tag43
                msg.setPossDupFlag( (_fixMsg[_idx++]=='Y') ? true : false );
                break;
            case FixDictionary42.SenderCompID:         // tag49
                getValLength();
                break;
            case FixDictionary42.SenderSubID:         // tag50
                getValLength();
                break;
            case FixDictionary42.SendingTime:         // tag52
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case FixDictionary42.TargetCompID:         // tag56
                getValLength();
                break;
            case FixDictionary42.TargetSubID:         // tag57
                getValLength();
                break;
            case FixDictionary42.Account:         // SKIP tag1
            case 2:         // SKIP tag2
            case 3:         // SKIP tag3
            case 4:         // SKIP tag4
            case 5:         // SKIP tag5
            case FixDictionary42.AvgPx:         // SKIP tag6
            case FixDictionary42.BeginSeqNo:         // SKIP tag7
            case FixDictionary42.BeginString:         // SKIP tag8
            case FixDictionary42.BodyLength:         // SKIP tag9
            case FixDictionary42.ClOrdId:         // SKIP tag11
            case 12:         // SKIP tag12
            case 13:         // SKIP tag13
            case FixDictionary42.CumQty:         // SKIP tag14
            case FixDictionary42.Currency:         // SKIP tag15
            case FixDictionary42.EndSeqNo:         // SKIP tag16
            case FixDictionary42.ExecID:         // SKIP tag17
            case FixDictionary42.ExecInst:         // SKIP tag18
            case FixDictionary42.ExecRefID:         // SKIP tag19
            case FixDictionary42.ExecTransType:         // SKIP tag20
            case FixDictionary42.HandlInst:         // SKIP tag21
            case FixDictionary42.SecurityIDSource:         // SKIP tag22
            case 23:         // SKIP tag23
            case 24:         // SKIP tag24
            case 25:         // SKIP tag25
            case 26:         // SKIP tag26
            case 27:         // SKIP tag27
            case 28:         // SKIP tag28
            case 29:         // SKIP tag29
            case FixDictionary42.LastMkt:         // SKIP tag30
            case FixDictionary42.LastPx:         // SKIP tag31
            case FixDictionary42.LastQty:         // SKIP tag32
            case 33:         // SKIP tag33
            case FixDictionary42.MsgType:         // SKIP tag35
            case FixDictionary42.NewSeqNo:         // SKIP tag36
            case FixDictionary42.OrderId:         // SKIP tag37
            case FixDictionary42.OrderQty:         // SKIP tag38
            case FixDictionary42.OrdStatus:         // SKIP tag39
            case FixDictionary42.OrdType:         // SKIP tag40
            case FixDictionary42.OrigClOrdId:         // SKIP tag41
            case 42:         // SKIP tag42
            case FixDictionary42.Price:         // SKIP tag44
            case FixDictionary42.RefSeqNum:         // SKIP tag45
            case 46:         // SKIP tag46
            case FixDictionary42.Rule80A:         // SKIP tag47
            case FixDictionary42.SecurityID:         // SKIP tag48
            case 51:         // SKIP tag51
            case 53:         // SKIP tag53
            case FixDictionary42.Side:         // SKIP tag54
            case FixDictionary42.Symbol:         // SKIP tag55
            case FixDictionary42.SrcLinkId:         // SKIP tag58
            case FixDictionary42.TimeInForce:         // SKIP tag59
            case FixDictionary42.TransactTime:         // SKIP tag60
                getValLength();
                break;
            default:
                switch( _tag ) {
                case FixDictionary42.testReqID:         // tag112
                    start = _idx;
                    valLen = getValLength();
                    msg.setTestReqID( _fixMsg, start, valLen );
                    break;
                default:
                    getValLength();
                    break;
                }
                break;
            }
            _idx++; /* past delimiter */ 
            _tag = getTag();
        }

        return msg;
    }


   // SubGrps

   // transform methods
         /**     * PostPend  Common Decoder File     */         private void enrich( ClientNewOrderSingleImpl nos ) {                Instrument instr = lookupInst( nos );        final Currency         clientCcy = nos.getCurrency();                if ( clientCcy == null ) {            nos.setCurrency( instr.getCurrency() );        }                nos.setInstrument( instr );        nos.setClient( _clientProfile );                // override any secClOrdId with this client clOrdId ... required as some downstream exchanges have restrictions on the clOrdId                final ViewString clOrdId = nos.getClOrdId();        nos.setSrcLinkId( clOrdId.getOffset(), clOrdId.length() );    }    private void enrich( ClientCancelReplaceRequestImpl rep ) {                rep.setClient( _clientProfile );                final ViewString clOrdId = rep.getClOrdId();        rep.getSrcLinkIdForUpdate().setValue( clOrdId.getBytes(), clOrdId.getOffset(), clOrdId.length() );    }    private void enrich( ClientCancelRequestImpl can ) {                can.setClient( _clientProfile );        final ViewString clOrdId = can.getClOrdId();        can.getSrcLinkIdForUpdate().setValue( clOrdId.getBytes(), clOrdId.getOffset(), clOrdId.length() );    }    private void enrich( RecoveryNewOrderSingleImpl nos ) {                final Instrument instr = lookupInst( nos );        final Currency         clientCcy = nos.getCurrency();                if ( clientCcy == null ) {            nos.setCurrency( instr.getCurrency() );        }                nos.setInstrument( instr );        nos.setClient( _clientProfile );        // override any secClOrdId with this client clOrdId ... required as some downstream exchanges have restrictions on the clOrdId                final ViewString clOrdId = nos.getClOrdId();        nos.setSrcLinkId( clOrdId.getBytes(), clOrdId.getOffset(), clOrdId.length() );    }    private void enrich( RecoveryCancelReplaceRequestImpl rep ) {                rep.setClient( _clientProfile );                final ViewString clOrdId = rep.getClOrdId();        rep.getSrcLinkIdForUpdate().setValue( clOrdId.getBytes(), clOrdId.getOffset(), clOrdId.length() );    }    private void enrich( RecoveryCancelRequestImpl can ) {                can.setClient( _clientProfile );        final ViewString clOrdId = can.getClOrdId();        can.getSrcLinkIdForUpdate().setValue( clOrdId.getBytes(), clOrdId.getOffset(), clOrdId.length() );    }        private Instrument lookupInst( final NewOrderSingle nos ) {        final Currency         clientCcy = nos.getCurrency();        final SecurityIDSource src       = nos.getSecurityIDSource();            Instrument instr;        if ( src == null ) {            instr = _instrumentLocator.getInstrumentBySymbol( nos.getSymbol(),                                                               nos.getExDest(),                                                               nos.getSecurityExchange(),                                                              clientCcy );        } else {            instr = _instrumentLocator.getInstrument( nos.getSecurityId(),                                                       src,                                                       nos.getExDest(),                                                       nos.getSecurityExchange(),                                                      clientCcy );        }                if ( instr == null ) {            throwDecodeException( "Instrument not found" );        }         return instr;    }    private void preExecMessageDetermination() {        switch( _execTransType.getID() ) {        case TypeIds.EXECTRANSTYPE_NEW:            if ( _execType == ExecType.PartialFill || _execType == ExecType.Fill ){                _execType = ExecType.Trade;                return;            }            break;        case TypeIds.EXECTRANSTYPE_CANCEL:            _execType = ExecType.TradeCancel;            break;        case TypeIds.EXECTRANSTYPE_CORRECT:            _execType = ExecType.TradeCorrect;            break;        case TypeIds.EXECTRANSTYPE_STATUS:            _execType = ExecType.OrderStatus;            break;        case TypeIds.EXECTRANSTYPE_UNKNOWN:            throwDecodeException( "ExecRpt missing valid ExecTransType " );            break;        }    }    private OrderCapacity decodeRule47A( byte b ) {        if ( b == 'P' ) {            return OrderCapacity.Principal;        } else if ( b == 'A' ){            return OrderCapacity.AgentForOtherMember;        }        return OrderCapacity.Principal;    }/* * HANDCODED DECODER METHDOS */}
