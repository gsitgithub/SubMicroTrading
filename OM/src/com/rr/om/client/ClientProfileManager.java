/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.component.SMTComponent;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.model.ClientProfile;

public class ClientProfileManager implements SMTComponent {

    private Map<ZString,ClientProfile> _profs = new ConcurrentHashMap<ZString, ClientProfile>();

    private ClientProfile _defaultClientProfile = new OMClientProfileImpl( "DefaultClientProfile", new ViewString( "DefaultClient" ) ); 
    private ClientProfile _dummyProfile         = new DummyClientProfile(); 
    
    private boolean _useDummyProfile = false;
    private String  _id;

    private boolean _allowDefaultClientProfile = false;

    public ClientProfileManager() {
        this( null );
    }
    
    public ClientProfileManager( String id ) {
        _id = id;
    }
    
    public ClientProfile create( ZString sessName, ZString clientName ) {
        ClientProfile prof = _profs.get( sessName );
        
        if ( prof == null ) {
            prof = new OMClientProfileImpl( "CP_" + clientName, clientName );
            _profs.put( sessName, prof );
        }
        
        return prof;
    }

    public ClientProfile get( ZString sessName ) {
        
        if ( _useDummyProfile ) {
            return _dummyProfile;
        }
        
        ClientProfile p = _profs.get( sessName );
        
        if ( p == null && _allowDefaultClientProfile ) {
            return _defaultClientProfile;
        }
        
        return p;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
}
