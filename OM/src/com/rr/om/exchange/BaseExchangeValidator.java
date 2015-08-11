/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.exchange;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Instrument;
import com.rr.om.Strings;

public abstract class BaseExchangeValidator implements OMExchangeValidator {

    private static final ZString UNSUPPORTED              = new ViewString( "Unsupported attribute value " );
    private static final ZString EXCHANGE_NOT_OPEN        = new ViewString( "Exchange not open " );

    protected final void validateOpen( final Instrument instrument, final long now, final ReusableString err ) {
        if ( ! instrument.getExchangeSession().isOpen( now ) )   addError( err, EXCHANGE_NOT_OPEN );
    }

    protected final void addErrorUnsupported( final Enum<?> val, final ReusableString err ) {

        if ( err.length() > 0 ) {
            err.append( Strings.DELIM );
        }
        
        err.append( UNSUPPORTED ).append( val.toString() ).append( Strings.TYPE ).append( val.getClass().getSimpleName() );
    }
    
    protected final void addError( final ReusableString err, final ZString msg ) {
        delim( err ).append( msg );
    }
    
    protected final ReusableString delim( final ReusableString err ) {
        if ( err.length() > 0 ) {
            err.append( Strings.DELIM );
        }
        
        return err;
    }

    
}
