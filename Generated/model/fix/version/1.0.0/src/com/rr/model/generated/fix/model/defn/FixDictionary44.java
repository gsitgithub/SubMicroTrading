/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.fix.model.defn;


import javax.annotation.Generated;

@Generated( "com.rr.model.generated.fix.model.FixDictionary44" )

public interface FixDictionary44 {
    public int Account = 1;
    public int AvgPx = 6;
    public int BeginSeqNo = 7;
    public int BeginString = 8;
    public int BodyLength = 9;
    public int CheckSum = 10;
    public int ClOrdId = 11;
    public int CumQty = 14;
    public int Currency = 15;
    public int EndSeqNo = 16;
    public int ExecID = 17;
    public int ExecRefID = 19;
    public int ExecInst = 18;
    public int HandlInst = 21;
    public int SecurityIDSource = 22;
    public int LastMkt = 30;
    public int LastPx = 31;
    public int LastQty = 32;
    public int MsgSeqNum = 34;
    public int MsgType = 35;
    public int NewSeqNo = 36;
    public int OrderId = 37;
    public int OrderQty = 38;
    public int OrdStatus = 39;
    public int OrdType = 40;
    public int OrigClOrdId = 41;
    public int PossDupFlag = 43;
    public int Price = 44;
    public int RefSeqNum = 45;
    public int SecurityID = 48;
    public int SenderCompID = 49;
    public int SenderSubID = 50;
    public int SendingTime = 52;
    public int Side = 54;
    public int Symbol = 55;
    public int TargetCompID = 56;
    public int TargetSubID = 57;
    public int Text = 58;
    public int TimeInForce = 59;
    public int TransactTime = 60;
    public int PositionEffect = 77;
    public int Signature = 89;
    public int SignatureLength = 93;
    public int RawDataLen = 95;
    public int RawData = 96;
    public int PossResend = 97;
    public int EncryptMethod = 98;
    public int ExDest = 100;
    public int CxlRejReason = 102;
    public int OrdRejReason = 103;
    public int SecurityDesc = 107;
    public int heartBtInt = 108;
    public int testReqID = 112;
    public int OnBehalfOfCompID = 115;
    public int OnBehalfOfSubID = 116;
    public int settlCurrency = 120;
    public int OrigSendingTime = 122;
    public int GapFillFlag = 123;
    public int DeliverToCompID = 128;
    public int DeliverToSubID = 129;
    public int ResetSeqNumFlag = 141;
    public int SenderLocationID = 142;
    public int TargetLocationID = 143;
    public int OnBehalfOfLocationID = 144;
    public int DeliverToLocationID = 145;
    public int numRelatedSym = 146;
    public int ExecType = 150;
    public int LeavesQty = 151;
    public int SecurityType = 167;
    public int SecurityExchange = 207;
    public int PegOffsetValue = 211;
    public int mdReqId = 262;
    public int subsReqType = 263;
    public int marketDepth = 264;
    public int numMDEntryTypes = 267;
    public int noMDEntries = 268;
    public int mdEntryType = 269;
    public int mdEntryPx = 270;
    public int mdEntrySize = 271;
    public int tradeTime = 273;
    public int tickDirection = 274;
    public int numberOfOrders = 346;
    public int lastMsgSeqNumProcessed = 369;
    public int RefTagID = 371;
    public int RefMsgType = 372;
    public int SessionRejectReason = 373;
    public int ExecRestatementReason = 378;
    public int CxlRejResponseTo = 434;
    public int MultiLegReportingType = 442;
    public int PartyIDSource = 447;
    public int SrcLinkId = 526;
    public int OrderCapacity = 528;
    public int OrderRestrictions = 529;
    public int BookingType = 775;
    public int NextExpectedMsgSeqNum = 789;
    public int LastLiquidityInd = 851;
    public int TradingSystemName = 1603;
    public int TradingSystemVersion = 1604;
    public int TradingSystemVendor = 1605;
    public int AckStats = 11611;
}
