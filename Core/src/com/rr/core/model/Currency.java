/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import java.util.HashMap;
import java.util.Map;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;

/**
 * Identifies currency used for price. Absence of this field is interpreted as the default for the security. It is recommended that systems provide the currency value whenever possible.
 */


public enum Currency implements MultiByteLookup {

    USD( "USD" ),
    EUR( "EUR" ),
    GBP( "GBP" ),
    GBp( "GBp", Currency.GBP, false ),
    GBX( "GBX", Currency.GBP, false ),
    NOK( "NOK" ),
    CHF( "CHF" ),
    DKK( "DKK" ),
    ZAR( "ZAR" ),
    ZAr( "ZAr", Currency.ZAR, false ),
    ZAC( "ZAC", Currency.ZAR, false ),
    ZAc( "ZAc", Currency.ZAR, false ),
    JPY( "JPY" ),
    AUD( "AUD" ),
    BRL( "BRL" ),
    KRW( "KRW" ),
    MXN( "MXN" ),
    MYR( "MYR" ),
    SEK( "SEK" ),
    CAD( "CAD" ),
    CNH( "CNH" ),
    CNY( "CNY" ),
    NZD( "NZD" ),
    TRY( "TRY" ),

    Other( "999" ),                         // legal fix 
    Unknown( "998");                        // null
    
    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 3; }

    private byte[]   _val;
    private double   _toUSDFactor;
    private Currency _majorCurrency;
    private boolean  _isMajor = true;

    Currency( String val,  Currency majorCurrency,  boolean isMajor ) {
        _val = val.getBytes();
        _majorCurrency = majorCurrency;
        _isMajor = isMajor;
        
        _toUSDFactor = (isMajor) ? 1.0 : 0.01;
    }

    Currency( String val ) {
        _val = val.getBytes();
        _majorCurrency = this;
        _isMajor = true;
        _toUSDFactor = 1.0;
    }

    private static Map<ZString,Currency> _map = new HashMap<ZString,Currency>();

    static {
        for ( Currency en : Currency.values() ) {
             byte[] val = en.getVal();
            ZString zVal = new ViewString( val );
            _map.put( zVal, en );
        }
    }

    /**
     * fastest lookup for Currency
     * 
     * @param buf
     * @param offset
     * @param len
     * @return
     */
    public static Currency getVal( byte[] buf, int offset, int len ) {
        
        if ( len == 0 ) return null;
        if ( len != 3 ) throw new RuntimeEncodingException( "Expected three character currency not " + len + ", offset=" + offset );
        
        byte b1 = buf[offset++];
        byte b2 = buf[offset++];
        byte b3 = buf[offset];
      
        switch( b1 ) {
        case 'A':        
            if ( b2 == 'U' && b3 == 'D' ) return AUD;
            break;
        case 'B':        
            if ( b2 == 'R' && b3 == 'L' ) return BRL;
            break;
        case 'G':        
            if ( b2 == 'B' ) {
                switch( b3 ) {
                case 'P' : return GBP;
                case 'p' : return GBp;
                case 'X' : return GBX;
                }            
            }
            break;
        case 'E': 
            if ( b2 == 'U' && b3 == 'R' ) return EUR;
            break;
        case 'U': 
            if ( b2 == 'S' && b3 == 'D' ) return USD;
            break;
        case 'C':        
            if ( b2 == 'H' && b3 == 'F' ) return CHF;
            if ( b2 == 'A' && b3 == 'D' ) return CAD;
            if ( b2 == 'N' && b3 == 'H' ) return CNH;
            if ( b2 == 'N' && b3 == 'Y' ) return CNY;
            break;
        case 'N':        
            if ( b2 == 'O' && b3 == 'K' ) return NOK;
            if ( b2 == 'Z' && b3 == 'D' ) return NZD;
            break;
        case 'D':        
            if ( b2 == 'K' && b3 == 'K' ) return DKK;
            break;
        case 'J':        
            if ( b2 == 'P' && b3 == 'Y' ) return JPY;
            break;
        case 'K':        
            if ( b2 == 'R' && b3 == 'W' ) return KRW;
            break;
        case 'S':        
            if ( b2 == 'E' && b3 == 'K' ) return SEK;
            break;
        case 'T':        
            if ( b2 == 'R' && b3 == 'Y' ) return TRY;
            break;
        case 'M':        
            switch( b2 ) {
            case 'X' :
                if ( b3 == 'N' ) {
                    return MXN;
                }
                break;
            case 'Y' : 
                if ( b3 == 'R' ) {
                    return MYR;
                }
                break;
            }            
            break;
        case 'Z':        
            if ( b2 == 'A' ) {
                switch( b3 ) {
                case 'R' : return ZAR;
                case 'r' : return ZAr;
                case 'C' : return ZAC;
                case 'c' : return ZAc;
                }            
            }
            break;
        // pad entries to force index lookup
        case 'F': case 'H': case 'I': case 'L': case 'O': case 'P':        
        case 'Q': case 'R': case 'V': case 'W': case 'X': case 'Y':        
            break;
        }
    
        throw new RuntimeDecodingException( "Unsupported value of " + (char)b1 + (char)b2 + (char)b3 + " for Currency" );
    }

    public static Currency getVal( ZString key ) {
        Currency val = _map.get( key );
        if ( val == null ) throw new RuntimeDecodingException( "Unsupported value of " + key + " for Currency" );
        return val;
    }

    @Override
    public byte[] getVal() {
        return _val;
    }

    public Currency getMajorCurrency() { return _majorCurrency; }
    public boolean  getIsMajor()       { return _isMajor; }

    public double toUSDFactor() {
        return _toUSDFactor;
    }

    public void setUSDFactor( double factor ) {
        _toUSDFactor = factor;
    }
    
    public double majorMinorConvert( Currency fromCcy, double price ) {
        if ( _isMajor ) {
            if ( fromCcy._isMajor == false ) { // convert from Minor to Major
                return( (price + Constants.WEIGHT) / 100.0 ); 
            }
        } else if ( fromCcy._isMajor ) { // convert from Major to Minor
            return( (price + Constants.WEIGHT) * 100.0 ); 
        }
        
        return price;
    }

    public void id( ReusableString out ) {
        out.append( toString() );
    }
}
