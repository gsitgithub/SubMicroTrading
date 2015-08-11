/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import com.rr.core.lang.ZString;

public class NumberFormatUtils {

    public static final int       MAX_DOUBLE_DIGITS = 18;

    public static int[]           _dig100           = makeTwoDigs(); // 1 to 100 encoded as short upperByte = char for tens, lowerbyte = char for units
    
    public static final byte[]    _dig10            = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    public static final int[]     _hundreds         = { 0, 100, 200, 300, 400, 500, 600, 700, 800, 900 };
    public static final int[]     _tens             = { 0, 10, 20, 30, 40, 50, 60, 70, 80, 90 };

    public static void addLong( byte[] buffer, int idx, long value, int length ) {
        long q;
        int r;
        int charPos = idx + length;
        byte sign = 0;

        if ( value < 0 ) {
            sign = '-';
            value = -value;
        }

        if ( value == 0 ) {
            buffer[--charPos] = '0';
        } else {                        // encoding right to left
            while( value > 65536 ) {
                q = value / 100;
                r = (int) (value - (q * 100));
                value = q;
                final int t = _dig100[ r ];
                
                buffer[--charPos] = (byte)(t & 0xFF);   // units
                buffer[--charPos] = (byte)(t >> 8);     // tens
            }

            do {
                q = value / 10;
                r = (int) (value - (q * 10));
                buffer[--charPos] = _dig10[r];
                value = q;
            } while( value != 0 );
        }

        if ( sign != 0 ) {
            buffer[--charPos] = sign;
        }
    }

    public static int toInteger( ZString strInt ) {

        int val = 0;

        byte[] b = strInt.getBytes();

        for ( int i = 0 ; i < b.length ; ++i ) {
            byte x = b[i];

            if ( x >= '0' && x <= '9' ) {
                val = (val * 10) + (x - '0');
            } else {
                break;
            }
        }

        return val;
    }

    public static long toLong( ZString strLong ) {

        long val = 0;

        byte[] b = strLong.getBytes();

        for ( int i = 0 ; i < b.length ; ++i ) {
            byte x = b[i];

            if ( x >= '0' && x <= '9' ) {
                val = (val * 10) + (x - '0');
            } else {
                break;
            }
        }

        return val;
    }

    public static void addInt( byte[] buffer, int idx, int value, int length ) {

        int r;
        int charPos = idx + length;
        byte sign = 0;

        if ( value < 0 ) {
            sign = '-';
            value = -value;
        }

        // Do the integer part now
        if ( value == 0 ) {
            buffer[--charPos] = '0';
        } else {
            // Get 2 digits/iteration using ints
            int q;
            while( value > 65536 ) {
                q = value / 100;
                r = value - (q * 100);
                value = q;
                final int t = _dig100[ r ];
                
                buffer[--charPos] = (byte)(t & 0xFF);   // units
                buffer[--charPos] = (byte)(t >> 8);     // tens
            }

            do {
                q = value / 10;
                r = value - (q * 10);
                buffer[--charPos] = _dig10[r];
                value = q;
            } while( value != 0 );
        }
        if ( sign != 0 ) {
            buffer[--charPos] = sign;
        }
    }

    public static void addPositiveIntFixedLength( byte[] buffer, int idx, int value, int length ) {

        int r;
        int charPos = idx + length;

        // Do the integer part now
        if ( value == 0 ) {
            buffer[--charPos] = '0';
        } else {
            // Get 2 digits/iteration using ints
            int q;
            while( value > 65536 ) {
                q = value / 100;
                r = (value - (q * 100));
                value = q;
                final int t = _dig100[ r ];
                
                buffer[--charPos] = (byte)(t & 0xFF);   // units
                buffer[--charPos] = (byte)(t >> 8);     // tens
            }

            do {
                q = value / 10;
                r = (value - (q * 10));
                buffer[--charPos] = _dig10[r];
                value = q;
            } while( value != 0 );
        }

        int padLen = charPos - idx;
        while( padLen > 0 ) {
            buffer[--charPos] = '0';
            padLen--;
        }
    }

