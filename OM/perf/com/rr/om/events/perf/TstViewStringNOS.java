/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import java.util.Currency;

import javax.sound.midi.Instrument;

import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.model.SecurityType;
import com.rr.model.generated.internal.type.BookingType;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.model.generated.internal.type.PositionEffect;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.om.client.OMClientProfile;

public class TstViewStringNOS implements TstNOS {

    public       byte[]     _buf               = new byte[ SizeConstants.DEFAULT_VIEW_NOS_BUFFER ];
    public       int        _len;
                      
    public final ViewString _account           = new ViewString( _buf );
    public final ViewString _clOrdID           = new ViewString( _buf );
    public final ViewString _securityID        = new ViewString( _buf );
    public final ViewString _senderCompID      = new ViewString( _buf );
    public final ViewString _senderSubID       = new ViewString( _buf );
    public final ViewString _onBehalfOfID      = new ViewString( _buf );
    public final ViewString _symbol            = new ViewString( _buf );
    public final ViewString _targetCompID      = new ViewString( _buf );
    public final ViewString _targetSubID       = new ViewString( _buf );
    public final ViewString _text              = new ViewString( _buf );
    public final ViewString _exDestination     = new ViewString( _buf );
    public final ViewString _securityExch      = new ViewString( _buf );

    public void ensureCapacity( int size ) {
        if ( size > _buf.length ) {
            _buf = new byte[ size ];
            
            _account.setValue( _buf );
            _clOrdID.setValue( _buf );
            _securityID.setValue( _buf );
            _senderCompID.setValue( _buf );
            _senderSubID.setValue( _buf );
            _onBehalfOfID.setValue( _buf );
            _symbol.setValue( _buf );
            _targetCompID.setValue( _buf );
            _targetSubID.setValue( _buf );
            _text.setValue( _buf );
            _exDestination.setValue( _buf );
            _securityExch.setValue( _buf );
        }
    }
    
    public void setValue( byte[] src, int srcOffset, int len ) {
        ensureCapacity( len );
        
        System.arraycopy( src, srcOffset, _buf, 0, len );
        
        _len = len;
    }
    
    @Override
    public ZString getAccount() {
        return _account;
    }

    @Override
    public BookingType getBookingType() {
        return null;
    }

    @Override
    public ZString getClOrdID() {
        return _clOrdID;
    }
    
    @Override
    public OMClientProfile getClientProfile() {
        return null;
    }
    
    @Override
    public Currency getCurrency() {
        return null;
    }
    
    @Override
    public long getEffectiveTime() {
        return 0;
    }
    
    @Override
    public ZString getExDestination() {
        return _exDestination;
    }
    
    @Override
    public long getExpireTime() {
        return 0;
    }
    
    @Override
    public HandlInst getHandlingInstruction() {
        return null;
    }
    
    @Override
    public SecurityIDSource getIDSource() {
        return null;
    }
    
    @Override
    public Instrument getInstrument() {
        return null;
    }
    
    @Override
    public long getMaxFloor() {
        return 0;
    }
    
    @Override
    public int getMsgSeqNo() {
        return 0;
    }
    
    @Override
    public ZString getOnBehalfOfID() {
        return null;
    }
    
    @Override
    public OrdType getOrdType() {
        return null;
    }
    
    @Override
    public OrderCapacity getOrderCapacity() {
        return null;
    }
    
    @Override
    public long getOrderQty() {
        return 0;
    }
    
    @Override
    public Currency getOriginalCurrency() {
        return null;
    }
    
    @Override
    public PositionEffect getPositionEffect() {
        return null;
    }
    
    @Override
    public boolean getPossDup() {
        return false;
    }
    
    @Override
    public double getPrice() {
        return 0;
    }
    
    @Override
    public ZString getSecClOrdID() {
        return _clOrdID;
    }
    
    @Override
    public ZString getSecurityExch() {
        return _securityExch;
    }
    
    @Override
    public ZString getSecurityID() {
        return _securityID;
    }
    
    @Override
    public SecurityType getSecurityType() {
        return null;
    }
    
    @Override
    public ZString getSenderCompID() {
        return _senderCompID;
    }
    
    @Override
    public ZString getSenderSubID() {
        return _senderSubID;
    }
    
    @Override
    public Side getSide() {
        return null;
    }
    
    @Override
    public ZString getSymbol() {
        return _symbol;
    }
    
    @Override
    public ZString getTargetCompID() {
        return _targetCompID;
    }
    
    @Override
    public ZString getTargetSubID() {
        return _targetSubID;
    }
    
    @Override
    public ZString getText() {
        return _text;
    }
    
    @Override
    public TimeInForce getTimeInForce() {
        return null;
    }
    
    @Override
    public long getTmReceive() {
        return 0;
    }
    
    @Override
    public long getTmTransmit() {
        return 0;
    }
    
    @Override
    public long getTransactTime() {
        return 0;
    }
    
    @Override
    public void setTmTransmit( long value ) {
        // not yet implemented
    }
    
}
