/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.router;

import com.rr.core.collections.ConcurrentPooledElementsMap;
import com.rr.core.component.SMTComponent;
import com.rr.core.lang.ZString;
import com.rr.core.model.MessageHandler;

/**
 * used to multiplex between multiple order sources and single exchange destination
 */
public final class RouterSharedData implements SMTComponent {

    private final String                                                _id;
    private final ConcurrentPooledElementsMap<ZString,MessageHandler>   _idToSrc;
    
    public RouterSharedData( String id, int expectedOrders ) {
        super();
        _id = id;
        
        _idToSrc = new ConcurrentPooledElementsMap<ZString,MessageHandler>( expectedOrders );         
    }

    /**
     * store map entry associating clOrdId with handler
     * 
     * @WARN keeps reference to the clOrdId so must supply copy for this func to use
     *       doesnt make copy here as dont know which thread invocation is on
     */
    public final void storeReturnRoute( final ZString clOrdId, final MessageHandler replyHandler ) {
        _idToSrc.put( clOrdId, replyHandler );
    }

    public final MessageHandler findHandler( ZString clOrdId, ZString orderId ) {
        
        MessageHandler mh = _idToSrc.get( orderId );
        
        if ( mh == null ) {
            mh = _idToSrc.get( clOrdId );
        }

        return mh;
    }

    public final MessageHandler findHandler( ZString clOrdId ) {
        
        MessageHandler mh = _idToSrc.get( clOrdId );
        
        return mh;
    }

    /**
     * store map entry associating orderId with same handler as clOrdId
     * 
     * required as many exchanges dont send clOrdId on fill
     * 
     * @WARN keeps reference to the orderId so must supply copy for this func to use
     *       doesnt make copy here as dont know which thread invocation is on
     */
    public final MessageHandler matchUpOrderIdToClOrdId( ZString clOrdId, ZString orderId ) {
        
        final MessageHandler mh = _idToSrc.get( clOrdId );
        
        if ( mh == null ) return null;
        
        if ( orderId.length() > 0 ) {
            _idToSrc.put( orderId, mh );
        }

        return mh;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}
