/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.fix.codec;

import com.rr.core.codec.CodecFactory;
import com.rr.model.generated.codec.*;

public class CodecFactoryPopulator {

    public void register( CodecFactory factory ) {
        factory.register( CodecId.MD44, 
                          MD44Encoder.class, 
                          MD44Decoder.class, 
                          RecoveryMD44Decoder.class ); 
        factory.register( CodecId.MD50, 
                          MD50Encoder.class, 
                          MD50Decoder.class, 
                          RecoveryMD50Decoder.class ); 
        factory.register( CodecId.Standard44, 
                          Standard44Encoder.class, 
                          Standard44Decoder.class, 
                          RecoveryStandard44Decoder.class ); 
        factory.register( CodecId.Standard50, 
                          Standard50Encoder.class, 
                          Standard50Decoder.class, 
                          RecoveryStandard50Decoder.class ); 
        factory.register( CodecId.Standard42, 
                          Standard42Encoder.class, 
                          Standard42Decoder.class, 
                          RecoveryStandard42Decoder.class ); 
        factory.register( CodecId.DropCopy44, 
                          DropCopy44Encoder.class, 
                          DropCopy44Decoder.class, 
                          RecoveryDropCopy44Decoder.class ); 
        factory.register( CodecId.ClientX_44, 
                          ClientX_44Encoder.class, 
                          ClientX_44Decoder.class, 
                          RecoveryClientX_44Decoder.class ); 
        factory.register( CodecId.CHIX, 
                          CHIXEncoder.class, 
                          CHIXDecoder.class, 
                          RecoveryCHIXDecoder.class ); 
        factory.register( CodecId.MDBSE, 
                          MDBSEEncoder.class, 
                          MDBSEDecoder.class, 
                          RecoveryMDBSEDecoder.class ); 
        factory.register( CodecId.CME, 
                          CMEEncoder.class, 
                          CMEDecoder.class, 
                          RecoveryCMEDecoder.class ); 
        factory.register( CodecId.UTPEuronextCash, 
                          UTPEuronextCashEncoder.class, 
                          UTPEuronextCashDecoder.class, 
                          UTPEuronextCashDecoder.class ); 
        factory.register( CodecId.MilleniumLSE, 
                          MilleniumLSEEncoder.class, 
                          MilleniumLSEDecoder.class, 
                          MilleniumLSEDecoder.class ); 
        factory.register( CodecId.ItchLSE, 
                          ItchLSEEncoder.class, 
                          ItchLSEDecoder.class, 
                          ItchLSEDecoder.class ); 
        factory.register( CodecId.ETIBSE, 
                          ETIBSEEncoder.class, 
                          ETIBSEDecoder.class, 
                          ETIBSEDecoder.class ); 
        factory.register( CodecId.ETIEurexHFT, 
                          ETIEurexHFTEncoder.class, 
                          ETIEurexHFTDecoder.class, 
                          ETIEurexHFTDecoder.class ); 
        factory.register( CodecId.ETIEurexLFT, 
                          ETIEurexLFTEncoder.class, 
                          ETIEurexLFTDecoder.class, 
                          ETIEurexLFTDecoder.class ); 
        factory.register( CodecId.CMESimpleBinary, 
                          CMESimpleBinaryEncoder.class, 
                          CMESimpleBinaryDecoder.class, 
                          CMESimpleBinaryDecoder.class ); 
    }
}
