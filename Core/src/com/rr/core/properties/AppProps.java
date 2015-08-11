/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.rr.core.component.SMTComponent;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.ReflectUtils;
import com.rr.core.utils.SMTRuntimeException;

/**
 * load application properties files
 * 
 * when use include can now supply local properties to be directly replaced in the file read in
 * idea is to avoid repition of same component and allow it to be reused by supplying local properties that can be used in key and value before resolve happens
 * 
 * ${var}      is a variable resolved after all property files read in
 * %{localVar} is a local variable which was passed as part of the include command, substitution is immediate  
 *
 * local variables always converted to uppercase
 * 
 * include fastfixsession.properties ID=cme05 port=11223 host=138.42.121.18
 * include fastfixsession.properties ID=cme06 PORT=11224 HOST=138.42.121.18
 * 
 * within fastfixsession.properties
 * 
 * sess.up.fastfix.%{ID}.port=%{PORT}
 *
 * @author Richard Rose
 */
public class AppProps implements SMTComponent {
    private static final Logger             _log      = LoggerFactory.console( AppProps.class );
    private static final AppProps           _instance = new AppProps();
    
    private static final int                MAX_DEPTH = 10;
    private static final String             LOAD_FILE = "include ";
    private static final ErrorCode          FAIL_LOAD = new ErrorCode( "APP100", "Failed to load property file" );

    private              boolean            _init     = false;
    private              PropertyTags       _propSet  = CoreProps.instance();
    private              Map<String,String> _props    = new LinkedHashMap<String,String>();
    private              String             _id       = "AppProps";

    private              Map<String,String> _caseSavedProps = new LinkedHashMap<String,String>();

    private              String             _errs           = "";
    private              int                _err            = 0;
    
    private              Exception          _firstException = null;
    private              String             _topFile        = null;
    
    
    public static AppProps instance() { return _instance; }

    protected AppProps() {
        checkTagOverride( CoreProps.APP_TAGS, System.getProperty( CoreProps.APP_TAGS ) );
    }
    
    @Override
    public String getComponentId() {
        return _id;
    }
    
    public void init( String propertyFile ) throws Exception {
        _topFile = propertyFile;
        
        _firstException = null;
        _errs = "";
        _err = 0;

        if ( propertyFile != null ) {
            loadProps( propertyFile, new LinkedHashMap<String,String>()  );
        }
        resolveProps();
        
        if ( _firstException != null ) {
            _log.error( FAIL_LOAD, propertyFile + _errs );
            
            throw _firstException;
        }
        
        setInit();
    }

    public void init( String propertyFile, PropertyTags validNames ) throws Exception {
        
        setPropSet( validNames );
        if ( propertyFile != null ) {
            loadProps( propertyFile, new LinkedHashMap<String,String>() );
        }
        resolveProps();
        setInit();
    }

    public void init( AppProps other ) {
        setPropSet( other._propSet );
        _props.putAll( other._props );
        _caseSavedProps.putAll( other._caseSavedProps );
        resolveProps();
        setInit();
    }
    
    @Override
    public String toString() {
        ReusableString m = new ReusableString();
        
        for( Map.Entry<String, String> entry : _caseSavedProps.entrySet() ) {
            m.append( entry.getKey() ).append( "=" ).append( entry.getValue() ).append( "\n" ); 
        }
        
        return m.toString();
    }
    
    public final void setPropSet( PropertyTags validNames ) {
        _propSet = validNames;
    }
    
    protected final void setInit() {
        _init = true;
    }

    public String getProperty( String property ) {
        return validateAndGet( property, true );
    }

    public String getProperty( String property, boolean isMand, String defaultVal ) {
        String val = validateAndGet( property, isMand );
        
        if ( val == null  && defaultVal != null ) {
            _log.info( "AppProperties : defaulting " + property + " to " + defaultVal );
            val = defaultVal;
        }
        
        return val;
    }

