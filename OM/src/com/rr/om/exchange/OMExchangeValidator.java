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
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.om.model.instrument.ExchangeValidator;

public interface OMExchangeValidator extends ExchangeValidator {

    /**
     * validate the NOS appending errors to the supplied err buf
     * 
     * @param msg
     * @param err
     * @param now 
     */
    public void validate( NewOrderSingle msg, ReusableString err, long now );

    /**
     * validate the cancel replace request, appending errors to the err buf
     * 
     * @param msg
     * @param err
     */
    public void validate( CancelReplaceRequest msg, ReusableString err, long now );
}
