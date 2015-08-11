/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.fix.codec;


import com.rr.core.codec.CodecName;

import com.rr.core.model.FixVersion;

public enum CodecId implements CodecName {
    MD44( FixVersion.MDFix4_4 ),
    MD50( FixVersion.MDFix5_0 ),
    Standard44( FixVersion.Fix4_4 ),
    Standard50( FixVersion.Fix5_0 ),
    Standard42( FixVersion.Fix4_2 ),
    DropCopy44( FixVersion.DCFix4_4 ),
    ClientX_44( FixVersion.Fix4_4 ),
    CHIX( FixVersion.Fix4_2 ),
    MDBSE( FixVersion.MDFix5_0 ),
    CME( FixVersion.Fix4_2 ),
    UTPEuronextCash( null ),
    MilleniumLSE( null ),
    ItchLSE( null ),
    BaseETI( null ),
    ETIBSE( null ),
    ETIEurexHFT( null ),
    ETIEurexLFT( null ),
    CMESimpleBinary( null );

    private FixVersion _ver;

    CodecId( FixVersion ver )  {
        _ver = ver;
    }

    public FixVersion getFixVersion() { return _ver; }
}
