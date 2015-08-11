/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.log;

import java.nio.ByteBuffer;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ZString;

/**
 * picking appropriate log method helps reduce required memory and growing of 
 * log event byte arrays
 *
 * @author Richard Rose
 */
public interface Logger {
    
    public void info( String msg ); // avoid as uses tempobjs

    public void info( ZString msg );
    public void warn( ZString msg );
    public void warn( String msg );
    public void error( ErrorCode code, ZString msg );
    public void error( ErrorCode code, ZString msg, Throwable t );
    public void error( ErrorCode code, String msg );
    public void error( ErrorCode code, String msg, Throwable t );

    // logging messages upto 512 bytes
    public void infoLarge( ZString msg );
    public void infoLarge( byte[] buf, int offset, int len );
    public void warnLarge( String msg );
    public void errorLarge( ErrorCode code, String msg );
    public void errorLarge( ErrorCode code, ZString msg );

    // logging messages over 512 bytes
    public void infoHuge( ZString msg );
    public void infoHuge( byte[] buf, int offset, int len );
    public void infoHuge( ByteBuffer buf );

    public void warnHuge( String msg );
    public void errorHuge( ErrorCode code, String msg );

    // logging binary messages typically 300 bytes, but in hex format can be 2K, this method saves an extra memcpy
    public void infoLargeAsHex( ZString event, int hexStartIdx );
}
