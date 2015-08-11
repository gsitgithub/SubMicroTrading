/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import java.nio.ByteBuffer;

import com.rr.core.lang.ReusableString;
import com.rr.core.model.Message;
import com.rr.core.persister.Persister;
import com.rr.core.recovery.RecoverySessionContext;
import com.rr.core.session.Session;

public class RecoverySessionContextImpl implements RecoverySessionContext {

    private final Session        _sess;
    private final boolean        _isInbound;
    private final ReusableString _warnMsg = new ReusableString();
    private final ReusableString _tmpMsgBuf = new ReusableString( 8192 );
    private final ByteBuffer     _tmpCtxBuf = ByteBuffer.allocate( 1024 );
    private       Persister      _persister;

    public RecoverySessionContextImpl( Session sess, boolean isInbound ) {
        super();
        _sess = sess;
        _isInbound = isInbound;
    }

    @Override
    public boolean isInbound() {
        return _isInbound;
    }

    @Override
    public Session getSession() {
        return _sess;
    }

    @Override
    public ReusableString getWarnMessage() {
        return _warnMsg;
    }

    @Override
    public boolean hasChainSession() {
        return _sess.getChainSession() != null;
    }

    @Override
    public boolean persistFlagConfirmSentEnabled() {
        return _sess.getConfig().isMarkConfirmationEnabled();
    }

    @Override
    public void setPersister( Persister persister ) {
        _persister = persister;
    }

    @Override
    public Persister getPersister() {
        return _persister;
    }

    @Override
    public Message regenerate( long persistKey ) {
        return _sess.recoverEvent( _isInbound, persistKey, _tmpMsgBuf, _tmpCtxBuf );
    }
}
