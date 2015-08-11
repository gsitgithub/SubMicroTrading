/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import com.rr.core.codec.RuntimeDecodingException;

public enum ProductType implements SingleByteLookup {

    Future( "F" ),
    Option( "O" ),
    Equity( "E" ),
    Other( "U" ),
    Unknown( "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private byte _val;

    ProductType( String val ) {
        _val = val.getBytes()[0];
    }


    private static ProductType[] _entries = new ProductType[256];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( ProductType en : ProductType.values() ) {
             _entries[ en.getVal() ] = en;
        }
    }

    public static ProductType getVal( byte val ) {
        ProductType eval;
        eval = _entries[ val ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for ProductType" );
        return eval;
    }

    @Override
    public final byte getVal() {
        return _val;
    }

}
