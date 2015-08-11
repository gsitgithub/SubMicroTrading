/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.dummy.warmup;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.om.Strings;
import com.rr.om.exchange.BaseExchangeValidator;

public class DummyValidator extends BaseExchangeValidator {

    private static final ZString INVALID_ORD_QTY = new ViewString( "Quantity must be greater than zero, " );
    
    @Override
    public void validate( NewOrderSingle msg, ReusableString err, long now ) {
        validateMinQty( msg.getOrderQty(), err );
        validateOpen( msg.getInstrument(), now, err );
    }

    @Override
    public void validate( CancelReplaceRequest msg, ReusableString err, long now ) {
        validateMinQty( msg.getOrderQty(), err );
        validateOpen( msg.getInstrument(), now, err );
    }
    
    private void validateMinQty( int orderQty, ReusableString err ) {
        if ( orderQty <= 0 ) delim( err ).append( INVALID_ORD_QTY ).append( Strings.QUANTITY ).append( orderQty );
    }
}
