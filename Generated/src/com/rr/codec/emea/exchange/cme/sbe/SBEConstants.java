/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.codec.emea.exchange.cme.sbe;


public interface SBEConstants {
    public static final double  KEEP_DECIMAL_PLACE_FACTOR = 10000000D;
    
    public static final long SBE_NULL_CHAR      = 0;

    public static final long SBE_NULL_BYTE      = -128;
    public static final long SBE_NULL_UBYTE     = 0xFF;
    
    public static final long SBE_NULL_INT16     = Short.MIN_VALUE;       // (-2 ^ 31)
    public static final long SBE_NULL_UINT16    = 0xFFFF;
    
    public static final long SBE_NULL_INT32     = Integer.MIN_VALUE;
    public static final long SBE_NULL_UINT32    = 0xFFFFFFFF;            // (2 ^ 32)- 1
    
    public static final long SBE_NULL_INT64     = Long.MIN_VALUE;        // (-2 ^ 63)
    public static final long SBE_NULL_UINT64    = 0xFFFFFFFFFFFFFFFFl;   // (2 ^ 64)- 1
}