    public static void addPositiveLongFixedLength( byte[] buffer, int idx, long value, int length ) {
        long q;
        int r;
        int charPos = idx + length;

        if ( value == 0 ) {
            buffer[--charPos] = '0';
        } else {
            while( value > 65536 ) {
                q = value / 100;
                r = (int) (value - (q * 100));
                value = q;
                final int t = _dig100[ r ];
                buffer[--charPos] = (byte)(t & 0xFF);   // units
                buffer[--charPos] = (byte)(t >> 8);     // tens
            }

            do {
                q = value / 10;
                r = (int) (value - (q * 10));
                buffer[--charPos] = _dig10[r];
                value = q;
            } while( value != 0 );
        }

        int padLen = charPos - idx;
        while( padLen > 0 ) {
            buffer[--charPos] = '0';
            padLen--;
        }
    }

    /**
     * append a double truncated at 6 dp to supplied buffer caller must ensure truncates decimal places for numbers bigger than 99,999,999,999 the buffer is big
     * enough
     */
    public static int addPrice( byte[] buffer, int idx, double doubleValue, int len ) {
        final long base = (long) doubleValue;

        if ( base > 99999999999L || base < -99999999999L ) {
            // not enough space for 6DP to be added to long
            len = getLongLen( base );
            addLong( buffer, idx, base, len );
            return len;
        }

        final long value = (long) (doubleValue * 1000000D);
        return addPriceLong( buffer, idx, value, len );
    }

    private static int addPriceLong( byte[] buffer, int idx, long value, int length ) {
        long q;
        int r;
        int charPos = idx + length;
        int nxtFreeIdx = idx;
        byte sign = 0;

        if ( value < 0 ) {
            sign = '-';
            value = -value;
        }

        // Ok, we know that the input value will be at least 6 digits long due
        // to 1M multiplier, so do 3 iterations (double digits)
        boolean digitsWritten = false;
        for ( int i = 0 ; i < 3 ; i++ ) {
            q = value / 100;
            r = (int) (value - ((q << 6) + (q << 5) + (q << 2)));
            value = q;

            final int t = _dig100[ r ];
            final int units = (t & 0xFF);

            value = q;
            if ( digitsWritten ) {
                buffer[--charPos] = (byte)units;   // units
                buffer[--charPos] = (byte)(t >> 8);     // tens
            } else {
                if ( r == 0 ) {
                    charPos -= 2;
                } else {
                    digitsWritten = true;
                    if ( units == 0x30 ) {
                        charPos--;
                        nxtFreeIdx = charPos;
                    } else {
                        nxtFreeIdx = charPos;
                        buffer[--charPos] = (byte)units;
                    }

                    buffer[--charPos] = (byte)(t >> 8);
                }
            }
        }

        if ( !digitsWritten ) {

            nxtFreeIdx = charPos + 1;

            // charPos already decremented by 6

            buffer[charPos] = '0';
        }

        buffer[--charPos] = '.';

        // Do the integer part now
        if ( value == 0 ) {
            buffer[--charPos] = '0';
        } else {
            while( value > 65536 ) {
                q = value / 100;
                r = (int) (value - (q * 100));
                value = q;
                final int t = _dig100[ r ];
                
                buffer[--charPos] = (byte)(t & 0xFF);   // units
                buffer[--charPos] = (byte)(t >> 8);     // tens
            }

            do {
                q = value / 10;
                r = (int) (value - (q * 10));
                buffer[--charPos] = _dig10[r];
                value = q;
            } while( value != 0 );
        }
        
        if ( sign != 0 ) {
            buffer[--charPos] = sign;
        }

        return (nxtFreeIdx - idx);
    }

    public static int getLongLen( long value ) {
        int len;
        if ( value < 0 ) {
            len = getNegLongLen( value );
        } else {
            len = getPosLongLen( value );
        }
        return len;
    }

    public static int getPosShortLen( final short v ) {
        if ( v < 10 )
            return 1;
        if ( v < 100 )
            return 2;
        if ( v < 1000 )
            return 3;
        if ( v < 10000 )
            return 4;
        return 5;
    }

