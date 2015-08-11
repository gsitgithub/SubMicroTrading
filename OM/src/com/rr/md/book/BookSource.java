/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book;

import com.rr.core.component.SMTControllableComponent;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;
import com.rr.core.model.MessageHandler;
import com.rr.core.thread.PipeLineable;


/**
 * source for subscription of books
 * 
 * pipeLineId is a logical pipe representing market data sessions + book source ... its used for load balancing, either round robin or by specific pipe
 */
public interface BookSource<T extends Book> extends MessageHandler, PipeLineable, SMTControllableComponent {

    /**
     * subscribe to requested book
     * 
     * @param securityDescription
     * 
     * @return the actual book instance used by the BookSrc or null
     */
    public T subscribe( Instrument inst );

    /**
     * @param inst
     * @return if the source supports the denoted instrument
     */
    public boolean supports( Instrument inst );
    
    /**
     * This is used by book consumers to force instrument subscription to particular controller via an allocated pipeLineId
     * 
     * @param pipeLineId
     * @return true if the BookSrc has the supplied pipeLineId
     */
    @Override
    public boolean hasPipeLineId( String pipeLineId );
}
