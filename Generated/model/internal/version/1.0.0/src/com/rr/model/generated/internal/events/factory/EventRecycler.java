/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.factory;

import com.rr.model.generated.internal.events.impl.*;
import com.rr.model.generated.internal.events.recycle.*;
import com.rr.model.generated.internal.core.FullEventIds;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.HasReusableType;

public class EventRecycler implements MessageRecycler {


    private ClientNewOrderSingleRecycler _clientNewOrderSingleRecycler;
    private MarketNewOrderSingleRecycler _marketNewOrderSingleRecycler;

    private RecoveryNewOrderSingleRecycler _recoveryNewOrderSingleRecycler;

    private ClientCancelReplaceRequestRecycler _clientCancelReplaceRequestRecycler;
    private MarketCancelReplaceRequestRecycler _marketCancelReplaceRequestRecycler;

    private RecoveryCancelReplaceRequestRecycler _recoveryCancelReplaceRequestRecycler;

    private ClientCancelRequestRecycler _clientCancelRequestRecycler;
    private MarketCancelRequestRecycler _marketCancelRequestRecycler;

    private RecoveryCancelRequestRecycler _recoveryCancelRequestRecycler;

    private ClientForceCancelRecycler _clientForceCancelRecycler;
    private MarketForceCancelRecycler _marketForceCancelRecycler;

    private RecoveryForceCancelRecycler _recoveryForceCancelRecycler;

    private ClientVagueOrderRejectRecycler _clientVagueOrderRejectRecycler;
    private MarketVagueOrderRejectRecycler _marketVagueOrderRejectRecycler;

    private RecoveryVagueOrderRejectRecycler _recoveryVagueOrderRejectRecycler;

    private ClientCancelRejectRecycler _clientCancelRejectRecycler;
    private MarketCancelRejectRecycler _marketCancelRejectRecycler;

    private RecoveryCancelRejectRecycler _recoveryCancelRejectRecycler;

    private ClientNewOrderAckRecycler _clientNewOrderAckRecycler;
    private MarketNewOrderAckRecycler _marketNewOrderAckRecycler;

    private RecoveryNewOrderAckRecycler _recoveryNewOrderAckRecycler;

    private ClientTradeNewRecycler _clientTradeNewRecycler;
    private MarketTradeNewRecycler _marketTradeNewRecycler;

    private RecoveryTradeNewRecycler _recoveryTradeNewRecycler;

    private ClientRejectedRecycler _clientRejectedRecycler;
    private MarketRejectedRecycler _marketRejectedRecycler;

    private RecoveryRejectedRecycler _recoveryRejectedRecycler;

    private ClientCancelledRecycler _clientCancelledRecycler;
    private MarketCancelledRecycler _marketCancelledRecycler;

    private RecoveryCancelledRecycler _recoveryCancelledRecycler;

    private ClientReplacedRecycler _clientReplacedRecycler;
    private MarketReplacedRecycler _marketReplacedRecycler;

    private RecoveryReplacedRecycler _recoveryReplacedRecycler;

    private ClientDoneForDayRecycler _clientDoneForDayRecycler;
    private MarketDoneForDayRecycler _marketDoneForDayRecycler;

    private RecoveryDoneForDayRecycler _recoveryDoneForDayRecycler;

    private ClientStoppedRecycler _clientStoppedRecycler;
    private MarketStoppedRecycler _marketStoppedRecycler;

    private RecoveryStoppedRecycler _recoveryStoppedRecycler;

    private ClientExpiredRecycler _clientExpiredRecycler;
    private MarketExpiredRecycler _marketExpiredRecycler;

    private RecoveryExpiredRecycler _recoveryExpiredRecycler;

    private ClientSuspendedRecycler _clientSuspendedRecycler;
    private MarketSuspendedRecycler _marketSuspendedRecycler;

    private RecoverySuspendedRecycler _recoverySuspendedRecycler;

    private ClientRestatedRecycler _clientRestatedRecycler;
    private MarketRestatedRecycler _marketRestatedRecycler;

    private RecoveryRestatedRecycler _recoveryRestatedRecycler;

    private ClientTradeCorrectRecycler _clientTradeCorrectRecycler;
    private MarketTradeCorrectRecycler _marketTradeCorrectRecycler;

    private RecoveryTradeCorrectRecycler _recoveryTradeCorrectRecycler;

    private ClientTradeCancelRecycler _clientTradeCancelRecycler;
    private MarketTradeCancelRecycler _marketTradeCancelRecycler;

    private RecoveryTradeCancelRecycler _recoveryTradeCancelRecycler;

    private ClientOrderStatusRecycler _clientOrderStatusRecycler;
    private MarketOrderStatusRecycler _marketOrderStatusRecycler;

    private RecoveryOrderStatusRecycler _recoveryOrderStatusRecycler;

    private HeartbeatRecycler _heartbeatRecycler;
    private TestRequestRecycler _testRequestRecycler;
    private LogonRecycler _logonRecycler;
    private LogoutRecycler _logoutRecycler;
    private SessionRejectRecycler _sessionRejectRecycler;
    private ResendRequestRecycler _resendRequestRecycler;
    private ClientResyncSentMsgsRecycler _clientResyncSentMsgsRecycler;
    private SequenceResetRecycler _sequenceResetRecycler;
    private TradingSessionStatusRecycler _tradingSessionStatusRecycler;
    private SecMassStatGrpRecycler _secMassStatGrpRecycler;
    private MassInstrumentStateChangeRecycler _massInstrumentStateChangeRecycler;
    private ClientAlertLimitBreachRecycler _clientAlertLimitBreachRecycler;
    private MarketAlertLimitBreachRecycler _marketAlertLimitBreachRecycler;

    private RecoveryAlertLimitBreachRecycler _recoveryAlertLimitBreachRecycler;

    private ClientAlertTradeMissingOrdersRecycler _clientAlertTradeMissingOrdersRecycler;
    private MarketAlertTradeMissingOrdersRecycler _marketAlertTradeMissingOrdersRecycler;

    private RecoveryAlertTradeMissingOrdersRecycler _recoveryAlertTradeMissingOrdersRecycler;

