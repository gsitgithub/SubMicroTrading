/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.internal.type;


// Describes the specific Execution Report (8) (i.e. Pending Cancel) while OrdStatus (39) will always identify the current order status (i.e. Partially Filled)

// needs to be hand crfted as the generator needs it to generate codec code

import static com.rr.model.internal.type.ManualTypeIds.*;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.SingleByteLookup;

public enum ExecType implements SingleByteLookup {

    New( EXECTYPE_NEW, "0" ),
    PartialFill( EXECTYPE_PARTIALFILL, "1" ),
    Fill( EXECTYPE_FILL, "2" ),
    DoneForDay( EXECTYPE_DONEFORDAY, "3" ),
    Canceled( EXECTYPE_CANCELED, "4" ),
    Replaced( EXECTYPE_REPLACED, "5" ),
    PendingCancel( EXECTYPE_PENDINGCANCEL, "6" ),
    Stopped( EXECTYPE_STOPPED, "7" ),
    Rejected( EXECTYPE_REJECTED, "8" ),
    Suspended( EXECTYPE_SUSPENDED, "9" ),
    PendingNew( EXECTYPE_PENDINGNEW, "A" ),
    Calculated( EXECTYPE_CALCULATED, "B" ),
    Expired( EXECTYPE_EXPIRED, "C" ),
    Restated( EXECTYPE_RESTATED, "D" ),
    PendingReplace( EXECTYPE_PENDINGREPLACE, "E" ),
    Trade( EXECTYPE_TRADE, "F" ),
    TradeCorrect( EXECTYPE_TRADECORRECT, "G" ),
    TradeCancel( EXECTYPE_TRADECANCEL, "H" ),
    OrderStatus( EXECTYPE_ORDERSTATUS, "I" ),
    Unknown( EXECTYPE_UNKNOWN, "?" );

    public static int getMaxOccurs() { return 1; }

    public static int getMaxValueLen() { return 1; }

    private final int  _id;
    private final byte _val;

    ExecType( final int id, final String val ) {
        _id  = id;
        _val = val.getBytes()[0];
    }

    private static ExecType[] _entries = new ExecType[256];

    static {
        for ( int i=0 ; i < _entries.length ; i++ ) {
             _entries[i] = Unknown; }

        for ( ExecType en : ExecType.values() ) {
             _entries[ en.getVal() ] = en;
        }
    }

    public static ExecType getVal( byte val ) {
        ExecType eval;
        eval = _entries[ val ];
        if ( eval == Unknown ) throw new RuntimeDecodingException( "Unsupported value of " + (char)val + " for ExecType" );
        return eval;
    }

    @Override
    public byte getVal() {
        return _val;
    }

    public final int getID(){
        return _id;
    }

    public void id( ReusableString out ) {
        out.append( toString() );
    }
}
