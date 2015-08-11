/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import java.io.BufferedReader;
import java.io.FileReader;

import com.rr.core.os.NativeHooks;
import com.rr.core.os.NativeHooksImpl;

public class ThreadUtils {

    public  static final String  DEFAULT_THREAD_CONFIG  = "./config/cpumasks.cfg";
    
    public static void init( String fileName ){
        
        if ( fileName == null ) {
            return;
        }
        
        // @NOTE THIS CODE MUST NOT USE LOGGING BUT SYSTEM.OUT AS THIS CODE WILL BE USED BEFORE POOLING SETUP
        BufferedReader reader = null;
        
        synchronized( ThreadUtils.class ) {
            try {
                System.out.println( "ThreadUtils loading thread masks from " + fileName );
                
                reader = new BufferedReader( new FileReader( fileName ) );
    
                for ( String line = reader.readLine() ; line != null ; line = reader.readLine() ) {
    
                    if ( line.startsWith( "#" ) ) continue;
                    
                    String req = line.trim();
                    
                    if ( req.length() > 0 ) {
                        String[] split = req.split( "\\s+" );
                        
                        if ( split.length == 3 ) {
                            ThreadPriority p = ThreadPriority.valueOf( split[0] );
                            
                            if ( p != null ) {
                                p.setPriority( Integer.parseInt( split[1] ) );
                                p.setMask( Integer.parseInt( split[2] ) );
                                
                                System.out.println( "ThreadUtils set " + split[0] + " to priority " + split[1] + ", mask " + split[2] );
                            }
                        } else {
                            System.out.println( "ThreadUtils.init skip bad line with " + split.length + " parts : " + line );
                        }
                    }
                }
            } catch( Exception e ) {
                System.out.println( "Failed to process Thread Mask file " + fileName + " : " + e.getMessage() );
            } finally {
                FileUtils.close( reader );
            }
        }
        
        sendStarted();
    }

    private static void sendStarted() {
        // @TODO generate email for sys start
    }

    public static void init( String fileName, boolean isDebug ) {
        
        for( ThreadPriority priority : ThreadPriority.values() ) {
            priority.setMask( SchedulingPriority.UNKNOWN_MASK );
        }

        init( fileName );

        ThreadPriority other = ThreadPriority.Other;
        
        for( ThreadPriority priority : ThreadPriority.values() ) {
            if ( priority.getMask() == SchedulingPriority.UNKNOWN_MASK ) {
                System.out.println( "WARNING: no config entry for " + priority.toString() + ", setting cpu mask to OTHER : " + other.getMask() );
                priority.setMask( other.getMask() );
            }
        }
    }
    
    public static void setPriority( Thread thread, ThreadPriority priority ) {

        int javaPriority = 5;
        
        switch( priority.getPriority() ) {
        case ThreadPriority.LOWEST:
            javaPriority = 1;
            break;
        case ThreadPriority.LOW:
            javaPriority = 3;
            break;
        case ThreadPriority.MEDIUM:
            javaPriority = 5;
            break;
        case ThreadPriority.HIGH:
            javaPriority = 8;
            break;
        case ThreadPriority.HIGHEST:
            javaPriority = NativeHooks.MAX_PRIORITY;
            break;
        }
        
        int mask = findMask( priority );

        if ( mask > 0 ) {
            System.out.println( "ThreadUtils.setPriority() thread " + thread.getName() + ", priority " + priority.toString() + 
                                ", javaPriority=" + javaPriority + ", cpumask=" + mask );
            
            System.out.flush();
    
            NativeHooksImpl.instance().setPriority( thread, mask, javaPriority );
        } else {
            System.out.println( "ThreadUtils.setPriority() *** NO CPU MASK ***  for thread " + thread.getName() + ", priority " + priority.toString() + 
                                ", javaPriority=" + javaPriority + ", cpumask=" + mask );
            
            System.out.flush();
        }
    }

    public static void setPriority( Thread thread, int priority, int mask ) {

        if ( mask > 0 ) {
            int javaPriority = 5;
            
            switch( priority ) {
            case ThreadPriority.LOWEST:
                javaPriority = 1;
                break;
            case ThreadPriority.LOW:
                javaPriority = 3;
                break;
            case ThreadPriority.MEDIUM:
                javaPriority = 5;
                break;
            case ThreadPriority.HIGH:
                javaPriority = 8;
                break;
            case ThreadPriority.HIGHEST:
                javaPriority = NativeHooks.MAX_PRIORITY;
                break;
            }
            
            //TODO use JNI to force priority
    
            System.out.println( "ThreadUtils.setPriority() thread " + thread.getName() + ", priority " + priority + 
                                ", javaPriority=" + javaPriority + ", cpumask=" + mask );
            
            System.out.flush();
            
            NativeHooksImpl.instance().setPriority( thread, mask, javaPriority );
        }
    }

    private static int findMask( ThreadPriority priority ) {
        int mask = priority.getMask();

        // note removed all clever mask generation, must now rely on mask file
        
        return mask;
    }

    public static void pause( int ms ) {
        try {
            Thread.sleep( ms );
        } catch( InterruptedException e ) { /* NADA */ }
    }
}
