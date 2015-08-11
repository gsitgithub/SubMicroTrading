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

public enum SubChannel {

    // note the ordinal IS used
    
    Zero,
    One,
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine;

    public static SubChannel get( int subChannel ) {
        switch( subChannel ) {
        case 1:
            return One;
        case 2:
            return Two;
        case 3:
            return Three;
        case 4:
            return Four;
        case 5:
            return Five;
        case 6:
            return Six;
        case 7:
            return Seven;
        case 8:
            return Eight;
        case 9:
            return Nine;
        }
        
        throw new SMTRuntimeException( "Invalid sub channel " + subChannel );
    }
}
