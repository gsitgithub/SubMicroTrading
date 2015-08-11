/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import com.rr.core.utils.NumberFormatUtils;


/**
 * used to decode readable FastFix messages which have been decoded from binary to readable ASCII
 * this is for market data messages of format  eg :-
 * 
 * 1128=9|35=X|49=CME|34=5457711|52=20120403194342222|75=20120403|268=1|279=1|1023=1|269=0|273=194342000|22=8|48=27069|83=7952927|270=252184990490.0|271=35|346=10|336=2|
 *  
 * @author Richard Rose
 */
public final class ReadableMDFastFixEncoder extends BaseFixEncodeBuilderImpl {

    private final static int  APPL_VER_ID     = 1128;
    private static final byte APPL_VER_ID_VAL = '9';


    // 1128=9;9=0000;
    private static final int       DATA_OFFSET = 14;

    private final int            _startBodyOffset;
    private final byte[]         _hdr;
    private final int            _hdrLen;
    private final int            _lastIdxForBodyLen;

    private final byte _applVerId;

    public ReadableMDFastFixEncoder( byte[] buffer, int offset, byte major, byte minor ) {
        super( buffer, offset );
        _applVerId = APPL_VER_ID_VAL; // @TODO get from config

        _hdr    = ("1128=" + (char)_applVerId + (char)FixField.FIELD_DELIMITER + "9=").getBytes();
        _hdrLen = _hdr.length + 1;

        _startBodyOffset   = _startOffset + DATA_OFFSET;
        
        _lastIdxForBodyLen = _startBodyOffset - 1; 
    }

    @Override
    public void start() {
        _idx = _startBodyOffset;
    }

    @Override
    public void encodeEnvelope() {

        encodeFixHeader();
        encodeChecksum();
        
        _msgLen = _idx - _msgOffset; 
    }
    
    /**
     * start encoding a fast fix readable message without tags 8, 9, 10
     */
    public void startNoLenOrChecksum() {
        _idx = _startOffset;
        encodeByte( APPL_VER_ID, _applVerId );
    }
    
    public void encodeEnvelopeNoLenOrChecksum() {
        _msgLen = _idx - _msgOffset;
    }

    private void encodeFixHeader() {

        //           1         2
        // 0123456789012345678901234
        // 1128=9;9=0000;35=D

        final int saved = _idx;
        
        final int bodyLen = _idx - _startBodyOffset;
        final int lenSize = NumberFormatUtils.getPosIntLen( bodyLen );
        _msgOffset = _startBodyOffset - (_hdrLen + lenSize);
        
        System.arraycopy( _hdr, 0, _buffer, _msgOffset, _hdr.length );
        
        _idx = _lastIdxForBodyLen - lenSize;
        
        writePosInt( bodyLen, lenSize );
        writeFixDelimiter();
        
        _idx = saved;
    }
}
