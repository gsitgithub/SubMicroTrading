/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.warmup.sim;

import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.lang.ErrorCode;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.FixVersion;
import com.rr.model.generated.fix.codec.DropCopy44Decoder;
import com.rr.model.generated.fix.codec.DropCopy44Encoder;
import com.rr.model.generated.fix.codec.MD44Decoder;
import com.rr.model.generated.fix.codec.MD44Encoder;
import com.rr.model.generated.fix.codec.MD50Decoder;
import com.rr.model.generated.fix.codec.MD50Encoder;
import com.rr.model.generated.fix.codec.RecoveryDropCopy44Decoder;
import com.rr.model.generated.fix.codec.RecoveryMD44Decoder;
import com.rr.model.generated.fix.codec.RecoveryMD50Decoder;
import com.rr.model.generated.fix.codec.RecoveryStandard42Decoder;
import com.rr.model.generated.fix.codec.RecoveryStandard44Decoder;
import com.rr.model.generated.fix.codec.RecoveryStandard50Decoder;
import com.rr.model.generated.fix.codec.Standard42Decoder;
import com.rr.model.generated.fix.codec.Standard42Encoder;
import com.rr.model.generated.fix.codec.Standard44Decoder;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.model.generated.fix.codec.Standard50Decoder;
import com.rr.model.generated.fix.codec.Standard50Encoder;

// @TODO generate this from model

public class FixFactory {
    private static final Logger _log = LoggerFactory.console( FixFactory.class );
    
    private static final ErrorCode UNSUPPORTED_FIX = new ErrorCode( "FIF100", "Fix Generator currently only generates fix 4.4" );

    public static FixDecoder createRecoveryDecoder( FixVersion ver ) {
        
        switch( ver ) {
        case Fix4_4:
            return new RecoveryStandard44Decoder( ver._major, ver._minor );
        case DCFix4_4:
            return new RecoveryDropCopy44Decoder( ver._major, ver._minor );
        case MDFix4_4:
            return new RecoveryMD44Decoder( ver._major, ver._minor );
        case MDFix5_0:
            return new RecoveryMD50Decoder( ver._major, ver._minor );
        case Fix4_2:
            return new RecoveryStandard42Decoder( ver._major, ver._minor );
        case Fix5_0:
            return new RecoveryStandard50Decoder( ver._major, ver._minor );
        case Fix4_0:
        case Fix4_1:
            _log.error( UNSUPPORTED_FIX, ver.toString() );
            
            return new RecoveryStandard44Decoder( ver._major, ver._minor );
        default:
            break;
        }

        return null;
    }

    public static FixEncoder createFixEncoder( FixVersion ver, byte[] buf, int offset ) {
        
        switch( ver ) {
        case Fix5_0:
            return new Standard50Encoder( ver._major, ver._minor, buf, offset );
        case Fix4_4:
            return new Standard44Encoder( ver._major, ver._minor, buf, offset );
        case Fix4_2:
            return new Standard42Encoder( ver._major, ver._minor, buf, offset );
        case DCFix4_4:
            return new DropCopy44Encoder( ver._major, ver._minor, buf, offset );
        case MDFix4_4:
            return new MD44Encoder( ver._major, ver._minor, buf, offset );
        case MDFix5_0:
            return new MD50Encoder( ver._major, ver._minor, buf, offset );
        case Fix4_0:
        case Fix4_1:
            _log.error( UNSUPPORTED_FIX, ver.toString() );
            
            return new Standard44Encoder( ver._major, ver._minor, buf, offset );
        }

        return null;
    }

    public static FixDecoder createFixDecoder( FixVersion ver ) {
        
        switch( ver ) {
        case Fix5_0:
            return new Standard50Decoder( ver._major, ver._minor );
        case Fix4_4:
            return new Standard44Decoder( ver._major, ver._minor );
        case Fix4_2:
            return new Standard42Decoder( ver._major, ver._minor );
        case DCFix4_4:
            return new DropCopy44Decoder( ver._major, ver._minor );
        case MDFix4_4:
            return new MD44Decoder( ver._major, ver._minor );
        case MDFix5_0:
            return new MD50Decoder( ver._major, ver._minor );
        case Fix4_0:
        case Fix4_1:
            _log.error( UNSUPPORTED_FIX, ver.toString() );
            
            return new Standard44Decoder( ver._major, ver._minor );
        }

        return null;
    }

}
