/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.fastfix;

import java.util.HashMap;
import java.util.Map;

import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.Message;
import com.rr.model.generated.internal.events.impl.HeartbeatImpl;
import com.rr.model.generated.internal.events.impl.LogonImpl;
import com.rr.model.generated.internal.events.impl.LogoutImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.SecurityDefinitionImpl;
import com.rr.model.generated.internal.events.impl.SecurityStatusImpl;
import com.rr.model.generated.internal.events.interfaces.MDSnapshotFullRefresh;

public class FastFixSMTMapping {

    // map of msgType to SMT event class
    private final static Map<ZString,Class<? extends Message>> _fixMsgTypeToEventName = createMsgEventMap();

    // map of msgType to SMT event class
    private final static Map<ZString,ZString> _fixMsgTypeToBaseName = createFixTypeToFastNameMap();


    private static Map<ZString, Class<? extends Message>> createMsgEventMap() {
        Map<ZString, Class<? extends Message>> map = new HashMap<ZString, Class<? extends Message>>();
        
        map.put( new ViewString("0"), HeartbeatImpl.class );
        map.put( new ViewString("5"), LogoutImpl.class );
        map.put( new ViewString("A"), LogonImpl.class );
        map.put( new ViewString("d"), SecurityDefinitionImpl.class );
        map.put( new ViewString("f"), SecurityStatusImpl.class );
        map.put( new ViewString("W"), MDSnapshotFullRefresh.class );
        map.put( new ViewString("X"), MDIncRefreshImpl.class );
        
        return map;
    }
    
    private static Map<ZString, ZString> createFixTypeToFastNameMap() {
        Map<ZString, ZString> map = new HashMap<ZString, ZString>();
        
        map.put( new ViewString("0"), new ViewString("MDHeartbeat") );
        map.put( new ViewString("5"), new ViewString("MDLogout") );
        map.put( new ViewString("A"), new ViewString("MDLogon") );
        map.put( new ViewString("d"), new ViewString("MDSecurityDefinition") );
        map.put( new ViewString("f"), new ViewString("MDSecurityStatus") );
        map.put( new ViewString("W"), new ViewString("MDSnapshotFullRefresh") );
        map.put( new ViewString("X"), new ViewString("MDIncRefresh") );

        map.put( new ViewString("B"), new ViewString("MDNewsMessage") );
        map.put( new ViewString("R"), new ViewString("MDQuoteRequest") );

        return map;
    }

    public static Class<? extends Message> getEventForFixMsgType( ZString fixMsgType ) {
        return _fixMsgTypeToEventName.get( fixMsgType );
    }

    public static ZString getFixMsgTypeToFastFixBaseName( ZString fixMsgType ) {
        return _fixMsgTypeToBaseName.get( fixMsgType );
    }

}