    public boolean getBooleanProperty( String property, boolean isMand, boolean defaultVal ) {
        String val = validateAndGet( property, isMand );
        
        if ( val != null ) {
            boolean bVal;
            
            try {
                bVal = Boolean.parseBoolean( val );
            } catch( NumberFormatException e ) {
                throw new SMTRuntimeException( "AppProperties property " + property + " has invalid boolean (" + val + ")" ); 
            }
            
            return bVal;
        } 
        
        _log.info( "AppProperties : defaulting " + property + " to " + defaultVal );

        return defaultVal;
    }

    public int getIntProperty( String property, boolean isMand, int defaultVal ) {
        String val = validateAndGet( property, isMand );
        
        if ( val != null ) {
            int iVal;
            
            try {
                iVal = Integer.parseInt( val );
            } catch( NumberFormatException e ) {
                throw new SMTRuntimeException( "AppProperties property " + property + " has invalid int (" + val + ")" ); 
            }
            
            return iVal;
        } 

        _log.info( "AppProperties : defaulting " + property + " to " + defaultVal );

        return defaultVal;
    }

    public long getLongProperty( String property, boolean isMand, long defaultVal ) {
        String val = validateAndGet( property, isMand );
        
        if ( val != null ) {
            long lVal;
            
            try {
                lVal = Long.parseLong( val );
            } catch( NumberFormatException e ) {
                throw new SMTRuntimeException( "AppProperties property " + property + " has invalid long (" + val + ")" ); 
            }
            
            return lVal;
        } 

        _log.info( "AppProperties : defaulting " + property + " to " + defaultVal );

        return defaultVal;
    }

    
    private String validateAndGet( String property, boolean isMand ) {
        if ( !_init ) {
            throw new SMTRuntimeException( "Must initialise AppProperties before use" );
        }

        String finalTag = getFinalTag( property );

        boolean validateTag = true;
        
        try {
            // if its a number dont bother validating against propSet
            Integer.parseInt( finalTag );
            
            validateTag = false;
        } catch( NumberFormatException e ) {
            // ignore 
        }
        
        if ( validateTag ) {
            if ( ! _propSet.isValidTag( finalTag ) ) {
                throw new SMTRuntimeException( "AppProperties property " + finalTag + " (from " + property + ") is not in property set " + 
                                               _propSet.getSetName() );
            }
        }
        
        String val = _props.get( property.toLowerCase() );
        
        if ( val != null ) val = val.trim();
        
        if ( val == null || val.length() == 0 ) {
            if ( isMand ) throw new SMTRuntimeException( "AppProperties missing mand property " + property );
            
            return null;
        }
        
        return val;
    }

    private String getFinalTag( String property ) {
        if ( property == null ) return null;
        
        int idx = property.lastIndexOf( '.' );
        if ( idx >= 0 && idx == (property.length()-1) ) {
            return property; 
        }
        
        return (idx==-1) ? property : property.substring( idx+1 );
    }

    private void loadProps( String propertyFile, Map<String, String> localProps ) throws Exception {
        File propFile = new File( propertyFile.trim() );
        
        if ( ! propFile.isFile() || ! propFile.canRead() ) {
            throw new SMTRuntimeException( "Property file doesnt exist or is not readable " + propertyFile );
        }
        
        BufferedReader reader = null;

        int lineNo = 0;
        
        synchronized( AppProps.class ) {
                    
            _log.info( "AppProperties loading from " + propertyFile );
            
            reader = new BufferedReader( new FileReader( propertyFile ) );

            for ( String line = reader.readLine() ; line != null ; line = reader.readLine() ) {

                try {
                    procLine( line, propertyFile, ++lineNo, localProps  );
                } catch( Exception e ) {
                    if ( _firstException == null ) _firstException = e;
                    
                    _errs += "\n\nERR #" + (++_err) + " [" + propertyFile + ":" + lineNo + "] - " + e.getMessage();
                }
            }
            
            FileUtils.close( reader );
        }
    }

