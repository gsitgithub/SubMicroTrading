/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.log;

import java.util.Map;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;

public class ExceptionTrace {

    private static final ViewString CAUSED_BY        = new ViewString( "\nCaused by: " );
    private static final ViewString INDENT_AT        = new ViewString( "\tat " );
    private static final ViewString INDENT_COMMON    = new ViewString( "\t... " );
    private static final ViewString MORE             = new ViewString( " more\n" );
    private static final ViewString NATIVE           = new ViewString( "(Native Method)" );
    private static final ViewString INDENT_SPACE     = new ViewString( "      " );

    public static void format( ReusableString trace, Throwable t ) {
        if ( t != null ) {
            String msg = t.getMessage();
            
            if ( msg == null ) msg = t.toString();

            trace.append( (byte)0x0A ).append( msg ).append( (byte)0x0A );

            StackTraceElement[] stack = t.getStackTrace();

            for ( StackTraceElement entry : stack ) {
                addStackFrame( trace, entry );
            }

            Throwable causedBy = t.getCause();
            if ( causedBy != null ) {
                addStackTraceAsCause( trace, stack, causedBy );
            }
        }
    }

    public static void dumpStackTrace( ReusableString trace ) {
        Map<Thread, StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
        
        for ( Thread t : stackTraceMap.keySet() ) {
            trace.append( "Thread " ).append( t.getName() ).append( (byte)0x0A );
            StackTraceElement[] stackTrace = stackTraceMap.get( t );
            for ( StackTraceElement entry : stackTrace ) {
                addStackFrame( trace, entry );
            }
        }
    }

    // following code is from Throwable.printStackTrace with temp objects removed

    private static void addStackFrame( ReusableString buf, StackTraceElement entry ) {

        buf.append( INDENT_SPACE ).append( "at " ).append( entry.getClassName() ).append( '.' ).append( entry.getMethodName() );

        String file = entry.getFileName();
        int    line = entry.getLineNumber();

        if ( entry.isNativeMethod() ) {
            buf.append( NATIVE );
        } else {
            if ( file != null && line >= 0 ) {
                buf.append( '(' ).append( file ).append( ':' ).append( line ).append( ')' );
            } else if ( file != null ) {
                buf.append( '(' ).append( file ).append( ')' );
            }
        }

        buf.append( (byte)0x0A );
    }

    private static void addStackTraceAsCause( ReusableString buf, StackTraceElement[] from, Throwable causedBy ) {

        // Compute number of frames in common between this and caused
        StackTraceElement[] trace = causedBy.getStackTrace();
        
        int causedIdx = trace.length - 1;
        int fromIdx   = from.length - 1;
        
        while( causedIdx >= 0 && fromIdx >= 0 && trace[causedIdx].equals( from[fromIdx] ) ) {
            causedIdx--;
            fromIdx--;
        }
        
        int framesInCommon = trace.length - 1 - causedIdx;

        buf.append( CAUSED_BY ).append( causedBy.getClass().getName() ).append( ':' )
           .append( causedBy.getMessage() ).append( (byte)0x0A );

        for ( int i = 0 ; i <= causedIdx ; i++ ) {
            buf.append( INDENT_AT );
            addStackFrame( buf, trace[i] );
        }

        if ( framesInCommon != 0 ) {
            buf.append( INDENT_COMMON ).append( framesInCommon ).append( MORE );
        }

        Throwable ourCause = causedBy.getCause();

        if ( ourCause != null ) {
            // Recurse if we have a cause
            addStackTraceAsCause( buf, trace, ourCause ); 
        }
    }
}
