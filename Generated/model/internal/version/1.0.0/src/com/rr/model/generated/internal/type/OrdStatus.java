/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.type;


/**
 Identifies current status of order
                  In an execution report the OrdStatus is used to convey the current state of the order. If an order simultaneously exists in more than one order state, the value
                  with highest precedence is the value that is reported in the OrdStatus field. The order statuses are as follows (in highest to lowest precedence):
               
                     11 - Pending Cancel
                     10 - Pending Replace
                      9 - Done for Day    (Order not, or partially, filled; no further executions forthcoming for the trading day)
                      8 - Calculated      (Order has been completed for the day (either filled or done for day). 
                      7 - Filled          (Order completely filled, no remaining quantity)
                      6 - Stopped         (Order has been stopped at the exchange. Used when guranteeing or protecting a price and quantity)
                      5 - Suspended       (Order has been placed in suspended state at the request of the client.)
                      4 - Canceled        (Canceled order with or without executions)
                      4 - Expired         (Order has been canceled in broker's system due to time in force instructions.)
                      3 - Partially Filled (Outstanding order with executions and remaining quantity)
                      2 - New              (Outstanding order with no executions)
                      2 - Rejected         (Order has been rejected by sell-side (broker, exchange, ECN). 
                                            NOTE: An order can be rejected subsequent to order acknowledgment,
                                            i.e. an order can pass from New to Rejected status.
                      2 - Pending New       (Order has been received by sell-side's (broker, exchange, ECN) system 
                                            but not yet accepted for execution. An execution message
                                            with this status will only be sent in response to a Status Request message.)
                      1 - Accepted for bidding  (Order has been received and is being evaluated for pricing. 
                                                It is anticipated that this status will only be used with the
                                                "Disclosed" BidType List Order Trading model.)
*/

import com.rr.model.internal.type.*;
import com.rr.core.model.*;
import com.rr.core.codec.RuntimeDecodingException;
import com.rr.model.generated.internal.type.TypeIds;

@SuppressWarnings( "unused" )

public enum OrdStatus implements SingleByteLookup {

    New( TypeIds.ORDSTATUS_NEW, "0", false, false ),
    PartiallyFilled( TypeIds.ORDSTATUS_PARTIALLYFILLED, "1", false, false ),
    Filled( TypeIds.ORDSTATUS_FILLED, "2", false, true ),
    DoneForDay( TypeIds.ORDSTATUS_DONEFORDAY, "3", false, true ),
    Canceled( TypeIds.ORDSTATUS_CANCELED, "4", false, true ),
    Replaced( TypeIds.ORDSTATUS_REPLACED, "5", false, false ),
    PendingCancel( TypeIds.ORDSTATUS_PENDINGCANCEL, "6", true, false ),
    Stopped( TypeIds.ORDSTATUS_STOPPED, "7", false, true ),
    Rejected( TypeIds.ORDSTATUS_REJECTED, "8", false, true ),
    Suspended( TypeIds.ORDSTATUS_SUSPENDED, "9", false, false ),
    PendingNew( TypeIds.ORDSTATUS_PENDINGNEW, "A", true, false ),
    Calculated( TypeIds.ORDSTATUS_CALCULATED, "B", false, false ),
    Expired( TypeIds.ORDSTATUS_EXPIRED, "C", false, true ),
    Restated( TypeIds.ORDSTATUS_RESTATED, "D", false, false ),
    PendingReplace( TypeIds.ORDSTATUS_PENDINGREPLACE, "E", true, false ),
    UnseenOrder( TypeIds.ORDSTATUS_UNSEENORDER, "F", false, false ),
    CMETradeCancel( TypeIds.ORDSTATUS_CMETRADECANCEL, "H", false, false ),
    CMEUndefined( TypeIds.ORDSTATUS_CMEUNDEFINED, "U", false, true ),
    Unknown( TypeIds.ORDSTATUS_UNKNOWN, "?", false, false );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final byte _val;
    private final int _id;
    private boolean _isPending = false;
    private boolean _isTerminal = false;

    OrdStatus( int id, String val,  boolean isPending,  boolean isTerminal ) {
        _val = val.getBytes()[0];
        _id = id;
        _isPending = isPending;
        _isTerminal = isTerminal;
    }
    private static final int _indexOffset = 48;
    private static final OrdStatus[] _entries = new OrdStatus[38];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( OrdStatus en : OrdStatus.values() ) {
             if ( en == Unknown ) continue;
            _entries[ en.getVal() - _indexOffset ] = en;
        }
    }

    public static OrdStatus getVal( byte val ) {
        final int arrIdx = val - _indexOffset;
        if ( arrIdx < 0 || arrIdx >= _entries.length ) {
            throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for OrdStatus" );
        }
        OrdStatus eval;
        eval = _entries[ arrIdx ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for OrdStatus" );
        return eval;
    }

    @Override
    public final byte getVal() {
        return _val;
    }

    public final int getID() {
        return _id;
    }

    public boolean getIsPending() { return _isPending; }
    public void setIsPending( boolean val ) { _isPending = val; }
    public boolean getIsTerminal() { return _isTerminal; }
    public void setIsTerminal( boolean val ) { _isTerminal = val; }
}
