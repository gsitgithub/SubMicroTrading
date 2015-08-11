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
 * helper for the FIX decode process
 * 
 * doesnt check for buffer overrun, so ensure buffer big enough !
 * 
 * can throw RuntimeEncodingException
 * 
 * @author Richard Rose
 */
public final class FixEncodeBuilderImpl extends BaseFixEncodeBuilderImpl {

    // 8=FIX.4.2;9=0000;
    private static final int       DATA_OFFSET = 17;

    private final int            _startBodyOffset;
    private final byte[]         _hdr;
    private final int            _hdrLen;
    private final int            _lastIdxForBodyLen;

    private final byte           _major;
    private final byte           _minor;

    public FixEncodeBuilderImpl( byte[] buffer, int offset, byte major, byte minor ) {
        super( buffer, offset );
        
        _major             = major;
        _minor             = minor;

        _hdr    = ("8=FIX." + (char)_major + "." + (char)_minor + (char)FixField.FIELD_DELIMITER + "9=").getBytes();
        _hdrLen = _hdr.length + 1;

        _startBodyOffset   = _startOffset + DATA_OFFSET;
        
        _lastIdxForBodyLen = _startBodyOffset - 1; 
    }

    @Override
    public void encodeEnvelope() {

        encodeFixHeader();
        encodeChecksum();
        
        _msgLen = _idx - _msgOffset; 
    }
    
    @Override
    public void start() {
        _idx = _startBodyOffset;
    }
    
    public final int getBodyOffset() {
        return _startBodyOffset;
    }

    public final int getCurLength() {
        return _idx - _startBodyOffset;
    }

    private void encodeFixHeader() {

        //           1         2
        // 0123456789012345678901234
        // 8=FIX.4.2;9=0000;35=D

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
