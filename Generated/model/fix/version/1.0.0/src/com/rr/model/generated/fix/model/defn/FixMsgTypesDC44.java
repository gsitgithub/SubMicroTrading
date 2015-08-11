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

@Generated( "com.rr.model.generated.fix.model.FixMsgTypesDC44" )

public interface FixMsgTypesDC44 {
    public byte[] NewOrderSingle = "D".getBytes();
    public byte[] CancelReplaceRequest = "G".getBytes();
    public byte[] CancelRequest = "F".getBytes();
    public byte[] CancelReject = "9".getBytes();
    public byte[] BaseExecRpt = "null".getBytes();
    public byte[] NewOrderAck = "8".getBytes();
    public byte[] BaseTrade = "null".getBytes();
    public byte[] Trade = "8".getBytes();
    public byte[] TradeCorrect = "8".getBytes();
    public byte[] TradeCancel = "8".getBytes();
    public byte[] Rejected = "8".getBytes();
    public byte[] Cancelled = "8".getBytes();
    public byte[] Replaced = "8".getBytes();
    public byte[] DoneForDay = "8".getBytes();
    public byte[] Stopped = "8".getBytes();
    public byte[] Suspended = "8".getBytes();
    public byte[] Expired = "8".getBytes();
    public byte[] OrderStatus = "8".getBytes();
    public byte[] Restated = "8".getBytes();
    public byte[] Calculated = "8".getBytes();
    public byte[] PendingCancel = "8".getBytes();
    public byte[] PendingNew = "8".getBytes();
    public byte[] PendingReplace = "8".getBytes();
    public byte[] Heartbeat = "0".getBytes();
    public byte[] Logon = "A".getBytes();
    public byte[] Logout = "5".getBytes();
    public byte[] SessionReject = "3".getBytes();
    public byte[] ResendRequest = "2".getBytes();
    public byte[] SequenceReset = "4".getBytes();
    public byte[] TestRequest = "1".getBytes();
    public byte[] StrategyState = "B".getBytes();
}
