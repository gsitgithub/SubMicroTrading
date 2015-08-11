/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.model.defn;


import javax.annotation.Generated;

@Generated( "com.rr.model.generated.model.ETIEurexCodes" )

public interface ETIEurexCodes {
    public int ConnectionGatewayRequest = 10020;
    public int ConnectionGatewayResponse = 10021;
    public int SessionLogonRequest = 10000;
    public int SessionLogonResponse = 10001;
    public int SessionLogoutRequest = 10002;
    public int SessionLogoutResponse = 10003;
    public int SessionLogoutNotification = 10012;
    public int Heartbeat = 10011;
    public int HeartbeatNotification = 10023;
    public int UserLogonRequest = 10018;
    public int UserLogonResponse = 10019;
    public int UserLogoutRequest = 10029;
    public int UserLogoutResponse = 10024;
    public int ThrottleUpdateNotification = 10028;
    public int Subscribe = 10025;
    public int SubscribeResponse = 10005;
    public int Unsubscribe = 10006;
    public int UnsubscribeResponse = 10007;
    public int Retransmit = 10008;
    public int RetransmitResponse = 10009;
    public int RetransmitOrderEvents = 10026;
    public int RetransmitOrderEventsResponse = 10027;
    public int Reject = 10010;
    public int NewOrderRequest = 10100;
    public int NewOrderRequestMultiLeg = 10113;
    public int NewOrderRequestSimple = 10125;
    public int NewOrderStandardResponse = 10101;
    public int NewOrderLeanResponse = 10102;
    public int ImmediateExecResponse = 10103;
    public int BookOrderExecution = 10104;
    public int ReplaceOrderSingleRequest = 10106;
    public int ReplaceOrderMultiLeg = 10114;
    public int ReplaceOrderSingleShortRequest = 10126;
    public int ReplaceOrderStandardResponse = 10107;
    public int ReplaceOrderLeanResponse = 10108;
    public int CancelOrderSingleRequest = 10109;
    public int CancelOrderMultiLeg = 10123;
    public int CancelOrderStandardResponse = 10110;
    public int CancelOrderLeanResponse = 10111;
    public int CancelOrderNotification = 10112;
    public int ExtendedOrderInformation = 10117;
    public int TradeConfirmNotification = 10500;

}
