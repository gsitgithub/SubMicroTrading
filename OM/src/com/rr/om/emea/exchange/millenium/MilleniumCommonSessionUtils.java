/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.millenium;

import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.model.generated.codec.MilleniumLSEDecoder;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.core.ModelReusableTypes;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.model.generated.internal.events.interfaces.NewOrderSingle;
import com.rr.om.emea.exchange.millenium.recovery.MilleniumRecoveryController;

class MilleniumCommonSessionUtils {

    static boolean isSessionMessage( Message msg ) {
        final int subId = msg.getReusableType().getSubId(); 
        
        switch( subId ) {
        case EventIds.ID_MILLENIUMLOGON:
        case EventIds.ID_MILLENIUMLOGONREPLY:
        case EventIds.ID_MILLENIUMLOGOUT:
        case EventIds.ID_MILLENIUMMISSEDMESSAGEREQUEST:
        case EventIds.ID_MILLENIUMMISSEDMSGREQUESTACK:
        case EventIds.ID_MILLENIUMMISSEDMSGREPORT:
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
    
    static Message recoveryDecode( MilleniumController ctl, MilleniumLSEDecoder decoder, byte[] buf, int offset, int len, boolean inBound ) {
        
        decoder.parseHeader( buf, offset, len );
        Message msg = decoder.postHeaderDecode();

        ctl.recoverContext( msg, inBound, decoder.getAppId() );
        
        return msg;
    }

    public static MilleniumController createSessionController( SeqNumSession session, MilleniumSocketConfig config ) {
        if ( config.isRecoverySession() ) {
            return new MilleniumRecoveryController( session, config );
        }
        
        return new MilleniumController( session, config );
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
