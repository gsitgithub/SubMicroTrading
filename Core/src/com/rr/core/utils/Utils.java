/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import java.util.Arrays;

import com.rr.core.lang.Constants;
import com.rr.core.lang.Env;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.os.NativeHooksImpl;

public final class Utils {

    private static final Logger _log     = LoggerFactory.console( Utils.class );
    private static final Logger _console = LoggerFactory.create( Utils.class );

    public static void invokeGC() {
        _console.info( "Starting forced GC" );
        _log.info( "Starting forced GC" );
        long start = System.currentTimeMillis();
        System.gc();
        long end = System.currentTimeMillis();
        _console.info( "Forced GC over duration=" + (end - start) + " msecs" );
        _log.info( "Forced GC over duration=" + (end - start) + " msecs" );
    }

    public static void delay( int ms ) {

//        try {
//            throw new SMTRuntimeException("Forced");
//        } catch( SMTRuntimeException e ) {
//            ReusableString stackTrace = new ReusableString();
//
//            ExceptionTrace.format( stackTrace, e );
//
//            String st = stackTrace.toString();
//            
//            if ( !st.contains( "AsyncAppender" ) && !st.contains("AntiSpringBootstrap")) {
//                _console.info( "PAUSING for " + ms + " from " + st );
//            }
//        }
        
        NativeHooksImpl.instance().sleep( ms );
    }

    public static void delayMicros( int micros ) {
        NativeHooksImpl.instance().sleepMicros( micros );
    }

    public static ZString getClassName( Object o ) {
        return new ViewString( o.getClass().getSimpleName() );
    }

    public static void delay( Object delayLock, int delayIntervalMS ) {
        synchronized( delayLock ) {
            try {
                delayLock.wait( delayIntervalMS );
            } catch( InterruptedException e ) {
                // ignore
            }
        }
    }
    
    public static long nanoTimeMonotonicRaw() {
        return NativeHooksImpl.instance().nanoTimeMonotonicRaw();
    }

    public static long nanoTime() {
        if ( Env.isUseTSCForNanoTime() ) {
            return NativeHooksImpl.instance().nanoTimeRDTSC();
        }
        return System.nanoTime();
    }
    
    public static synchronized <T> T[] arrayCopyAndAddEntry( T[] base, T extra ) {
        int last = base.length;
        
        T[] tmp = Arrays.copyOf( base, last + 1 );
        tmp[ last ] = extra;
        
        return tmp;
    }

    public static double nullCheck( final double val ) {
        return (val == Constants.UNSET_DOUBLE) ? 0 : val;
    }

    public static int nullCheck( final int val ) {
        return (val == Constants.UNSET_INT) ? 0 : val;
    }

    public static short nullCheck( final short val ) {
        return (val == Constants.UNSET_SHORT) ? 0 : val;
    }

    public static float nullCheck( final float val ) {
        return (val == Constants.UNSET_FLOAT) ? 0 : val;
    }

    public static long nullCheck( final long val ) {
        return (val == Constants.UNSET_LONG) ? 0 : val;
    }

    public static byte nullCheck( final byte val ) {
        return (val == Constants.UNSET_BYTE) ? (byte)0 : val;
    }
}
