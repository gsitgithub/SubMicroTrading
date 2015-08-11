/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.common;

import com.rr.core.lang.Constants;

public class FieldUtils {

    public static double parseDouble( String init ) {
        if ( init == null || init.length() == 0 ) return Constants.UNSET_DOUBLE;
        return Double.parseDouble( init );
    }

    public static int parseInt( String init ) {
        if ( init == null || init.length() == 0 ) return Constants.UNSET_INT;
        return Integer.parseInt( init );
    }

    public static long parseLong( String init ) {
        if ( init == null || init.length() == 0 ) return Constants.UNSET_LONG;
        return Long.parseLong( init );
    }

}
