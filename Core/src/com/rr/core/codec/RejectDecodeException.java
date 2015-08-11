/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;

public class RejectDecodeException extends BaseReject<RejectDecodeException>{
    
    private final ReusableString _trace = new ReusableString();

    public RejectDecodeException( byte[] fixMsg, int offset, int maxIdx, RuntimeDecodingException t ) {
        this( fixMsg, offset, maxIdx, t, null );
    }

    public RejectDecodeException( byte[] fixMsg, int offset, int maxIdx, RuntimeDecodingException t, ReusableString dump ) {
        super( fixMsg, offset, maxIdx, t );
        
        _trace.copy( dump );
    }

    @Override
    public void reset()  {
        super.reset();
        _trace.reset();
    }

    @Override
    public void dump( ReusableString out ) {
        if ( _trace.length() > 0 ) {
            out.append( ", TRACE - " );
            out.append( _trace );
            out.append( " :: " );

            _trace.reset(); // stop multiple copies of this appearing in log
        }

        super.dump( out );
    }

    @Override
    public ReusableType getReusableType() {
        return CoreReusableType.RejectDecodeException;
    }
}
