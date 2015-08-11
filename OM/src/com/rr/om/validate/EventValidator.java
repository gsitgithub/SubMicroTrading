/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.validate;

import com.rr.core.lang.ViewString;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.om.order.Order;

/**
 * Event validator .. note dont throw exception so dont need pay cost of extra try catch handlers 
 * caller will generally need to generate reject so returning false makes more sense in this case
 * 
 * EventValidator should NOT validate exchange specifics like OrdType, TIF
 *
 * @author Richard Rose
 */
public interface EventValidator {

    /**
     * validate the NOS event
     *  
     * @param msg
     * @return true if validated ok
     */
    boolean validate( NewOrderSingle msg, Order order );

    /**
     * validate the AMEND event
     *  
     * @param msg
     * @return true if validated ok
     */
    boolean validate( CancelReplaceRequest newReq, Order order );

    
    /**
     * @return error message from last validation, suitable for encoding in text field
     */
    ViewString getRejectReason();


    /**
     * @return appropriate reject reason from last validation of NOS
     */
    OrdRejReason getOrdRejectReason();

    /**
     * @return appropriate reject reason from last validation of CancelReplaceRequest
     */
    CxlRejReason getReplaceRejectReason();
}