    public static int getPosIntLen( final int v ) {
        if ( v < 10 )
            return 1;
        if ( v < 100 )
            return 2;
        if ( v < 1000 )
            return 3;
        if ( v < 10000 )
            return 4;
        if ( v < 100000 )
            return 5;
        if ( v < 1000000 )
            return 6;
        if ( v < 10000000 )
            return 7;
        if ( v < 100000000 )
            return 8;
        if ( v < 1000000000 )
            return 9;
        return 10;
    }

    public static int getNegIntLen( final int v ) {
        if ( v > -10 )
            return 2;
        if ( v > -100 )
            return 3;
        if ( v > -1000 )
            return 4;
        if ( v > -10000 )
            return 5;
        if ( v > -100000 )
            return 6;
        if ( v > -1000000 )
            return 7;
        if ( v > -10000000 )
            return 8;
        if ( v > -100000000 )
            return 9;
        if ( v > -1000000000 )
            return 10;
        return 11;
    }

    public static int getPosLongLen( final long v ) {
        if ( v < 10L )
            return 1;
        if ( v < 100L )
            return 2;
        if ( v < 1000L )
            return 3;
        if ( v < 10000L )
            return 4;
        if ( v < 100000L )
            return 5;
        if ( v < 1000000L )
            return 6;
        if ( v < 10000000L )
            return 7;
        if ( v < 100000000L )
            return 8;
        if ( v < 1000000000L )
            return 9;
        if ( v < 10000000000L )
            return 10;
        if ( v < 100000000000L )
            return 11;
        if ( v < 1000000000000L )
            return 12;
        if ( v < 10000000000000L )
            return 13;
        if ( v < 100000000000000L )
            return 14;
        if ( v < 1000000000000000L )
            return 15;
        if ( v < 10000000000000000L )
            return 16;
        if ( v < 100000000000000000L )
            return 17;
        if ( v < 1000000000000000000L )
            return 18;
        return 19;
    }

    public static int getNegLongLen( final long v ) {
        if ( v > -10L )
            return 2;
        if ( v > -100L )
            return 3;
        if ( v > -1000L )
            return 4;
        if ( v > -10000L )
            return 5;
        if ( v > -100000L )
            return 6;
        if ( v > -1000000L )
            return 7;
        if ( v > -10000000L )
            return 8;
        if ( v > -100000000L )
            return 9;
        if ( v > -1000000000L )
            return 10;
        if ( v > -10000000000L )
            return 11;
        if ( v > -100000000000L )
            return 12;
        if ( v > -1000000000000L )
            return 13;
        if ( v > -10000000000000L )
            return 14;
        if ( v > -100000000000000L )
            return 15;
        if ( v > -1000000000000000L )
            return 16;
        if ( v > -10000000000000000L )
            return 17;
        if ( v > -100000000000000000L )
            return 18;
        if ( v > -1000000000000000000L )
            return 19;
        return 20;
    }

    public static int getPriceLen( final double doubleValue ) {
        long value = (long) (doubleValue * 1000000D);
        int len;
        if ( value < 0 ) {
            len = getNegLongLen( value );
            if ( len < 8 ) // if length is less than 7 it means that the original values was 0.something
                len = 8; // 6 for the fractional + 1 for the leading integer part
        } else {
            len = getPosLongLen( value );
            if ( len < 7 ) // if length is less than 7 it means that the original values was 0.something
                len = 7; // 6 for the fractional + 1 for the leading integer part
        }
        len++; // adjust for the dot '.'
        return len;
    }

    private static int[] makeTwoDigs() {
        int[] t = new int[100];
        
        for( int tens=0 ; tens < 10 ; tens++ ) {
            int tenCH = tens + '0';
            
            for( int units=0 ; units < 10 ; units++ ) {
                int unitCH = units + '0';
                int v = (tenCH << 8) + unitCH;
                t[tens*10 + units] = v;
            }
        }
        
        return t;
    }
}
