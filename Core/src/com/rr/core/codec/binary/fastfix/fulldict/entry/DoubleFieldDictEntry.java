/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.fulldict.entry;

import com.rr.core.codec.binary.fastfix.common.FastFixDecimal;
import com.rr.core.codec.binary.fastfix.common.FieldUtils;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;

public final class DoubleFieldDictEntry implements DictEntry {

    private final FastFixDecimal _prevDecimal = new FastFixDecimal();

    private final double _init;
    private       double _val;

    public DoubleFieldDictEntry() {
        this( Constants.UNSET_DOUBLE );
    }
    
    public DoubleFieldDictEntry( String init ) {
        this( FieldUtils.parseDouble( init ) );
    }
    
    public DoubleFieldDictEntry( double init ) {
        _init = init;
    }
    
    public double getVal() {
        return _val;
    }

    public double getInit() {
        return _init;
    }

    public void setVal( double val ) {
        _val = val;
    }

    @Override
    public void log( ReusableString dest ) {
        dest.append( _val );
    }

    @Override
    public boolean hasValue() {
        return _val != Constants.UNSET_DOUBLE;
    }

    public FastFixDecimal getPrevDecimal() {
        return _prevDecimal;
    }
    
    @Override
    public void reset() {
        _val = _init;
        _prevDecimal.set( _init );
    }

    public double setVal( final int exponent, final long mantissa ) {
        return _val = _prevDecimal.set( exponent, mantissa );
    }
}
