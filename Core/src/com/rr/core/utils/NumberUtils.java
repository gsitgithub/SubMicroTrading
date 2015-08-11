/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import com.rr.core.codec.RuntimeEncodingException;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;

public class NumberUtils {

    public static int nextPowerTwo( int initialNum ) {
        // Find a power of 2 >= initialCapacity
        int num = 1;
        while( num < initialNum )
            num <<= 1;
        
        return num;
    }

    public static long parseLong( final ZString strLong ) {
        
        long val = 0;
        
        byte[] b = strLong.getBytes();
        
              int idx    = strLong.getOffset(); 
        final int maxLen = idx + strLong.length();
        for( ; idx < maxLen ; ++idx )  {
            byte x = b[idx];
            
            if ( x >= '0' && x <= '9' )  {
                val = (val*10) + (x - '0');
            } else if ( x == 0x00 || x == ' ' ) {
                break;
            } else {
                throw new RuntimeEncodingException( "Invalid number, idx=" + idx + ", byte=" + (int) x + ", numStr=" + strLong );
            }
        }

        return val;
    }
    
    public static int parseInt( final ViewString src ) throws NumberFormatException {
        
        byte[] bytes  = src.getBytes();
        int    length = src.length();
        int    num    = 0;
        int    idx    = 0;
        
        byte b = bytes[0];

        // skip leading space
        while( b == ' ' && idx < length ) {
            b = bytes[++idx];
        }
        
        while( b >= '0' && b <='9' && idx < length) {
            num = (num*10) + (b - '0');
            b   = bytes[++idx];
        }
        
        // skip trailing space
        while( b == ' ' && idx < length ) {
            b = bytes[++idx];
        }
        
        if ( idx < length && (b < '0' || b > '9' ) ) {
            throw new NumberFormatException( "Unexpected byte " + (char)b + " in numeric string [" + bytes + "]" );
        }

        return num;
    }

    public static int priceToExternalInt( double price ) {
        
        if ( price >= 0 ) {
            if ( price == Double.MAX_VALUE ) {
                return Integer.MAX_VALUE;
            }
            return (int) ((price + Constants.WEIGHT) * Constants.KEEP_DECIMAL_PLACE_FACTOR);
        }
        
        if ( price == Constants.UNSET_DOUBLE ) { // NULL
            return 0;
        } else if ( price == Double.MIN_VALUE ) {
            return Integer.MIN_VALUE;
        }

        return (int) ((price - Constants.WEIGHT) * Constants.KEEP_DECIMAL_PLACE_FACTOR);
    }

    public static long priceToExternalLong( double price ) {
        
        if ( price >= 0 ) {
            if ( price == Double.MAX_VALUE ) {
                return Long.MAX_VALUE;
            }
            return (long) ((price + Constants.WEIGHT) * Constants.KEEP_DECIMAL_PLACE_FACTOR);
        }
        
        if ( price == Constants.UNSET_DOUBLE ) { // NULL
            return 0;
        } else if ( price == Double.MIN_VALUE ) {
            return Long.MIN_VALUE;
        }

        return (long) ((price - Constants.WEIGHT) * Constants.KEEP_DECIMAL_PLACE_FACTOR);
    }
}
