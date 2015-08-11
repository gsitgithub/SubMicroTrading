/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import java.util.Set;

import com.rr.core.component.SMTComponent;
import com.rr.core.lang.ZString;


public interface InstrumentLocator extends SMTComponent {

    public Instrument getInstrument( ZString          securityId, 
                                     SecurityIDSource securityIDSource, 
                                     ZString          exDest, 
                                     ZString          securityExchange, 
                                     Currency         currency );

    public Instrument getInstrumentBySecurityDesc( ZString securityDescription ); // fix tag 107

    public Instrument getInstrumentByRIC( ZString ric );

    public Instrument getInstrumentBySymbol( ZString symbol, ZString exDest, ZString securityExchange, Currency clientCcy );

    public Instrument getInstrumentByID( ZString rec, long instrumentId );
    
    public Instrument getDummyInstrument(); // get the dummy instrument singleton

    public void getInstruments( Set<Instrument> instruments, Exchange ex );
}
