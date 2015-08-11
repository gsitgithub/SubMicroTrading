/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

public enum FixVersion {

    Fix4_0( 999999, '4', '0' ),
    Fix4_1( 999999, '4', '1' ),
    Fix4_2( 0,      '4', '2' ),
    Fix4_4( 0,      '4', '4' ),
    Fix5_0( 0,      '5', '0' ),
    DCFix4_4( 0,    '4', '4' ),
    MDFix4_4( 0,    '4', '4' ),
    MDFix5_0( 0,    '5', '0' );
    
    public  final byte _major;
    public  final byte _minor;

    private final int  _maxSeqNum;

    FixVersion( int maxSeqNum, char bMajor, char bMinor ) {
        _maxSeqNum = maxSeqNum;
        _major     = (byte) bMajor;
        _minor     = (byte) bMinor;
    }
    
    public int getMaxSeqNum() {
        return _maxSeqNum;
    }
}
