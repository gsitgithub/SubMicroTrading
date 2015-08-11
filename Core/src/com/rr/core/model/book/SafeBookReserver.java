/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.model.book;

import com.rr.core.model.BookReserver;


public final class SafeBookReserver implements BookReserver {
    public static final int MIN_RESET_DELAY_NANOS = 1000 * 1000 * 1;
    
    private static final class SingleThreadLetter implements BookReserver {

        /**
         * NANO_AGE_MIN_RESET disables age reset unless 50 micros have elapsed
         * 
         * realistically exchange will take 100usecs to get reply to order so should be good enough
         * 
         * required to provide protection from book change while 1 thread has grabbed liquidity and another thread may be about to overlap
         */
        private long _minResetDelayNanos = MIN_RESET_DELAY_NANOS;

        private int _curReserve;
        
        private long _lastTickNanos;
        
        public SingleThreadLetter() { /* nothing */ }

        @Override public void attachReserveWorkerThread( Thread t ) { /* nothing */ }
        
        @Override public void setMinResetDelayNANOS( long nanos ) {
            _minResetDelayNanos = nanos;
        }

        @Override public int grabQty( int requestedQty, int currentQtyFromBook, long timeNanos ) {
            
            checkAgeReset( timeNanos );
            
            int qty = currentQtyFromBook - _curReserve;
            
            if ( qty > requestedQty ) qty = requestedQty; 

            if ( qty <= 0 ) return 0;

            _curReserve += qty;
            
            return qty;
        }
        
        @Override public int  getReserved() {
            return _curReserve;
        }

        @Override public void reset() {
            _lastTickNanos = 0;
            _curReserve = 0;
        }

        @Override public void completed( int orderQty ) {
            _curReserve -= orderQty;
            
            if ( _curReserve < 0 ) _curReserve = 0;
        }

        @Override public int getAttachedWorkerThreads() { return 0; }

        private void checkAgeReset( long timeNanos ) {
            if ( Math.abs( _lastTickNanos - timeNanos ) > _minResetDelayNanos ) { 
                _curReserve = 0;
                _lastTickNanos = timeNanos;
            }
        }
    }
    
    private static final class MultiThreadLetter implements BookReserver {

        private final BookReserver _mletter;

        public MultiThreadLetter( BookReserver letter ) {
            _mletter = letter;
        }

        @Override
        public void setMinResetDelayNANOS( long nanos ) {
            _mletter.setMinResetDelayNANOS( nanos );
        }

        @Override public void attachReserveWorkerThread( Thread t ) { 
            _mletter.attachReserveWorkerThread( t );
        }

        @Override public synchronized int grabQty( int requestedQty, int currentQtyFromBook, long timeNanos ) {
            return _mletter.grabQty( requestedQty, currentQtyFromBook, timeNanos );
        }

        @Override public synchronized int getReserved() { 
            return _mletter.getReserved();
        }

        @Override public synchronized void completed( int orderQty ) {
            _mletter.completed( orderQty );
        }

        @Override public synchronized void reset() {
            _mletter.reset();
        }

        @Override public int getAttachedWorkerThreads() { return 0; }
    }
 
    private Thread _firstThread = null;
    private int    _threadCount = 0;
    
    private BookReserver _letter = new SingleThreadLetter();

    @Override
    public synchronized void attachReserveWorkerThread( Thread t ) {
        if ( _firstThread == null ) {
            ++_threadCount;
            _firstThread = t;

            return;
            
        } 
        
        if ( t == _firstThread ) {
            return;
        }

        ++_threadCount;
        
        if ( _threadCount == 2 ) {
            _letter = new MultiThreadLetter( _letter );
        }
    }

    @Override
    public int grabQty( int requestedQty, int currentQtyFromBook, long timeNanos ) {
        return _letter.grabQty( requestedQty, currentQtyFromBook, timeNanos );
    }

    @Override
    public void setMinResetDelayNANOS( long nanos ) {
        _letter.setMinResetDelayNANOS( nanos );
    }

    @Override
    public void reset() {
        _letter.reset();
    }

    @Override
    public void completed( int orderQty ) {
        _letter.completed( orderQty );
    }

    @Override
    public int  getReserved() {
        return _letter.getReserved();
    }

    @Override
    public synchronized int getAttachedWorkerThreads() {
        return _threadCount;
    }
}
