/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix;


/**
 * FastFixUtils 7bit version of NumberFormatUtils
 */
public class FastFixUtils {
    public static final byte MSB_DATA_BIT              = (byte)1 << 6; // 7 data bits (assuming no sign bit) 
    public static final byte SIGN_BIT                  = (byte)1 << 6; 
    public static final byte OVERFLOW_BIT              = (byte)1 << 5; 
    public static final byte STOP_BIT                  = (byte)0x80; 
    public static final byte NULL_BYTE                 = (byte)0x80;
    public static final byte DATA_BIT_MASK             = (byte)0x7F;

    public static final byte LONG_MSB_MASK             = (byte)0x01; // the 10th byte of a LONG only has 1 bit of data
    public static final byte INT_MSB_MASK              = (byte)0x0F; // the 5th byte of an INT only has 4 bits of data

    public static final byte OVERFLOW_MSB_BIT_MASK     = ~OVERFLOW_BIT & DATA_BIT_MASK;
    public static final byte OVERFLOW_OFF_MASK         = ~OVERFLOW_BIT & DATA_BIT_MASK;
    
    public static final int  MAX_LONG_BYTES            = 10;          // 64 bits divide by 7 round up
    public static final int  MAX_INT_BYTES             = 5;           // 32 bits divide by 7 round up
    public static final int  MAX_SHORT_BYTES           = 3;           // 16 bits divide by 7 round up
    
    
    // UNSIGNED MAX NOT NULLABLE
    // NULLABLE IS SAME EXCEPT INCREMENT POSITIVE NUM BEFORE LENGTH CHECK
    private static final int  MAX_UVAL_ONE_BYTE     = (1  << 7)  -1;
    private static final int  MAX_UVAL_TWO_BYTES    = (1  << 14) -1;
    private static final int  MAX_UVAL_THREE_BYTES  = (1  << 21) -1;
    private static final int  MAX_UVAL_FOUR_BYTES   = (1  << 28) -1;
    private static final long MAX_UVAL_FIVE_BYTES   = (1L << 35) -1; 
    private static final long MAX_UVAL_SIX_BYTES    = (1L << 42) -1; 
    private static final long MAX_UVAL_SEVEN_BYTES  = (1L << 49) -1; 
    private static final long MAX_UVAL_EIGHT_BYTES  = (1L << 56) -1; 
    private static final long MAX_UVAL_NINE_BYTES   = (1L << 63) -1; 
    
    
    // SIGNED NEGATIVE MAX 
    // NULLABLE IS SAME 
    // in 7 bit encoding the MSB the 7th bit is sign bit
    private static final int  MIN_NVAL_ONE_BYTE     = ~(1  << 6)  +1;
    private static final int  MIN_NVAL_TWO_BYTES    = ~(1  << 13) +1;
    private static final int  MIN_NVAL_THREE_BYTES  = ~(1  << 20) +1;
    private static final int  MIN_NVAL_FOUR_BYTES   = ~(1  << 27) +1;
    private static final long MIN_NVAL_FIVE_BYTES   = ~(1L << 34) +1; 
    private static final long MIN_NVAL_SIX_BYTES    = ~(1L << 41) +1; 
    private static final long MIN_NVAL_SEVEN_BYTES  = ~(1L << 48) +1; 
    private static final long MIN_NVAL_EIGHT_BYTES  = ~(1L << 55) +1; 
    private static final long MIN_NVAL_NINE_BYTES   = ~(1L << 62) +1; 
    
    // SIGNED POSITIVE MAX 
    // NULLABLE IS SAME EXCEPT INCREMENT BEFORE LENGTH CHECK
    // in 7 bit encoding the MSB the 7th bit is sign bit
    private static final int  MAX_PVAL_ONE_BYTE     = 1 << 6;
    private static final int  MAX_PVAL_TWO_BYTES    = 1 << 13;
    private static final int  MAX_PVAL_THREE_BYTES  = 1 << 20;
    private static final int  MAX_PVAL_FOUR_BYTES   = 1 << 27;
    private static final long MAX_PVAL_FIVE_BYTES   = 1L << 34; 
    private static final long MAX_PVAL_SIX_BYTES    = 1L << 41; 
    private static final long MAX_PVAL_SEVEN_BYTES  = 1L << 48; 
    private static final long MAX_PVAL_EIGHT_BYTES  = 1L << 55; 
    private static final long MAX_PVAL_NINE_BYTES   = 1L << 62;
    
