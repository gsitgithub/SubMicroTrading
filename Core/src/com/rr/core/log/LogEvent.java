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
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;

public interface LogEvent extends Message {

    public void reset();

    @Override
    public ReusableType getReusableType();

    public int length();

    public long getTime();

    public Level getLevel();

    /**
     * encodes the log event to the destination buffer
     * 
     * LogEventLarge uses static vars for HEX encoding which MUST ONLY be invoked from same thread ... ie the AsyncLogger background thread
     */
    public void encode( ByteBuffer dest );

    public void setError( ErrorCode code, String msg );

    public void setError( ErrorCode code, String msg, Throwable t );

    public void setError( ErrorCode code, byte[] bytes, int offset, int length );

    public void setError( ErrorCode code, byte[] bytes, int offset, int length, byte[] other, int offsetOther, int otherLength );

    public void set( Level lvl, byte[] bytes, int i, int length );

    public void set( Level lvl, byte[] bytes, int offset, int length, byte[] other, int offsetOther, int otherLength );

    public void set( Level info, ByteBuffer buf );

    public void set( Level info, String msg );

    public ZString getMessage();

}
