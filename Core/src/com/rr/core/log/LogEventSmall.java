/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.log;

import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.stats.SizeConstants;

public class LogEventSmall extends BaseLogEvent<LogEventSmall> {

    private static final int EXPECTED_SIZE = SizeConstants.DEFAULT_LOG_EVENT_SMALL;

    public LogEventSmall() {
        super();
    }
    
    public LogEventSmall( String str ) {
        super( str );
    }

    @Override
    protected final int getExpectedMaxEventSize() {
        return EXPECTED_SIZE;
    }

    @Override
    public final ReusableType getReusableType() {
        return CoreReusableType.LogEventSmall;
    }
}