    protected final void resolveProps() {
        for( Map.Entry<String,String> entry : _props.entrySet() ) {
            String value    = entry.getValue();
            
            value = resolve( value, 0 );
            entry.setValue( value );
        }

        for( Map.Entry<String,String> entry : _caseSavedProps.entrySet() ) {

            String property = entry.getKey();
            String value    = entry.getValue();
            
            value = resolve( value, 0 );
            entry.setValue( value );
            
            _log.info( "AppProperties set   " + property + "=" + value );
        }
    }

    protected final void procLine( String line, String propertyFile, int lineNo, Map<String,String> localProps  ) throws Exception {
        if ( line.startsWith( "#" ) )
            return;
        
        String req = line.trim();
        
        if ( req.length() > 0 ) {
            
            if ( req.startsWith( LOAD_FILE ) ) {
                req = checkLocalVarSubst( req, localProps, false );

                String postInclude = req.substring( LOAD_FILE.length() ).trim();
                
                if ( postInclude.length() > 0 ) {
                    String[] parts = postInclude.split( " +" );
                    
                    String includeFile = resolve( parts[0].trim(), 0 );
                    
                    // new local properties defaults to current properties and then allows override
                    Map<String,String> newLocalProps = new LinkedHashMap<String,String>( localProps );
                    
                    for( int i=1 ; i < parts.length ; i++ ) {
                        String[] keyVal = parts[i].split( "=" );
                        if ( keyVal.length == 2 ) {
                            String key = keyVal[0].trim().toUpperCase();
                            String val = keyVal[1].trim();
                            
                            if ( key.length() > 0 && val.length() > 0 ) {
                                newLocalProps.put( key, val );
                            } else {
                                throw new SMTRuntimeException( "Bad include line local property must be key=value, idx=" + i + ", entry=[" + parts[i] + "] line=" + line );
                            }
                        }
                    }
                    
                    loadProps( includeFile, newLocalProps );
                }
                
                return;
            }
            
            req = checkLocalVarSubst( req, localProps, true );
            
            String[] split = req.split( "=" );
            
            if ( split.length == 2 ) {
                String property = split[0].trim();
                String val = split[1].trim();

                checkTagOverride( property, val );
                
                // val can be a variable (ie no "." seperators or a property with "." seperators)
                String finalTag = getFinalTag( property );
                
                if ( property != finalTag && ! property.startsWith( "map." ) && ! _propSet.isValidTag( finalTag ) ) {
                    throw new InvalidPropertyException( "AppProperties property " + finalTag + " (from " + property + 
                                                        ") is not in property set " + _propSet.getSetName() );
                }

                _props.put( property.toLowerCase(), val );
                _caseSavedProps.put( property, val );
            } else if ( split.length == 1 && req.charAt( req.length()-1 ) == '=' ) {
                String property = split[0].trim();

                // val can be a variable (ie no "." seperators or a property with "." seperators)
                String finalTag = getFinalTag( property );
                
                if ( property != finalTag && ! _propSet.isValidTag( finalTag ) ) {
                    throw new SMTRuntimeException( "AppProperties property " + finalTag + " (from " + property + 
                                                   ") is not in property set " + _propSet.getSetName() );
                }

                _props.put( property.toLowerCase(), "" );
                _caseSavedProps.put( property, "" );
            } else {
                _log.info( "AppProperties.loadProps " + propertyFile + ", lineNo=" + lineNo + ", skip bad line with " + split.length + " parts : " + line );
            }
        }
    }
    
