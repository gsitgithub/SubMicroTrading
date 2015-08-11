/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.recovery;

import com.rr.core.model.Message;
import com.rr.core.persister.Persister;
import com.rr.core.persister.PersisterException;
import com.rr.model.generated.fix.codec.Standard44Encoder;
import com.rr.om.processor.BaseProcessorTestCase;
import com.rr.om.processor.EventProcessorImpl;
import com.rr.om.warmup.FixTestUtils;

public abstract class BaseRecoveryTst extends BaseProcessorTestCase {
    
    protected static class TestHighFreqSimpleRecoveryController extends HighFreqSimpleRecoveryController {

        public TestHighFreqSimpleRecoveryController( int expOrders, int totalSessions, EventProcessorImpl proc ) {
            super( expOrders, totalSessions, proc );
        }

        @Override
        public Message getUpstreamChain() {
            return super.getUpstreamChain();
        }

        @Override
        public Message getDownChain() {
            return super.getDownChain();
        }
    }
    
    
    private byte[]                       _deBuf = new byte[8192];
    private byte[]                       _enBuf = new byte[8192];
    private Standard44Encoder            _encoder = FixTestUtils.getEncoder44( _enBuf, 0 );
    
    protected long persist( DummyRecoverySession sess, Message msg, boolean isInbound ) {
        long key = 0;
        
        try {
            _encoder.encode( msg );

            Persister p = (isInbound) ? sess.getInboundPersister() : sess.getOutboundPersister();
            key = p.persist( _enBuf, _encoder.getOffset(), _encoder.getLength() );
        } catch( PersisterException e ) {
            fail();
        }
        
        return key;
    }
    
    protected Message regen( DummyRecoverySession client, boolean isInbound, long key ) {
        Persister p = (isInbound) ? client.getInboundPersister() : client.getOutboundPersister();

        int len = 0;
        
        try {
            len = p.read( key, _deBuf, 0 );
        } catch( PersisterException e ) {
            fail();
        }
        
        return _decoder.decode( _deBuf, 0, len );
    }
}
