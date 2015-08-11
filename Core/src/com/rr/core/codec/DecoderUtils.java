/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import java.util.Map;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;

public class DecoderUtils {

    public static void populate( Map<ReusableString, ReusableString> map, byte[] fixMsg, int offset, int maxIdx ) {

        int startKey = 0;
        int endKey = -1;
        int startVal = -1;
        int endVal =-1;
        
        if ( maxIdx > fixMsg.length ) maxIdx = fixMsg.length;
        
        for( int i=0 ; i < maxIdx ;++i ) {
        
            byte b = fixMsg[i];
            
            if ( b == '=' ) {
                endKey = i-1;
                startVal = i+1;
            } else if ( b == FixField.FIELD_DELIMITER ) {
                endVal = i-1;
                if ( endKey > 0 ){
                    ReusableString key = TLC.instance().pop();
                    ReusableString val = TLC.instance().pop();
                    key.setValue( fixMsg, startKey, endKey-startKey+1 );
                    val.setValue( fixMsg, startVal, endVal-startVal+1 ); 
                    map.put( key, val );
                }
                startKey = i+1;
                endKey = -1;
            }
        }
    }
}
