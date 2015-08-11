/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.codec.emea.exchange.eti;

import com.rr.core.codec.BinaryEncoder;
import com.rr.core.lang.ZString;

public interface ETIEncoder extends BinaryEncoder {

    public void setUniqueClientCode( ZString uniqueClientCode );
    public void setSenderSubID( int newId );
    public void setLocationId( long locationId );
    public void setExchangeEmulationOn();
}
