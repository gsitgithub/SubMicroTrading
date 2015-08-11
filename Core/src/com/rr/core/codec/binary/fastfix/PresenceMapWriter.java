/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix;

public final class PresenceMapWriter {

    private FastFixBuilder    _encoder;       // start at most significant data bit down to LSB
    private byte[]            _buf;
    private int               _offset;
    private int               _initPMapSize;
    private int               _curPMapIndex;
    private int               _lastPMapIndex;
    private int               _nextBit;
    
    /**
     * correct presizing map is KEY for encode efficiency and avoid array shifting 
     *  
     * @param encoder
     * @param mapOffset
     * @param initialPMapSize
     */
    public PresenceMapWriter( FastFixBuilder encoder, int mapOffset, int initialPMapSize ) {
        set( encoder, mapOffset, initialPMapSize );
    }

    public PresenceMapWriter() {
        // constructor for sequence pMaps
    }

    public void set( FastFixBuilder encoder, int mapOffset, int initialPMapSize ) {
        _encoder       = encoder;
        _buf           = encoder.getBuffer();
        _offset        = mapOffset;
        _initPMapSize  = initialPMapSize;

        reset();
    }

    public void reset() {
        _lastPMapIndex = _offset + _initPMapSize - 1;

        _encoder.insertBytes( _offset , _initPMapSize );
        
        _curPMapIndex = _offset;
        _nextBit      = FastFixUtils.MSB_DATA_BIT;
    }
    
    public void setCurrentField() {
        if ( _nextBit == 0 ) {
            checkGrow();
        }
        _buf[_curPMapIndex] |= (byte)_nextBit;
        _nextBit>>=1;
    }

    public void clearCurrentField() {
        if ( _nextBit == 0 ) {
            checkGrow();
        }
        // bit already zero
        _nextBit>>=1;
    }

    public void end() {
        _buf[_curPMapIndex] |= FastFixUtils.STOP_BIT;
    }
    
    private void checkGrow() {
        if ( _curPMapIndex == _lastPMapIndex ) {
            // need shift entire buffer down one byte
            
            ++_lastPMapIndex; // shift every byte AFTER current last pmap byte down the array one space
            
            _encoder.insertByte( _lastPMapIndex );
        }
        
        ++_curPMapIndex;
        _nextBit = FastFixUtils.MSB_DATA_BIT;
    }
}
