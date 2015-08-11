/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.common;

import com.rr.core.lang.ReusableString;

public final class StringFieldValWrapper implements FieldValWrapper {

    // DONT MAKE NON-FINAL 
    private final ReusableString _val = new ReusableString();

    public ReusableString getVal() {
        return _val;
    }

    public void setVal( ReusableString val ) {
        _val.copy( val );
    }
    
    @Override
    public void log( ReusableString dest ) {
        dest.append( _val );
    }

    @Override
    public boolean hasValue() {
        return _val != null && _val.length() > 0;
    }

    @Override
    public void reset() {
        _val.reset();
    }
}