    private StratInstrumentStateRecycler _stratInstrumentStateRecycler;
    private StrategyStateRecycler _strategyStateRecycler;
    private UTPLogonRecycler _uTPLogonRecycler;
    private UTPLogonRejectRecycler _uTPLogonRejectRecycler;
    private UTPTradingSessionStatusRecycler _uTPTradingSessionStatusRecycler;
    private ETIConnectionGatewayRequestRecycler _eTIConnectionGatewayRequestRecycler;
    private ETIConnectionGatewayResponseRecycler _eTIConnectionGatewayResponseRecycler;
    private ETISessionLogonRequestRecycler _eTISessionLogonRequestRecycler;
    private ETISessionLogonResponseRecycler _eTISessionLogonResponseRecycler;
    private ETISessionLogoutRequestRecycler _eTISessionLogoutRequestRecycler;
    private ETISessionLogoutResponseRecycler _eTISessionLogoutResponseRecycler;
    private ETISessionLogoutNotificationRecycler _eTISessionLogoutNotificationRecycler;
    private ETIUserLogonRequestRecycler _eTIUserLogonRequestRecycler;
    private ETIUserLogonResponseRecycler _eTIUserLogonResponseRecycler;
    private ETIUserLogoutRequestRecycler _eTIUserLogoutRequestRecycler;
    private ETIUserLogoutResponseRecycler _eTIUserLogoutResponseRecycler;
    private ETIThrottleUpdateNotificationRecycler _eTIThrottleUpdateNotificationRecycler;
    private ETISubscribeRecycler _eTISubscribeRecycler;
    private ETISubscribeResponseRecycler _eTISubscribeResponseRecycler;
    private ETIUnsubscribeRecycler _eTIUnsubscribeRecycler;
    private ETIUnsubscribeResponseRecycler _eTIUnsubscribeResponseRecycler;
    private ETIRetransmitRecycler _eTIRetransmitRecycler;
    private ETIRetransmitResponseRecycler _eTIRetransmitResponseRecycler;
    private ETIRetransmitOrderEventsRecycler _eTIRetransmitOrderEventsRecycler;
    private ETIRetransmitOrderEventsResponseRecycler _eTIRetransmitOrderEventsResponseRecycler;
    private MilleniumLogonRecycler _milleniumLogonRecycler;
    private MilleniumLogonReplyRecycler _milleniumLogonReplyRecycler;
    private MilleniumLogoutRecycler _milleniumLogoutRecycler;
    private MilleniumMissedMessageRequestRecycler _milleniumMissedMessageRequestRecycler;
    private MilleniumMissedMsgRequestAckRecycler _milleniumMissedMsgRequestAckRecycler;
    private MilleniumMissedMsgReportRecycler _milleniumMissedMsgReportRecycler;
    private BookAddOrderRecycler _bookAddOrderRecycler;
    private BookDeleteOrderRecycler _bookDeleteOrderRecycler;
    private BookModifyOrderRecycler _bookModifyOrderRecycler;
    private BookClearRecycler _bookClearRecycler;
    private SymbolRepeatingGrpRecycler _symbolRepeatingGrpRecycler;
    private MDRequestRecycler _mDRequestRecycler;
    private TickUpdateRecycler _tickUpdateRecycler;
    private MDUpdateRecycler _mDUpdateRecycler;
    private SecDefEventsRecycler _secDefEventsRecycler;
    private SecurityAltIDRecycler _securityAltIDRecycler;
    private SDFeedTypeRecycler _sDFeedTypeRecycler;
    private SecDefLegRecycler _secDefLegRecycler;
    private MDEntryRecycler _mDEntryRecycler;
    private MDSnapEntryRecycler _mDSnapEntryRecycler;
    private MsgSeqNumGapRecycler _msgSeqNumGapRecycler;
    private MDIncRefreshRecycler _mDIncRefreshRecycler;
    private MDSnapshotFullRefreshRecycler _mDSnapshotFullRefreshRecycler;
    private SecurityDefinitionRecycler _securityDefinitionRecycler;
    private SecurityDefinitionUpdateRecycler _securityDefinitionUpdateRecycler;
    private ProductSnapshotRecycler _productSnapshotRecycler;
    private SecurityStatusRecycler _securityStatusRecycler;

