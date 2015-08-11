/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import com.rr.core.lang.Constants;
import com.rr.core.model.Currency;
import com.rr.core.model.Message;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.events.factory.ClientNewOrderSingleFactory;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.interfaces.MarketNewOrderAckWrite;
import com.rr.model.generated.internal.type.HandlInst;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.OrderCapacity;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.model.internal.type.ExecType;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;

// this class is the forerunner to the generated decoder

public class TstFixDecoder extends AbstractFixDecoder {

    // execRpt fields
    
    protected int _clOrdIdStart          = 0;
    protected int _clOrdIdLen            = 0;
    protected int _orderIdStart          = 0;
    protected int _orderIdLen            = 0;
    protected int _execIdStart           = 0;
    protected int _execIdLen             = 0;

    protected OrdStatus _ordStatus  = OrdStatus.Unknown;
    protected ExecType  _execStatus = ExecType.Unknown;
    
    // only for use in exec reports
    // NOTE DONT DECODE WHAT WE DONT NEED
    protected int              _lastQty   = Constants.UNSET_INT;
    protected int              _leavesQty = Constants.UNSET_INT;
    protected int              _cumQty    = Constants.UNSET_INT;
    protected int              _qty       = Constants.UNSET_INT;
    protected double           _avgPrice  = Double.NEGATIVE_INFINITY;
    protected double           _lastPrice = Double.NEGATIVE_INFINITY;
    protected double           _price     = Double.NEGATIVE_INFINITY;


    private final SuperPool<ClientNewOrderSingleImpl> _clientNOSPool = SuperpoolManager.instance().getSuperPool( ClientNewOrderSingleImpl.class );
    
//    private final PoolFactory<ClientNewOrderSingleImpl>_newOrderSingleFactory = _clientNOSPool.getPoolFactory();
    
    private final ClientNewOrderSingleFactory _newOrderSingleFactory = new ClientNewOrderSingleFactory( _clientNOSPool );

    public TstFixDecoder( byte major, byte minor ) {
        super( major, minor );
        setInstrumentLocator( new DummyInstrumentLocator() );
    }

    public void presize( int chains, int chainSize, int extraAlloc ) {
        _clientNOSPool.init( chains, chainSize, extraAlloc );
    }
    
    @Override
    protected final Message doMessageDecode() {
        // get message type field
        if ( _fixMsg[_idx] != '3' || _fixMsg[_idx+1] != '5' || _fixMsg[_idx+2] != '=' )
            throwDecodeException( "Fix Messsage missing message type" );
        _idx += 3;

        byte msgType = _fixMsg[ _idx ];
        switch( msgType ) {
        case 8:
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeExecReport();
        case 'D':
            if ( _fixMsg[_idx+1 ] != FixField.FIELD_DELIMITER ) { // 2 byte message type
                throwDecodeException( "Unsupported fix message type " + _fixMsg[_idx] + _fixMsg[_idx+1] );
            }
            _idx += 2;
            return decodeNewOrderSingle();
        case 'G':
        case 'F':
        case '9':
        case '0':
        case 'A':
        case '5':
        case '3':
        case '2':
        case '4':
        case '1':
        case 'V':
        case 'W':
        case '6':
        case '7':
        case ':':
        case ';':
        case '<':
        case '=':
        case '>':
        case '?':
        case '@':
        case 'B':
        case 'C':
        case 'E':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
            break;
        }
        _idx += 2;
        throwDecodeException( "Unsupported fix message type " + msgType );
        return null;
    }

    
    public Message decodeNewOrderSingle() {
        // TODO get from pool
        ClientNewOrderSingleImpl msg = _newOrderSingleFactory.get();
        int tag = getTag();
        
        int start;
        int valLen;
        
        while( tag != 0 ) {
            switch( tag ) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 5:
            case 7:
            case 8:
            case 9:
            case 10:
            case 12:
            case 13:
            case 14:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
                // forced table switch from  0 to 60
                start = _idx;
                valLen = getValLength();
                break;
            case 1:
                start = _idx;
                valLen = getValLength();
                msg.setAccount( start, valLen );
                break;
            case 6:
                _avgPrice = getDoubleVal();
                break;
            case 11:
                start = _idx;
                valLen = getValLength();
                msg.setClOrdId( start, valLen );
                break;
            case 15:
                start = _idx;
                valLen = getValLength();
                msg.setCurrency( Currency.getVal(_fixMsg, start, valLen) );
                break;
            case 21:
                msg.setHandlInst( HandlInst.getVal( _fixMsg[_idx++] ) );
                break;
            case 22:
                msg.setSecurityIDSource( SecurityIDSource.getVal( _fixMsg[_idx++] ) );
                break;
            case 38:
                msg.setOrderQty( getIntVal() );
                break;
            case 40:
                msg.setOrdType( OrdType.getVal( _fixMsg[_idx++] ) );
                break;
            case 44:
                msg.setPrice( getDoubleVal() );
                break;
            case 48:
                start = _idx;
                valLen = getValLength();
                msg.setSecurityId( start, valLen );
                break;
            case 49:
                decodeSenderCompID();
                break;
            case 52:
                msg.setSendingTime( getMSFromStartDayUTC() );
                break;
            case 54:
                msg.setSide( Side.getVal( _fixMsg[_idx++] ) );
                break;
            case 55:
                start = _idx;
                valLen = getValLength();
                msg.setSymbol( start, valLen );
                break;
            case 56:
                decodeTargetCompID();
                break;
            case 58:
                start = _idx;
                valLen = getValLength();
                msg.setText( start, valLen );
                break;
            case 59:
                msg.setTimeInForce( TimeInForce.getVal( _fixMsg[_idx++] ) );
                break;
            case 60:
                msg.setTransactTime( getMSFromStartDayUTC() );
                break;
            default: //  SKIP
                switch( tag ) {
                case 100: // exDest
                    start = _idx;
                    valLen = getValLength();
                    msg.setExDest( start, valLen );
                    break;
                case 207: // securityExchange
                    start = _idx;
                    valLen = getValLength();
                    msg.setSecurityExchange( start, valLen );
                    break;
                case 528: 
                    msg.setOrderCapacity( OrderCapacity.getVal( _fixMsg[_idx++] ) );
                    break;
                default:
                    start = _idx;
                    valLen = getValLength();
                    break;
                }
            }
    
            _idx++; // past delimiter
            
            tag = getTag();
        }

