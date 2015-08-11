/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.MsgFlag;
import com.rr.core.recovery.RecoveryController;
import com.rr.core.recovery.RecoverySessionContext;
import com.rr.core.recycler.MessageRecycler;
import com.rr.core.session.Session;
import com.rr.core.session.SessionDirection;
import com.rr.core.session.TradingSessionConfig;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.ClientTradeCorrectImpl;
import com.rr.model.generated.internal.events.interfaces.BaseOrderRequest;
import com.rr.model.generated.internal.events.interfaces.CancelReject;
import com.rr.model.generated.internal.events.interfaces.CancelReplaceRequest;
import com.rr.model.generated.internal.events.interfaces.CancelRequest;
import com.rr.model.generated.internal.events.interfaces.Cancelled;
import com.rr.model.generated.internal.events.interfaces.CommonExecRpt;
import com.rr.model.generated.internal.events.interfaces.NewOrderAck;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.model.generated.internal.events.interfaces.OrderRequest;
import com.rr.model.generated.internal.events.interfaces.Rejected;
import com.rr.model.generated.internal.events.interfaces.Replaced;
import com.rr.model.generated.internal.events.interfaces.TradeBase;
import com.rr.model.generated.internal.events.interfaces.TradeCancel;
import com.rr.model.generated.internal.events.interfaces.TradeCorrect;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.events.interfaces.VagueOrderReject;
import com.rr.model.generated.internal.type.CxlRejReason;
import com.rr.model.generated.internal.type.CxlRejResponseTo;
import com.rr.model.generated.internal.type.MultiLegReportingType;
import com.rr.model.generated.internal.type.OrdRejReason;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.model.internal.type.ExecType;
import com.rr.om.model.event.EventBuilder;
import com.rr.om.model.event.EventUtils;
import com.rr.om.order.collections.OrderMap;
import com.rr.om.processor.EventProcessorImpl;

/**
 * A high frequency recovery controller for one to one parent/child orders.
 * Parent order is also known as upstream or client order
 * Child order is also known as downstream or market order
 * 
 * IMPORTANT KEEP MEM STRUCTURES AS LEAN AS POSSIBLE TO AVOID OOM
 * 
 * All exchanges should be configured for cancel on disconnect
 * 
 * Each session has two event streams one for inbound events, one for outbound events
 * Within a stream each event is guarenteed to be in time order (ie order received / sent)
 * 
 * Each session replays its inbound and outbound streams from persistence into the controller concurrently
 * Each session is replayed into the controller concurrently
 * 
 * LINKAGE BETWEEN UPSTREAM AND DOWNSTREAM IS THROUGH THE srcLinkId WHICH MUST BE PRESENT IN THE RECOVERED 
 * DOWNSTREAM NOS
 * THIS MEANS CORRECT LINKAGE IS ONLY POSSIBLE ONCE ALL THE KEYS HAVE BEEN READ INTO THE RECOVERY ORDER STRUCTURE
 * 
 * Each downstream session must store the srcClOrdId so can always match up, either within the outbound message or as extra context if necessary
 * srcLinkId is used as follows
 *
 *    fix 4.4 use tag 526 (NOS)
 *    fix 4.2 use tag tag 58 with srcClOrdId{space}{id}
 *    UTP use 
 *
 *               <Map fixTag="58">
 *                   <Hook type="encode" code="_encoder.encodeString( FixDictionary42.Text, msg.getSrcLinkId() )"/>
 *                   <!-- as fix4.2 doesnt suppport 526, the linkId is hacked to tag 58, required here for recovery decoder -->
 *                   <Hook type="postDecode" code="msg.setSrcLinkId( start, valLen )"/>
 *               </Map>
 *    Millenium has no space in outbound NOS, so linkId is stored along with the NOS within the context block
 *
 * behaviour
 * 
 * 1) fills from exchange not sent to client will be sent to client
 * 2) and pending orders rejected to client
 * 3) any open orders cancelled to client
 * 4) any potentially open orders will be force cancelled to exchange
 * 5) events received but not sent to hub are sent to hub 
 * 6) events sent but not sent to hub are sent to hub
 * 7) processors order map and trade registry should look as if the processes hadnt been restarted  
 * 
 * Two Stage Recovery
 * 
 * Stage 1
 * 
 * replay all persistent streams into recovery controller which builds upstream and downstream structures to represent orders and order
 * versions. These structures are very minimal and retain the persistent key for later reading as necessary. 
 * Each order version will have chain of execIds.
 * Also builds linkage from downstream order request to upstream order request required for correct reconciliation
 * 
 * Stage 2
 * 
 * actual reconciliation between upstream and downstream views of orders
 * 
 */

@SuppressWarnings( "synthetic-access" )

public class HighFreqSimpleRecoveryController implements RecoveryController {

    private static final Logger _log = LoggerFactory.create( HighFreqSimpleRecoveryController.class );

    private static final ErrorCode ERR_INBOUND_REPLY   = new ErrorCode( "REC100", "FAILED replay of inbound messages" );
    private static final ErrorCode ERR_OUTBOUND_REPLY  = new ErrorCode( "REC110", "FAILED replay of outbound messages" );
    private static final ErrorCode ERR_MISS_ORD_VER    = new ErrorCode( "REC120", "Missing Order Version" );
    private static final ErrorCode ERR_MISMATCH_TRADES = new ErrorCode( "REC130", "Upstream and Downstream Trades out of sync" );
    private static final ErrorCode ERR_MISMATCH_REQ    = new ErrorCode( "REC140", "Regenerated event mismatch" );
    private static final ErrorCode ERR_MISS_TRADE      = new ErrorCode( "REC150", "Unable to find reference trade" );

    
    private static final ZString   RECOVERY_FORCE_CANCEL = new ViewString( "Recovery foced cancel" );
    private static final ZString   RECOVERY_FORCE_REJECT = new ViewString( "Recovery forced reject of request never sent to market" );
    private static final ZString   UNKNOWN_ORDER         = new ViewString( "UnknownOrder" );

    private final MessageRecycler _inboundRecycler  = new EventRecycler();
    private final MessageRecycler _outboundRecycler = new EventRecycler();

    private final ConcurrentHashMap<Session,RecoverySessionContext> _inSessCtx  = new ConcurrentHashMap<Session,RecoverySessionContext>();
    private final ConcurrentHashMap<Session,RecoverySessionContext> _outSessCtx = new ConcurrentHashMap<Session,RecoverySessionContext>();

    private final EventProcessorImpl _proc;
    private final EventBuilder       _eventBuilder;
    
    private final ReusableString _reconcileErrMsgBuf = new ReusableString();
    private final ReusableString _reconcileInfoMsgBuf = new ReusableString();
    
    private Message _upChainRoot;
    private Message _upChainTail;

    private Message _downChainRoot;
    private Message _downChainTail;
    
    
    private static class RecoveryException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private ErrorCode _code;

        public RecoveryException( ErrorCode code, String message ) {
            super( message );
            _code = code;
        }

