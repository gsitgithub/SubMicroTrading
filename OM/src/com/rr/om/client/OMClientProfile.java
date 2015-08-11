/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.client;

import com.rr.core.model.ClientProfile;
import com.rr.model.generated.internal.events.interfaces.Alert;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.om.model.event.OrderEventHandlers;
import com.rr.om.order.Order;
import com.rr.om.processor.states.StateException;

public interface OMClientProfile extends ClientProfile, OrderEventHandlers {

    public Alert handleNOSGetAlerts(   final Order order, final NewOrderSingle        msg ) throws StateException;
    public Alert handleAmendGetAlerts( final Order order, final CancelReplaceRequest  msg ) throws StateException;

    public boolean isSendClientLateFills();

    /**
     * @param sendClientLateFills
     * @return previous value
     */
    public boolean setSendClientLateFills( boolean sendClientLateFills );
}
