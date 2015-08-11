/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.registry;

import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ZString;
import com.rr.om.order.Order;

public interface TradeWrapper {

    public ZString    getExecId();
    public int        getQty();
    public double     getPrice();
    
    public TradeWrapper getNextWrapper();
    public void setNextWrapper( TradeWrapper next );
    
    public ReusableType getReusableType();
    
    /**
     * compares the trade wrappers using order and execId
     * @return true if the same order and execId
     */
    public boolean equals( TradeWrapper tw );
    
    /**
     * exchanges that dont provide unique execIds will have to generate unique id for client 
     * 
     * @param execId
     */
    public void setClientExecId( ZString execId );
    public ZString getClientExecId();
    public Order getOrder();
}
