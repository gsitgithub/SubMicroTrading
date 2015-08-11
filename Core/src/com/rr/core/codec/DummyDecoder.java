/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.InstrumentLocator;
import com.rr.core.model.Message;


public class DummyDecoder implements Decoder {

    @Override
    public void setClientProfile( ClientProfile client ) {
        /* nothing */
    }

    @Override
    public void setInstrumentLocator( InstrumentLocator instrumentLocator ) {
        /* nothing */
    }
    
    @Override public InstrumentLocator getInstrumentLocator() { 
        return null; 
    }

    @Override
    public Message decode( byte[] msg, int offset, int maxIdx ) {
        return null;
    }

    @Override
    public void setReceived( long nanos ) {
        /* nothing */
    }

    @Override
    public long getReceived() {
        return 0;
    }

    @Override
    public int parseHeader( byte[] inBuffer, int inHdrLen, int bytesRead ) {
        return 0;
    }

    @Override
    public ResyncCode resync( byte[] fixMsg, int offset, int maxIdx ) {
        return null;
    }

    @Override
    public int getSkipBytes() {
        return 0;
    }

    @Override
    public void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        /* nothing */
    }

    @Override
    public Message postHeaderDecode() {
        return null;
    }

    @Override
    public void setNanoStats( boolean nanoTiming ) {
        /* nothing */
    }

    @Override
    public int getLength() {
        return 0;
    }
}