        if ( _idx > SizeType.VIEW_NOS_BUFFER.getSize() ) {
        
            throw new RuntimeDecodingException( "NewOrderSingle Message too big " + _idx + ", max=" + SizeType.VIEW_NOS_BUFFER.getSize() );
        }
        
        msg.setViewBuf( _fixMsg, _offset, _idx );
        
        // TODO instrument enrichment & ccy major/minor changes
        
        return msg;
    }

    public Message decodeExecReport() {
        
        // exclude fields in ClientSideExecReport which are derived from the clients last order message
        
        _clOrdIdStart = _clOrdIdLen = 0;
        _orderIdStart = _orderIdLen = 0;
        _execIdStart  = _execIdLen  = 0;

        _ordStatus  = OrdStatus.Unknown;
        _execStatus = ExecType.Unknown;
        
        _leavesQty = _lastQty = Constants.UNSET_INT;
        
        _lastPrice = _avgPrice = Double.NEGATIVE_INFINITY;
        
        int tag = getTag();
        
        while( tag != 0 ) {
            switch( tag ) {
            case 11:
                _clOrdIdStart = _idx;
                _clOrdIdLen   = getValLength();
                break;
            case 39:
                _ordStatus = OrdStatus.getVal( _fixMsg[_idx++] );
                break;
            case 150:
                _execStatus = ExecType.getVal( _fixMsg[_idx++] );
                break;
            case 37:
                _orderIdStart = _idx;
                _orderIdLen   = getValLength();
                break;
            case 17:
                _execIdStart = _idx;
                _execIdLen   = getValLength();
                break;
            case 151:
                _leavesQty = getIntVal();
                break;
            case 14:
                _cumQty = getIntVal();
                break;
            case 31:
                _lastPrice = getDoubleVal();
                break;
            case 32:
                _lastQty = getIntVal();
                break;
            case 6:
                _avgPrice = getDoubleVal();
                break;
            }
            
            _idx++; // past delimiter
            
            tag = getTag();
        }
        
        if ( _ordStatus == OrdStatus.Unknown || _execStatus == ExecType.Unknown ){
            throwDecodeException( "Execution report missing order or exec status " );
        }
        
        switch( _execStatus ){
        case New:
            return populateNewAck();
        case Canceled:
            throwDecodeException( "ExecRpt type " + _execStatus + " not yet implemented" );
            break;
        case Replaced:
            throwDecodeException( "ExecRpt type " + _execStatus + " not yet implemented" );
            break;
        case Trade:
            throwDecodeException( "ExecRpt type " + _execStatus + " not yet implemented" );
            break;
        case Rejected:
            throwDecodeException( "ExecRpt type " + _execStatus + " not yet implemented" );
            break;
        case Stopped:
        case DoneForDay:
        case Expired:
            throwDecodeException( "ExecRpt type " + _execStatus + " not yet implemented" );
            break;
        case TradeCancel:
            throwDecodeException( "ExecRpt type " + _execStatus + " not yet implemented" );
            break;
        case TradeCorrect:
            throwDecodeException( "ExecRpt type " + _execStatus + " not yet implemented" );
            break;
        case OrderStatus:
        case PendingCancel:
        case PendingNew:
        case PendingReplace:
        case Restated:
        case Suspended:
        case Calculated:
        case Unknown:
        case Fill:
        case PartialFill:
            break;
        }
                    
        return null;
    }

    private Message populateNewAck() {
        MarketNewOrderAckWrite msg = new MarketNewOrderAckImpl();

        if ( _orderIdLen > 0 ) msg.setOrderId( _fixMsg, _orderIdStart, _orderIdLen );
        if ( _clOrdIdLen > 0 ) msg.setClOrdId( _fixMsg, _clOrdIdStart, _clOrdIdLen );
        
        msg.setExecType( _execStatus );
        msg.setOrdStatus( _ordStatus );
        
        msg.setLeavesQty( _leavesQty );
        msg.setCumQty( _cumQty );
        msg.setAvgPx( _avgPrice );

        return msg;
    }

    public void logStats() {
        _clientNOSPool.logStats();
    }
}
