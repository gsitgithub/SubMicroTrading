/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.loaders;

import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.collections.JavaConcMessageQueue;
import com.rr.core.collections.MessageQueue;
import com.rr.core.collections.NonBlockingSyncQueue;
import com.rr.core.collections.RingBufferMsgQueue1C1P;
import com.rr.core.collections.RingBufferMsgQueueSingleConsumer;
import com.rr.core.collections.SimpleMessageQueue;
import com.rr.core.collections.SlowNonBlockingYieldingSyncQueue;
import com.rr.core.component.SMTComponent;
import com.rr.core.component.SMTSingleComponentLoader;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.SMTRuntimeException;


public class QueueLoader implements SMTSingleComponentLoader {

    private static final Logger _log = LoggerFactory.create( QueueLoader.class );
    
    private String  _type               = null;
    private boolean _enableSendSpinLock = false;
    private int     _queuePresize       = 1024;
    
    @Override
    public SMTComponent create( String id ) {

        boolean useSpinLocks = _enableSendSpinLock;
        
        MessageQueue queue;
        
        if ( _type != null ) {
            
            if ( _type.equals( "SlowNonBlockingSync" ) ) {
                queue = new SlowNonBlockingYieldingSyncQueue( id );
            } else if ( _type.equals( "NonBlockingSync" ) ) {
                queue = new NonBlockingSyncQueue( id );
            } else  if ( _type.equalsIgnoreCase( "UNSAFE" ) ) {
                queue = new SimpleMessageQueue( id );
            } else  if ( _type.equalsIgnoreCase( "SunCAS" ) ) {
                queue = new JavaConcMessageQueue( id );
            } else  if ( _type.equalsIgnoreCase( "SMTCAS" ) ) {
                queue = new ConcLinkedMsgQueueSingle( id );
            } else  if ( _type.equalsIgnoreCase( "BlockingSync" ) ) {
                queue = new BlockingSyncQueue( id );
            } else  if ( _type.equalsIgnoreCase( "RingBuffer1P1C" ) ) {
                queue = new RingBufferMsgQueue1C1P( id, _queuePresize );
            } else  if ( _type.equalsIgnoreCase( "RingBuffer1C" ) ) {
                queue = new RingBufferMsgQueueSingleConsumer( id, _queuePresize );
            } else {
                throw new SMTRuntimeException( "Configured 'queue' property of " + _type + ", not valid must be one of " +
                                               " [SlowNonBlockingSync|NonBlockingSync|SunCAS|SMTCAS|BlockingSync|RingBuffer1C|RingBuffer1P1C]" );
            }
        } else {
            if ( useSpinLocks ) {
                queue = new ConcLinkedMsgQueueSingle();
            } else {
                queue = new BlockingSyncQueue();
            }
        }
        
        _log.info( "QUEUE: Using " + queue.getClass().getSimpleName() + " for " + id );
        
        return queue;
    }
}