    public EventRecycler() {
        SuperpoolManager sp = SuperpoolManager.instance();
        _clientNewOrderSingleRecycler = sp.getRecycler( ClientNewOrderSingleRecycler.class, ClientNewOrderSingleImpl.class );
        _marketNewOrderSingleRecycler = sp.getRecycler( MarketNewOrderSingleRecycler.class, MarketNewOrderSingleImpl.class );

        _recoveryNewOrderSingleRecycler = sp.getRecycler( RecoveryNewOrderSingleRecycler.class, RecoveryNewOrderSingleImpl.class );

        _clientCancelReplaceRequestRecycler = sp.getRecycler( ClientCancelReplaceRequestRecycler.class, ClientCancelReplaceRequestImpl.class );
        _marketCancelReplaceRequestRecycler = sp.getRecycler( MarketCancelReplaceRequestRecycler.class, MarketCancelReplaceRequestImpl.class );

        _recoveryCancelReplaceRequestRecycler = sp.getRecycler( RecoveryCancelReplaceRequestRecycler.class, RecoveryCancelReplaceRequestImpl.class );

        _clientCancelRequestRecycler = sp.getRecycler( ClientCancelRequestRecycler.class, ClientCancelRequestImpl.class );
        _marketCancelRequestRecycler = sp.getRecycler( MarketCancelRequestRecycler.class, MarketCancelRequestImpl.class );

        _recoveryCancelRequestRecycler = sp.getRecycler( RecoveryCancelRequestRecycler.class, RecoveryCancelRequestImpl.class );

        _clientForceCancelRecycler = sp.getRecycler( ClientForceCancelRecycler.class, ClientForceCancelImpl.class );
        _marketForceCancelRecycler = sp.getRecycler( MarketForceCancelRecycler.class, MarketForceCancelImpl.class );

        _recoveryForceCancelRecycler = sp.getRecycler( RecoveryForceCancelRecycler.class, RecoveryForceCancelImpl.class );

        _clientVagueOrderRejectRecycler = sp.getRecycler( ClientVagueOrderRejectRecycler.class, ClientVagueOrderRejectImpl.class );
        _marketVagueOrderRejectRecycler = sp.getRecycler( MarketVagueOrderRejectRecycler.class, MarketVagueOrderRejectImpl.class );

        _recoveryVagueOrderRejectRecycler = sp.getRecycler( RecoveryVagueOrderRejectRecycler.class, RecoveryVagueOrderRejectImpl.class );

        _clientCancelRejectRecycler = sp.getRecycler( ClientCancelRejectRecycler.class, ClientCancelRejectImpl.class );
        _marketCancelRejectRecycler = sp.getRecycler( MarketCancelRejectRecycler.class, MarketCancelRejectImpl.class );

        _recoveryCancelRejectRecycler = sp.getRecycler( RecoveryCancelRejectRecycler.class, RecoveryCancelRejectImpl.class );

        _clientNewOrderAckRecycler = sp.getRecycler( ClientNewOrderAckRecycler.class, ClientNewOrderAckImpl.class );
        _marketNewOrderAckRecycler = sp.getRecycler( MarketNewOrderAckRecycler.class, MarketNewOrderAckImpl.class );

        _recoveryNewOrderAckRecycler = sp.getRecycler( RecoveryNewOrderAckRecycler.class, RecoveryNewOrderAckImpl.class );

        _clientTradeNewRecycler = sp.getRecycler( ClientTradeNewRecycler.class, ClientTradeNewImpl.class );
        _marketTradeNewRecycler = sp.getRecycler( MarketTradeNewRecycler.class, MarketTradeNewImpl.class );

        _recoveryTradeNewRecycler = sp.getRecycler( RecoveryTradeNewRecycler.class, RecoveryTradeNewImpl.class );

        _clientRejectedRecycler = sp.getRecycler( ClientRejectedRecycler.class, ClientRejectedImpl.class );
        _marketRejectedRecycler = sp.getRecycler( MarketRejectedRecycler.class, MarketRejectedImpl.class );

        _recoveryRejectedRecycler = sp.getRecycler( RecoveryRejectedRecycler.class, RecoveryRejectedImpl.class );

        _clientCancelledRecycler = sp.getRecycler( ClientCancelledRecycler.class, ClientCancelledImpl.class );
        _marketCancelledRecycler = sp.getRecycler( MarketCancelledRecycler.class, MarketCancelledImpl.class );

        _recoveryCancelledRecycler = sp.getRecycler( RecoveryCancelledRecycler.class, RecoveryCancelledImpl.class );

        _clientReplacedRecycler = sp.getRecycler( ClientReplacedRecycler.class, ClientReplacedImpl.class );
        _marketReplacedRecycler = sp.getRecycler( MarketReplacedRecycler.class, MarketReplacedImpl.class );

        _recoveryReplacedRecycler = sp.getRecycler( RecoveryReplacedRecycler.class, RecoveryReplacedImpl.class );

        _clientDoneForDayRecycler = sp.getRecycler( ClientDoneForDayRecycler.class, ClientDoneForDayImpl.class );
        _marketDoneForDayRecycler = sp.getRecycler( MarketDoneForDayRecycler.class, MarketDoneForDayImpl.class );

        _recoveryDoneForDayRecycler = sp.getRecycler( RecoveryDoneForDayRecycler.class, RecoveryDoneForDayImpl.class );

        _clientStoppedRecycler = sp.getRecycler( ClientStoppedRecycler.class, ClientStoppedImpl.class );
        _marketStoppedRecycler = sp.getRecycler( MarketStoppedRecycler.class, MarketStoppedImpl.class );

        _recoveryStoppedRecycler = sp.getRecycler( RecoveryStoppedRecycler.class, RecoveryStoppedImpl.class );

        _clientExpiredRecycler = sp.getRecycler( ClientExpiredRecycler.class, ClientExpiredImpl.class );
        _marketExpiredRecycler = sp.getRecycler( MarketExpiredRecycler.class, MarketExpiredImpl.class );

        _recoveryExpiredRecycler = sp.getRecycler( RecoveryExpiredRecycler.class, RecoveryExpiredImpl.class );

        _clientSuspendedRecycler = sp.getRecycler( ClientSuspendedRecycler.class, ClientSuspendedImpl.class );
        _marketSuspendedRecycler = sp.getRecycler( MarketSuspendedRecycler.class, MarketSuspendedImpl.class );

        _recoverySuspendedRecycler = sp.getRecycler( RecoverySuspendedRecycler.class, RecoverySuspendedImpl.class );

        _clientRestatedRecycler = sp.getRecycler( ClientRestatedRecycler.class, ClientRestatedImpl.class );
        _marketRestatedRecycler = sp.getRecycler( MarketRestatedRecycler.class, MarketRestatedImpl.class );

        _recoveryRestatedRecycler = sp.getRecycler( RecoveryRestatedRecycler.class, RecoveryRestatedImpl.class );

        _clientTradeCorrectRecycler = sp.getRecycler( ClientTradeCorrectRecycler.class, ClientTradeCorrectImpl.class );
        _marketTradeCorrectRecycler = sp.getRecycler( MarketTradeCorrectRecycler.class, MarketTradeCorrectImpl.class );

        _recoveryTradeCorrectRecycler = sp.getRecycler( RecoveryTradeCorrectRecycler.class, RecoveryTradeCorrectImpl.class );

        _clientTradeCancelRecycler = sp.getRecycler( ClientTradeCancelRecycler.class, ClientTradeCancelImpl.class );
        _marketTradeCancelRecycler = sp.getRecycler( MarketTradeCancelRecycler.class, MarketTradeCancelImpl.class );

        _recoveryTradeCancelRecycler = sp.getRecycler( RecoveryTradeCancelRecycler.class, RecoveryTradeCancelImpl.class );

        _clientOrderStatusRecycler = sp.getRecycler( ClientOrderStatusRecycler.class, ClientOrderStatusImpl.class );
        _marketOrderStatusRecycler = sp.getRecycler( MarketOrderStatusRecycler.class, MarketOrderStatusImpl.class );

        _recoveryOrderStatusRecycler = sp.getRecycler( RecoveryOrderStatusRecycler.class, RecoveryOrderStatusImpl.class );

        _heartbeatRecycler = sp.getRecycler( HeartbeatRecycler.class, HeartbeatImpl.class );
        _testRequestRecycler = sp.getRecycler( TestRequestRecycler.class, TestRequestImpl.class );
        _logonRecycler = sp.getRecycler( LogonRecycler.class, LogonImpl.class );
        _logoutRecycler = sp.getRecycler( LogoutRecycler.class, LogoutImpl.class );
        _sessionRejectRecycler = sp.getRecycler( SessionRejectRecycler.class, SessionRejectImpl.class );
        _resendRequestRecycler = sp.getRecycler( ResendRequestRecycler.class, ResendRequestImpl.class );
        _clientResyncSentMsgsRecycler = sp.getRecycler( ClientResyncSentMsgsRecycler.class, ClientResyncSentMsgsImpl.class );
        _sequenceResetRecycler = sp.getRecycler( SequenceResetRecycler.class, SequenceResetImpl.class );
        _tradingSessionStatusRecycler = sp.getRecycler( TradingSessionStatusRecycler.class, TradingSessionStatusImpl.class );
        _secMassStatGrpRecycler = sp.getRecycler( SecMassStatGrpRecycler.class, SecMassStatGrpImpl.class );
        _massInstrumentStateChangeRecycler = sp.getRecycler( MassInstrumentStateChangeRecycler.class, MassInstrumentStateChangeImpl.class );
        _clientAlertLimitBreachRecycler = sp.getRecycler( ClientAlertLimitBreachRecycler.class, ClientAlertLimitBreachImpl.class );
        _marketAlertLimitBreachRecycler = sp.getRecycler( MarketAlertLimitBreachRecycler.class, MarketAlertLimitBreachImpl.class );

        _recoveryAlertLimitBreachRecycler = sp.getRecycler( RecoveryAlertLimitBreachRecycler.class, RecoveryAlertLimitBreachImpl.class );

        _clientAlertTradeMissingOrdersRecycler = sp.getRecycler( ClientAlertTradeMissingOrdersRecycler.class, ClientAlertTradeMissingOrdersImpl.class );
        _marketAlertTradeMissingOrdersRecycler = sp.getRecycler( MarketAlertTradeMissingOrdersRecycler.class, MarketAlertTradeMissingOrdersImpl.class );

        _recoveryAlertTradeMissingOrdersRecycler = sp.getRecycler( RecoveryAlertTradeMissingOrdersRecycler.class, RecoveryAlertTradeMissingOrdersImpl.class );

        _stratInstrumentStateRecycler = sp.getRecycler( StratInstrumentStateRecycler.class, StratInstrumentStateImpl.class );
        _strategyStateRecycler = sp.getRecycler( StrategyStateRecycler.class, StrategyStateImpl.class );
        _uTPLogonRecycler = sp.getRecycler( UTPLogonRecycler.class, UTPLogonImpl.class );
        _uTPLogonRejectRecycler = sp.getRecycler( UTPLogonRejectRecycler.class, UTPLogonRejectImpl.class );
        _uTPTradingSessionStatusRecycler = sp.getRecycler( UTPTradingSessionStatusRecycler.class, UTPTradingSessionStatusImpl.class );
        _eTIConnectionGatewayRequestRecycler = sp.getRecycler( ETIConnectionGatewayRequestRecycler.class, ETIConnectionGatewayRequestImpl.class );
        _eTIConnectionGatewayResponseRecycler = sp.getRecycler( ETIConnectionGatewayResponseRecycler.class, ETIConnectionGatewayResponseImpl.class );
        _eTISessionLogonRequestRecycler = sp.getRecycler( ETISessionLogonRequestRecycler.class, ETISessionLogonRequestImpl.class );
        _eTISessionLogonResponseRecycler = sp.getRecycler( ETISessionLogonResponseRecycler.class, ETISessionLogonResponseImpl.class );
        _eTISessionLogoutRequestRecycler = sp.getRecycler( ETISessionLogoutRequestRecycler.class, ETISessionLogoutRequestImpl.class );
        _eTISessionLogoutResponseRecycler = sp.getRecycler( ETISessionLogoutResponseRecycler.class, ETISessionLogoutResponseImpl.class );
        _eTISessionLogoutNotificationRecycler = sp.getRecycler( ETISessionLogoutNotificationRecycler.class, ETISessionLogoutNotificationImpl.class );
        _eTIUserLogonRequestRecycler = sp.getRecycler( ETIUserLogonRequestRecycler.class, ETIUserLogonRequestImpl.class );
        _eTIUserLogonResponseRecycler = sp.getRecycler( ETIUserLogonResponseRecycler.class, ETIUserLogonResponseImpl.class );
        _eTIUserLogoutRequestRecycler = sp.getRecycler( ETIUserLogoutRequestRecycler.class, ETIUserLogoutRequestImpl.class );
        _eTIUserLogoutResponseRecycler = sp.getRecycler( ETIUserLogoutResponseRecycler.class, ETIUserLogoutResponseImpl.class );
        _eTIThrottleUpdateNotificationRecycler = sp.getRecycler( ETIThrottleUpdateNotificationRecycler.class, ETIThrottleUpdateNotificationImpl.class );
        _eTISubscribeRecycler = sp.getRecycler( ETISubscribeRecycler.class, ETISubscribeImpl.class );
        _eTISubscribeResponseRecycler = sp.getRecycler( ETISubscribeResponseRecycler.class, ETISubscribeResponseImpl.class );
        _eTIUnsubscribeRecycler = sp.getRecycler( ETIUnsubscribeRecycler.class, ETIUnsubscribeImpl.class );
        _eTIUnsubscribeResponseRecycler = sp.getRecycler( ETIUnsubscribeResponseRecycler.class, ETIUnsubscribeResponseImpl.class );
        _eTIRetransmitRecycler = sp.getRecycler( ETIRetransmitRecycler.class, ETIRetransmitImpl.class );
        _eTIRetransmitResponseRecycler = sp.getRecycler( ETIRetransmitResponseRecycler.class, ETIRetransmitResponseImpl.class );
        _eTIRetransmitOrderEventsRecycler = sp.getRecycler( ETIRetransmitOrderEventsRecycler.class, ETIRetransmitOrderEventsImpl.class );
        _eTIRetransmitOrderEventsResponseRecycler = sp.getRecycler( ETIRetransmitOrderEventsResponseRecycler.class, ETIRetransmitOrderEventsResponseImpl.class );
        _milleniumLogonRecycler = sp.getRecycler( MilleniumLogonRecycler.class, MilleniumLogonImpl.class );
        _milleniumLogonReplyRecycler = sp.getRecycler( MilleniumLogonReplyRecycler.class, MilleniumLogonReplyImpl.class );
        _milleniumLogoutRecycler = sp.getRecycler( MilleniumLogoutRecycler.class, MilleniumLogoutImpl.class );
        _milleniumMissedMessageRequestRecycler = sp.getRecycler( MilleniumMissedMessageRequestRecycler.class, MilleniumMissedMessageRequestImpl.class );
        _milleniumMissedMsgRequestAckRecycler = sp.getRecycler( MilleniumMissedMsgRequestAckRecycler.class, MilleniumMissedMsgRequestAckImpl.class );
        _milleniumMissedMsgReportRecycler = sp.getRecycler( MilleniumMissedMsgReportRecycler.class, MilleniumMissedMsgReportImpl.class );
        _bookAddOrderRecycler = sp.getRecycler( BookAddOrderRecycler.class, BookAddOrderImpl.class );
        _bookDeleteOrderRecycler = sp.getRecycler( BookDeleteOrderRecycler.class, BookDeleteOrderImpl.class );
        _bookModifyOrderRecycler = sp.getRecycler( BookModifyOrderRecycler.class, BookModifyOrderImpl.class );
        _bookClearRecycler = sp.getRecycler( BookClearRecycler.class, BookClearImpl.class );
        _symbolRepeatingGrpRecycler = sp.getRecycler( SymbolRepeatingGrpRecycler.class, SymbolRepeatingGrpImpl.class );
        _mDRequestRecycler = sp.getRecycler( MDRequestRecycler.class, MDRequestImpl.class );
        _tickUpdateRecycler = sp.getRecycler( TickUpdateRecycler.class, TickUpdateImpl.class );
        _mDUpdateRecycler = sp.getRecycler( MDUpdateRecycler.class, MDUpdateImpl.class );
        _secDefEventsRecycler = sp.getRecycler( SecDefEventsRecycler.class, SecDefEventsImpl.class );
        _securityAltIDRecycler = sp.getRecycler( SecurityAltIDRecycler.class, SecurityAltIDImpl.class );
        _sDFeedTypeRecycler = sp.getRecycler( SDFeedTypeRecycler.class, SDFeedTypeImpl.class );
        _secDefLegRecycler = sp.getRecycler( SecDefLegRecycler.class, SecDefLegImpl.class );
        _mDEntryRecycler = sp.getRecycler( MDEntryRecycler.class, MDEntryImpl.class );
        _mDSnapEntryRecycler = sp.getRecycler( MDSnapEntryRecycler.class, MDSnapEntryImpl.class );
        _msgSeqNumGapRecycler = sp.getRecycler( MsgSeqNumGapRecycler.class, MsgSeqNumGapImpl.class );
        _mDIncRefreshRecycler = sp.getRecycler( MDIncRefreshRecycler.class, MDIncRefreshImpl.class );
        _mDSnapshotFullRefreshRecycler = sp.getRecycler( MDSnapshotFullRefreshRecycler.class, MDSnapshotFullRefreshImpl.class );
        _securityDefinitionRecycler = sp.getRecycler( SecurityDefinitionRecycler.class, SecurityDefinitionImpl.class );
        _securityDefinitionUpdateRecycler = sp.getRecycler( SecurityDefinitionUpdateRecycler.class, SecurityDefinitionUpdateImpl.class );
        _productSnapshotRecycler = sp.getRecycler( ProductSnapshotRecycler.class, ProductSnapshotImpl.class );
        _securityStatusRecycler = sp.getRecycler( SecurityStatusRecycler.class, SecurityStatusImpl.class );
    }
    public void recycle( ClientNewOrderSingleImpl msg ) {
        _clientNewOrderSingleRecycler.recycle( msg );
    }

