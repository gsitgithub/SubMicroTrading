/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model;

import java.util.TimeZone;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;
import com.rr.om.model.instrument.ExchangeSession;
import com.rr.om.model.instrument.ExchangeState;
import com.rr.om.model.instrument.ExchangeValidator;

public interface Exchange {

    /**
     * @return the unique sequential identifier for this exchange
     */
    public int getId();
    
    public ZString getMIC();
    
    public ZString getRecCode();

    public boolean isExchangeAnMTF();
    
    public boolean isPrimaryRICRequired();

    public TimeZone getTimeZone();

    public ExchangeSession getSession();

    public ExchangeState getExchangeState( long timeUTC );

    public ExchangeState getExchangeState();

    public boolean isSendCancelToExchangeAtEOD();
    
    public boolean isGeneratedExecIDRequired();

    public long getExpireTimeToSendEndOfDayEvents();
    
    public Enricher getEnricher();

    public ExchangeValidator getExchangeEventValidator();
    
    public boolean isHalfDay();
    

    /**
     * some exchanges have different trading segments for different markets
     * 
     * @param marketSegment
     * @return
     */
    public ExchangeSession getExchangeSession( ZString marketSegment );
    
    /**
     * generate an appropriate clOrdId for the marketside order 
     * 
     * @param dest - the reusable string to hold the mkt clOrdId
     * @param clientClOrdId - client clOrdId will be used as market clOrdId if allowed by exchange
     */
    public void generateMarketClOrdId( ReusableString dest, ZString clientClOrdId );
    
    /**
     * some exchanges like ENX require the instrument and possbly side to make unique
     * 
     * @param execIdForUpdate
     * @param execId
     * @param inst
     */
    public void makeExecIdUnique( ReusableString execIdForUpdate, ZString execId, Instrument inst );

    /**
     * @return true if the exchange requires trade busts / corrects to be supported
     *         this requires execId/qty/price for all fills to be kept
     */
    public boolean isTradeCorrectionSupported();

    /**
     * @param time
     *  
     * @return true if the exchange is open for trading at the specified time
     */
    public boolean isOpenForTrading( long time );
    
    /**
     * write the exchange info into the supplied buf in a readable log format
     * 
     * @param buf
     * @return
     */
    public ReusableString toString( ReusableString buf );
}
