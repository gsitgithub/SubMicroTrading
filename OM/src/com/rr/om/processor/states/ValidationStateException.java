/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor.states;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;


public class ValidationStateException extends StateException {

    private static final long serialVersionUID = 1L;
    
    private ReusableString _message;

    /**
     * create a validation exception 
     * 
     * @param message
     */
    public ValidationStateException( ZString message ) {
        super();
        
        ReusableString s = TLC.instance().pop();
        s.setValue( message );
        _message = s;
    }
    
    public void recycle() {
        TLC.instance().pushback( _message );
        _message = null;
    }
    
    public ViewString getValidationError() {
        return _message;
    }
}
