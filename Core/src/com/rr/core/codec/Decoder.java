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

public interface Decoder {

    public enum ResyncCode { FOUND_FULL_HEADER, FOUND_PARTIAL_HEADER_NEED_MORE_DATA; } 
    
    // utility methods
    public void setClientProfile( ClientProfile client );
    public void setInstrumentLocator( InstrumentLocator instrumentLocator );
    public InstrumentLocator getInstrumentLocator();
    
    /**
     * decode message of unknown length, must decode message to determine length
     * 
     * decode can create multiple different messages using message chain 
     * 
     * @param msg
     * @param offset    offset data starts at in buffer
     * @param maxIdx    maxIdx in buffer
     * @return
     */
    public Message decode( final byte[] msg, final int offset, final int maxIdx );
    
    public void setReceived( long nanos );

    public long getReceived();

    /**
     * verify the header is as expected
     * @param inBuffer
     * @param inHdrLen offset to start of data
     * @param bytesRead
     * @return  total length of message from start of the header to end of the trailer (not the same as tag 9 in fix)
     *          -1 if inBuffer doesnt have expected header information
     */
    public int parseHeader( byte[] inBuffer, int inHdrLen, int bytesRead );

    /**
     * when parse header fails, the resync can be called to try and rescyn the data stream
     * 
     * if find full header ... invoke getSkipped() to find how many bytes lost, user must shiftLeft then invoke parseHeader to parse hdr
     * if find partial header at end of buffer shift left ... invoke getSkipped() to find how many bytes lost, user must read more data
     * if WASTE throw away buffer as unusable
     * 
     * @param fixMsg
     * @param offset
     * @param maxIdx
     * @throws RuntimeDecodingException if buffer doesnt contain a header
     * @return FOUND_FULL_HEADER - found complete header,
     *         FOUND_PARTIAL_HEADER_NEED_MORE_DATA
     */
    public ResyncCode resync( final byte[] fixMsg, final int offset, final int maxIdx );
    
    /**
     * only to be used after invoking resync
     * 
     * @return the number of bytes skipped to reach start of next header ... should be used to shift left buffer before reinvoking parseHeader
     */
    public int getSkipBytes();
    
    /**
     * @param calc - time zone calculator to use
     */
    public void setTimeZoneCalculator( TimeZoneCalculator calc );
    
    /**
     * message now fully read continue processing from parseHeader call
     * @return
     */
    public Message postHeaderDecode();
    
    /**
     * @param nanoTiming if true enable nano stat collection
     */
    public void setNanoStats( boolean nanoTiming );
    
    /**
     * @return last index parsed minus offset .. ie bytes consumes in decode
     */
    public int getLength();
}
