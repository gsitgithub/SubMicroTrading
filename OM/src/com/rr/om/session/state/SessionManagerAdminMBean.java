/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.session.state;

import com.rr.core.admin.AdminCommand;

public interface SessionManagerAdminMBean extends AdminCommand {

    public String listAllSessions();
    public String listClientSessions();
    public String listExchangeSessions();
    
    public String pauseSession( String sessionName );
    public String resumeSession( String sessionName );

    public String setSessionSeqNums( String sessionName, int nextInSeqNum, int nextOutSeqNum, boolean passiveReset );
    
    public String logoutSession( String sessionName );
    public String loginSession( String sessionName );

}
