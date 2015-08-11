/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

public interface CoreCommonFix {
    public int BodyLength = 9;
    public int CheckSum = 10;
    public int ClOrdId = 11;
    public int ExecID = 17;
    public int MsgSeqNum = 34;
    public int MsgType = 35;
    public int OrderId = 37;
    public int OrderQty = 38;
    public int OrdStatus = 39;
    public int OrdType = 40;
    public int OrigClOrdId = 41;
    public int SenderCompID = 49;
    public int PossDupFlag = 43;
    public int Price = 44;
    public int SecurityID = 48;
    public int SenderSubID = 50;
    public int SendingTime = 52;
    public int Side = 54;
    public int Symbol = 55;
    public int TargetCompID = 56;
    public int TargetSubID = 57;
    public int Text = 58;
    public int TimeInForce = 59;
    public int TransactTime = 60;
}
