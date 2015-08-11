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

@Generated( "com.rr.model.generated.fix.model.FixMsgTypesMD44" )

public interface FixMsgTypesMD44 {
    public byte[] MDRequest = "V".getBytes();
    public byte[] MDIncRefresh = "X".getBytes();
    public byte[] MDSnapshotFullRefresh = "W".getBytes();
    public byte[] SecurityDefinition = "d".getBytes();
    public byte[] SecurityStatus = "f".getBytes();
    public byte[] Heartbeat = "0".getBytes();
    public byte[] Logon = "A".getBytes();
    public byte[] Logout = "5".getBytes();
    public byte[] SessionReject = "3".getBytes();
    public byte[] ResendRequest = "2".getBytes();
    public byte[] SequenceReset = "4".getBytes();
    public byte[] TestRequest = "1".getBytes();
    public byte[] TradingSessionStatus = "h".getBytes();
    public byte[] MassInstrumentStateChange = "CO".getBytes();
}
