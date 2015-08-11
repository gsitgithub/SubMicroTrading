/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book;

import com.rr.core.model.Book;
import com.rr.core.model.BookListener;


/**
 * specialisation of BookListener for subscribers that only use subscribe upto 32 books.
 * 
 * BookSubscriber used by the listener must maintain fixed indexed set of books per subscriber 
 *
 * @author Richard Rose
 */
public interface SmallBookListener<T extends Book> extends BookListener<T> {

    /**
     * notification that 1 or more books have changed, each changed book identified by a bit
     * in the bookChangeBitSet
     * 
     * @WARN only use where the subscription list is static and setup before market data processing
     *       does not guarentee thread safety for changes by the subscriber to its subscription list
     * 
     * @param bookChangeBitSet
     */
    public void changed( int bookChangeBitSet );
}
