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

@Generated( "com.rr.model.generated.model.MilleniumLSECodes" )

public interface MilleniumLSECodes {
    public byte[] Logon = "A".getBytes();
    public byte[] LogonReply = "B".getBytes();
    public byte[] Logout = "5".getBytes();
    public byte[] Heartbeat = "0".getBytes();
    public byte[] MissedMessageRequest = "M".getBytes();
    public byte[] MissedMsgRequestAck = "N".getBytes();
    public byte[] MissedMsgReport = "P".getBytes();
    public byte[] Reject = "3".getBytes();
    public byte[] NewOrder = "D".getBytes();
    public byte[] OrderReplaceRequest = "G".getBytes();
    public byte[] OrderCancelRequest = "F".getBytes();
    public byte[] ExecutionReport = "8".getBytes();
    public byte[] CancelReject = "9".getBytes();
    public byte[] BusinessReject = "j".getBytes();

}
