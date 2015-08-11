/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.ets;

import com.rr.core.model.Message;
import com.rr.model.generated.internal.core.EventIds;

class ETSCommonSessionUtils {

    static int setOutSeqNum( ETSController controller, final Message msg ) {
        int nextOut = 0;
        if ( isSessionMessage( msg ) == false ) {
            nextOut = controller.getAndIncNextOutSeqNum();
            msg.setMsgSeqNum( nextOut );
        }
        return nextOut;
    }
    
    static boolean isSessionMessage( Message msg ) {
        final int subId = msg.getReusableType().getSubId(); 

        switch( subId ) {
        case EventIds.ID_HEARTBEAT:
        case EventIds.ID_TESTREQUEST:
        case EventIds.ID_UTPLOGON:
        case EventIds.ID_UTPLOGONREJECT:
            return true;
        }
        return false;
    }
}