    public static int getULongLen( final long v ) {
        if ( v < 0 ) 
            return 10;
        if ( v < MAX_UVAL_ONE_BYTE )
            return 1;
        if ( v < MAX_UVAL_TWO_BYTES )
            return 2;
        if ( v < MAX_UVAL_THREE_BYTES )
            return 3;
        if ( v < MAX_UVAL_FOUR_BYTES )
            return 4;
        if ( v < MAX_UVAL_FIVE_BYTES )
            return 5;
        if ( v < MAX_UVAL_SIX_BYTES )
            return 6;
        if ( v < MAX_UVAL_SEVEN_BYTES )
            return 7;
        if ( v < MAX_UVAL_EIGHT_BYTES )
            return 8;
        if ( v < MAX_UVAL_NINE_BYTES )
            return 9;
        return 10;
    }

    public static int getLongLen( final long value ) {
        int len;
        if ( value < 0 ) {
            len = getNegLongLen( value );
        } else {
            len = getPosLongLen( value );
        }
        return len;
    }

    public static int getPosLongLen( final long v ) {
        if ( v < MAX_PVAL_ONE_BYTE )
            return 1;
        if ( v < MAX_PVAL_TWO_BYTES )
            return 2;
        if ( v < MAX_PVAL_THREE_BYTES )
            return 3;
        if ( v < MAX_PVAL_FOUR_BYTES )
            return 4;
        if ( v < MAX_PVAL_FIVE_BYTES )
            return 5;
        if ( v < MAX_PVAL_SIX_BYTES )
            return 6;
        if ( v < MAX_PVAL_SEVEN_BYTES )
            return 7;
        if ( v < MAX_PVAL_EIGHT_BYTES )
            return 8;
        if ( v < MAX_PVAL_NINE_BYTES )
            return 9;
        return 10;
    }

    public static int getNegLongLen( final long v ) {
        if ( v > MIN_NVAL_ONE_BYTE )
            return 1;
        if ( v > MIN_NVAL_TWO_BYTES )
            return 2;
        if ( v > MIN_NVAL_THREE_BYTES )
            return 3;
        if ( v > MIN_NVAL_FOUR_BYTES )
            return 4;
        if ( v > MIN_NVAL_FIVE_BYTES )
            return 5;
        if ( v > MIN_NVAL_SIX_BYTES )
            return 6;
        if ( v > MIN_NVAL_SEVEN_BYTES )
            return 7;
        if ( v > MIN_NVAL_EIGHT_BYTES )
            return 8;
        if ( v > MIN_NVAL_NINE_BYTES )
            return 9;
        return 10;
    }

    public static int getUIntLen( final int v ) {
        if ( v < 0 ) 
            return 5;
        if ( v < MAX_UVAL_ONE_BYTE )
            return 1;
        if ( v < MAX_UVAL_TWO_BYTES )
            return 2;
        if ( v < MAX_UVAL_THREE_BYTES )
            return 3;
        if ( v < MAX_UVAL_FOUR_BYTES )
            return 4;
        return 5;
    }

    public static int getIntLen( final int value ) {
        int len;
        if ( value < 0 ) {
            len = getNegIntLen( value );
        } else {
            len = getPosIntLen( value );
        }
        return len;
    }

    public static int getPosIntLen( final int v ) {
        if ( v < MAX_PVAL_ONE_BYTE )
            return 1;
        if ( v < MAX_PVAL_TWO_BYTES )
            return 2;
        if ( v < MAX_PVAL_THREE_BYTES )
            return 3;
        if ( v < MAX_PVAL_FOUR_BYTES )
            return 4;
        return 5;
    }

    public static int getNegIntLen( final int v ) {
        if ( v > MIN_NVAL_ONE_BYTE )
            return 1;
        if ( v > MIN_NVAL_TWO_BYTES )
            return 2;
        if ( v > MIN_NVAL_THREE_BYTES )
            return 3;
        if ( v > MIN_NVAL_FOUR_BYTES )
            return 4;
        return 5;
    }

    public static int getUShortLen( final short v ) {
        if ( v < 0 ) 
            return 3;
        if ( v < MAX_UVAL_ONE_BYTE )
            return 1;
        if ( v < MAX_UVAL_TWO_BYTES )
            return 2;
        return 3;
    }

    public static int getShortLen( final short value ) {
        int len;
        if ( value < 0 ) {
            len = getNegShortLen( value );
        } else {
            len = getPosShortLen( value );
        }
        return len;
    }


    public static int getPosShortLen( final short v ) {
        if ( v < MAX_PVAL_ONE_BYTE )
            return 1;
        if ( v < MAX_PVAL_TWO_BYTES )
            return 2;
        return 3;
    }

    public static int getNegShortLen( final short v ) {
        if ( v > MIN_NVAL_ONE_BYTE )
            return 1;
        if ( v > MIN_NVAL_TWO_BYTES )
            return 2;
        return 3;
    }
}
