/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;


// Identifies class or source of the SecurityID (48) value. Required if SecurityID (48) is specified

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.lang.ReusableString;

public enum SecurityIDSource implements SingleByteLookup {

    CUSIP( "1" ),
    SEDOL( "2" ),
    QUIK( "3" ),
    ISIN( "4" ),
    RIC( "5" ),
    ISO_CcyCode( "6" ),
    ISO_CountryCode( "7" ),
    ExchangeSymbol( "8" ),
    CTA( "9" ),
    Bloomberg( "A" ),
    Wertpapier( "B" ),
    Dutch( "C" ),
    Valoren( "D" ),
    Sicovam( "E" ),
    Belgian( "F" ),
    Common( "G" ),
    ClearingHouse( "H" ),
    ISDA_FPML( "I" ),
    OptionsPRA( "J" ),
    ISDA_FPML_ProdURLSecId( "K" ),
    LetterOfCredit( "K" ),
    Unknown( "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private byte _val;

    SecurityIDSource( String val ) {
        _val = val.getBytes()[0];
    }


    private static SecurityIDSource[] _entries = new SecurityIDSource[256];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( SecurityIDSource en : SecurityIDSource.values() ) {
             _entries[ en.getVal() ] = en;
        }
        
        _entries[ (byte)'M' ] = ExchangeSymbol; // map MarketPlaceAssignedIdentifier  to ExchangeSymbol
    }

    public static SecurityIDSource getVal( byte val ) {
        SecurityIDSource eval;
        eval = _entries[ val ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for SecurityIDSource" );
        return eval;
    }

    @Override
    public final byte getVal() {
        return _val;
    }

    public void id( ReusableString out ) {
        out.append( toString() );
    }
}