    public void recycle( MarketNewOrderSingleImpl msg ) {
        _marketNewOrderSingleRecycler.recycle( msg );
    }

    public void recycle( RecoveryNewOrderSingleImpl msg ) {
        _recoveryNewOrderSingleRecycler.recycle( msg );
    }

    public void recycle( ClientCancelReplaceRequestImpl msg ) {
        _clientCancelReplaceRequestRecycler.recycle( msg );
    }

    public void recycle( MarketCancelReplaceRequestImpl msg ) {
        _marketCancelReplaceRequestRecycler.recycle( msg );
    }

    public void recycle( RecoveryCancelReplaceRequestImpl msg ) {
        _recoveryCancelReplaceRequestRecycler.recycle( msg );
    }

    public void recycle( ClientCancelRequestImpl msg ) {
        _clientCancelRequestRecycler.recycle( msg );
    }

    public void recycle( MarketCancelRequestImpl msg ) {
        _marketCancelRequestRecycler.recycle( msg );
    }

    public void recycle( RecoveryCancelRequestImpl msg ) {
        _recoveryCancelRequestRecycler.recycle( msg );
    }

    public void recycle( ClientForceCancelImpl msg ) {
        _clientForceCancelRecycler.recycle( msg );
    }

    public void recycle( MarketForceCancelImpl msg ) {
        _marketForceCancelRecycler.recycle( msg );
    }

    public void recycle( RecoveryForceCancelImpl msg ) {
        _recoveryForceCancelRecycler.recycle( msg );
    }

    public void recycle( ClientVagueOrderRejectImpl msg ) {
        _clientVagueOrderRejectRecycler.recycle( msg );
    }

    public void recycle( MarketVagueOrderRejectImpl msg ) {
        _marketVagueOrderRejectRecycler.recycle( msg );
    }

    public void recycle( RecoveryVagueOrderRejectImpl msg ) {
        _recoveryVagueOrderRejectRecycler.recycle( msg );
    }

    public void recycle( ClientCancelRejectImpl msg ) {
        _clientCancelRejectRecycler.recycle( msg );
    }

    public void recycle( MarketCancelRejectImpl msg ) {
        _marketCancelRejectRecycler.recycle( msg );
    }

    public void recycle( RecoveryCancelRejectImpl msg ) {
        _recoveryCancelRejectRecycler.recycle( msg );
    }

    public void recycle( ClientNewOrderAckImpl msg ) {
        _clientNewOrderAckRecycler.recycle( msg );
    }

    public void recycle( MarketNewOrderAckImpl msg ) {
        _marketNewOrderAckRecycler.recycle( msg );
    }

    public void recycle( RecoveryNewOrderAckImpl msg ) {
        _recoveryNewOrderAckRecycler.recycle( msg );
    }

    public void recycle( ClientTradeNewImpl msg ) {
        _clientTradeNewRecycler.recycle( msg );
    }

    public void recycle( MarketTradeNewImpl msg ) {
        _marketTradeNewRecycler.recycle( msg );
    }

    public void recycle( RecoveryTradeNewImpl msg ) {
        _recoveryTradeNewRecycler.recycle( msg );
    }

    public void recycle( ClientRejectedImpl msg ) {
        _clientRejectedRecycler.recycle( msg );
    }

    public void recycle( MarketRejectedImpl msg ) {
        _marketRejectedRecycler.recycle( msg );
    }

    public void recycle( RecoveryRejectedImpl msg ) {
        _recoveryRejectedRecycler.recycle( msg );
    }

    public void recycle( ClientCancelledImpl msg ) {
        _clientCancelledRecycler.recycle( msg );
    }

