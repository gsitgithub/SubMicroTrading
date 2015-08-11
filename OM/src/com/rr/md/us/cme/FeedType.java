/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme;

import com.rr.core.utils.SMTRuntimeException;

public enum FeedType {

    HistoricalReplay( 'H' ),
    Incremental( 'I' ),
    InstrumentReplay( 'N' ),
    Snapshot( 'S' );
    
    private byte _code;

    private FeedType( char code ) {
        _code = (byte)code;
    }
    
    public byte getCode() {
        return _code;
    }
    
    public static FeedType lookup( byte code ) {
        switch( code ) {
        case 'H':
            return HistoricalReplay;
        case 'I':
            return Incremental;
        case 'N':
            return InstrumentReplay;
        case 'S':
            return Snapshot;
        // force 
        case 'J': case 'K': case 'L': case 'M': case 'O': case 'P': case 'Q': case 'R': 
        default:
            throw new SMTRuntimeException( "Invalid FeedType code of " + code );
        }
    }
}


