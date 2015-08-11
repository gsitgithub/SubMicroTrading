/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.os;

import com.rr.core.lang.Env;


public class NativeHooksImpl implements NativeHooks {

    private static boolean _linuxNative   = false;
    private static boolean _windowsNative = false;
    
    static {
        if ( Env.isUseLinuxNative() ) {
            System.loadLibrary( "submicrocore" );
            _linuxNative = true;
        }
    }

    static {
        if ( Env.isUseWindowsNative() ) {
            System.loadLibrary( "submicrocore" );
            _windowsNative = true;
        }
    }

    private static NativeHooks _instance = new NativeHooksImpl();

    public static NativeHooks instance() { return _instance; }
    
    private static native void jniCalibrateTicks();
    private static native void jniSetPriority( int mask, int priority );
    private static native void jniSleep( int ms );
    private static native void jniSleepMicros( int micros );
    private static native void jniSetProcessMaxPriority();
    private static native long jniNanoTimeRDTSC();
    private static native long jniNanoTimeMonotonicRaw();
    private static native long jniNanoRDTSCStart();
    private static native long jniNanoRDTSCStop();
    
    public NativeHooksImpl() {
        if ( _linuxNative || _windowsNative ) {
            jniCalibrateTicks();
        } 
    }
    
    @Override
    public void setPriority( Thread thread, int mask, int priority ) {
        if ( priority == MAX_PRIORITY ) {
            setProcessMaxPriority();
        }
        if ( _linuxNative ) {
            jniSetPriority( mask, priority );
        } else {
            thread.setPriority( priority );
        }
    }

    @Override
    public void sleep( int ms ) {
        if ( ms < 10 ) {                // if sleeping more than 10ms dont care about accuracy that much
            if ( _linuxNative || _windowsNative ) {
                jniSleep( ms );
            } else {
                try {
                    Thread.sleep( ms );
                } catch( InterruptedException e ) {
                    // ignore
                }
            }
        } else {
            try {
                Thread.sleep( ms );
            } catch( InterruptedException e ) {
                // ignore
            }
        }
    }

    @Override
    public void sleepMicros( int micros )  {
        if ( _linuxNative || _windowsNative ) {
            jniSleepMicros( micros );
        } else {
            try {
                Thread.sleep( (micros >> 10) + 1 ); // rough estimate
            } catch( InterruptedException e ) {
                // ignore
            }
        }
    }
    
    @Override
    public void setProcessMaxPriority() {
        if ( _linuxNative || _windowsNative ) {
            jniSetProcessMaxPriority();
        }
    }

    @Override
    public long nanoTimeRDTSC() {
        if ( _linuxNative || _windowsNative ) {
            return jniNanoTimeRDTSC();
        } 
        return System.nanoTime();
    }
    
    @Override
    public long nanoTimeMonotonicRaw() {
        if ( _linuxNative || _windowsNative ) {
            return jniNanoTimeMonotonicRaw();
        } 
        return System.nanoTime();
    }
}