    private String checkLocalVarSubst( String value, Map<String,String> localProps, boolean resolve ) {
        if ( value == null ) return null;
        
        if ( resolve ) {
            value = resolve( value, 0 ); 
        }
        
        int lastIdx     = 0;
        int varStartIdx = value.indexOf( "%{" );
        
        if ( varStartIdx == -1 ) return value;

        String val = "";
        
        do {
            int varEndIdx = value.indexOf( '}', varStartIdx );
                
            if ( varEndIdx == -1 ) {
                val += value.substring( lastIdx );
                break;
            }
            
            val += value.substring( lastIdx, varStartIdx );
            
            String var = value.substring( varStartIdx+2, varEndIdx ).toUpperCase();
            String varVal = localProps.get( var );
            if ( varVal == null ){
                throw new SMTRuntimeException( "Configuration error local property %{"+ var + "} is not defined, must be passed on include line invocation" );
            }
            
            val += varVal;
            
            lastIdx = varEndIdx + 1;
            
            if ( lastIdx >= value.length() ) break; // macro was at end of value
            
            varStartIdx = value.indexOf( "%{", lastIdx );
            
        } while( varStartIdx > 0 );

        if ( lastIdx < value.length() ) {
            val += value.substring( lastIdx );
        }
        
        return val;
    }

    private void checkTagOverride( String property, String val ) {
        if ( val != null && val.trim().length() > 0 && CoreProps.APP_TAGS.equals( property ) ) {
            PropertyTags props = ReflectUtils.findInstance( val.trim() );
            setPropSet( props );
        }
    }

    protected void put( String key, String value ) {
        _props.put( key.toLowerCase(), value );
        _caseSavedProps.put( key, value );
    }

    private String resolve( String value, int depth ) {
        if ( value == null ) return null;
        
        ++depth;
        
        if ( depth > MAX_DEPTH ) throw new SMTRuntimeException( "Config recursive reference error with " + value );
        
        int lastIdx     = 0;
        int varStartIdx = value.indexOf( "${" );
        
        if ( varStartIdx == -1 ) return value;

        String val = "";
        
        do {
            int varEndIdx = value.indexOf( '}', varStartIdx );
                
            if ( varEndIdx == -1 ) {
                val += value.substring( lastIdx );
                break;
            }
            
            val += value.substring( lastIdx, varStartIdx );
            
            String var = value.substring( varStartIdx+2, varEndIdx ).toLowerCase();
            String varVal = _props.get( var );
            if ( varVal == null ) {
                throw new SMTRuntimeException( "Configuration error ${"+ value.substring( varStartIdx+2, varEndIdx ) + "} is not defined" );
            }
            
            val += resolve( varVal.trim(), depth );        // *** RECURSE ***
            
            lastIdx = varEndIdx + 1;
            
            if ( lastIdx >= value.length() ) break; // macro was at end of value
            
            varStartIdx = value.indexOf( "${", lastIdx );
            
        } while( varStartIdx > 0 );
        
        if ( lastIdx < value.length() ) {
            val += value.substring( lastIdx );
        }
        
        return val;
    }

    public String[] getNodes( String propertyBase ) {
        String[]    parts    = propertyBase.split( "\\." );
        int         depth    = parts.length;
        Set<String> nodes    = new LinkedHashSet<String>();
        
        for ( String key : _props.keySet() ) {
            String[] keyParts = key.split( "\\." );
            int keyDepth = keyParts.length;
            
            if ( keyDepth > depth && startsWithIgnoreCase( key, propertyBase ) ) {
                String finalTag = keyParts[ depth ]; // arrays are 0 indexed 
                
                nodes.add( finalTag );
            }
        }
        
        return nodes.toArray( new String[ nodes.size() ] );
    }

    public String[] getNodesWithCaseIntact( String propertyBase ) {
        String[]    parts    = propertyBase.split( "\\." );
        int         depth    = parts.length;
        Set<String> nodes    = new LinkedHashSet<String>();
        
        for ( String key : _caseSavedProps.keySet() ) {
            String[] keyParts = key.split( "\\." );
            int keyDepth = keyParts.length;
            
            if ( keyDepth > depth && startsWithIgnoreCase( key, propertyBase ) ) {
                String finalTag = keyParts[ depth ]; // arrays are 0 indexed 
                
                nodes.add( finalTag );
            }
        }
        
        return nodes.toArray( new String[ nodes.size() ] );
    }

    private boolean startsWithIgnoreCase( String fullKey, String base ) {
        return fullKey.toLowerCase().startsWith( base.toLowerCase() );
    }

    public String getFile() {
        return _topFile;
    }
}
