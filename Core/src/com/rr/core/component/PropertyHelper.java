/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.component;

import com.rr.core.properties.AppProps;
import com.rr.core.properties.PropertyGroup;
import com.rr.core.properties.PropertyTags.Tag;
import com.rr.core.utils.SMTRuntimeException;


public class PropertyHelper {

    public static <T extends Enum<T>> T getProperty( AppProps prop, 
                                                     String   tag, 
                                                     Class<T> enumClass,
                                                     T        defaultVal ) {
        String val = prop.getProperty( tag, false, null );
        
        if ( val == null || val.length() == 0 ) {
            return defaultVal;
        }
        
        T tVal;
        try {
            tVal = Enum.valueOf( enumClass, val );
        } catch( Exception e ) {
            throw new SMTRuntimeException( "BadProperty, " + tag + " value (" + val + 
                                           ") is not valid entry for " + enumClass.getCanonicalName() );
        }
          
        return tVal;
    }

    public static <T extends Enum<T>> T getProperty( PropertyGroup prop, 
                                                     Tag           propTag, 
                                                     Class<T>      enumClass ) {
        String val = prop.getProperty( propTag );
          
        T tVal;
        try {
            tVal = Enum.valueOf( enumClass, val );
        } catch( Exception e ) {
            throw new SMTRuntimeException( "BadProperty, " + prop.getPropertyGroup() + "." + propTag.toString() + " value (" + val + 
                                           ") is not valid entry for " + enumClass.getCanonicalName() );
        }
          
        return tVal;
    }

    public static <T extends Enum<T>> T getProperty( String property, Class<T> enumClass, T defVal ) {
        String val = AppProps.instance().getProperty( property, false, null );
          
        if ( val == null ) return defVal;
        
        T tVal;
        try {
            tVal = Enum.valueOf( enumClass, val );
        } catch( Exception e ) {
            throw new SMTRuntimeException( "BadProperty, " + property + " value (" + val + 
                                           ") is not valid entry for " + enumClass.getCanonicalName() );
        }
          
        return tVal;
    }


}
