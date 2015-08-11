/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix;

import com.rr.core.lang.ReusableString;

public final class PresenceMapReader {

    private       byte[] _buf;
    private       int    _offset;
    private       int    _curPMapIndex;
    private       int    _nextBit;
    private       int    _lastPMapIndex;
    
    /**
     * correct presizing map is KEY for encode efficiency and avoid array shifting 
     *  
     * @param encoder
     * @param mapOffset
     * @param initialPMapSize
     */
    public PresenceMapReader() {
    }

    /**
     * set this pMap to current decoder position then move current index to end of pMap
     * @param mapOffset start index for pMap
     * @return next index after pMap
     */
    public void readMap( FastFixDecodeBuilder decoder ) {
        _buf          = decoder.getBuffer();
        _offset       = decoder.getCurrentIndex();
        _nextBit      = FastFixUtils.MSB_DATA_BIT;

        _curPMapIndex = _offset;
        _nextBit      = FastFixUtils.MSB_DATA_BIT;
        
        _lastPMapIndex = decoder.skipPMap();
    }
    
    public boolean isNextFieldPresent() {
        if ( _nextBit == 0 ) {
            if ( _curPMapIndex == _lastPMapIndex ) {
                // attempt to go beyond presence map
                return false;
            }
            ++_curPMapIndex;
            _nextBit      = FastFixUtils.MSB_DATA_BIT;
        }
        
        boolean present = (_buf[_curPMapIndex] & (byte)_nextBit) != 0;

        _nextBit>>=1;
        
        return present;
    }

    public void trace( ReusableString log ) {
        log.append( "pMap nxtBit=" ).append( _nextBit ).append( ", _mapIdx=" ).append( _curPMapIndex ) ;
    }
}
