/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils.file;


public final class AsciiFileAppender extends BaseFileAppender {

    public AsciiFileAppender( String fileName, long maxFileSize, boolean enforceMinFileSize ) {
        super( fileName, maxFileSize, enforceMinFileSize );
    }

    public AsciiFileAppender( String fileName, long maxFileSize ) {
        super( fileName, maxFileSize );
    }

    @Override
    public void doLog( byte[] buf, int offset, int len ) {

        if ( (len + _directBlockBuf.position() + 2) >= _directBlockBuf.capacity() ) {
            blockWriteToDisk();
        }

        _directBlockBuf.put( buf, offset, len );
        _directBlockBuf.put( (byte)0x0A );
    }
}