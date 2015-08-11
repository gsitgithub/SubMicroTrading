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

@Generated( "com.rr.model.generated.fix.model.FixDictionaryMD50" )

public interface FixDictionaryMD50 {
    public int BeginSeqNo = 7;
    public int BeginString = 8;
    public int BodyLength = 9;
    public int CheckSum = 10;
    public int Currency = 15;
    public int EndSeqNo = 16;
    public int securityIDSource = 22;
    public int MsgSeqNum = 34;
    public int MsgType = 35;
    public int NewSeqNo = 36;
    public int PossDupFlag = 43;
    public int Price = 44;
    public int RefSeqNum = 45;
    public int securityID = 48;
    public int SenderCompID = 49;
    public int SenderSubID = 50;
    public int SendingTime = 52;
    public int Side = 54;
    public int Symbol = 55;
    public int TargetCompID = 56;
    public int TargetSubID = 57;
    public int Text = 58;
    public int TransactTime = 60;
    public int TradeDate = 75;
    public int repeatSeq = 83;
    public int RawDataLen = 95;
    public int RawData = 96;
    public int PossResend = 97;
    public int EncryptMethod = 98;
    public int ExDest = 100;
    public int securityDesc = 107;
    public int heartBtInt = 108;
    public int minQty = 110;
    public int testReqID = 112;
    public int OnBehalfOfCompID = 115;
    public int OnBehalfOfSubID = 116;
    public int settlCurrency = 120;
    public int GapFillFlag = 123;
    public int DeliverToCompID = 128;
    public int DeliverToSubID = 129;
    public int ResetSeqNumFlag = 141;
    public int SenderLocationID = 142;
    public int TargetLocationID = 143;
    public int OnBehalfOfLocationID = 144;
    public int DeliverToLocationID = 145;
    public int numRelatedSym = 146;
    public int securityType = 167;
    public int maturityMonthYear = 200;
    public int strikePrice = 202;
    public int securityExchange = 207;
    public int contractMultiplier = 231;
    public int mdReqId = 262;
    public int subsReqType = 263;
    public int marketDepth = 264;
    public int numMDEntryTypes = 267;
    public int noMDEntries = 268;
    public int mdEntryType = 269;
    public int mdEntryPx = 270;
    public int mdEntrySize = 271;
    public int mdEntryTime = 273;
    public int tickDirection = 274;
    public int mdUpdateAction = 279;
    public int securityTradingStatus = 326;
    public int haltReason = 327;
    public int highPx = 332;
    public int lowPx = 333;
    public int TradingSessionID = 336;
    public int TradSesStatus = 340;
    public int numberOfOrders = 346;
    public int lastMsgSeqNumProcessed = 369;
    public int RefTagID = 371;
    public int RefMsgType = 372;
    public int SessionRejectReason = 373;
    public int noSecurityAltID = 454;
    public int securityAltID = 455;
    public int securityAltIDSource = 456;
    public int CFICode = 461;
    public int underlyingProduct = 462;
    public int noLegs = 555;
    public int minTradeVol = 562;
    public int LegPrice = 566;
    public int legSymbol = 600;
    public int legSecurityID = 602;
    public int legSecurityIDSource = 603;
    public int legSecurityDesc = 620;
    public int legRatioQty = 623;
    public int legSide = 624;
    public int TradingSessionSubID = 625;
    public int lastUpdateTime = 779;
    public int NextExpectedMsgSeqNum = 789;
    public int LastLiquidityInd = 851;
    public int noEvents = 864;
    public int eventType = 865;
    public int eventDate = 866;
    public int totNumReports = 911;
    public int strikeCurrency = 947;
    public int securityStatus = 965;
    public int minPriceIncrement = 969;
    public int SecurityUpdateAction = 980;
    public int tradeVolume = 1020;
    public int mdBookType = 1021;
    public int feedType = 1022;
    public int mdPriceLevel = 1023;
    public int applVerID = 1128;
    public int maxTradeVol = 1140;
    public int noSDFeedTypes = 1141;
    public int eventTime = 1145;
    public int minPriceIncrementAmount = 1146;
    public int lowLimitPx = 1148;
    public int highLimitPx = 1149;
    public int tradingReferencePrice = 1150;
    public int securityGroup = 1151;
    public int SecurityTradingEvent = 1174;
    public int applID = 1180;
    public int pricePrecision = 1200;
    public int marketSegmentId = 1300;
    public int contractMultiplierType = 1435;
    public int instrumentScopeProductComplex = 1544;
    public int securityMassTradingStatus = 1679;
    public int mdSecurityTradingStatus = 1682;
    public int priceRatio = 5770;
    public int openInterestQty = 5792;
    public int tradingReferenceDate = 5796;
    public int displayFactor = 9787;
    public int AckStats = 11611;
}
