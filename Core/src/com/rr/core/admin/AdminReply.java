/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.admin;

import com.rr.core.lang.ZString;

public interface AdminReply {

    public void add( ZString val );
    public void add( String val );
    public void add( boolean val );
    public void add( long val );
    public void add( int val );
    public void add( double val );
    
    /**
     * reply is complete, perform end of reply processing
     * 
     * @return the final formatted String
     */
    public String end();
}
