/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.t1;

import com.rr.core.algo.strats.BaseL2BookStrategy;
import com.rr.core.algo.strats.StratInstrumentStateWrapper;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.Book;
import com.rr.core.model.book.DoubleSidedBookEntry;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.TimeInForce;


public final class StrategyT1 extends BaseL2BookStrategy {

    private int _tickToTradeRatio = 10;
            
    public StrategyT1( String id ) {
        super( id );
    }

    @Override
    protected void validateProps() {
        super.validateProps();
        
        if ( _tickToTradeRatio == 0 ) {
            throw new SMTRuntimeException( "StrategyT1 missing tickToTradeRatio property id=" + getComponentId() );
        }
    }
    
    @Override
    public void postEventProcessing() {
        dispatchQueued();
    }
    
    @Override
    public void bookChanged( StratInstrumentStateWrapper<? extends Book> stratInstState ) {
        
        Book snappedBook = stratInstState.snap();
        
        if ( snappedBook.isValidBBO() ) {
            // we have top of book ... should check not crossed and not zero
            DoubleSidedBookEntry entry = stratInstState.getBBOSnapEntry();
            
            if ( snappedBook.getLevel( 0, entry ) ) {
                
                final int bookTickCount = snappedBook.getTickCount();
    
                Side side = ((snappedBook.getLastTickId()%2) == 1) ? Side.Buy : Side.Sell;
                
                int qty;
                double price;
                
                if ( side.getIsBuySide() ) {
                    qty = entry.getAskQty();
                    price = entry.getAskPx();
                } else {
                    qty = entry.getAskQty();
                    price = entry.getAskPx();
                }
                
                if ( ( bookTickCount % _tickToTradeRatio) == 0 ) { // mod isnt power of 2 so have to use MOD
                    NewOrderSingle nos = stratInstState.getExchangeHandler().makeNOS( getAccount(),
                                                                                      OrdType.Limit,
                                                                                      TimeInForce.Day, 
                                                                                      qty, 
                                                                                      price, 
                                                                                      side, 
                                                                                      snappedBook.getLastExchangeTickTime(), 
                                                                                      snappedBook.getLastTickId() );

                    stratInstState.setSliceStart( System.currentTimeMillis() );
                    stratInstState.updateStratInstState( nos );
                    
                    enqueueForDownDispatch( nos );
                }
            }
        }
    }

    @Override
    public void dumpInstDetails( ReusableString s ) {
        // nothing
    }
    
    @Override
    public void forceSliceReset() {
        // nothing
    }

    @Override
    public void legSliceCompleted() {
        // nothing
    }

    @Override
    public void setMarketOrderTerminalEvent( StratInstrumentStateWrapper<? extends Book> stratInstState ) {
        // nothing
    }
}
