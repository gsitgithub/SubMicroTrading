/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

public class StringFactory {

    public static ViewString hexToViewString( String hex ) {
        byte[] bytes = hex.getBytes();
        
        if ( bytes[0] == '0' && bytes[1] == 'x' ) {
            int approxlen = (bytes.length - 2) / 2 + 1;
            ReusableString s = new ReusableString( approxlen );
            
            int loopSize = bytes.length - 1;
            int i = 2;
            
            while ( i < loopSize ) {
                int b1 = chkHex( bytes[i++] );
                int b2 = chkHex( bytes[i++] );
                
                if ( b1 == -1 || b2 == -1 ) {
                    throw new RuntimeException( "Invalid char in hex string " + hex + ", offset=" + (i-2) + ", b1=" + b1 + ", b2=" + b2 );
                }

                int val = (b1 << 4) + b2;
                
                s.append( (byte) val );
            }
            
            return s;
            
        } 
        
        return new ViewString( bytes );
    }

    private static int chkHex( byte b ) {
        if ( b >= '0' && b <= '9' ) {
            return b - '0';
        }
        if ( b >= 'A' && b <= 'F' ) {
            return b + 10 - 'A';
        }
        if ( b >= 'a' && b <= 'f' ) {
            return b + 10 - 'a';
        }
        return -1;
    }
}
