/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.om.model.instrument.ExchangeSession;
import com.rr.om.model.instrument.TickType;
import com.rr.om.model.instrument.TradingRange;


public interface Instrument {
 
    public static final int DUMMY_INSTRUMENT_ID = -1;

    public Exchange getExchange();
    
    public Currency getCurrency();

    public ZString getRIC();

    /**
     * @return the integer symbol as assigned by exchange (or 0 if none)
     */
    public long getLongSymbol();

    /**
     * @TODO refactor intSegment out of instrument ... use decorator pattern
     * 
     * aka channel
     * 
     * @return the integer segment the instrument is assigned to (or 0 if none)
     */
    public int getIntSegment();
    
    /**
     * different instrument segments can be traded at different times
     * 
     * @return exchange session appropriate for the instrument
     */
    public ExchangeSession getExchangeSession();

    /**
     * @return true if this instrument is a test instrument at the exchange
     */
    public boolean isTestInstrument();

    /**
     * @return the tickscale can be fixed or banded
     */
    public TickType getTickscale();

    /**
     * @return true if the instrument is restricted
     */
    public boolean isRestricted();

    /**
     * @return true if the instrument is enabled for trading
     */
    public boolean isEnabled();

    /**
     * @return valid trading range for this stock assuming price tolerance is running
     */
    public TradingRange getValidTradingRange();

    /**
     * uniquely identify the instrument in readable format in supplied buffer
     * 
     * @param out
     */
    public void id( ReusableString out );

    /**
     * @return number of book levels for instrument, or 0 if unknown
     */
    public int getBookLevels();

    public ZString getExchangeSymbol();
    
    public SecurityType getSecurityType();
    
    public ZString getSecurityDesc();
    
    public ZString getSecurityGroup();

    public int getSecurityGroupId();

    /**
     * @TODO PUSH FOLLOWING INTO InstrumentDeriv
     */
    
    /**
     * @return number of legs, or Constants.UNSET_INT if number of legs unknown ... ie could be multileg instrument but we didnt get the leg info
     */
    public int getNumLegs();
    
    /**
     * helper method to abstract logic of obtaining the security description from a leg
     * it can reside on the LegInstrument or on the LegInstruments underlying instrument letter 
     * @param secDef - destination, which will have the security description copied too
     * @return the supplied secDef buffer
     */
    public ZString getLegSecurityDesc( int legIdx, ReusableString secDef );
    
    /**
     * @NOTE use getNumLegs first and dont invoke if numLegs == 0 as it will cause IndexOutOfBounds or NullPointerException 
     * @param idx leg index starting from 0
     * @return instrument representing the leg 
     */
    public LegInstrument getLeg( int idx );

    public int getContractMultiplier();

    public int getMinQty();
}
