/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.fix;

public class FixTags {

    public static final int Account              = 1;
    public static final int AvgPx                = 6;
    public static final int BeginSeqNo           = 7;
    public static final int BeginString          = 8;
    public static final int BodyLength           = 9;
    public static final int CheckSum             = 10;
    public static final int ClOrdID              = 11;
    public static final int CumQty               = 14;
    public static final int Currency             = 15;
    public static final int EndSeqNo             = 16;
    public static final int ExecID               = 17;
    public static final int ExecInst             = 18;
    public static final int ExecRefID            = 19;
    public static final int ExecTransType        = 20;
    public static final int HandlInst            = 21;
    public static final int IDSource             = 22;
    public static final int LastMkt              = 30;
    public static final int LastPx               = 31;
    public static final int LastShares           = 32;
    public static final int MsgSeqNum            = 34;
    public static final int MsgType              = 35;
    public static final int NewSeqNo             = 36;
    public static final int OrderID              = 37;
    public static final int OrderQty             = 38;
    public static final int OrderStatus          = 39;
    public static final int OrdType              = 40;
    public static final int OrigClOrdID          = 41;
    public static final int PossDupFlag          = 43;
    public static final int Price                = 44;
    public static final int RefSeqNum            = 45;
    public static final int Rule80A              = 47;
    public static final int SecurityID           = 48;
    public static final int SenderCompID         = 49;
    public static final int SenderSubID          = 50;
    public static final int SendingTime          = 52;
    public static final int Side                 = 54;
    public static final int Symbol               = 55;
    public static final int TargetCompID         = 56;
    public static final int TargetSubID          = 57;
    public static final int Text                 = 58;
    public static final int TimeInForce          = 59;
    public static final int TransactTime         = 60;
    public static final int ExecBroker           = 76;
    public static final int PositionEffect       = 77; 
    public static final int RawDataLength        = 95;
    public static final int RawData              = 96;
    public static final int EncryptMethod        = 98;
    public static final int ExDestination        = 100;
    public static final int CxlRejReason         = 102;
    public static final int OrderRejReason       = 103;
    public static final int HeartBtInt           = 108;
    public static final int ClientID             = 109;
    public static final int MaxFloor             = 111;
    public static final int TestReqID            = 112;
    public static final int OnBehalfOfCompID     = 115;
    public static final int SettlementCurrency   = 120;
    public static final int OrigSendingTime      = 122;
    public static final int GapFillFlag          = 123;
    public static final int DeliverToCompID      = 128;
    public static final int DeliverToSubID       = 129;
    public static final int ResetSeqNumFlag      = 141;
    public static final int SenderLocationID     = 142;
    public static final int ExecType             = 150;
    public static final int LeavesQty            = 151;
    public static final int SecurityType         = 167;
    public static final int SecondaryOrderID     = 198;
    public static final int MaturityMonthYear    = 200;
    public static final int PutOrCall            = 201;
    public static final int StrikePrice          = 202;
    public static final int CustomerOrFirm       = 204;
    public static final int MaturityDay          = 205;
    public static final int OptAttribute         = 206;
    public static final int SecurityExch         = 207;
    public static final int UnderlyingAssetType  = 310;
    public static final int RefTagID             = 371;
    public static final int RefMsgType           = 372;
    public static final int SessionRejectReason  = 373;
    public static final int ContraBroker         = 375;
    public static final int BusRejRefID          = 379;
    public static final int CxlRejResponseTo     = 434;
    public static final int PartyIDSource        = 447;
    public static final int PartyID              = 448;
    public static final int PartyRole            = 452;
    public static final int NoPartyIDs           = 453;
    public static final int CountryCode          = 470;
    public static final int PartySubID           = 523;
    public static final int SecClOrdID           = 526;
    public static final int SecondaryExecID      = 527;
    public static final int OrderCapacity        = 528;
    public static final int Username             = 553;
    public static final int Password             = 554;
    public static final int NextExpectedSeqNum   = 789; 
    public static final int NoPartySubIDs        = 802;
    public static final int PartySubIDType       = 803;
    public static final int TQLastLiquidityInd   = 851; 
    public static final int SWXOrderCapacityQty  = 863; 
}
