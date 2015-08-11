/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;


// Indicates type of security. See also the Product (460) and CFICode (461) fields. It is recommended that CFICode (461) be used instead of SecurityType (167) for non-Fixed Income instruments

import java.util.HashMap;
import java.util.Map;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;

public enum SecurityType implements MultiByteLookup {

    Option( "OPT", ProductType.Option ),
    Future( "FUT", ProductType.Future ),
    Equity( "EQUITY", ProductType.Equity ),
    Cash( "CASH", ProductType.Other ),
    Unknown( "?", ProductType.Other );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 6; }

    private byte[] _val;
    private ProductType _prodType = ProductType.Other;

    SecurityType( String val,  ProductType prodType ) {
        _val = val.getBytes();
        _prodType = prodType;
    }


private static Map<ZString,SecurityType> _map = new HashMap<ZString,SecurityType>();

    static {
        for ( SecurityType en : SecurityType.values() ) {
             byte[] val = en.getVal();
            ZString zVal = new ViewString( val );
            _map.put( zVal, en );
        }
    }

    private static ViewString _lookup = new ViewString();
    
    public static SecurityType getVal( final byte[] buf, final int offset, final int len ) {
        _lookup.setValue( buf, offset, len );
        SecurityType val = _map.get( _lookup );
        if ( val == null ) throw new RuntimeDecodingException( "Unsupported value of " + _lookup + " for SecurityType" );
        return val;
    }

    public static SecurityType getVal( ZString key ) {
        SecurityType val = _map.get( key );
        if ( val == null ) throw new RuntimeDecodingException( "Unsupported value of " + key + " for SecurityType" );
        return val;
    }

    @Override
    public final byte[] getVal() {
        return _val;
    }

    public ProductType getProdType() { return _prodType; }

    public void id( ReusableString out ) {
        out.append( toString() );
    }
}