    public void recycle( MarketCancelledImpl msg ) {
        _marketCancelledRecycler.recycle( msg );
    }

    public void recycle( RecoveryCancelledImpl msg ) {
        _recoveryCancelledRecycler.recycle( msg );
    }

    public void recycle( ClientReplacedImpl msg ) {
        _clientReplacedRecycler.recycle( msg );
    }

    public void recycle( MarketReplacedImpl msg ) {
        _marketReplacedRecycler.recycle( msg );
    }

    public void recycle( RecoveryReplacedImpl msg ) {
        _recoveryReplacedRecycler.recycle( msg );
    }

    public void recycle( ClientDoneForDayImpl msg ) {
        _clientDoneForDayRecycler.recycle( msg );
    }

    public void recycle( MarketDoneForDayImpl msg ) {
        _marketDoneForDayRecycler.recycle( msg );
    }

    public void recycle( RecoveryDoneForDayImpl msg ) {
        _recoveryDoneForDayRecycler.recycle( msg );
    }

    public void recycle( ClientStoppedImpl msg ) {
        _clientStoppedRecycler.recycle( msg );
    }

    public void recycle( MarketStoppedImpl msg ) {
        _marketStoppedRecycler.recycle( msg );
    }

    public void recycle( RecoveryStoppedImpl msg ) {
        _recoveryStoppedRecycler.recycle( msg );
    }

    public void recycle( ClientExpiredImpl msg ) {
        _clientExpiredRecycler.recycle( msg );
    }

    public void recycle( MarketExpiredImpl msg ) {
        _marketExpiredRecycler.recycle( msg );
    }

    public void recycle( RecoveryExpiredImpl msg ) {
        _recoveryExpiredRecycler.recycle( msg );
    }

    public void recycle( ClientSuspendedImpl msg ) {
        _clientSuspendedRecycler.recycle( msg );
    }

    public void recycle( MarketSuspendedImpl msg ) {
        _marketSuspendedRecycler.recycle( msg );
    }

    public void recycle( RecoverySuspendedImpl msg ) {
        _recoverySuspendedRecycler.recycle( msg );
    }

    public void recycle( ClientRestatedImpl msg ) {
        _clientRestatedRecycler.recycle( msg );
    }

    public void recycle( MarketRestatedImpl msg ) {
        _marketRestatedRecycler.recycle( msg );
    }

    public void recycle( RecoveryRestatedImpl msg ) {
        _recoveryRestatedRecycler.recycle( msg );
    }

    public void recycle( ClientTradeCorrectImpl msg ) {
        _clientTradeCorrectRecycler.recycle( msg );
    }

    public void recycle( MarketTradeCorrectImpl msg ) {
        _marketTradeCorrectRecycler.recycle( msg );
    }

    public void recycle( RecoveryTradeCorrectImpl msg ) {
        _recoveryTradeCorrectRecycler.recycle( msg );
    }

    public void recycle( ClientTradeCancelImpl msg ) {
        _clientTradeCancelRecycler.recycle( msg );
    }

    public void recycle( MarketTradeCancelImpl msg ) {
        _marketTradeCancelRecycler.recycle( msg );
    }

    public void recycle( RecoveryTradeCancelImpl msg ) {
        _recoveryTradeCancelRecycler.recycle( msg );
    }

    public void recycle( ClientOrderStatusImpl msg ) {
        _clientOrderStatusRecycler.recycle( msg );
    }

    public void recycle( MarketOrderStatusImpl msg ) {
        _marketOrderStatusRecycler.recycle( msg );
    }

    public void recycle( RecoveryOrderStatusImpl msg ) {
        _recoveryOrderStatusRecycler.recycle( msg );
    }

    public void recycle( HeartbeatImpl msg ) {
        _heartbeatRecycler.recycle( msg );
    }

    public void recycle( TestRequestImpl msg ) {
        _testRequestRecycler.recycle( msg );
    }

    public void recycle( LogonImpl msg ) {
        _logonRecycler.recycle( msg );
    }

    public void recycle( LogoutImpl msg ) {
        _logoutRecycler.recycle( msg );
    }

    public void recycle( SessionRejectImpl msg ) {
        _sessionRejectRecycler.recycle( msg );
    }

    public void recycle( ResendRequestImpl msg ) {
        _resendRequestRecycler.recycle( msg );
    }

    public void recycle( ClientResyncSentMsgsImpl msg ) {
        _clientResyncSentMsgsRecycler.recycle( msg );
    }

    public void recycle( SequenceResetImpl msg ) {
        _sequenceResetRecycler.recycle( msg );
    }

    public void recycle( TradingSessionStatusImpl msg ) {
        _tradingSessionStatusRecycler.recycle( msg );
    }

    public void recycle( SecMassStatGrpImpl msg ) {
        _secMassStatGrpRecycler.recycle( msg );
    }

    public void recycle( MassInstrumentStateChangeImpl msg ) {
        _massInstrumentStateChangeRecycler.recycle( msg );
    }

    public void recycle( ClientAlertLimitBreachImpl msg ) {
        _clientAlertLimitBreachRecycler.recycle( msg );
    }

    public void recycle( MarketAlertLimitBreachImpl msg ) {
        _marketAlertLimitBreachRecycler.recycle( msg );
    }

    public void recycle( RecoveryAlertLimitBreachImpl msg ) {
        _recoveryAlertLimitBreachRecycler.recycle( msg );
    }

    public void recycle( ClientAlertTradeMissingOrdersImpl msg ) {
        _clientAlertTradeMissingOrdersRecycler.recycle( msg );
    }

    public void recycle( MarketAlertTradeMissingOrdersImpl msg ) {
        _marketAlertTradeMissingOrdersRecycler.recycle( msg );
    }

    public void recycle( RecoveryAlertTradeMissingOrdersImpl msg ) {
        _recoveryAlertTradeMissingOrdersRecycler.recycle( msg );
    }

    public void recycle( StratInstrumentStateImpl msg ) {
        _stratInstrumentStateRecycler.recycle( msg );
    }

    public void recycle( StrategyStateImpl msg ) {
        _strategyStateRecycler.recycle( msg );
    }

    public void recycle( UTPLogonImpl msg ) {
        _uTPLogonRecycler.recycle( msg );
    }

    public void recycle( UTPLogonRejectImpl msg ) {
        _uTPLogonRejectRecycler.recycle( msg );
    }

    public void recycle( UTPTradingSessionStatusImpl msg ) {
        _uTPTradingSessionStatusRecycler.recycle( msg );
    }

    public void recycle( ETIConnectionGatewayRequestImpl msg ) {
        _eTIConnectionGatewayRequestRecycler.recycle( msg );
    }

    public void recycle( ETIConnectionGatewayResponseImpl msg ) {
        _eTIConnectionGatewayResponseRecycler.recycle( msg );
    }

    public void recycle( ETISessionLogonRequestImpl msg ) {
        _eTISessionLogonRequestRecycler.recycle( msg );
    }

    public void recycle( ETISessionLogonResponseImpl msg ) {
        _eTISessionLogonResponseRecycler.recycle( msg );
    }

    public void recycle( ETISessionLogoutRequestImpl msg ) {
        _eTISessionLogoutRequestRecycler.recycle( msg );
    }

    public void recycle( ETISessionLogoutResponseImpl msg ) {
        _eTISessionLogoutResponseRecycler.recycle( msg );
    }

    public void recycle( ETISessionLogoutNotificationImpl msg ) {
        _eTISessionLogoutNotificationRecycler.recycle( msg );
    }

    public void recycle( ETIUserLogonRequestImpl msg ) {
        _eTIUserLogonRequestRecycler.recycle( msg );
    }

    public void recycle( ETIUserLogonResponseImpl msg ) {
        _eTIUserLogonResponseRecycler.recycle( msg );
    }

    public void recycle( ETIUserLogoutRequestImpl msg ) {
        _eTIUserLogoutRequestRecycler.recycle( msg );
    }

    public void recycle( ETIUserLogoutResponseImpl msg ) {
        _eTIUserLogoutResponseRecycler.recycle( msg );
    }

    public void recycle( ETIThrottleUpdateNotificationImpl msg ) {
        _eTIThrottleUpdateNotificationRecycler.recycle( msg );
    }

