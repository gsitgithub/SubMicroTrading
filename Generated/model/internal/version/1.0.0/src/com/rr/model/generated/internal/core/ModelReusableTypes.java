/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.core;


import com.rr.core.lang.ReusableCategory;


import com.rr.core.lang.ReusableCategoryEnum;


import com.rr.core.lang.ReusableType;


import com.rr.core.lang.ReusableTypeIDFactory;


import javax.annotation.Generated;

@Generated( "com.rr.model.generated.internal.core.ModelReusableTypes" )

public enum ModelReusableTypes implements ReusableType {

    RecoveryNewOrderSingle( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_NEWORDERSINGLE, EventIds.ID_NEWORDERSINGLE ),
    ClientNewOrderSingle( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_NEWORDERSINGLE, EventIds.ID_NEWORDERSINGLE ),
    MarketNewOrderSingle( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_NEWORDERSINGLE, EventIds.ID_NEWORDERSINGLE ),
    RecoveryCancelReplaceRequest( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_CANCELREPLACEREQUEST, EventIds.ID_CANCELREPLACEREQUEST ),
    ClientCancelReplaceRequest( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_CANCELREPLACEREQUEST, EventIds.ID_CANCELREPLACEREQUEST ),
    MarketCancelReplaceRequest( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_CANCELREPLACEREQUEST, EventIds.ID_CANCELREPLACEREQUEST ),
    RecoveryCancelRequest( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_CANCELREQUEST, EventIds.ID_CANCELREQUEST ),
    ClientCancelRequest( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_CANCELREQUEST, EventIds.ID_CANCELREQUEST ),
    MarketCancelRequest( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_CANCELREQUEST, EventIds.ID_CANCELREQUEST ),
    RecoveryForceCancel( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_FORCECANCEL, EventIds.ID_FORCECANCEL ),
    ClientForceCancel( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_FORCECANCEL, EventIds.ID_FORCECANCEL ),
    MarketForceCancel( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_FORCECANCEL, EventIds.ID_FORCECANCEL ),
    RecoveryVagueOrderReject( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_VAGUEORDERREJECT, EventIds.ID_VAGUEORDERREJECT ),
    ClientVagueOrderReject( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_VAGUEORDERREJECT, EventIds.ID_VAGUEORDERREJECT ),
    MarketVagueOrderReject( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_VAGUEORDERREJECT, EventIds.ID_VAGUEORDERREJECT ),
    RecoveryCancelReject( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_CANCELREJECT, EventIds.ID_CANCELREJECT ),
    ClientCancelReject( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_CANCELREJECT, EventIds.ID_CANCELREJECT ),
    MarketCancelReject( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_CANCELREJECT, EventIds.ID_CANCELREJECT ),
    RecoveryNewOrderAck( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_NEWORDERACK, EventIds.ID_NEWORDERACK ),
    ClientNewOrderAck( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_NEWORDERACK, EventIds.ID_NEWORDERACK ),
    MarketNewOrderAck( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_NEWORDERACK, EventIds.ID_NEWORDERACK ),
    RecoveryTradeNew( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_TRADENEW, EventIds.ID_TRADENEW ),
    ClientTradeNew( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_TRADENEW, EventIds.ID_TRADENEW ),
    MarketTradeNew( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_TRADENEW, EventIds.ID_TRADENEW ),
    RecoveryRejected( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_REJECTED, EventIds.ID_REJECTED ),
    ClientRejected( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_REJECTED, EventIds.ID_REJECTED ),
    MarketRejected( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_REJECTED, EventIds.ID_REJECTED ),
    RecoveryCancelled( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_CANCELLED, EventIds.ID_CANCELLED ),
    ClientCancelled( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_CANCELLED, EventIds.ID_CANCELLED ),
    MarketCancelled( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_CANCELLED, EventIds.ID_CANCELLED ),
    RecoveryReplaced( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_REPLACED, EventIds.ID_REPLACED ),
    ClientReplaced( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_REPLACED, EventIds.ID_REPLACED ),
    MarketReplaced( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_REPLACED, EventIds.ID_REPLACED ),
    RecoveryDoneForDay( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_DONEFORDAY, EventIds.ID_DONEFORDAY ),
    ClientDoneForDay( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_DONEFORDAY, EventIds.ID_DONEFORDAY ),
    MarketDoneForDay( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_DONEFORDAY, EventIds.ID_DONEFORDAY ),
    RecoveryStopped( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_STOPPED, EventIds.ID_STOPPED ),
    ClientStopped( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_STOPPED, EventIds.ID_STOPPED ),
    MarketStopped( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_STOPPED, EventIds.ID_STOPPED ),
    RecoveryExpired( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_EXPIRED, EventIds.ID_EXPIRED ),
    ClientExpired( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_EXPIRED, EventIds.ID_EXPIRED ),
    MarketExpired( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_EXPIRED, EventIds.ID_EXPIRED ),
    RecoverySuspended( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_SUSPENDED, EventIds.ID_SUSPENDED ),
    ClientSuspended( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_SUSPENDED, EventIds.ID_SUSPENDED ),
    MarketSuspended( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_SUSPENDED, EventIds.ID_SUSPENDED ),
    RecoveryRestated( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_RESTATED, EventIds.ID_RESTATED ),
    ClientRestated( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_RESTATED, EventIds.ID_RESTATED ),
    MarketRestated( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_RESTATED, EventIds.ID_RESTATED ),
    RecoveryTradeCorrect( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_TRADECORRECT, EventIds.ID_TRADECORRECT ),
    ClientTradeCorrect( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_TRADECORRECT, EventIds.ID_TRADECORRECT ),
    MarketTradeCorrect( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_TRADECORRECT, EventIds.ID_TRADECORRECT ),
    RecoveryTradeCancel( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_TRADECANCEL, EventIds.ID_TRADECANCEL ),
    ClientTradeCancel( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_TRADECANCEL, EventIds.ID_TRADECANCEL ),
    MarketTradeCancel( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_TRADECANCEL, EventIds.ID_TRADECANCEL ),
    RecoveryOrderStatus( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_ORDERSTATUS, EventIds.ID_ORDERSTATUS ),
    ClientOrderStatus( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_ORDERSTATUS, EventIds.ID_ORDERSTATUS ),
    MarketOrderStatus( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_ORDERSTATUS, EventIds.ID_ORDERSTATUS ),
    Heartbeat( ReusableCategoryEnum.Event, FullEventIds.ID_HEARTBEAT, EventIds.ID_HEARTBEAT ),
    TestRequest( ReusableCategoryEnum.Event, FullEventIds.ID_TESTREQUEST, EventIds.ID_TESTREQUEST ),
    Logon( ReusableCategoryEnum.Event, FullEventIds.ID_LOGON, EventIds.ID_LOGON ),
    Logout( ReusableCategoryEnum.Event, FullEventIds.ID_LOGOUT, EventIds.ID_LOGOUT ),
    SessionReject( ReusableCategoryEnum.Event, FullEventIds.ID_SESSIONREJECT, EventIds.ID_SESSIONREJECT ),
    ResendRequest( ReusableCategoryEnum.Event, FullEventIds.ID_RESENDREQUEST, EventIds.ID_RESENDREQUEST ),
    ClientResyncSentMsgs( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENTRESYNCSENTMSGS, EventIds.ID_CLIENTRESYNCSENTMSGS ),
    SequenceReset( ReusableCategoryEnum.Event, FullEventIds.ID_SEQUENCERESET, EventIds.ID_SEQUENCERESET ),
    TradingSessionStatus( ReusableCategoryEnum.Event, FullEventIds.ID_TRADINGSESSIONSTATUS, EventIds.ID_TRADINGSESSIONSTATUS ),
    SecMassStatGrp( ReusableCategoryEnum.Event, FullEventIds.ID_SECMASSSTATGRP, EventIds.ID_SECMASSSTATGRP ),
    MassInstrumentStateChange( ReusableCategoryEnum.Event, FullEventIds.ID_MASSINSTRUMENTSTATECHANGE, EventIds.ID_MASSINSTRUMENTSTATECHANGE ),
    RecoveryAlertLimitBreach( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_ALERTLIMITBREACH, EventIds.ID_ALERTLIMITBREACH ),
    ClientAlertLimitBreach( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_ALERTLIMITBREACH, EventIds.ID_ALERTLIMITBREACH ),
    MarketAlertLimitBreach( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_ALERTLIMITBREACH, EventIds.ID_ALERTLIMITBREACH ),
    RecoveryAlertTradeMissingOrders( ReusableCategoryEnum.Event, FullEventIds.ID_RECOVERY_ALERTTRADEMISSINGORDERS, EventIds.ID_ALERTTRADEMISSINGORDERS ),
    ClientAlertTradeMissingOrders( ReusableCategoryEnum.Event, FullEventIds.ID_CLIENT_ALERTTRADEMISSINGORDERS, EventIds.ID_ALERTTRADEMISSINGORDERS ),
    MarketAlertTradeMissingOrders( ReusableCategoryEnum.Event, FullEventIds.ID_MARKET_ALERTTRADEMISSINGORDERS, EventIds.ID_ALERTTRADEMISSINGORDERS ),
    StratInstrumentState( ReusableCategoryEnum.Event, FullEventIds.ID_STRATINSTRUMENTSTATE, EventIds.ID_STRATINSTRUMENTSTATE ),
    StrategyState( ReusableCategoryEnum.Event, FullEventIds.ID_STRATEGYSTATE, EventIds.ID_STRATEGYSTATE ),
    UTPLogon( ReusableCategoryEnum.Event, FullEventIds.ID_UTPLOGON, EventIds.ID_UTPLOGON ),
    UTPLogonReject( ReusableCategoryEnum.Event, FullEventIds.ID_UTPLOGONREJECT, EventIds.ID_UTPLOGONREJECT ),
    UTPTradingSessionStatus( ReusableCategoryEnum.Event, FullEventIds.ID_UTPTRADINGSESSIONSTATUS, EventIds.ID_UTPTRADINGSESSIONSTATUS ),
    ETIConnectionGatewayRequest( ReusableCategoryEnum.Event, FullEventIds.ID_ETICONNECTIONGATEWAYREQUEST, EventIds.ID_ETICONNECTIONGATEWAYREQUEST ),
    ETIConnectionGatewayResponse( ReusableCategoryEnum.Event, FullEventIds.ID_ETICONNECTIONGATEWAYRESPONSE, EventIds.ID_ETICONNECTIONGATEWAYRESPONSE ),
    ETISessionLogonRequest( ReusableCategoryEnum.Event, FullEventIds.ID_ETISESSIONLOGONREQUEST, EventIds.ID_ETISESSIONLOGONREQUEST ),
    ETISessionLogonResponse( ReusableCategoryEnum.Event, FullEventIds.ID_ETISESSIONLOGONRESPONSE, EventIds.ID_ETISESSIONLOGONRESPONSE ),
    ETISessionLogoutRequest( ReusableCategoryEnum.Event, FullEventIds.ID_ETISESSIONLOGOUTREQUEST, EventIds.ID_ETISESSIONLOGOUTREQUEST ),
    ETISessionLogoutResponse( ReusableCategoryEnum.Event, FullEventIds.ID_ETISESSIONLOGOUTRESPONSE, EventIds.ID_ETISESSIONLOGOUTRESPONSE ),
    ETISessionLogoutNotification( ReusableCategoryEnum.Event, FullEventIds.ID_ETISESSIONLOGOUTNOTIFICATION, EventIds.ID_ETISESSIONLOGOUTNOTIFICATION ),
    ETIUserLogonRequest( ReusableCategoryEnum.Event, FullEventIds.ID_ETIUSERLOGONREQUEST, EventIds.ID_ETIUSERLOGONREQUEST ),
    ETIUserLogonResponse( ReusableCategoryEnum.Event, FullEventIds.ID_ETIUSERLOGONRESPONSE, EventIds.ID_ETIUSERLOGONRESPONSE ),
    ETIUserLogoutRequest( ReusableCategoryEnum.Event, FullEventIds.ID_ETIUSERLOGOUTREQUEST, EventIds.ID_ETIUSERLOGOUTREQUEST ),
    ETIUserLogoutResponse( ReusableCategoryEnum.Event, FullEventIds.ID_ETIUSERLOGOUTRESPONSE, EventIds.ID_ETIUSERLOGOUTRESPONSE ),
    ETIThrottleUpdateNotification( ReusableCategoryEnum.Event, FullEventIds.ID_ETITHROTTLEUPDATENOTIFICATION, EventIds.ID_ETITHROTTLEUPDATENOTIFICATION ),
    ETISubscribe( ReusableCategoryEnum.Event, FullEventIds.ID_ETISUBSCRIBE, EventIds.ID_ETISUBSCRIBE ),
    ETISubscribeResponse( ReusableCategoryEnum.Event, FullEventIds.ID_ETISUBSCRIBERESPONSE, EventIds.ID_ETISUBSCRIBERESPONSE ),
    ETIUnsubscribe( ReusableCategoryEnum.Event, FullEventIds.ID_ETIUNSUBSCRIBE, EventIds.ID_ETIUNSUBSCRIBE ),
    ETIUnsubscribeResponse( ReusableCategoryEnum.Event, FullEventIds.ID_ETIUNSUBSCRIBERESPONSE, EventIds.ID_ETIUNSUBSCRIBERESPONSE ),
    ETIRetransmit( ReusableCategoryEnum.Event, FullEventIds.ID_ETIRETRANSMIT, EventIds.ID_ETIRETRANSMIT ),
    ETIRetransmitResponse( ReusableCategoryEnum.Event, FullEventIds.ID_ETIRETRANSMITRESPONSE, EventIds.ID_ETIRETRANSMITRESPONSE ),
    ETIRetransmitOrderEvents( ReusableCategoryEnum.Event, FullEventIds.ID_ETIRETRANSMITORDEREVENTS, EventIds.ID_ETIRETRANSMITORDEREVENTS ),
    ETIRetransmitOrderEventsResponse( ReusableCategoryEnum.Event, FullEventIds.ID_ETIRETRANSMITORDEREVENTSRESPONSE, EventIds.ID_ETIRETRANSMITORDEREVENTSRESPONSE ),
    MilleniumLogon( ReusableCategoryEnum.Event, FullEventIds.ID_MILLENIUMLOGON, EventIds.ID_MILLENIUMLOGON ),
    MilleniumLogonReply( ReusableCategoryEnum.Event, FullEventIds.ID_MILLENIUMLOGONREPLY, EventIds.ID_MILLENIUMLOGONREPLY ),
    MilleniumLogout( ReusableCategoryEnum.Event, FullEventIds.ID_MILLENIUMLOGOUT, EventIds.ID_MILLENIUMLOGOUT ),
    MilleniumMissedMessageRequest( ReusableCategoryEnum.Event, FullEventIds.ID_MILLENIUMMISSEDMESSAGEREQUEST, EventIds.ID_MILLENIUMMISSEDMESSAGEREQUEST ),
    MilleniumMissedMsgRequestAck( ReusableCategoryEnum.Event, FullEventIds.ID_MILLENIUMMISSEDMSGREQUESTACK, EventIds.ID_MILLENIUMMISSEDMSGREQUESTACK ),
    MilleniumMissedMsgReport( ReusableCategoryEnum.Event, FullEventIds.ID_MILLENIUMMISSEDMSGREPORT, EventIds.ID_MILLENIUMMISSEDMSGREPORT ),
    BookAddOrder( ReusableCategoryEnum.Event, FullEventIds.ID_BOOKADDORDER, EventIds.ID_BOOKADDORDER ),
    BookDeleteOrder( ReusableCategoryEnum.Event, FullEventIds.ID_BOOKDELETEORDER, EventIds.ID_BOOKDELETEORDER ),
    BookModifyOrder( ReusableCategoryEnum.Event, FullEventIds.ID_BOOKMODIFYORDER, EventIds.ID_BOOKMODIFYORDER ),
    BookClear( ReusableCategoryEnum.Event, FullEventIds.ID_BOOKCLEAR, EventIds.ID_BOOKCLEAR ),
    SymbolRepeatingGrp( ReusableCategoryEnum.Event, FullEventIds.ID_SYMBOLREPEATINGGRP, EventIds.ID_SYMBOLREPEATINGGRP ),
    MDRequest( ReusableCategoryEnum.Event, FullEventIds.ID_MDREQUEST, EventIds.ID_MDREQUEST ),
    TickUpdate( ReusableCategoryEnum.Event, FullEventIds.ID_TICKUPDATE, EventIds.ID_TICKUPDATE ),
    MDUpdate( ReusableCategoryEnum.Event, FullEventIds.ID_MDUPDATE, EventIds.ID_MDUPDATE ),
    SecDefEvents( ReusableCategoryEnum.Event, FullEventIds.ID_SECDEFEVENTS, EventIds.ID_SECDEFEVENTS ),
    SecurityAltID( ReusableCategoryEnum.Event, FullEventIds.ID_SECURITYALTID, EventIds.ID_SECURITYALTID ),
    SDFeedType( ReusableCategoryEnum.Event, FullEventIds.ID_SDFEEDTYPE, EventIds.ID_SDFEEDTYPE ),
    SecDefLeg( ReusableCategoryEnum.Event, FullEventIds.ID_SECDEFLEG, EventIds.ID_SECDEFLEG ),
    MDEntry( ReusableCategoryEnum.Event, FullEventIds.ID_MDENTRY, EventIds.ID_MDENTRY ),
    MDSnapEntry( ReusableCategoryEnum.Event, FullEventIds.ID_MDSNAPENTRY, EventIds.ID_MDSNAPENTRY ),
    MsgSeqNumGap( ReusableCategoryEnum.Event, FullEventIds.ID_MSGSEQNUMGAP, EventIds.ID_MSGSEQNUMGAP ),
    MDIncRefresh( ReusableCategoryEnum.Event, FullEventIds.ID_MDINCREFRESH, EventIds.ID_MDINCREFRESH ),
    MDSnapshotFullRefresh( ReusableCategoryEnum.Event, FullEventIds.ID_MDSNAPSHOTFULLREFRESH, EventIds.ID_MDSNAPSHOTFULLREFRESH ),
    SecurityDefinition( ReusableCategoryEnum.Event, FullEventIds.ID_SECURITYDEFINITION, EventIds.ID_SECURITYDEFINITION ),
    SecurityDefinitionUpdate( ReusableCategoryEnum.Event, FullEventIds.ID_SECURITYDEFINITIONUPDATE, EventIds.ID_SECURITYDEFINITIONUPDATE ),
    ProductSnapshot( ReusableCategoryEnum.Event, FullEventIds.ID_PRODUCTSNAPSHOT, EventIds.ID_PRODUCTSNAPSHOT ),
    SecurityStatus( ReusableCategoryEnum.Event, FullEventIds.ID_SECURITYSTATUS, EventIds.ID_SECURITYSTATUS );

    private final int              _eventId;
    private final int              _id;
    private final ReusableCategory _cat;

    private ModelReusableTypes( ReusableCategory cat, int catId, int eventId ) {
        _cat     = cat;
        _id      = ReusableTypeIDFactory.setID( cat, catId );
        _eventId = eventId;
    }

    @Override
    public int getSubId() {
        return _eventId;
    }

    @Override
    public int getId() {
        return _id;
    }

    @Override
    public ReusableCategory getReusableCategory() {
        return _cat;
    }

}
