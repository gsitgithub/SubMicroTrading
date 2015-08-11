/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.trading;

import com.rr.codec.emea.exchange.eti.ETIDecodeContext;
import com.rr.codec.emea.exchange.eti.ETIDecoder;
import com.rr.core.collections.IntToLongHashMap;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.model.Message;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.utils.NumberUtils;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.core.ModelReusableTypes;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.model.generated.internal.events.interfaces.BaseOrderRequest;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.om.emea.exchange.eti.gateway.ETIGatewayController;

class ETICommonSessionUtils {
    
    static int setOutSeqNum( ETIController controller, ETIDecodeContext decodeContext, Message msg ) {
        final int subId = msg.getReusableType().getSubId(); 
        
        int nextOut = 0;

        switch( subId ) {
        case EventIds.ID_NEWORDERSINGLE:
        case EventIds.ID_CANCELREPLACEREQUEST:
        case EventIds.ID_CANCELREQUEST:
            nextOut = controller.getAndIncNextOutSeqNum();
            msg.setMsgSeqNum( nextOut );
            
            // MUST STORE THE seqNum to clOrdId FOR REJECT PROCESSING
            
            store( decodeContext, nextOut, ((BaseOrderRequest)msg).getClOrdId());
            break;
        case EventIds.ID_FORCECANCEL:
        case EventIds.ID_VAGUEORDERREJECT:
        case EventIds.ID_CANCELREJECT:
        case EventIds.ID_NEWORDERACK:
        case EventIds.ID_TRADENEW:
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
            nextOut = controller.getAndIncNextOutSeqNum();
            msg.setMsgSeqNum( nextOut );
            break;
        case EventIds.ID_HEARTBEAT:
        case EventIds.ID_TESTREQUEST:
            // no seq num on heartbeat
            return 0;
        default:
            nextOut = controller.getAndIncNextOutSeqNum();
            msg.setMsgSeqNum( nextOut );
            break;
        }
        
        return nextOut;
    }

    private static void store( ETIDecodeContext decodeContext, int seqNum, ViewString clOrdId ) {
        IntToLongHashMap map = decodeContext.getMapSeqNumClOrdId();
        
        long mktOrdId = NumberUtils.parseLong( clOrdId );

        // will only have any possible contention when processing reject messages which should be rare
        
        synchronized( map ) {
            map.put( seqNum, mktOrdId );
        }
    }

    static boolean isSessionMessage( Message msg ) {
        final int subId = msg.getReusableType().getSubId(); 
        
        switch( subId ) {
        case EventIds.ID_ETICONNECTIONGATEWAYREQUEST:
        case EventIds.ID_ETICONNECTIONGATEWAYRESPONSE:
        case EventIds.ID_ETISESSIONLOGONREQUEST:
        case EventIds.ID_ETISESSIONLOGONRESPONSE:
        case EventIds.ID_ETISESSIONLOGOUTREQUEST:
        case EventIds.ID_ETISESSIONLOGOUTRESPONSE:
        case EventIds.ID_ETISESSIONLOGOUTNOTIFICATION:
        case EventIds.ID_ETIUSERLOGONREQUEST:
        case EventIds.ID_ETIUSERLOGONRESPONSE:
        case EventIds.ID_ETIUSERLOGOUTREQUEST:
        case EventIds.ID_ETIUSERLOGOUTRESPONSE:
        case EventIds.ID_ETITHROTTLEUPDATENOTIFICATION:
        case EventIds.ID_ETISUBSCRIBE:
        case EventIds.ID_ETISUBSCRIBERESPONSE:
        case EventIds.ID_ETIUNSUBSCRIBE:
        case EventIds.ID_ETIUNSUBSCRIBERESPONSE:
        case EventIds.ID_ETIRETRANSMIT:
        case EventIds.ID_ETIRETRANSMITRESPONSE:
        case EventIds.ID_ETIRETRANSMITORDEREVENTS:
        case EventIds.ID_ETIRETRANSMITORDEREVENTSRESPONSE:
            return true;
        default:
            switch( subId ) {
            case EventIds.ID_HEARTBEAT:
            case EventIds.ID_TESTREQUEST:
                return true;
            }
        }
        return false;
    }
    
    static Message recoveryDecode( ETIController    ctl, 
                                   ETIDecoder       decoder, 
                                   ETIDecodeContext lastDecodeContext, 
                                   byte[]           buf, 
                                   int              offset, 
                                   int              len, 
                                   boolean          inBound ) {
        
        decoder.parseHeader( buf, offset, len );
        Message msg = decoder.postHeaderDecode();

        decoder.getLastContext( lastDecodeContext );
        
        ctl.recoverContext( msg, inBound, lastDecodeContext );
        
        return msg;
    }

    public static ETIController createSessionController( SeqNumSession session, ETISocketConfig config ) {
        if ( config.isGatewaySession() ) {
            return new ETIGatewayController( session, config );
        }
        
        return new ETIController( session, config );
    }

    public static void getContextForOutPersist( Message msg, ReusableString msgContext ) {
        msgContext.reset();
        if ( msg.getReusableType().getSubId() == EventIds.ID_NEWORDERSINGLE ) {
            msgContext.copy( ((NewOrderSingle)msg).getSrcLinkId() );
        }
    }

    public static void enrichRecoveredContext( Message msg, byte[] opt, int optOffset, int optLen ) {
        if ( msg.getReusableType() == ModelReusableTypes.RecoveryNewOrderSingle ) {
            ((RecoveryNewOrderSingleImpl)msg).getSrcLinkIdForUpdate().setValue( opt, optOffset, optLen );
        }
    }
}