    public void recycle( ETISubscribeImpl msg ) {
        _eTISubscribeRecycler.recycle( msg );
    }

    public void recycle( ETISubscribeResponseImpl msg ) {
        _eTISubscribeResponseRecycler.recycle( msg );
    }

    public void recycle( ETIUnsubscribeImpl msg ) {
        _eTIUnsubscribeRecycler.recycle( msg );
    }

    public void recycle( ETIUnsubscribeResponseImpl msg ) {
        _eTIUnsubscribeResponseRecycler.recycle( msg );
    }

    public void recycle( ETIRetransmitImpl msg ) {
        _eTIRetransmitRecycler.recycle( msg );
    }

    public void recycle( ETIRetransmitResponseImpl msg ) {
        _eTIRetransmitResponseRecycler.recycle( msg );
    }

    public void recycle( ETIRetransmitOrderEventsImpl msg ) {
        _eTIRetransmitOrderEventsRecycler.recycle( msg );
    }

    public void recycle( ETIRetransmitOrderEventsResponseImpl msg ) {
        _eTIRetransmitOrderEventsResponseRecycler.recycle( msg );
    }

    public void recycle( MilleniumLogonImpl msg ) {
        _milleniumLogonRecycler.recycle( msg );
    }

    public void recycle( MilleniumLogonReplyImpl msg ) {
        _milleniumLogonReplyRecycler.recycle( msg );
    }

    public void recycle( MilleniumLogoutImpl msg ) {
        _milleniumLogoutRecycler.recycle( msg );
    }

    public void recycle( MilleniumMissedMessageRequestImpl msg ) {
        _milleniumMissedMessageRequestRecycler.recycle( msg );
    }

    public void recycle( MilleniumMissedMsgRequestAckImpl msg ) {
        _milleniumMissedMsgRequestAckRecycler.recycle( msg );
    }

    public void recycle( MilleniumMissedMsgReportImpl msg ) {
        _milleniumMissedMsgReportRecycler.recycle( msg );
    }

    public void recycle( BookAddOrderImpl msg ) {
        _bookAddOrderRecycler.recycle( msg );
    }

    public void recycle( BookDeleteOrderImpl msg ) {
        _bookDeleteOrderRecycler.recycle( msg );
    }

    public void recycle( BookModifyOrderImpl msg ) {
        _bookModifyOrderRecycler.recycle( msg );
    }

    public void recycle( BookClearImpl msg ) {
        _bookClearRecycler.recycle( msg );
    }

    public void recycle( SymbolRepeatingGrpImpl msg ) {
        _symbolRepeatingGrpRecycler.recycle( msg );
    }

    public void recycle( MDRequestImpl msg ) {
        _mDRequestRecycler.recycle( msg );
    }

    public void recycle( TickUpdateImpl msg ) {
        _tickUpdateRecycler.recycle( msg );
    }

    public void recycle( MDUpdateImpl msg ) {
        _mDUpdateRecycler.recycle( msg );
    }

    public void recycle( SecDefEventsImpl msg ) {
        _secDefEventsRecycler.recycle( msg );
    }

    public void recycle( SecurityAltIDImpl msg ) {
        _securityAltIDRecycler.recycle( msg );
    }

    public void recycle( SDFeedTypeImpl msg ) {
        _sDFeedTypeRecycler.recycle( msg );
    }

    public void recycle( SecDefLegImpl msg ) {
        _secDefLegRecycler.recycle( msg );
    }

    public void recycle( MDEntryImpl msg ) {
        _mDEntryRecycler.recycle( msg );
    }

    public void recycle( MDSnapEntryImpl msg ) {
        _mDSnapEntryRecycler.recycle( msg );
    }

    public void recycle( MsgSeqNumGapImpl msg ) {
        _msgSeqNumGapRecycler.recycle( msg );
    }

    public void recycle( MDIncRefreshImpl msg ) {
        _mDIncRefreshRecycler.recycle( msg );
    }

    public void recycle( MDSnapshotFullRefreshImpl msg ) {
        _mDSnapshotFullRefreshRecycler.recycle( msg );
    }

    public void recycle( SecurityDefinitionImpl msg ) {
        _securityDefinitionRecycler.recycle( msg );
    }

    public void recycle( SecurityDefinitionUpdateImpl msg ) {
        _securityDefinitionUpdateRecycler.recycle( msg );
    }

    public void recycle( ProductSnapshotImpl msg ) {
        _productSnapshotRecycler.recycle( msg );
    }

    public void recycle( SecurityStatusImpl msg ) {
        _securityStatusRecycler.recycle( msg );
    }

