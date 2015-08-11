/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

public interface Constants {

    public static final double  KEEP_DECIMAL_PLACE_FACTOR = 1000000D;

    /**
     * strictly speaking should have UNSET values for UINT, ULONG
     * however dont have a real use case for that need and extra complexity
     * WOULD NEED CAREFUL INTRODUCTION
     */
    public static final long    UNSET_LONG        = Long.MIN_VALUE;
    public static final int     UNSET_INT         = Integer.MIN_VALUE;
    public static final short   UNSET_SHORT       = Short.MIN_VALUE;
    public static final char    UNSET_CHAR        = '?';
    public static final byte    UNSET_BYTE        = (byte)0xFF;
    public static final float   UNSET_FLOAT       = Float.NEGATIVE_INFINITY;
    public static final double  UNSET_DOUBLE      = Double.NEGATIVE_INFINITY;
    public static final long    MIN_PRICE_AS_LONG = Long.MIN_VALUE+1;
    public static final int     MIN_PRICE_AS_INT  = Integer.MIN_VALUE+1;
    
    // dont change the weight without re-enabling and running the testEncodePrice
    public static final double  WEIGHT            = 0.00000000001;
    public static final double  TICK_WEIGHT       = 0.00000001;

    public static final int     FIX_TAG_ACK_STATS = 11611;

    public static final int     MS_IN_DAY         = 24 * 60 * 60 * 1000;

    public static final int     PRICE_DP          = 6;

    public static final int     MAX_BUF_LEN       = 4096;

    public static final int     MAX_SESSIONS      = 32;

    public static final int     LOW_PRI_LOOP_WAIT_MS = 10;

    
}
