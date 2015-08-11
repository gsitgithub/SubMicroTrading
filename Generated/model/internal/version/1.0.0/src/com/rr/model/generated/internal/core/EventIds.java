/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.core;


import javax.annotation.Generated;

@Generated( "com.rr.model.generated.internal.core.EventIds" )

public interface EventIds {

    public int ID_NEWORDERSINGLE = 1;
    public int ID_CANCELREPLACEREQUEST = 2;
    public int ID_CANCELREQUEST = 3;
    public int ID_FORCECANCEL = 4;
    public int ID_VAGUEORDERREJECT = 5;
    public int ID_CANCELREJECT = 6;
    public int ID_NEWORDERACK = 7;
    public int ID_TRADENEW = 8;
    public int ID_REJECTED = 9;
    public int ID_CANCELLED = 10;
    public int ID_REPLACED = 11;
    public int ID_DONEFORDAY = 12;
    public int ID_STOPPED = 13;
    public int ID_EXPIRED = 14;
    public int ID_SUSPENDED = 15;
    public int ID_RESTATED = 16;
    public int ID_TRADECORRECT = 17;
    public int ID_TRADECANCEL = 18;
    public int ID_ORDERSTATUS = 19;
    public int ID_HEARTBEAT = 20;
    public int ID_TESTREQUEST = 21;
    public int ID_LOGON = 22;
    public int ID_LOGOUT = 23;
    public int ID_SESSIONREJECT = 24;
    public int ID_RESENDREQUEST = 25;
    public int ID_CLIENTRESYNCSENTMSGS = 26;
    public int ID_SEQUENCERESET = 27;
    public int ID_TRADINGSESSIONSTATUS = 28;
    public int ID_SECMASSSTATGRP = 29;
    public int ID_MASSINSTRUMENTSTATECHANGE = 30;
    public int ID_ALERTLIMITBREACH = 31;
    public int ID_ALERTTRADEMISSINGORDERS = 32;
    public int ID_STRATINSTRUMENTSTATE = 33;
    public int ID_STRATEGYSTATE = 34;
    public int ID_UTPLOGON = 35;
    public int ID_UTPLOGONREJECT = 36;
    public int ID_UTPTRADINGSESSIONSTATUS = 37;
    public int ID_ETICONNECTIONGATEWAYREQUEST = 38;
    public int ID_ETICONNECTIONGATEWAYRESPONSE = 39;
    public int ID_ETISESSIONLOGONREQUEST = 40;
    public int ID_ETISESSIONLOGONRESPONSE = 41;
    public int ID_ETISESSIONLOGOUTREQUEST = 42;
    public int ID_ETISESSIONLOGOUTRESPONSE = 43;
    public int ID_ETISESSIONLOGOUTNOTIFICATION = 44;
    public int ID_ETIUSERLOGONREQUEST = 45;
    public int ID_ETIUSERLOGONRESPONSE = 46;
    public int ID_ETIUSERLOGOUTREQUEST = 47;
    public int ID_ETIUSERLOGOUTRESPONSE = 48;
    public int ID_ETITHROTTLEUPDATENOTIFICATION = 49;
    public int ID_ETISUBSCRIBE = 50;
    public int ID_ETISUBSCRIBERESPONSE = 51;
    public int ID_ETIUNSUBSCRIBE = 52;
    public int ID_ETIUNSUBSCRIBERESPONSE = 53;
    public int ID_ETIRETRANSMIT = 54;
    public int ID_ETIRETRANSMITRESPONSE = 55;
    public int ID_ETIRETRANSMITORDEREVENTS = 56;
    public int ID_ETIRETRANSMITORDEREVENTSRESPONSE = 57;
    public int ID_MILLENIUMLOGON = 58;
    public int ID_MILLENIUMLOGONREPLY = 59;
    public int ID_MILLENIUMLOGOUT = 60;
    public int ID_MILLENIUMMISSEDMESSAGEREQUEST = 61;
    public int ID_MILLENIUMMISSEDMSGREQUESTACK = 62;
    public int ID_MILLENIUMMISSEDMSGREPORT = 63;
    public int ID_BOOKADDORDER = 64;
    public int ID_BOOKDELETEORDER = 65;
    public int ID_BOOKMODIFYORDER = 66;
    public int ID_BOOKCLEAR = 67;
    public int ID_SYMBOLREPEATINGGRP = 68;
    public int ID_MDREQUEST = 69;
    public int ID_TICKUPDATE = 70;
    public int ID_MDUPDATE = 71;
    public int ID_SECDEFEVENTS = 72;
    public int ID_SECURITYALTID = 73;
    public int ID_SDFEEDTYPE = 74;
    public int ID_SECDEFLEG = 75;
    public int ID_MDENTRY = 76;
    public int ID_MDSNAPENTRY = 77;
    public int ID_MSGSEQNUMGAP = 78;
    public int ID_MDINCREFRESH = 79;
    public int ID_MDSNAPSHOTFULLREFRESH = 80;
    public int ID_SECURITYDEFINITION = 81;
    public int ID_SECURITYDEFINITIONUPDATE = 82;
    public int ID_PRODUCTSNAPSHOT = 83;
    public int ID_SECURITYSTATUS = 84;
}