    public void recycle( HasReusableType msg ) {
        if ( msg == null ) return;

        final ReusableType type = msg.getReusableType();

        switch( type.getId() ) {
        case FullEventIds.ID_CLIENT_NEWORDERSINGLE:
            _clientNewOrderSingleRecycler.recycle( (ClientNewOrderSingleImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_CANCELREPLACEREQUEST:
            _clientCancelReplaceRequestRecycler.recycle( (ClientCancelReplaceRequestImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_CANCELREQUEST:
            _clientCancelRequestRecycler.recycle( (ClientCancelRequestImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_FORCECANCEL:
            _clientForceCancelRecycler.recycle( (ClientForceCancelImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_VAGUEORDERREJECT:
            _clientVagueOrderRejectRecycler.recycle( (ClientVagueOrderRejectImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_CANCELREJECT:
            _clientCancelRejectRecycler.recycle( (ClientCancelRejectImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_NEWORDERACK:
            _clientNewOrderAckRecycler.recycle( (ClientNewOrderAckImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_TRADENEW:
            _clientTradeNewRecycler.recycle( (ClientTradeNewImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_REJECTED:
            _clientRejectedRecycler.recycle( (ClientRejectedImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_CANCELLED:
            _clientCancelledRecycler.recycle( (ClientCancelledImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_REPLACED:
            _clientReplacedRecycler.recycle( (ClientReplacedImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_DONEFORDAY:
            _clientDoneForDayRecycler.recycle( (ClientDoneForDayImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_STOPPED:
            _clientStoppedRecycler.recycle( (ClientStoppedImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_EXPIRED:
            _clientExpiredRecycler.recycle( (ClientExpiredImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_SUSPENDED:
            _clientSuspendedRecycler.recycle( (ClientSuspendedImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_RESTATED:
            _clientRestatedRecycler.recycle( (ClientRestatedImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_TRADECORRECT:
            _clientTradeCorrectRecycler.recycle( (ClientTradeCorrectImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_TRADECANCEL:
            _clientTradeCancelRecycler.recycle( (ClientTradeCancelImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_ORDERSTATUS:
            _clientOrderStatusRecycler.recycle( (ClientOrderStatusImpl) msg );
            break;
        case FullEventIds.ID_HEARTBEAT:
            _heartbeatRecycler.recycle( (HeartbeatImpl) msg );
            break;
        case FullEventIds.ID_TESTREQUEST:
            _testRequestRecycler.recycle( (TestRequestImpl) msg );
            break;
        case FullEventIds.ID_LOGON:
            _logonRecycler.recycle( (LogonImpl) msg );
            break;
        case FullEventIds.ID_LOGOUT:
            _logoutRecycler.recycle( (LogoutImpl) msg );
            break;
        case FullEventIds.ID_SESSIONREJECT:
            _sessionRejectRecycler.recycle( (SessionRejectImpl) msg );
            break;
        case FullEventIds.ID_RESENDREQUEST:
            _resendRequestRecycler.recycle( (ResendRequestImpl) msg );
            break;
        case FullEventIds.ID_CLIENTRESYNCSENTMSGS:
            _clientResyncSentMsgsRecycler.recycle( (ClientResyncSentMsgsImpl) msg );
            break;
        case FullEventIds.ID_SEQUENCERESET:
            _sequenceResetRecycler.recycle( (SequenceResetImpl) msg );
            break;
        case FullEventIds.ID_TRADINGSESSIONSTATUS:
            _tradingSessionStatusRecycler.recycle( (TradingSessionStatusImpl) msg );
            break;
        case FullEventIds.ID_SECMASSSTATGRP:
            _secMassStatGrpRecycler.recycle( (SecMassStatGrpImpl) msg );
            break;
        case FullEventIds.ID_MASSINSTRUMENTSTATECHANGE:
            _massInstrumentStateChangeRecycler.recycle( (MassInstrumentStateChangeImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_ALERTLIMITBREACH:
            _clientAlertLimitBreachRecycler.recycle( (ClientAlertLimitBreachImpl) msg );
            break;
        case FullEventIds.ID_CLIENT_ALERTTRADEMISSINGORDERS:
            _clientAlertTradeMissingOrdersRecycler.recycle( (ClientAlertTradeMissingOrdersImpl) msg );
            break;
        case FullEventIds.ID_STRATINSTRUMENTSTATE:
            _stratInstrumentStateRecycler.recycle( (StratInstrumentStateImpl) msg );
            break;
        case FullEventIds.ID_STRATEGYSTATE:
            _strategyStateRecycler.recycle( (StrategyStateImpl) msg );
            break;
        case FullEventIds.ID_UTPLOGON:
            _uTPLogonRecycler.recycle( (UTPLogonImpl) msg );
            break;
        case FullEventIds.ID_UTPLOGONREJECT:
            _uTPLogonRejectRecycler.recycle( (UTPLogonRejectImpl) msg );
            break;
        case FullEventIds.ID_UTPTRADINGSESSIONSTATUS:
            _uTPTradingSessionStatusRecycler.recycle( (UTPTradingSessionStatusImpl) msg );
            break;
        case FullEventIds.ID_ETICONNECTIONGATEWAYREQUEST:
            _eTIConnectionGatewayRequestRecycler.recycle( (ETIConnectionGatewayRequestImpl) msg );
            break;
        case FullEventIds.ID_ETICONNECTIONGATEWAYRESPONSE:
            _eTIConnectionGatewayResponseRecycler.recycle( (ETIConnectionGatewayResponseImpl) msg );
            break;
        case FullEventIds.ID_ETISESSIONLOGONREQUEST:
            _eTISessionLogonRequestRecycler.recycle( (ETISessionLogonRequestImpl) msg );
            break;
        case FullEventIds.ID_ETISESSIONLOGONRESPONSE:
            _eTISessionLogonResponseRecycler.recycle( (ETISessionLogonResponseImpl) msg );
            break;
        case FullEventIds.ID_ETISESSIONLOGOUTREQUEST:
            _eTISessionLogoutRequestRecycler.recycle( (ETISessionLogoutRequestImpl) msg );
            break;
        case FullEventIds.ID_ETISESSIONLOGOUTRESPONSE:
            _eTISessionLogoutResponseRecycler.recycle( (ETISessionLogoutResponseImpl) msg );
            break;
        case FullEventIds.ID_ETISESSIONLOGOUTNOTIFICATION:
            _eTISessionLogoutNotificationRecycler.recycle( (ETISessionLogoutNotificationImpl) msg );
            break;
        case FullEventIds.ID_ETIUSERLOGONREQUEST:
            _eTIUserLogonRequestRecycler.recycle( (ETIUserLogonRequestImpl) msg );
            break;
        case FullEventIds.ID_ETIUSERLOGONRESPONSE:
            _eTIUserLogonResponseRecycler.recycle( (ETIUserLogonResponseImpl) msg );
            break;
        case FullEventIds.ID_ETIUSERLOGOUTREQUEST:
            _eTIUserLogoutRequestRecycler.recycle( (ETIUserLogoutRequestImpl) msg );
            break;
        case FullEventIds.ID_ETIUSERLOGOUTRESPONSE:
            _eTIUserLogoutResponseRecycler.recycle( (ETIUserLogoutResponseImpl) msg );
            break;
        case FullEventIds.ID_ETITHROTTLEUPDATENOTIFICATION:
            _eTIThrottleUpdateNotificationRecycler.recycle( (ETIThrottleUpdateNotificationImpl) msg );
            break;
        case FullEventIds.ID_ETISUBSCRIBE:
            _eTISubscribeRecycler.recycle( (ETISubscribeImpl) msg );
            break;
        case FullEventIds.ID_ETISUBSCRIBERESPONSE:
            _eTISubscribeResponseRecycler.recycle( (ETISubscribeResponseImpl) msg );
            break;
        case FullEventIds.ID_ETIUNSUBSCRIBE:
            _eTIUnsubscribeRecycler.recycle( (ETIUnsubscribeImpl) msg );
            break;
        case FullEventIds.ID_ETIUNSUBSCRIBERESPONSE:
            _eTIUnsubscribeResponseRecycler.recycle( (ETIUnsubscribeResponseImpl) msg );
            break;
        case FullEventIds.ID_ETIRETRANSMIT:
            _eTIRetransmitRecycler.recycle( (ETIRetransmitImpl) msg );
            break;
        case FullEventIds.ID_ETIRETRANSMITRESPONSE:
            _eTIRetransmitResponseRecycler.recycle( (ETIRetransmitResponseImpl) msg );
            break;
        case FullEventIds.ID_ETIRETRANSMITORDEREVENTS:
            _eTIRetransmitOrderEventsRecycler.recycle( (ETIRetransmitOrderEventsImpl) msg );
            break;
        case FullEventIds.ID_ETIRETRANSMITORDEREVENTSRESPONSE:
            _eTIRetransmitOrderEventsResponseRecycler.recycle( (ETIRetransmitOrderEventsResponseImpl) msg );
            break;
        case FullEventIds.ID_MILLENIUMLOGON:
            _milleniumLogonRecycler.recycle( (MilleniumLogonImpl) msg );
            break;
        case FullEventIds.ID_MILLENIUMLOGONREPLY:
            _milleniumLogonReplyRecycler.recycle( (MilleniumLogonReplyImpl) msg );
            break;
        case FullEventIds.ID_MILLENIUMLOGOUT:
            _milleniumLogoutRecycler.recycle( (MilleniumLogoutImpl) msg );
            break;
        case FullEventIds.ID_MILLENIUMMISSEDMESSAGEREQUEST:
            _milleniumMissedMessageRequestRecycler.recycle( (MilleniumMissedMessageRequestImpl) msg );
            break;
        case FullEventIds.ID_MILLENIUMMISSEDMSGREQUESTACK:
            _milleniumMissedMsgRequestAckRecycler.recycle( (MilleniumMissedMsgRequestAckImpl) msg );
            break;
        case FullEventIds.ID_MILLENIUMMISSEDMSGREPORT:
            _milleniumMissedMsgReportRecycler.recycle( (MilleniumMissedMsgReportImpl) msg );
            break;
        case FullEventIds.ID_BOOKADDORDER:
            _bookAddOrderRecycler.recycle( (BookAddOrderImpl) msg );
            break;
        case FullEventIds.ID_BOOKDELETEORDER:
            _bookDeleteOrderRecycler.recycle( (BookDeleteOrderImpl) msg );
            break;
        case FullEventIds.ID_BOOKMODIFYORDER:
            _bookModifyOrderRecycler.recycle( (BookModifyOrderImpl) msg );
            break;
        case FullEventIds.ID_BOOKCLEAR:
            _bookClearRecycler.recycle( (BookClearImpl) msg );
            break;
        case FullEventIds.ID_SYMBOLREPEATINGGRP:
            _symbolRepeatingGrpRecycler.recycle( (SymbolRepeatingGrpImpl) msg );
            break;
        case FullEventIds.ID_MDREQUEST:
            _mDRequestRecycler.recycle( (MDRequestImpl) msg );
            break;
        case FullEventIds.ID_TICKUPDATE:
            _tickUpdateRecycler.recycle( (TickUpdateImpl) msg );
            break;
        case FullEventIds.ID_MDUPDATE:
            _mDUpdateRecycler.recycle( (MDUpdateImpl) msg );
            break;
        case FullEventIds.ID_SECDEFEVENTS:
            _secDefEventsRecycler.recycle( (SecDefEventsImpl) msg );
            break;
        case FullEventIds.ID_SECURITYALTID:
            _securityAltIDRecycler.recycle( (SecurityAltIDImpl) msg );
            break;
        case FullEventIds.ID_SDFEEDTYPE:
            _sDFeedTypeRecycler.recycle( (SDFeedTypeImpl) msg );
            break;
        case FullEventIds.ID_SECDEFLEG:
            _secDefLegRecycler.recycle( (SecDefLegImpl) msg );
            break;
        case FullEventIds.ID_MDENTRY:
            _mDEntryRecycler.recycle( (MDEntryImpl) msg );
            break;
        case FullEventIds.ID_MDSNAPENTRY:
            _mDSnapEntryRecycler.recycle( (MDSnapEntryImpl) msg );
            break;
        case FullEventIds.ID_MSGSEQNUMGAP:
            _msgSeqNumGapRecycler.recycle( (MsgSeqNumGapImpl) msg );
            break;
        case FullEventIds.ID_MDINCREFRESH:
            _mDIncRefreshRecycler.recycle( (MDIncRefreshImpl) msg );
            break;
        case FullEventIds.ID_MDSNAPSHOTFULLREFRESH:
            _mDSnapshotFullRefreshRecycler.recycle( (MDSnapshotFullRefreshImpl) msg );
            break;
        case FullEventIds.ID_SECURITYDEFINITION:
            _securityDefinitionRecycler.recycle( (SecurityDefinitionImpl) msg );
            break;
        case FullEventIds.ID_SECURITYDEFINITIONUPDATE:
            _securityDefinitionUpdateRecycler.recycle( (SecurityDefinitionUpdateImpl) msg );
            break;
        case FullEventIds.ID_PRODUCTSNAPSHOT:
            _productSnapshotRecycler.recycle( (ProductSnapshotImpl) msg );
            break;
        case FullEventIds.ID_SECURITYSTATUS:
            _securityStatusRecycler.recycle( (SecurityStatusImpl) msg );
            break;
        case FullEventIds.ID_MARKET_NEWORDERSINGLE:
            _marketNewOrderSingleRecycler.recycle( (MarketNewOrderSingleImpl) msg );
            break;
        case FullEventIds.ID_MARKET_CANCELREPLACEREQUEST:
            _marketCancelReplaceRequestRecycler.recycle( (MarketCancelReplaceRequestImpl) msg );
            break;
        case FullEventIds.ID_MARKET_CANCELREQUEST:
            _marketCancelRequestRecycler.recycle( (MarketCancelRequestImpl) msg );
            break;
        case FullEventIds.ID_MARKET_FORCECANCEL:
            _marketForceCancelRecycler.recycle( (MarketForceCancelImpl) msg );
            break;
        case FullEventIds.ID_MARKET_VAGUEORDERREJECT:
            _marketVagueOrderRejectRecycler.recycle( (MarketVagueOrderRejectImpl) msg );
            break;
        case FullEventIds.ID_MARKET_CANCELREJECT:
            _marketCancelRejectRecycler.recycle( (MarketCancelRejectImpl) msg );
            break;
        case FullEventIds.ID_MARKET_NEWORDERACK:
            _marketNewOrderAckRecycler.recycle( (MarketNewOrderAckImpl) msg );
            break;
        case FullEventIds.ID_MARKET_TRADENEW:
            _marketTradeNewRecycler.recycle( (MarketTradeNewImpl) msg );
            break;
        case FullEventIds.ID_MARKET_REJECTED:
            _marketRejectedRecycler.recycle( (MarketRejectedImpl) msg );
            break;
        case FullEventIds.ID_MARKET_CANCELLED:
            _marketCancelledRecycler.recycle( (MarketCancelledImpl) msg );
            break;
        case FullEventIds.ID_MARKET_REPLACED:
            _marketReplacedRecycler.recycle( (MarketReplacedImpl) msg );
            break;
        case FullEventIds.ID_MARKET_DONEFORDAY:
            _marketDoneForDayRecycler.recycle( (MarketDoneForDayImpl) msg );
            break;
        case FullEventIds.ID_MARKET_STOPPED:
            _marketStoppedRecycler.recycle( (MarketStoppedImpl) msg );
            break;
        case FullEventIds.ID_MARKET_EXPIRED:
            _marketExpiredRecycler.recycle( (MarketExpiredImpl) msg );
            break;
        case FullEventIds.ID_MARKET_SUSPENDED:
            _marketSuspendedRecycler.recycle( (MarketSuspendedImpl) msg );
            break;
        case FullEventIds.ID_MARKET_RESTATED:
            _marketRestatedRecycler.recycle( (MarketRestatedImpl) msg );
            break;
        case FullEventIds.ID_MARKET_TRADECORRECT:
            _marketTradeCorrectRecycler.recycle( (MarketTradeCorrectImpl) msg );
            break;
        case FullEventIds.ID_MARKET_TRADECANCEL:
            _marketTradeCancelRecycler.recycle( (MarketTradeCancelImpl) msg );
            break;
        case FullEventIds.ID_MARKET_ORDERSTATUS:
            _marketOrderStatusRecycler.recycle( (MarketOrderStatusImpl) msg );
            break;
        case FullEventIds.ID_MARKET_ALERTLIMITBREACH:
            _marketAlertLimitBreachRecycler.recycle( (MarketAlertLimitBreachImpl) msg );
            break;
        case FullEventIds.ID_MARKET_ALERTTRADEMISSINGORDERS:
            _marketAlertTradeMissingOrdersRecycler.recycle( (MarketAlertTradeMissingOrdersImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_NEWORDERSINGLE:
            _recoveryNewOrderSingleRecycler.recycle( (RecoveryNewOrderSingleImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_CANCELREPLACEREQUEST:
            _recoveryCancelReplaceRequestRecycler.recycle( (RecoveryCancelReplaceRequestImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_CANCELREQUEST:
            _recoveryCancelRequestRecycler.recycle( (RecoveryCancelRequestImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_FORCECANCEL:
            _recoveryForceCancelRecycler.recycle( (RecoveryForceCancelImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_VAGUEORDERREJECT:
            _recoveryVagueOrderRejectRecycler.recycle( (RecoveryVagueOrderRejectImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_CANCELREJECT:
            _recoveryCancelRejectRecycler.recycle( (RecoveryCancelRejectImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_NEWORDERACK:
            _recoveryNewOrderAckRecycler.recycle( (RecoveryNewOrderAckImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_TRADENEW:
            _recoveryTradeNewRecycler.recycle( (RecoveryTradeNewImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_REJECTED:
            _recoveryRejectedRecycler.recycle( (RecoveryRejectedImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_CANCELLED:
            _recoveryCancelledRecycler.recycle( (RecoveryCancelledImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_REPLACED:
            _recoveryReplacedRecycler.recycle( (RecoveryReplacedImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_DONEFORDAY:
            _recoveryDoneForDayRecycler.recycle( (RecoveryDoneForDayImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_STOPPED:
            _recoveryStoppedRecycler.recycle( (RecoveryStoppedImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_EXPIRED:
            _recoveryExpiredRecycler.recycle( (RecoveryExpiredImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_SUSPENDED:
            _recoverySuspendedRecycler.recycle( (RecoverySuspendedImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_RESTATED:
            _recoveryRestatedRecycler.recycle( (RecoveryRestatedImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_TRADECORRECT:
            _recoveryTradeCorrectRecycler.recycle( (RecoveryTradeCorrectImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_TRADECANCEL:
            _recoveryTradeCancelRecycler.recycle( (RecoveryTradeCancelImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_ORDERSTATUS:
            _recoveryOrderStatusRecycler.recycle( (RecoveryOrderStatusImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_ALERTLIMITBREACH:
            _recoveryAlertLimitBreachRecycler.recycle( (RecoveryAlertLimitBreachImpl) msg );
            break;
        case FullEventIds.ID_RECOVERY_ALERTTRADEMISSINGORDERS:
            _recoveryAlertTradeMissingOrdersRecycler.recycle( (RecoveryAlertTradeMissingOrdersImpl) msg );
            break;
        }
    }

}
