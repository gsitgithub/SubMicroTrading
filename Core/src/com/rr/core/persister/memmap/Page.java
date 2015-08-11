/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.persister.memmap;

import java.nio.MappedByteBuffer;

public final class Page {

    private          int              _pageNo = -1;
    private volatile MappedByteBuffer _buf    = null;

    public Page() {
        super();
    }

    public Page( int pageNo, MappedByteBuffer buf ) {
        super();
        _pageNo = pageNo;
        _buf = buf;
    }

    public MappedByteBuffer getMappedByteBuf() {
        return _buf;
    }

    public int getPageNo() {
        return _pageNo;
    }

    public void setMappedByteBuf( MappedByteBuffer buf ) {
        _buf = buf;
    }

    public void setPageNo( int pageNo ) {
        _pageNo = pageNo;
    }

    public void reset() {
        _pageNo = -1;
        _buf    = null;
    }
}