        public ErrorCode getError() {
            return _code;
        }
    }
    
    private static final class RecOrder {
        private boolean       _reconciled; // ensure order only reconciled once
        private Session       _sess;
        private RecOrdVer     _root; // first ever version
        private RecOrdVer     _tail; // latest version
        
        // fills which were received from mkt and were not sent to client require adjustment of avePx and cumQty 
        public double            _totalTraded;
        public int               _cumQty;
        
        void addLatest( RecOrdVer v ) {
            v._prv = _tail;
            v._nxt = null;
            
            if ( _tail != null ) {
                _tail._nxt = v;
            }
            
            _tail = v;
            
            if ( _root == null ) {
                _root = v;
            }
        }

        void setReconciled( boolean isReconciled ) {
            _reconciled = isReconciled;
        }

        boolean isReconciled() {
            return _reconciled;
        }

        @Override
        public String toString() {
            ReusableString tmp = new ReusableString();
            dump( tmp );
            return tmp.toString(); 
        }

        public double getAvPx() {
            return (_cumQty==0) ? 0 : (_totalTraded / _cumQty);
        }

        public void dump( ReusableString buf ) {
            buf.append( "Order [" ).append( _sess.getComponentId() ); 
            buf.append( ", _totalTraded=" ).append( _totalTraded ).append( ", _cumQty=" ).append( _cumQty ).append( ", avePx=" ).append( getAvPx() );
            buf.append( ", _tail=" );
            _tail.dump( buf );
            buf.append( ", _reconciled=" ).append( _reconciled + "]" );
        }
    }

    private enum OrdReqType { NOS, Amend, Cancel }
    
    /**
     * in this model, each client order has single exchange (child) order
     * in lift time of an order it will have one version for each amend/cancel request
     */
    private static final class RecOrdVer {
        
        private OrdReqType       _type;
        private OrdStatus        _status;       // APPROXIMATION 
        private ReusableString   _clOrdIdChain; // resuableString next pointer to next clOrdId 
        private ViewString       _orderId;
        private long             _srcPersistOrderReqKey; // persist key for source order request, used to rerequest msg from persistent store  
        private RecOrdVer        _prv; // previous ie the previous / older version
        private RecOrdVer        _nxt; // next ie the next newer version
        
        private RecExec          _root; // first exec
        private RecExec          _tail; // last exec

        void addLatest( RecExec v ) {
            v._nxt = null;
            
            if ( _tail != null ) {
                _tail._nxt = v;
            }
            
            _tail = v;
            
            if ( _root == null ) {
                _root = v;
            }
        }

        public void dump( ReusableString buf ) {
            buf.append( "OrderVersion [type=" ).append( _type ); 
            buf.append( ", clOrdIdChain=" ).append( _clOrdIdChain ); 
            buf.append( ", status="       ).append( _status ); 
            buf.append( ", lastStatus="   ).append( getLastStatus() ); 
            buf.append( ", orderId="      ).append( _orderId );       
            buf.append( ", srcPersistOrderReqKey=" ).append( _srcPersistOrderReqKey + "]" );
        }

        @Override
        public String toString() {
            ReusableString tmp = new ReusableString();
            dump( tmp );
            return tmp.toString(); 
        }

        public OrdStatus getLastStatus() {
            if ( _status != null )
                return _status;
                
            if ( _tail != null && _tail._ordStatus != null ) {
                return _tail._ordStatus;
            }
            
            return _status;
        }
    }

    private static class RecExec {
        ReusableString  _execId;        // resuableString next pointer to next execId 
        ExecType        _execType;
        long            _persistKey;
        short           _execFlags;
        RecExec         _nxt;
        OrdStatus       _ordStatus;

        @Override
        public String toString() {
            return "Exec [ _execId=" + _execId + ", _execType=" + _execType + ", _persistKey=" + _persistKey
                   + ", _execFlags=" + _execFlags + ", _ordStatus=" + _ordStatus + "]";
        }
    }

    private static class RecTrade extends RecExec {
        private static final double MIN_PX_DELTA = 0.0000005;
        
        int             _qty;
        double          _px;
        
        boolean match( RecTrade other ) {
            return _execType == other._execType && _qty == other._qty && (Math.abs( _px - other._px ) < MIN_PX_DELTA);
        }

        @Override
        public String toString() {
            return "Trade [ _execId=" + _execId + ", _qty=" + _qty + ", _px=" + _px + ", _execType=" + _execType + ", _persistKey=" + _persistKey
                   + ", _execFlags=" + _execFlags + ", _ordStatus=" + _ordStatus + "]";
        }
    }

    private static class RecTradeCorr extends RecTrade {
        ReusableString  _execRefId = new ReusableString(); 
    }

    private final ConcurrentHashMap<ZString,RecOrder>          _idToUpstreamOrder;
    private final ConcurrentHashMap<ZString,RecOrder>          _idToDownstreamOrder;
    private final ConcurrentHashMap<ZString,RecOrdVer>   _idToDownstreamOrderVersion;
    private final ConcurrentHashMap<ZString,ZString>        _downClOrdIdToUpClOrdId;
    private final ConcurrentHashMap<ZString,ZString>        _upClOrdIdToDownClOrdId;
    
    /**
     * some exchanges only provide orderId on fills or other execs, the orderId is not unique across exchanges so is combined with symbol
     * only used for downstream linkage
     */
    private final ConcurrentHashMap<ZString,RecOrdVer>   _orderIdSymToVersion;

    private boolean _debug = true;

    private long _startTimeMS;

    private final String _id;
    
    public HighFreqSimpleRecoveryController( int expOrders, int totalSessions, EventProcessorImpl proc ) {
        this( "HighFreqSimpleRecoveryController", expOrders, totalSessions, proc );
    }
    
    public HighFreqSimpleRecoveryController( String id, int expOrders, int totalSessions, EventProcessorImpl proc ) {
        _id = id;
        _idToUpstreamOrder = new ConcurrentHashMap<ZString,RecOrder>( expOrders, 0.75f, totalSessions );
        _idToDownstreamOrder = new ConcurrentHashMap<ZString,RecOrder>( expOrders, 0.75f, totalSessions );
        _idToDownstreamOrderVersion = new ConcurrentHashMap<ZString,RecOrdVer>( expOrders, 0.75f, totalSessions );
        _downClOrdIdToUpClOrdId = new ConcurrentHashMap<ZString,ZString>( expOrders, 0.75f, totalSessions );
        _upClOrdIdToDownClOrdId = new ConcurrentHashMap<ZString,ZString>( expOrders, 0.75f, totalSessions );
        _orderIdSymToVersion = new ConcurrentHashMap<ZString,RecOrdVer>( expOrders, 0.75f, totalSessions );
        _eventBuilder = proc.getEventBuilder();
        _proc = proc;
    }

    /**
     * for each upstream order from the upstream order map VALUES
     *      find downstream order 
     *      if downstream order is null
     *          if order is open generate reject for request
     *      else
     *          iterate over mkt ACK/REPLACED/CANCELLED/FILLs
     *              if client didnt get message
     *                  generate client exec and queue4UP to client
     *               
     *          if any trades missing from upstream make them and send
     *          
     *          if order is pending on downstream
     *              queue4Down force cancel to downstream for the pending order req
     *              queue4Up reject back to upstream
     *          if order is open
     *              queue4Down force cancel to downstream for the last acked order req
     *              queue4Up unsol cancel to upstream
     *              
     *      create a LightOrderImpl with single order version in force cancel state
     *      for each client clOrdId create link to the LightOrderImpl in order processor
     * end for              
     */
    
    @Override
    public void reconcile() {
        
        long start = System.currentTimeMillis();

        _log.info( "Recovery playback took " + ((start - _startTimeMS) / 1000) + " secs" );
        _log.info( "Starting reconcile" );
        
        _upChainRoot = _upChainTail = null;
        _downChainRoot = _downChainTail = null;
        
        ReusableString buf = new ReusableString();
        
        /**
         * @NOTE before doing any actual reconciliation could iterate over the now complete order versions and execs
         *       and link mkt to client orders and verify items such as OrdStatus etc
         */
        
        int orders = 0;
        
        for( RecOrder upOrder : _idToUpstreamOrder.values() ) { // values() will give the Order once per key entry 
            
            if ( ! upOrder.isReconciled() ) {
                if ( _debug ) {
                    _reconcileInfoMsgBuf.copy( "Reconciling order " );
                    upOrder.dump( _reconcileInfoMsgBuf );
                    _log.info( _reconcileInfoMsgBuf );
                }
                
                try {
                    /**
                     * search from oldest to latest version, there may be multiple versions in pending state (unlikely but possible)
                     * while checking for missedExecs the orders cumQty and totalTraded is calculated 
                     */
                    replayMissedExecsAndRejectPendingVersions( buf, upOrder );
    
                    // search back from latest version to find the working version and cancel it if open
                    cancelOrderIfOpen( buf, upOrder );
                } catch( RecoveryException e ) {
                    _log.error( e.getError(), e.getMessage(), e );
                }

                upOrder.setReconciled(true);
                ++orders;
            }
        }
        
        populateProcOrderMap(); // avoid dups by seeding order map with key
    
        long end = System.currentTimeMillis();
        _log.info( "Reconciled " + orders + " orders, completed in " + (end - start) / 1000 + " secs" );
    }

    private void replayMissedExecsAndRejectPendingVersions( ReusableString buf, RecOrder upOrder ) {
        RecOrdVer upOrdVer = upOrder._root;

        // replay any missed execs and reject any pending orders upstream
        while( upOrdVer != null ) {
            ReusableString upClOrdId        = upOrdVer._clOrdIdChain;
            OrdStatus      upVerStatus      = upOrdVer._status;
            
            checkForPosResendUp( upOrder, upOrdVer, buf ); // resend persisted execs which dont have the sent confirmed marker
            
            if ( upVerStatus == null || upVerStatus.getIsPending() ) {      // reply to order request was never sent upstream
                rejectUpstream( upOrder, upOrdVer );
            }

            checkDownstreamFillsSentUpstream( upClOrdId, upOrder, upOrdVer );
            
            upOrdVer = upOrdVer._nxt;
        }
    }

    private void checkDownstreamFillsSentUpstream( ReusableString upClOrdId, RecOrder upOrder, RecOrdVer upOrdVer ) {
        ZString     downClOrdId = _upClOrdIdToDownClOrdId.get( upClOrdId );
        RecOrdVer   downOrdVer  = (downClOrdId==null) ? null : _idToDownstreamOrderVersion.get( downClOrdId );
        
        if ( downOrdVer != null ) {
            RecTrade downTrade = nextTrade( downOrdVer._root );
            RecTrade upTrade   = nextTrade( upOrdVer._root );
            
            // trades will always be in order
            while( downTrade != null && upTrade != null ) {
                
                if ( !downTrade.match( upTrade ) ) {
                    throw new RecoveryException( ERR_MISMATCH_TRADES, "Mismatch trades upClOrdId=" + upClOrdId + 
                                                 ", upExecId=" + upTrade._execId  + ", downExecId=" + downTrade._execId  );
                }

                switch( downTrade._execType ) {
                case Trade:
                    upOrder._totalTraded += (upTrade._px * upTrade._qty);
                    upOrder._cumQty += upTrade._qty;
                    break;
                case TradeCorrect:
                    RecTrade refTrade = findTradeEntry( upOrder, ((RecTradeCorr)upTrade)._execRefId );
                    if ( refTrade != null ) {
                        upOrder._totalTraded -= (refTrade._px * refTrade._qty);
                        upOrder._cumQty -= refTrade._qty;
                        
                        upOrder._totalTraded += (upTrade._px * upTrade._qty);
                        upOrder._cumQty += upTrade._qty;
                    }
                    break;
                case TradeCancel:
                    upOrder._totalTraded -= (upTrade._px * upTrade._qty);
                    upOrder._cumQty -= upTrade._qty;
                    break;
                default:
                    break;
                }
                
                downTrade = nextTrade( downTrade._nxt );
                upTrade = nextTrade( upTrade._nxt );
            }
            
            if ( downTrade != null ) {
                // trades will only be on NOS or AMEND not CancelRequest
                OrderRequest srcReq = (OrderRequest) regenerateOrdRequest( _inSessCtx, upOrder._sess, upOrdVer );
                
                /**
                 *  need the instrument trading ccy and order ccy to adjust major/minor
                 *  rather than store extra 16 bytes per order, refetch the instrument when we find a trade that needs propogating
                 *  which should be rare
                 */
                
                while( downTrade != null ) {
                    sendTradeUpstream( srcReq, upClOrdId, upOrder, upOrdVer, downOrdVer, downTrade );
                    
                    downTrade = nextTrade( downTrade._nxt );
                }
            }
        }
        
    }

    private RecTrade findTradeEntry( RecOrder upOrder, ZString execRefId ) {
        RecOrdVer v = upOrder._tail;
        
        while( v != null ) {
            RecExec e = v._root;
            
            while( e != null ) {
                if ( execRefId.equals( e._execId ) ) {
                    return (RecTrade) e;
                }
                e = e._nxt;
            }
            
            v = v._prv;
        }
        
        return null;
    }

    /**
     * note will need to get the exchange and do minor/major ccy changes
     * also add up extraTotal and extraQty to Order as when cancel will need adjust the cumQty avePx of the last exec sent
     * @param srcReq 
     */
    private void sendTradeUpstream( OrderRequest   src, 
                                    ReusableString upClOrdId, 
                                    RecOrder       upOrder, 
                                    RecOrdVer      upOrdVer,
                                    RecOrdVer      downOrdVer, 
                                    RecTrade       downTrade ) {
        
        RecOrder downOrder = _idToDownstreamOrder.get( downOrdVer._clOrdIdChain );
        TradeBase mktTrade = (TradeBase) regenerateDownExec( downOrder, downOrdVer, _reconcileErrMsgBuf, downTrade );
        TradeBase upTrade = null;
        
        switch( mktTrade.getReusableType().getSubId() ) {
        case EventIds.ID_TRADENEW:
            upTrade = handleMissingTradeNew( src, upOrder, upOrdVer, downOrdVer, mktTrade );
            break;
        case EventIds.ID_TRADECORRECT:
            upTrade = handleMissingTradeCorrect( src, upOrder, upOrdVer, downOrdVer, downOrder, mktTrade );
            break;
        case EventIds.ID_TRADECANCEL:
            upTrade = handleMissingTradeCancel( src, upOrder, upOrdVer, downOrdVer, downOrder, mktTrade );
            break;
        }
        queueUpstream( upTrade, upOrder._sess );
    }

    private TradeBase handleMissingTradeNew( OrderRequest src, RecOrder upOrder, RecOrdVer upOrdVer, RecOrdVer downOrdVer, TradeBase mktTrade ) {
        
        final double  lastPx = EventUtils.convertForMajorMinor( src, mktTrade.getLastPx() );
        
        if ( mktTrade.getMultiLegReportingType() != MultiLegReportingType.LegOfSpread ) {
        
            upOrder._totalTraded += (mktTrade.getLastQty() * lastPx); 
            upOrder._cumQty += mktTrade.getLastQty();
            
            int leavesQty = src.getOrderQty() - upOrder._cumQty;
            
            if ( leavesQty <= 0 && ! upOrdVer._status.getIsTerminal() ) {
                _log.info( "Reconcile recovered missing client side fill, setting status from open to filled" );
                upOrdVer._status = OrdStatus.Filled;
                
            } else if ( upOrder._cumQty > 0 && (upOrdVer._status == null || upOrdVer._status == OrdStatus.New) ) {
                _log.info( "Reconcile recovered missing client side fill, setting status from New to Partial" );
                upOrdVer._status = OrdStatus.PartiallyFilled;
            }
        }
        
        TradeBase upTrade = RecoveryUtils.populateUpTradeNew( src, upOrder._cumQty, upOrder._totalTraded, upOrdVer._status, 
                                                              downOrdVer._orderId, (TradeNew)mktTrade );
        return upTrade;
    }

    private TradeBase handleMissingTradeCancel( OrderRequest src, 
                                                RecOrder     upOrder, 
                                                RecOrdVer    upOrdVer, 
                                                RecOrdVer    downOrdVer, 
                                                RecOrder     downOrder, 
                                                TradeBase    mktTrade ) {

        TradeCancel mktTradeCancel = (TradeCancel) mktTrade;
        TradeBase origTrade = findTrade( downOrder, mktTradeCancel.getExecRefID() );
        if ( origTrade == null ) {
            throw new RecoveryException( ERR_MISS_TRADE, 
                                         ", tradeCancelId=" + mktTradeCancel.getExecId() + ", refExecId=" + mktTradeCancel.getExecRefID() );
        }
        
        final double  lastPx = EventUtils.convertForMajorMinor( src, origTrade.getLastPx() );
        
        if ( mktTrade.getMultiLegReportingType() != MultiLegReportingType.LegOfSpread ) {
            upOrder._totalTraded -= (origTrade.getLastQty() * lastPx); 
            upOrder._cumQty -= origTrade.getLastQty();

            tradeStatusAdjust( src, upOrder, upOrdVer );
        }
        
        TradeBase upTrade = RecoveryUtils.populateUpTradeCancel( src, 
                                                                 upOrder._cumQty, 
                                                                 upOrder._totalTraded, 
                                                                 upOrdVer._status, 
                                                                 downOrdVer._orderId, 
                                                                 (TradeCancel)mktTrade,
                                                                 origTrade);

        return upTrade;
    }

    /**
     * when a client side trade cancel / correct was missing it must be regenerated which means the order status requires modification
     */
    private void tradeStatusAdjust( OrderRequest src, RecOrder upOrder, RecOrdVer upOrdVer ) {
        int leavesQty = src.getOrderQty() - upOrder._cumQty; 
        if ( leavesQty > 0  ) {
            _log.info( "Reconcile recovered missing client side trade cancel/correct, changing status from filled to open" );
            if ( upOrder._cumQty > 0 ) {
                upOrdVer._status = OrdStatus.PartiallyFilled;
            } else if ( src.getOrigClOrdId().length() > 0 ) {
                upOrdVer._status = OrdStatus.Replaced;
            } else {
                upOrdVer._status = OrdStatus.New;
            }
        } else if ( leavesQty == 0 && upOrdVer._status != OrdStatus.Filled) {
            _log.info( "Reconcile recovered missing client side trade cancel/correct, changing status from partial to filled" );
            upOrdVer._status = OrdStatus.Filled;
        }
    }

    private TradeBase handleMissingTradeCorrect( OrderRequest src, 
                                                 RecOrder        upOrder, 
                                                 RecOrdVer upOrdVer, 
                                                 RecOrdVer downOrdVer, 
                                                 RecOrder        downOrder,
                                                 TradeBase    mktTrade ) {
        ClientTradeCorrectImpl upTrade;
        TradeCorrect mktTradeCorrect = (TradeCorrect) mktTrade;
        TradeBase origTrade = findTrade( downOrder, mktTradeCorrect.getExecRefID() );
        
        if ( origTrade == null ) {
            throw new RecoveryException( ERR_MISS_TRADE, 
                                         ", tradeCorrectId=" + mktTradeCorrect.getExecId() + ", refExecId=" + mktTradeCorrect.getExecRefID() );
        }
        
        final double  origPx = EventUtils.convertForMajorMinor( src, origTrade.getLastPx() );

        if ( mktTrade.getMultiLegReportingType() != MultiLegReportingType.LegOfSpread ) {

            final double  newPx = EventUtils.convertForMajorMinor( src, mktTrade.getLastPx() );
            double oldTradeVal = (origTrade.getLastQty() * origPx );
            double newTradeVal = (mktTrade.getLastQty() * newPx);
            upOrder._totalTraded = upOrder._totalTraded + newTradeVal - oldTradeVal;
    
            int qtyDiff = origTrade.getLastQty() - mktTrade.getLastQty();
            upOrder._cumQty -= qtyDiff;
            
            tradeStatusAdjust( src, upOrder, upOrdVer );
        }
        
        upTrade = RecoveryUtils.populateUpTradeCorrect( src, 
                                                        upOrder._cumQty, 
                                                        upOrder._totalTraded, 
                                                        upOrdVer._status, 
                                                        downOrdVer._orderId, 
                                                        (TradeCorrect)mktTrade );
        
        upTrade.setOrigQty( origTrade.getLastQty() );
        upTrade.setOrigPx( origPx );
        
        return upTrade;
    }

    private TradeBase findTrade( RecOrder downOrder, ZString execRefID ) {
        RecOrdVer downOrdVer = downOrder._tail;
        while( downOrdVer != null ) {
            RecExec exec = downOrdVer._root;
            
            while( exec != null ) {
                if ( execRefID.equals( exec._execId ) ) {
                    return (TradeBase) regenerateDownExec( downOrder, downOrdVer, _reconcileErrMsgBuf, exec );
                }
                
                exec = exec._nxt;
            }
            
            downOrdVer = downOrdVer._nxt;
        }
        
        return null;
    }

    private RecTrade nextTrade( RecExec exec ) {
        while( exec != null ) {
            if ( exec instanceof RecTrade )  {
                return (RecTrade) exec;
            }
            exec = exec._nxt;
        }
        return null;
    }

    private void cancelOrderIfOpen( ReusableString buf, RecOrder upOrder ) {
        RecOrdVer upOrdVer = upOrder._tail;
        boolean orderCancelled = false;
        
        boolean downOrderTerminal = false;
        
        while( upOrdVer != null && !orderCancelled ) {
            ReusableString upClOrdId        = upOrdVer._clOrdIdChain;
            ZString        downClOrdId      = _upClOrdIdToDownClOrdId.get( upClOrdId );
            RecOrdVer   downOrderVersion = (downClOrdId==null) ? null : _idToDownstreamOrderVersion.get( downClOrdId );
            OrdStatus      upVerStatus      = upOrdVer.getLastStatus();
            
            if ( upVerStatus == null || upVerStatus.getIsPending() ) {      // reply to order request was never sent upstream
                
                if ( downOrderVersion != null && !downOrderTerminal ) {                           // upstream order was sent to market
                    OrdStatus downOrdStatus = downOrderVersion.getLastStatus();                    
                    if ( downOrderVersion._type != OrdReqType.Cancel ) {
                        forceCancelDownstream( _idToDownstreamOrder.get(downClOrdId), downOrderVersion );
                    } else if ( downOrdStatus != null && downOrdStatus.getIsTerminal() ) {
                        downOrderTerminal= true;
                    }
                }
            } else if ( upVerStatus.getIsTerminal() ) {
                return;                                                     // order is terminal nothing to do
            } else { // upstream order is open
                forceCancelUpstream( upOrder, upOrdVer );                   // force cancel upstream
            
                if ( downOrderVersion != null ) {                           // downstream order may be open so force cancel it
                    OrdStatus downVerStatus = downOrderVersion.getLastStatus();
                    boolean   cancelDown    = (downVerStatus == null || downVerStatus.getIsTerminal() == false) && 
                                              downOrderVersion._type != OrdReqType.Cancel && !downOrderTerminal;
                    
                    if ( cancelDown ) {
                        forceCancelDownstream( _idToDownstreamOrder.get(downClOrdId), downOrderVersion );
                    }
                }
                
                orderCancelled = true;
            } 

            upOrdVer = upOrdVer._prv;
        }
    }

    private boolean forceCancelDownstream( RecOrder downOrder, RecOrdVer downOrdVer ) {
        Session downSess = downOrder._sess;
        downOrdVer = getLastNonCancelReqVersion(downOrdVer);
        OrderRequest regenMsg = (OrderRequest) regenerateOrdRequest( _outSessCtx, downSess, downOrdVer );
        
        if ( ! isImmediateOrder( regenMsg ) && ! ((TradingSessionConfig) downSess.getConfig()).isCancelOnDisconnect() ) {
            final CancelRequest mktCancel = _eventBuilder.createForceMarketCancel( downOrdVer._clOrdIdChain, 
                                                                                   regenMsg.getSide(), 
                                                                                   downOrdVer._orderId,
                                                                                   regenMsg.getSrcLinkId() );
            
            queueDownstream( mktCancel, downSess );
        
            return true;
        }
        
        return false;
    }

    private void rejectUpstream( RecOrder upOrder, RecOrdVer upOrdVer ) {
        Session upSess = upOrder._sess;
        BaseOrderRequest regenMsg = regenerateOrdRequest( _inSessCtx, upSess, upOrdVer );
        Message reject = null;

        switch( regenMsg.getReusableType().getSubId() ) {
        case EventIds.ID_NEWORDERSINGLE:
            reject = _eventBuilder.synthNOSRejected( (NewOrderSingle)regenMsg, RECOVERY_FORCE_REJECT, OrdRejReason.Other, OrdStatus.Rejected );
            break;
        case EventIds.ID_CANCELREPLACEREQUEST:
            reject = _eventBuilder.getCancelReject( regenMsg.getClOrdId(), 
                                                          regenMsg.getOrigClOrdId(),
                                                          UNKNOWN_ORDER,
                                                          RECOVERY_FORCE_REJECT, 
                                                          CxlRejReason.Other,
                                                          CxlRejResponseTo.CancelReplace, 
                                                          OrdStatus.Canceled );
            break;
        case EventIds.ID_CANCELREQUEST:
            reject = _eventBuilder.getCancelReject( regenMsg.getClOrdId(), 
                                                          regenMsg.getOrigClOrdId(),
                                                          UNKNOWN_ORDER,
                                                          RECOVERY_FORCE_REJECT, 
                                                          CxlRejReason.Other,
                                                          CxlRejResponseTo.CancelRequest, 
                                                          OrdStatus.Canceled );
            break;
        }

        if ( reject != null ) {
            queueUpstream( reject, upSess );
        }
    }

    private BaseOrderRequest  regenerateOrdRequest( Map<Session,RecoverySessionContext> sessCtx, Session sess, RecOrdVer ordVer ) {
        RecoverySessionContext ctx = sessCtx.get( sess );
        Message regenMsg = ctx.regenerate( ordVer._srcPersistOrderReqKey );
        
        if ( regenMsg instanceof BaseOrderRequest ) {
            BaseOrderRequest req = (BaseOrderRequest) regenMsg;
            
            if ( req.getClOrdId().equals( ordVer._clOrdIdChain ) ) {
                
                return req; // OK REGENERATED REQUEST
            }
        }
        
        _reconcileErrMsgBuf.copy( "Expected " ).append( sess.getComponentId() ).append( " key=" ).append( ordVer._srcPersistOrderReqKey );
        _reconcileErrMsgBuf.append( ", to recover clOrdId=" ).append( ordVer._clOrdIdChain ).append( ", but got : " );
        regenMsg.dump( _reconcileErrMsgBuf );

        throw new RecoveryException( ERR_MISMATCH_REQ, _reconcileErrMsgBuf.toString() );
    }

    private void forceCancelUpstream( RecOrder upOrder, RecOrdVer upOrdVer ) {
        Session upSess = upOrder._sess;
        upOrdVer = getLastNonCancelReqVersion(upOrdVer);
        OrderRequest regenMsg = (OrderRequest) regenerateOrdRequest( _inSessCtx, upSess, upOrdVer );
        
        int    cumQty        = upOrder._cumQty;
        
        if ( regenMsg.getOrderQty() - cumQty <= 0 ) {
            _log.info( "Reconciliation dont force cancel upstream as leaves qty zero " + upOrdVer._clOrdIdChain + ", cumQty" + cumQty );
            return;
        }
        
        RecExec base = upOrdVer._tail; // use last exec sent for this order version to generate cancelled
        CommonExecRpt lastExec = regenerateUpExec( upOrder, upOrdVer, _reconcileErrMsgBuf, base );
        
        double avePx         = (cumQty > 0 ) ? (upOrder._totalTraded / cumQty) : 0; 

        Cancelled cancelled = RecoveryUtils.createForceCancelled( regenMsg, lastExec, avePx, cumQty, RECOVERY_FORCE_CANCEL );
        
        queueUpstream( cancelled, upSess );
        
        return; // OK SENT FORCE CANCELLED UPSTREAM
    }

    private RecOrdVer getLastNonCancelReqVersion( RecOrdVer ver ) {
        RecOrdVer tmp = ver;
        
        while( tmp != null ) {
            if ( tmp._type != OrdReqType.Cancel ) {
                return tmp;
            }
            tmp = tmp._prv;
        }
        
        return ver;
    }

    private boolean isImmediateOrder( OrderRequest regenMsg ) {
        return (regenMsg.getTimeInForce() == TimeInForce.FillOrKill) || (regenMsg.getTimeInForce() == TimeInForce.ImmediateOrCancel);
    }

    private void populateProcOrderMap() {
        OrderMap map = _proc.getInternalMap();
        
        for( ZString clOrdId : _idToUpstreamOrder.keySet() ) {
            // avoid future dups by adding null value entries to map
            // must copy the clOrdId to prevent linkage issues 
            
            map.put( new ReusableString(clOrdId), null );
        }

        for( ZString clOrdId : _idToDownstreamOrder.keySet() ) {
            map.put( new ReusableString(clOrdId), null );
        }
    }

    private void checkForPosResendUp( RecOrder upOrder, RecOrdVer upOrdVer, ReusableString buf ) {

        if ( ! upOrder._sess.getConfig().isMarkConfirmationEnabled() ) {
            return;
        }
        
        RecExec curExec = upOrdVer._root;
        
        while( curExec != null ) {
            if ( (curExec._execFlags & Session.PERSIST_FLAG_CONFIRM_SENT) == 0 ) {
                buf.copy( "Reconcile missing confirm sent flag, will resend as posDup " );
                buf.append( ", clOrdId=" ).append( upOrdVer._clOrdIdChain );
                buf.append( ", execId="  ).append( curExec._execId );
                buf.append( ", execType="  ).append( curExec._execType );
                buf.append( ", status="  ).append( curExec._ordStatus );
                buf.append( ", key="  ).append( curExec._persistKey );
                
                _log.info( buf );
                
                Message m = regenerateUpExec( upOrder, upOrdVer, buf, curExec );

                m.setFlag( MsgFlag.PossDupFlag, true ); // THIS IS AN EDGE CASE WHERE MSG MAY HAVE BEEN SENT (tho unlikely)
                
                queueUpstream( m, upOrder._sess );
            }
            
            curExec = curExec._nxt;
        }
    }

    /**
     * regenerate an exec from upstream session
     * @return regenerated exec with verified execId
     */
    private CommonExecRpt regenerateUpExec( RecOrder upOrder, RecOrdVer upOrdVer, ReusableString buf, RecExec e ) {
        RecoverySessionContext ctx = _outSessCtx.get( upOrder._sess );
        return regenerateAnExec( ctx, upOrder, upOrdVer, buf, e );
    }

    private CommonExecRpt regenerateDownExec( RecOrder downOrder, RecOrdVer downOrdVer, ReusableString buf, RecExec e ) {
        RecoverySessionContext ctx = _inSessCtx.get( downOrder._sess );
        return regenerateAnExec( ctx, downOrder, downOrdVer, buf, e );
    }

    private CommonExecRpt regenerateAnExec( RecoverySessionContext ctx, RecOrder order, RecOrdVer ordVer, ReusableString buf, RecExec e ) {
        Message m = ctx.regenerate( e._persistKey );
        
        if ( m instanceof CommonExecRpt ) {
            CommonExecRpt regenMsg = (CommonExecRpt) m;

            if ( e._execId.equals( regenMsg.getExecId() ) ) {
                return regenMsg;
            }
        }
        
        buf.copy( "Warning persistence didnt reextract exec : Expected " );
        buf.append( ", clOrdId=" ).append( ordVer._clOrdIdChain );
        buf.append( ", execId="  ).append( e._execId );
        buf.append( ", execType="  ).append( e._execType );
        buf.append( ", status="  ).append( e._ordStatus );
        buf.append( ", key="  ).append( e._persistKey );
        buf.append( ", Regenerated : " );
        m.dump( buf );

        throw new RecoveryException( ERR_MISMATCH_REQ, buf.toString() );
    }

    private void queueUpstream( Message m, MessageHandler dest ) {
        
        if ( _debug ) {
            _reconcileInfoMsgBuf.copy( "RECOVERY Q for UP   " ).append( dest.getComponentId() ).append( " : " );
            m.dump( _reconcileInfoMsgBuf );
            _log.info( _reconcileInfoMsgBuf );
        }
        
        m.setMessageHandler( dest );
        
        if ( _upChainRoot == null ) {
            _upChainRoot = m;
        }
        
        if ( _upChainTail != null ) {
            _upChainTail.attachQueue( m );
        }
        
        _upChainTail = m;
    }

    private void queueDownstream( Message m, MessageHandler dest ) {
        if ( _debug ) {
            _reconcileInfoMsgBuf.copy( "RECOVERY Q for DOWN " ).append( dest.getComponentId() ).append( " : " );
            m.dump( _reconcileInfoMsgBuf );
            _log.info( _reconcileInfoMsgBuf );
        }
        
        m.setMessageHandler( dest );
        if ( _downChainRoot == null ) {
            _downChainRoot = m;
        }
        
        if ( _downChainTail != null ) {
            _downChainTail.attachQueue( m );
        }
        
        _downChainTail = m;
    }

    /**
     * send all queued messages from reconcile
     */
    
    @Override
    public void commit() {
        _log.info( "Starting reconcile commit" );
        long start = System.currentTimeMillis();

        dispatchEvents( getDownChain() );
        dispatchEvents( getUpstreamChain() );
        
        long end = System.currentTimeMillis();
        _log.info( "Reconcile commit completed in " + (end - start) / 1000 + " secs" );
    }

    private Message dispatchEvents( Message msg ) {
        while( msg != null ) {
            Message next = msg.getNextQueueEntry();
            
            msg.detachQueue();
            
            /**
             * mark message with reconciliation flag so session knows not to discard
             */
            msg.setFlag( MsgFlag.Reconciliation, true );
            
            Session hdl = (Session) msg.getMessageHandler();
            
            hdl.handle( msg );
            
            msg = next;
        }
        return msg;
    }
    
    @Override
    public void start() {
        _log.info( "Starting recovery process" );
        _startTimeMS = System.currentTimeMillis();
    }

    @Override
    public RecoverySessionContext startedInbound( Session sess ) {
        RecoverySessionContextImpl ctx = new RecoverySessionContextImpl(sess, true);
        _inSessCtx.put( sess, ctx );
        return ctx;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    @Override
    public void completedInbound( RecoverySessionContext ctx ) {
        _log.info( "Completed replay of inbound messages from " + ctx.getSession().getComponentId() );
    }

    @Override
    public void failedInbound( RecoverySessionContext ctx ) {
        _log.error( ERR_INBOUND_REPLY, "from " + ctx.getSession().getComponentId() );
    }

    @Override
    public void processInbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags) {
        try {
            while( msg != null ) {
                doProcessInbound( ctx, persistKey, msg, persistFlags );
                msg = msg.getNextQueueEntry();
            }
        } catch( RecoveryException e ) {
            ReusableString warn = ctx.getWarnMessage();
            warn.copy( "Unexpected inbound recovery exception in " ).append( ctx.getSession().getComponentId() ).append( " - " );
            warn.append( e.getMessage() ).append( ", persistKey=" ).append( persistKey ).append( " : ");
            if ( msg != null ) msg.dump( warn );
            _log.warn( warn );
        }
    }
        
    @Override
    public RecoverySessionContext startedOutbound( Session sess ) {
        RecoverySessionContextImpl ctx = new RecoverySessionContextImpl(sess, false);
        _outSessCtx.put( sess, ctx );
        return ctx;
    }

    @Override
    public void completedOutbound( RecoverySessionContext ctx ) {
        _log.info( "Completed replay of outbound messages from " + ctx.getSession().getComponentId() );
    }
    
    @Override
    public void failedOutbound( RecoverySessionContext ctx ) {
        _log.error( ERR_OUTBOUND_REPLY, "from " + ctx.getSession().getComponentId() );
    }
    
    @Override
    public void processOutbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags) {
        try {
            while( msg != null ) {
                doProcessOutbound( ctx, persistKey, msg, persistFlags );
                msg = msg.getNextQueueEntry();
            }
        } catch( RecoveryException e ) {
            ReusableString warn = ctx.getWarnMessage();
            warn.copy( "Unexpected outbound recovery exception in " ).append( ctx.getSession().getComponentId() ).append( " - " );
            warn.append( e.getMessage() ).append( ", persistKey=" ).append( persistKey ).append( " : ");
            if ( msg != null ) msg.dump( warn );
            _log.warn( warn );
        }
    }

    protected Message getUpstreamChain() {
        return _upChainRoot;
    }

    protected Message getDownChain() {
        return _downChainRoot;
    }

    private void doProcessInbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags) {
        final SessionDirection sd = ctx.getSession().getDirection();
        
        if ( sd == SessionDirection.Upstream ) {
            // inbound message from an upstream session, process NOS/AmendReq/CanReq
            switch( msg.getReusableType().getSubId() ) {
            case EventIds.ID_NEWORDERSINGLE:
                upstreamInHandleNewOrderSingle( (NewOrderSingle) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_CANCELREPLACEREQUEST:
                upstreamInHandleCancelReplaceRequest( (CancelReplaceRequest) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_CANCELREQUEST:
                upstreamInHandleCancelRequest( (CancelRequest) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_NEWORDERACK:
            case EventIds.ID_TRADENEW:
            case EventIds.ID_CANCELREJECT:
            case EventIds.ID_REJECTED:
            case EventIds.ID_CANCELLED:
            case EventIds.ID_REPLACED:
            case EventIds.ID_DONEFORDAY:
            case EventIds.ID_STOPPED:
            case EventIds.ID_EXPIRED:
            case EventIds.ID_SUSPENDED:
            case EventIds.ID_RESTATED:
            case EventIds.ID_TRADECORRECT:
            case EventIds.ID_TRADECANCEL:
            case EventIds.ID_ORDERSTATUS:
            case EventIds.ID_VAGUEORDERREJECT:
                ReusableString warn = ctx.getWarnMessage(); 
                warn.copy( "Unexpected inbound Message from upstream system " );
                msg.dump( warn );
                _log.warn( warn );
                break;
            default:
                break;
            }
        } else if ( sd == SessionDirection.Downstream ) {
            // inbound message from a downstream session, process execReport
            switch( msg.getReusableType().getSubId() ) {
            case EventIds.ID_NEWORDERACK:
                downstreamInHandleNewOrderAck( (NewOrderAck) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_CANCELREJECT:
                downstreamInHandleCancelReject( (CancelReject) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_REJECTED:
                downstreamInHandleRejected( (Rejected) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_CANCELLED:
                downstreamInHandleCancelled( (Cancelled) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_REPLACED:
                downstreamInHandleReplaced( (Replaced) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_VAGUEORDERREJECT:
                downstreamInHandleVagueReject( (VagueOrderReject) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_DONEFORDAY:
            case EventIds.ID_STOPPED:
            case EventIds.ID_EXPIRED:
            case EventIds.ID_SUSPENDED:
            case EventIds.ID_RESTATED:
            case EventIds.ID_TRADECORRECT:
            case EventIds.ID_TRADECANCEL:
            case EventIds.ID_ORDERSTATUS:
            case EventIds.ID_TRADENEW:
                downstreamInHandleOtherExec( (CommonExecRpt)msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_NEWORDERSINGLE:
            case EventIds.ID_CANCELREPLACEREQUEST:
            case EventIds.ID_CANCELREQUEST:
                ReusableString warn = ctx.getWarnMessage(); 
                msg.dump( warn );
                _log.warn( "Unexpected inbound Message from upstream system " + warn.toString() );
                break;
            default:
                break;
            }        
        }
        
        synchronized( _inboundRecycler ) {
            _inboundRecycler.recycle( msg );
        }
    }

    private void doProcessOutbound( RecoverySessionContext ctx, long persistKey, Message msg, short persistFlags) {
        final SessionDirection sd = ctx.getSession().getDirection();
        
        if ( sd == SessionDirection.Downstream ) {
            // inbound message from an upstream session, process NOS/AmendReq/CanReq
            switch( msg.getReusableType().getSubId() ) {
            case EventIds.ID_NEWORDERSINGLE:
                downstreamOutHandleNewOrderSingle( (NewOrderSingle) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_CANCELREPLACEREQUEST:
                downstreamOutHandleCancelReplaceRequest( (CancelReplaceRequest) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_CANCELREQUEST:
                downstreamOutHandleCancelRequest( (CancelRequest) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_NEWORDERACK:
            case EventIds.ID_TRADENEW:
            case EventIds.ID_CANCELREJECT:
            case EventIds.ID_REJECTED:
            case EventIds.ID_CANCELLED:
            case EventIds.ID_REPLACED:
            case EventIds.ID_DONEFORDAY:
            case EventIds.ID_STOPPED:
            case EventIds.ID_EXPIRED:
            case EventIds.ID_SUSPENDED:
            case EventIds.ID_RESTATED:
            case EventIds.ID_TRADECORRECT:
            case EventIds.ID_TRADECANCEL:
            case EventIds.ID_ORDERSTATUS:
            case EventIds.ID_VAGUEORDERREJECT:
                ReusableString warn = ctx.getWarnMessage(); 
                msg.dump( warn );
                _log.warn( "Unexpected outbound Message from downstream system " + warn.toString() );
                break;
            default:
                break;
            }
        } else if ( sd == SessionDirection.Upstream ) {
            // inbound message from a downstream session, process execReport
            switch( msg.getReusableType().getSubId() ) {
            case EventIds.ID_NEWORDERACK:
                upstreamOutHandleNewOrderAck( (NewOrderAck) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_CANCELREJECT:
                upstreamOutHandleCancelReject( (CancelReject) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_REJECTED:
                upstreamOutHandleRejected( (Rejected) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_CANCELLED:
                upstreamOutHandleCancelled( (Cancelled) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_REPLACED:
                upstreamOutHandleReplaced( (Replaced) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_DONEFORDAY:
            case EventIds.ID_STOPPED:
            case EventIds.ID_EXPIRED:
            case EventIds.ID_SUSPENDED:
            case EventIds.ID_RESTATED:
            case EventIds.ID_TRADECORRECT:
            case EventIds.ID_TRADECANCEL:
            case EventIds.ID_ORDERSTATUS:
            case EventIds.ID_TRADENEW:
                upstreamOutHandleOtherExec( (CommonExecRpt) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_VAGUEORDERREJECT:
                upstreamOutHandleVagueReject( (VagueOrderReject) msg, persistKey, ctx, persistFlags );
                break;
            case EventIds.ID_NEWORDERSINGLE:
            case EventIds.ID_CANCELREPLACEREQUEST:
            case EventIds.ID_CANCELREQUEST:
                ReusableString warn = ctx.getWarnMessage(); 
                msg.dump( warn );
                _log.warn( "Unexpected outbound Message from downstream system " + warn.toString() );
                break;
            default:
                break;
            }        
        }

        synchronized( _outboundRecycler ) {
            _outboundRecycler.recycle( msg );
        }
    }
    
    private void upstreamInHandleNewOrderSingle( NewOrderSingle msg, long persistKey, RecoverySessionContext ctx, short persistFlags ) {
        RecOrder    order   = getOrder( _idToUpstreamOrder, msg.getClOrdId() );
        RecOrdVer   version = getVersion( order, msg.getClOrdId() );

        version._type = OrdReqType.NOS;
        order._sess   = ctx.getSession();
        
        version._srcPersistOrderReqKey = persistKey;
    }

    private void upstreamInHandleCancelReplaceRequest( CancelReplaceRequest msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        ZString     prevClOrdId  = msg.getOrigClOrdId();
        ZString     newclOrdId   = msg.getClOrdId();
        RecOrder    order        = getOrder( _idToUpstreamOrder, prevClOrdId );
        RecOrdVer   curVersion   = getVersion( order, prevClOrdId );
        RecOrdVer   newVersion   = getVersion( order, newclOrdId );
        
        curVersion._clOrdIdChain.setNext( newVersion._clOrdIdChain );         // link the prevClOrdId to the newClOrdId
        _idToUpstreamOrder.putIfAbsent( newVersion._clOrdIdChain, order );
        newVersion._srcPersistOrderReqKey = persistKey;
        newVersion._type = OrdReqType.Amend;
    }

    private void upstreamInHandleCancelRequest( CancelRequest msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        ZString     prevClOrdId  = msg.getOrigClOrdId();
        ZString     newclOrdId   = msg.getClOrdId();
        RecOrder    order        = getOrder( _idToUpstreamOrder, prevClOrdId );
        RecOrdVer   curVersion   = getVersion( order, prevClOrdId );
        RecOrdVer   newVersion   = getVersion( order, newclOrdId );
        
        curVersion._clOrdIdChain.setNext( newVersion._clOrdIdChain );         // link the prevClOrdId to the newClOrdId
        _idToUpstreamOrder.putIfAbsent( newVersion._clOrdIdChain, order );
        newVersion._srcPersistOrderReqKey = persistKey;
        newVersion._type = OrdReqType.Cancel;
    }

    private RecOrdVer findDownstreamOrderVersion( ZString clOrdId, ZString symbol, ZString orderId ) {
        if ( clOrdId.length() > 0 ) {
            RecOrder order = getOrder( _idToDownstreamOrder, clOrdId );
            return getVersion( order, clOrdId );
        }
        
        ReusableString key = formOrderIdLookupKey( orderId, symbol );
        RecOrdVer version = _orderIdSymToVersion.get( key );
        
        if ( version != null ) {
            throw new RecoveryException( ERR_MISS_ORD_VER, 
                                         "Unable to find order version for exec clOrdId=" + clOrdId + ", orderId=" + orderId + ", sym=" + symbol );
        }
        
        return version;
    }

    private void downstreamInHandleNewOrderAck( NewOrderAck msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        downstreamInReqReply( null, msg, OrdStatus.New, persistKey, ctx, persistFlags );
    }

    private void downstreamInHandleCancelled( Cancelled msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        downstreamInReqReply( msg.getOrigClOrdId(), msg, OrdStatus.Canceled, persistKey, ctx, persistFlags );
    }

    private void downstreamInHandleReplaced( Replaced msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        downstreamInReqReply( msg.getOrigClOrdId(), msg, OrdStatus.Replaced, persistKey, ctx, persistFlags );
    }

    private void downstreamInHandleCancelReject( CancelReject msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        ZString        prevClOrdId  = msg.getOrigClOrdId();
        RecOrder          order        = getOrder( _idToDownstreamOrder, prevClOrdId );
        ZString        clOrdId      = msg.getClOrdId();
        RecOrdVer   version      = getVersion( order, clOrdId );
        
        setOrderStatus( version, OrdStatus.Rejected );
    }

    private void downstreamInHandleRejected( Rejected msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        downstreamInReqReply( msg.getOrigClOrdId(), msg, OrdStatus.Rejected, persistKey, ctx, persistFlags );
    }

    /**
     * common request reply handling
     */
    private void downstreamInReqReply( ZString origClOrdId, CommonExecRpt msg, OrdStatus status, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        RecOrder          order;
        
        if ( origClOrdId != null && origClOrdId.length() > 0 ) {
            order = getOrder( _idToDownstreamOrder, origClOrdId );
        } else {
            order = getOrder( _idToDownstreamOrder, msg.getClOrdId() );
        }
        
        RecOrdVer   version = getVersion( order, msg.getClOrdId() );
        
        if ( status != null ) {
            setOrderStatus( version, status );
        }
        
        version._orderId   = new ReusableString( msg.getOrderId() );
        ReusableString key = formOrderIdLookupKey( msg.getOrderId(), getInstrumentKey( msg ) );
        if ( key.length() > 0 ) {
            _orderIdSymToVersion.put( key, version );
        }
        
        addExec( version, msg, persistKey, persistFlags );
    }

    private ViewString getInstrumentKey( CommonExecRpt msg ) {
        if ( msg.getSymbol().length() > 0 ) {
            return msg.getSymbol();
        }
        return msg.getSecurityId();
    }

    private void downstreamInHandleOtherExec( CommonExecRpt msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        RecOrdVer version = findDownstreamOrderVersion( msg.getClOrdId(), getInstrumentKey( msg ), msg.getOrderId() );
        addExec( version, msg, persistKey, persistFlags );
    }

    private void downstreamInHandleVagueReject( VagueOrderReject msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        // @TODO vague reject processing
    }

    private void downstreamOutHandleNewOrderSingle( NewOrderSingle msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        ZString        clOrdId = msg.getClOrdId();
        RecOrder          order   = getOrder( _idToDownstreamOrder, clOrdId );
        RecOrdVer   version = getVersion( order, clOrdId );
        
        order._sess    = ctx.getSession();
        
        version._srcPersistOrderReqKey = persistKey;
        version._type = OrdReqType.NOS;
        
        setLinkId( msg.getSrcLinkId(), persistKey, ctx, version._clOrdIdChain , version );
    }

    private void downstreamOutHandleCancelReplaceRequest( CancelReplaceRequest msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        ZString     newClOrdId   = msg.getClOrdId();
        ZString     prevClOrdId  = msg.getOrigClOrdId();
        RecOrder    order        = getOrder( _idToDownstreamOrder, prevClOrdId );
        RecOrdVer   curVersion   = getVersion( order, prevClOrdId );
        RecOrdVer   newVersion   = getVersion( order, newClOrdId );
        
        curVersion._clOrdIdChain.setNext( newVersion._clOrdIdChain );         // link the prevClOrdId to the newClOrdId
        _idToDownstreamOrder.putIfAbsent( newVersion._clOrdIdChain, order );
        newVersion._srcPersistOrderReqKey = persistKey;
        newVersion._type = OrdReqType.Amend;
        setLinkId( msg.getSrcLinkId(), persistKey, ctx, newVersion._clOrdIdChain , newVersion );
    }

    private void downstreamOutHandleCancelRequest( CancelRequest msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        ZString     newClOrdId   = msg.getClOrdId();
        ZString     prevClOrdId  = msg.getOrigClOrdId();
        RecOrder    order        = getOrder( _idToDownstreamOrder, prevClOrdId );
        RecOrdVer   curVersion   = getVersion( order, prevClOrdId );
        RecOrdVer   newVersion   = getVersion( order, newClOrdId );
        
        curVersion._clOrdIdChain.setNext( newVersion._clOrdIdChain );         // link the prevClOrdId to the newClOrdId
        _idToDownstreamOrder.putIfAbsent( newVersion._clOrdIdChain, order );
        newVersion._srcPersistOrderReqKey = persistKey;
        newVersion._type = OrdReqType.Cancel;
        setLinkId( msg.getSrcLinkId(), persistKey, ctx, newVersion._clOrdIdChain , newVersion );
    }

    private void setLinkId( ZString linkId, long persistKey, RecoverySessionContext ctx, ReusableString downClOrdId, RecOrdVer downVersion ) {
        if ( linkId.length() == 0 ) {
            _log.warn( "DownstreamOrderRequest missing upstream link id, downClOrdId=" + downClOrdId + ", sess=" + ctx.getSession().getComponentId() + 
                       ", persistKey=" + persistKey );
        } else {
            ZString linkIdCopy = new ReusableString( linkId );
            
            _downClOrdIdToUpClOrdId.put( downClOrdId, linkIdCopy );
            _upClOrdIdToDownClOrdId.put( linkIdCopy, downClOrdId );
            _idToDownstreamOrderVersion.put( downClOrdId, downVersion );
        }
    }

    private void upstreamOutHandleNewOrderAck( NewOrderAck msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        upstreamOutReqReply( null, msg, OrdStatus.New, persistKey, ctx, persistFlags );
    }

    private void upstreamOutReqReply( ZString origClOrdId, CommonExecRpt msg, OrdStatus status, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        ZString        clOrdId = msg.getClOrdId();
        
        RecOrder          order;
        
        if ( origClOrdId != null && origClOrdId.length() > 0 ) {
            order = getOrder( _idToUpstreamOrder, origClOrdId );
        } else {
            order = getOrder( _idToUpstreamOrder, msg.getClOrdId() );
        }
        
        RecOrdVer   version = getVersion( order, clOrdId );
        
        if ( status != null ) {
            setOrderStatus( version, status );
        }
        
        version._orderId   = new ReusableString( msg.getOrderId() );
        
        addExec( version, msg, persistKey, persistFlags );
    }

    private void upstreamOutHandleOtherExec( CommonExecRpt msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        ZString     clOrdId = msg.getClOrdId();
        RecOrder    order   = getOrder( _idToUpstreamOrder, clOrdId );
        RecOrdVer   version = getVersion( order, clOrdId );

        addExec( version, msg, persistKey, persistFlags );
    }

    private void upstreamOutHandleCancelReject( CancelReject msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        ZString     clOrdId = msg.getClOrdId();
        RecOrder    order   = getOrder( _idToUpstreamOrder, clOrdId );
        RecOrdVer   version = getVersion( order, clOrdId );
        
        setOrderStatus( version, OrdStatus.Rejected );
    }

    private void upstreamOutHandleRejected( Rejected msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        upstreamOutReqReply( msg.getOrigClOrdId(), msg, OrdStatus.Rejected, persistKey, ctx, persistFlags );
    }

    private void upstreamOutHandleCancelled( Cancelled msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        upstreamOutReqReply( msg.getOrigClOrdId(), msg, OrdStatus.Canceled, persistKey, ctx, persistFlags );
    }

    private void upstreamOutHandleReplaced( Replaced msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        upstreamOutReqReply( msg.getOrigClOrdId(), msg, OrdStatus.Replaced, persistKey, ctx, persistFlags );
    }

    private void upstreamOutHandleVagueReject( VagueOrderReject msg, long persistKey, RecoverySessionContext ctx, short persistFlags) {
        // @TODO handle vague reject
    }

    private RecOrdVer getVersion( RecOrder order, ZString id ) {
        RecOrdVer v;
        
        synchronized( order ) {
            v = order._tail;
            
            while( v != null ) {
                if ( id.equals( v._clOrdIdChain ) ) {
                    return v;                           // found version
                }
                v = v._prv;
            }
            
            v = new RecOrdVer();
            v._clOrdIdChain = new ReusableString(id);
            
            order.addLatest( v );
        }
        
        return v;
    }

    /**
     * get order for the clOrdId, caters for edge case multi threaded overlapping calls 
     * 
     * @param map
     * @param id clOrdId instance to be put in map ... must not be recycled which in map !
     * @return
     */
    private RecOrder getOrder( ConcurrentHashMap<ZString, RecOrder> map, ZString id ) {
        RecOrder order = map.get( id );
        
        if ( order == null ) {
            order = new RecOrder();
            
            RecOrder prev = map.putIfAbsent( new ReusableString(id), order );
            
            if ( prev != null ) {
                order = prev;
            }
        }
        
        return order;
    }

    /**
     * status only set by inbound exec report
     * 
     * @param version
     * @param newStatus
     */
    private void setOrderStatus( RecOrdVer version, OrdStatus newStatus ) {
        synchronized( version ) {
            version._status = newStatus;
        }
    }

    private void addExec( RecOrdVer version, CommonExecRpt msg, long persistKey, short persistFlags ) {
        ZString execId = msg.getExecId();
        if ( execId.length() > 0 ) {
            synchronized( version ) {
                RecExec exec;
                
                if ( msg instanceof TradeCorrect ) {
                    TradeCorrect baseTrade = (TradeCorrect) msg;
                    RecTradeCorr t = new RecTradeCorr();
                    
                    t._qty = baseTrade.getLastQty();
                    t._px = baseTrade.getLastPx();
                    t._execRefId.copy( baseTrade.getExecRefID() );
                    
                    exec = t;
                } else if ( msg instanceof TradeBase ) {
                    TradeBase baseTrade = (TradeBase) msg;
                    RecTrade t = new RecTrade();
                    
                    t._qty = baseTrade.getLastQty();
                    t._px = baseTrade.getLastPx();
                    
                    exec = t;
                } else {
                    exec = new RecExec();
                }
                
                exec._execId = new ReusableString(execId);
                exec._execType = msg.getExecType();
                exec._ordStatus = msg.getOrdStatus();
                exec._persistKey = persistKey;
                exec._execFlags = persistFlags;
                
                version.addLatest( exec );
                version._status = exec._ordStatus;
            }
        }
    }

    private ReusableString formOrderIdLookupKey( ZString orderId, ZString symbol ) {
        ReusableString orderIdKey = new ReusableString( orderId.length() + symbol.length() );
        if ( orderId.length() > 0 ) { 
            orderIdKey.append( orderId ).append( ':' ).append( symbol );
        }
        return orderIdKey;
    }
}
