/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.sbe;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;

public class DummySBEEncoder implements SBEEncoder {

    private byte[] _buf = new byte[100];

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public void setDebug( boolean debug ) {
        // nothing
    }

    @Override
    public void encode( Message msg ) {
        // nothing
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public Message unableToSend( Message msg, ZString errMsg ) {
        return null;
    }

    @Override
    public byte[] getBytes() {
        return _buf ;
    }

    @Override
    public void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        // nothing
    }

    @Override
    public void setNanoStats( boolean nanoTiming ) {
        // nothing
    }

    @Override
    public void addStats( ReusableString outBuf, Message msg, long time ) {
        // nothing
    }

    @Override
    public void logStats() {
        // nothing
    }

    @Override
    public void logLastMsg() {
        // nothing
    }

    @Override
    public void encodeStartPacket( SBEPacketHeader h ) {
        // nothing
    }
}
