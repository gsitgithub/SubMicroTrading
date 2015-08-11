/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.exchange;

import com.rr.core.codec.Encoder;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.utils.Utils;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.core.FullEventIds;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.events.factory.ClientCancelRejectFactory;
import com.rr.model.generated.internal.events.factory.ClientRejectedFactory;
import com.rr.model.generated.internal.events.impl.ClientCancelRejectImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientRejectedImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.internal.type.ExecType;

/**
 * abstract common class for all manually written OM exchange encoders
 */
public abstract class BaseEncoder implements Encoder {

    private static final byte[]  STATS       = "     [".getBytes();
    private static final byte    STAT_DELIM  = ',';
    private static final byte    STAT_END    = ']';

    private static final ZString ENCODE_REJ  = new ViewString( "ERJ" );
    private static final ZString NONE        = new ViewString( "NON" );

    protected final byte[]               _buf;
    protected final int                  _offset;
    
    protected       TimeZoneCalculator   _tzCalculator = new TimeZoneCalculator();
    private         int                  _rejectIdx       = 1;

    private final ClientCancelRejectFactory _canRejFactory   = SuperpoolManager.instance().getFactory( ClientCancelRejectFactory.class, ClientCancelRejectImpl.class );
    private final ClientRejectedFactory     _rejectedFactory = SuperpoolManager.instance().getFactory( ClientRejectedFactory.class,     ClientRejectedImpl.class ); 
    
    public BaseEncoder( byte[] buf, int offset ) {
        if ( buf.length < SizeType.MIN_ENCODE_BUFFER.getSize() ) {
            throw new RuntimeException( "Encode buffer too small only " + buf.length + ", min=" + SizeType.MIN_ENCODE_BUFFER.getSize() );
        }
        
        _buf = buf;
        _offset = offset;
    }

    @Override
    public final int getOffset() {
        return _offset;
    }

    @Override
    public final Message unableToSend( Message msg, ZString errMsg ) {
        switch( msg.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERSINGLE:
            return rejectNewOrderSingle( (NewOrderSingle) msg, errMsg );
        case EventIds.ID_NEWORDERACK:
            break;
        case EventIds.ID_TRADENEW:
            break;
        case EventIds.ID_CANCELREPLACEREQUEST:
            return rejectCancelReplaceRequest( (CancelReplaceRequest) msg, errMsg );
        case EventIds.ID_CANCELREQUEST:
            return rejectCancelRequest( (CancelRequest) msg, errMsg );
        }
        
        return null;
    }

    @Override
    public final byte[] getBytes() {
        return _buf;
    }

    @Override
    public void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        _tzCalculator = calc;
    }
    
    @Override
    public final void addStats( ReusableString outBuf, Message msg, long msgSent ) {
        if ( msg.getReusableType().getId() == FullEventIds.ID_MARKET_NEWORDERSINGLE ) {
            MarketNewOrderSingleImpl nos = (MarketNewOrderSingleImpl) msg;
            nos.setOrderSent( Utils.nanoTime() );        // HOOK
        } else if ( msg.getReusableType().getId() == FullEventIds.ID_CLIENT_NEWORDERACK ) {
            ClientNewOrderAckImpl ack = (ClientNewOrderAckImpl) msg;

            final long orderIn  = ack.getOrderReceived();
            final long orderOut = ack.getOrderSent();
            final long ackIn    = ack.getAckReceived();
            final long ackOut   = msgSent;

            final long microNOSToMKt    = (orderOut - orderIn)  >> 10;
            final long microInMkt       = (ackIn    - orderOut) >> 10;
            final long microAckToClient = (ackOut   - ackIn)    >> 10;
            
            outBuf.append( STATS      ).append( microNOSToMKt )
                  .append( STAT_DELIM ).append( microInMkt )
                  .append( STAT_DELIM ).append( microAckToClient ).append( STAT_END );
        }
    }

    private Message rejectNewOrderSingle( NewOrderSingle nos, ZString errMsg ) {
        final ClientRejectedImpl reject = _rejectedFactory.get();

        reject.setSrcEvent( nos );
        reject.getExecIdForUpdate().copy( ENCODE_REJ ).append( nos.getClOrdId() ).append( ++_rejectIdx );
        reject.getOrderIdForUpdate().setValue( NONE );
        reject.setOrdRejReason( OrdRejReason.Other );
        reject.getTextForUpdate().setValue( errMsg );
        reject.setOrdStatus( OrdStatus.Rejected );
        reject.setExecType( ExecType.Rejected );

        reject.setCumQty( 0 );
        reject.setAvgPx( 0.0 );

        reject.setMessageHandler( nos.getMessageHandler() );
        return reject;
    }

    private Message rejectCancelReplaceRequest( CancelReplaceRequest msg, ZString errMsg ) {
        final ClientCancelRejectImpl reject = _canRejFactory.get();
        
        reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );
        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );
        reject.getOrderIdForUpdate().    setValue( NONE );
        reject.getTextForUpdate().       setValue( errMsg );

        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelReplace );
        reject.setCxlRejReason(     CxlRejReason.Other );
        reject.setOrdStatus(        OrdStatus.Unknown );

        return reject;
    }

    private Message rejectCancelRequest( CancelRequest msg, ZString errMsg ) {
        final ClientCancelRejectImpl reject = _canRejFactory.get();
        
        reject.getClOrdIdForUpdate().    setValue( msg.getClOrdId() );
        reject.getOrigClOrdIdForUpdate().setValue( msg.getOrigClOrdId() );
        reject.getOrderIdForUpdate().    setValue( NONE );
        reject.getTextForUpdate().       setValue( errMsg );

        reject.setCxlRejResponseTo( CxlRejResponseTo.CancelRequest );
        reject.setCxlRejReason(     CxlRejReason.Other );
        reject.setOrdStatus(        OrdStatus.Unknown );

        return reject;
    }
}
