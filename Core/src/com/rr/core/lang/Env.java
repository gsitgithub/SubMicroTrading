/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

public class Env {
    // @NOTE DONT USE LOGGING HERE MUST USE SYSTEM.OUT
    
    private static String   _socketFactoryClass  = null;
    private static boolean  _isUseLinuxNative    = false;
    private static boolean  _isUseWindowsNative  = false;
    private static boolean  _isUseTSCForNanoTime = false;
    private static boolean  _allowOnloadCalls    = false;

    static {
        String socketFactoryClass  = System.getProperty( "SOCKET_FACTORY_CLASS" );
        String isUseLinuxNative    = System.getProperty( "USE_NATIVE_LINUX",      "false" );
        String isUseWindowsNative  = System.getProperty( "USE_NATIVE_WINDOWS",    "false" );
        String isUseTSCForNanoTime = System.getProperty( "USE_TSC_FOR_NANO_TIME", "false" );
        String allowOnloadCalls    = System.getProperty( "ALLOW_ONLOAD_CALLS",    "true" );
        
        System.out.println( "ENV : SOCKET_FACTORY_CLASS "  + socketFactoryClass );
        System.out.println( "ENV : USE_NATIVE_LINUX "      + isUseLinuxNative );
        System.out.println( "ENV : USE_NATIVE_WINDOWS "    + isUseWindowsNative );
        System.out.println( "ENV : USE_TSC_FOR_NANO_TIME " + isUseTSCForNanoTime );
        System.out.println( "ENV : ALLOW_ONLOAD_CALLS "    + allowOnloadCalls );
        
        _socketFactoryClass  = socketFactoryClass;
        _isUseLinuxNative    = Boolean.parseBoolean( isUseLinuxNative );
        _isUseWindowsNative  = Boolean.parseBoolean( isUseWindowsNative );
        _isUseTSCForNanoTime = Boolean.parseBoolean( isUseTSCForNanoTime ); 
        _allowOnloadCalls    = Boolean.parseBoolean( allowOnloadCalls ); 
    }

    public static final String getSocketFactoryClass() {
        return _socketFactoryClass;
    }

    public static boolean isUseTSCForNanoTime() {
        return _isUseTSCForNanoTime;
    }

    public static boolean isUseLinuxNative() {
        return _isUseLinuxNative;
    }

    public static boolean isUseWindowsNative() {
        return _isUseWindowsNative;
    }
    
    public static boolean isAllowOnloadCalls() {
        return _allowOnloadCalls;
    }

    public static int getIntProperty( String property, int defVal ) {
        String val = System.getProperty( property, "" + defVal );
        return Integer.parseInt( val );
    }

    public static long getLongProperty( String property, long defVal ) {
        String val = System.getProperty( property, "" + defVal );
        return Long.parseLong( val );
    }
}
