/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.dispatch;

import com.rr.core.component.SMTControllableComponent;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;

/**
 * @NOTE avoid adding more methods, create new interfaces and implement where appropriate to avoid plethora of empty methods where not wanted 
 */
public interface MessageDispatcher extends SMTControllableComponent {

    /**
     * start the dispatcher, also invoke init on the handler, if invoked more than once ignore
     */
    public void start();

    public void setStopping();

    public void dispatch( Message msg );

    public void setHandler( MessageHandler handler );

    /**
     * some routers may need different behaviour if the delegate is disconnected ie unable to  
     * 
     * @param isOk
     */
    public void handlerStatusChange( MessageHandler handler, boolean isOk );

    /**
     * @return true if when disconnected an enqueue and replay later
     */
    public boolean canQueue();

    /**
     * @return string of key properties 
     */
    public String info();

    /**
     * dispatch message required for synchronisation ... i.e. before full logOn can be achieved
     * 
     * @param msg
     */
    public void dispatchForSync( Message msg );
}
