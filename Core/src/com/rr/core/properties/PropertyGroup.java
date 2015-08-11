/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.properties;

import java.lang.reflect.Field;
import java.util.Set;

import com.rr.core.properties.PropertyTags.Tag;
import com.rr.core.utils.ReflectUtils;
import com.rr.core.utils.SMTRuntimeException;

public class PropertyGroup {
    private final AppProps      _appProps;
    private final String        _propertyGroup;
    private final String        _majorDefaultGroup;
    private final String        _minorDefaultGroup;

    public PropertyGroup( String propertyGroup, String majorDefaultGroup, String minorDefaultGrou ) {
        this( AppProps.instance(), propertyGroup, majorDefaultGroup, minorDefaultGrou );
    }
    
    public PropertyGroup( AppProps appProps, String propertyGroup, String majorDefaultGroup, String minorDefaultGroup ) {
        super();
        _appProps = appProps;
        if ( propertyGroup.charAt( propertyGroup.length()-1 ) != '.' ) propertyGroup += ".";
        if ( majorDefaultGroup != null && majorDefaultGroup.charAt( majorDefaultGroup.length()-1 ) != '.' ) majorDefaultGroup += ".";
        if ( minorDefaultGroup != null && minorDefaultGroup.charAt( minorDefaultGroup.length()-1 ) != '.' ) minorDefaultGroup += ".";
        _propertyGroup = propertyGroup;
        _majorDefaultGroup = majorDefaultGroup;
        _minorDefaultGroup = minorDefaultGroup;
    }

    @Override
    public String toString() {
        return _propertyGroup;
    }
    
    public String getProperty( Tag propTag ) {
        return getProperty( propTag, true, null );
    }
    
    public String getProperty( Tag propTag, boolean isMand, String defaultVal ) {
            
        String val = getProperty( _propertyGroup, propTag );
        
        if ( val == null ) {
            val = getProperty( _majorDefaultGroup, propTag );
        }

        if ( val == null ) {
            val = getProperty( _minorDefaultGroup, propTag );
        }
        
        if ( val == null ) {
            if ( isMand ) throw new SMTRuntimeException( "Missing property " + _propertyGroup + propTag.toString() );
            
            val = defaultVal;
        }
        
        return val;
    }
    
    public String getPropertyGroup() {
        return _propertyGroup;
    }

    private String getProperty( String propGroup, Tag propTag ) {
        if ( propGroup == null || propTag == null ) return null;
        
        String prop = propGroup + propTag.toString();
        return _appProps.getProperty( prop, false, null );
    }

    /**
     * iterate over every member field of propHolder and IF it has a setter invoke with appropriate value IF matching prop found
     * 
     * use the AppProperty.setObjectProperties() where possible as its stricter
     * 
     * @param propHolder
     */
    public void reflectSet( PropertyTags app, Object propHolder ) {
        Set<Field> fields = ReflectUtils.getMembers( propHolder );
        
        for( Field f : fields ) {
            String fieldName = f.getName();
            
            if ( fieldName.charAt( 0 ) == '_' ) fieldName = fieldName.substring( 1 );
            
            if ( app.isValidTag( fieldName ) ) {
                Tag tag = app.lookup( fieldName );
                
                String value = getProperty( tag, false, null );
                
                if ( value != null ){
                    ReflectUtils.setMember( propHolder, f, value );
                }
            }
        }
    }

    public int getIntProperty( Tag tag, boolean isMand, int defVal ) {
        String val = getProperty( tag, isMand, null );
        
        if ( val != null ) {
            int iVal;
            
            try {
                iVal = Integer.parseInt( val );
            } catch( NumberFormatException e ) {
                throw new SMTRuntimeException( "PropertyGroup property " + tag.toString() + " has invalid int (" + val + ")" ); 
            }
            
            return iVal;
        } 

        return defVal;
    }

    public float getFloatProperty( Tag tag, boolean isMand, float defVal ) {
        String val = getProperty( tag, isMand, null );
        
        if ( val != null ) {
            float fVal;
            
            try {
                fVal = Float.parseFloat( val );
            } catch( NumberFormatException e ) {
                throw new SMTRuntimeException( "PropertyGroup property " + tag.toString() + " has invalid float (" + val + ")" ); 
            }
            
            return fVal;
        } 

        return defVal;
    }

    public boolean getBoolProperty( Tag tag, boolean isMand, boolean defVal ) {
        String val = getProperty( tag, isMand, null );
        
        if ( val != null ) {
            boolean bVal;
            
            try {
                bVal = Boolean.parseBoolean( val );
            } catch( NumberFormatException e ) {
                throw new SMTRuntimeException( "PropertyGroup property " + tag.toString() + " has invalid boolean (" + val + ")" ); 
            }
            
            return bVal;
        } 
        
        return defVal;
    }

    public long getLongProperty( Tag tag, boolean isMand, long defVal ) {
        String val = getProperty( tag, isMand, null );
        
        if ( val != null ) {
            long lVal;
            
            try {
                lVal = Long.parseLong( val );
            } catch( NumberFormatException e ) {
                throw new SMTRuntimeException( "PropertyGroup property " + tag.toString() + " has invalid int (" + val + ")" ); 
            }
            
            return lVal;
        } 
        
        return defVal;
    }
}
