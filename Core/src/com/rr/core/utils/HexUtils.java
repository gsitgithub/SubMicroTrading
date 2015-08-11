/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import com.rr.core.lang.ReusableString;

public class HexUtils {

    public static byte[] hexStringToBytes( String exp ) {
        String[] hexArray = exp.split( " " );
        byte[] tmp = new byte[hexArray.length];

        int bytes = 0;
        
        for ( int i = 0 ; i < hexArray.length ; i++ ) {
            if ( hexArray[i].length() != 2 )
                continue;
            
            tmp[i] = (byte) ( ( Character.digit( hexArray[i].charAt( 0 ), 16 ) << 4) +
                            Character.digit( hexArray[i].charAt( 1 ), 16 ) );
            
            ++bytes;
        }
        
        byte[] result = new byte[bytes];
        System.arraycopy( tmp, 0, result, 0, bytes );
        
        return result;
    }


    public static void bytesToHexSpacedString( final byte[] buf, int offset, int len, final ReusableString s ) {
        int maxOffset = offset + len;
        for( int i=offset ; i < maxOffset ; i++ ) {
            byte b = buf[ i ];
            
            byte major = (byte)((0xFF & b) >> 4 ); // upper nybble 
            byte minor = (byte)((0x0F & b) );      // lower nybble
            
            if ( major <= 9 )   major = (byte)(major + '0');
            else                major = (byte)((major + 'A') - 10);
            
            if ( minor <= 9 )   minor = (byte)(minor + '0');
            else                minor = (byte)((minor + 'A') - 10);
            
            if ( i > 0 ) {
                s.append( " " );                
            }
            
            s.append( major ).append( minor );
        }
    }


    public static byte[] convert( String msg, byte src, byte change ) {
        byte[] dest = new byte[ msg.length() ];
        
        for( int i=0  ; i < msg.length() ; i++ ) {
            byte b = (byte) msg.charAt( i );
            
            dest[i] = (( b == src ) ? change : b); 
        }
        
        return dest;
    }


    public static void hexStringToBytes( byte[] hexStr, int offset, ReusableString destBuf ) {
        destBuf.ensureCapacity( hexStr.length >> 1 );
        
        destBuf.reset();
        
        byte[] dest = destBuf.getBytes();

        int j=0;
        int maxSafeIdx = hexStr.length - 1;
        
        int i = offset;
        
        while( i < maxSafeIdx ) {
            
            byte b1 = hexStr[i++];
            byte b2 = hexStr[i++];
            
            dest[j++] = (byte) ( ( Character.digit( b1, 16 ) << 4) + Character.digit( b2, 16 ) );
            
            if ( i < hexStr.length ) {
                if ( hexStr[i] == ' ' ) {
                    ++i;
                }
            }
        }

        destBuf.setLength( j );
        
    }
}
