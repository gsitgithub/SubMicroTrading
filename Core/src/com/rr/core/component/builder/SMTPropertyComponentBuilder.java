/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.component.builder;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rr.core.component.SMTComponent;
import com.rr.core.component.SMTComponentWithPostConstructHook;
import com.rr.core.component.SMTMultiComponentLoader;
import com.rr.core.component.SMTSingleComponentLoader;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.properties.AppProps;
import com.rr.core.utils.ReflectUtils;
import com.rr.core.utils.SMTException;
import com.rr.core.utils.SMTRuntimeException;


/**
 * Component Builder : Instantiate components from property file
 * 
 * Must be called after initiating logger
 * 
 * components can be either directly instantiated or instantiated by a custom component loader
 * 
 * components must implement SMTComponent interface
 * 
 * component identifier is the second part of the component chain, eg componentId1, componentId2 are identifiers that can be used in type "ref" for references
 * 
 * Component loaders should have property member vars setup for all required config so loaders are abstracted from source of wiring
 * The reflective loader will check type of member and if not simple type will assume its a reference so will assume value is a componentId 
 * 
 * @NOTE the loader will autowire missing property references of SMTComponent/SMTLoader where the member var name matches the componentId 
 * @WARNING can only autowire references to components already defined (ie already added to the components map)
 * 
 * To avoid typing errors, each propertyTag must be valid member of AppProps
 * (propertyTag is the last element in property ie from a.b.c only c is a propertyTag)
 * 
    component.componentId1.loader=com.rr.core.loaders.FixSessionLoader
    #avoid duplicating config by loading up some default properties using recursion
    component.componentId1.properties.defaultProperties=session.default.up.properties
    component.componentId1.properties.file=./data/cme/secdef.t1.dat
    component.componentId1.properties.codecId=CME
    component.componentId1.properties.port=1234
    component.componentId1.properties.host=127.0.0.1
    component.componentId1.properties.instMgr=instMgrComponentID
    
    component.componentId2.className=com.rr.core.SomeClass
     # example of arguments for constructor, default type is String
     # type can be   ref|long|int|string|zstring|double|{className}|{arrayClassName}
     # for type "ref" the value is a componentID
     # arguments index must start at 1 and match constructor arg .. arg0 is reserved for the componentId
    component.componentId2.arg.1.value=./data/cme/secdef.t1.dat
    component.componentId2.arg.2.type=int
    component.componentId2.arg.2.value=99
     # example of property set via reflection after instantiation
    component.componentId2.properties.someProperty=someValue
*    
* format for COMPONENT property identifiers is 
* component.{componentId}.[className|loader]  
* component.{componentId}.properties.{propertyName}
* where componentId and propertyName must be a single string with no periods  
* ie  c1.properties.p1.p2=XX is invalid
* 
* Note for a constructor arg using an array of components you should set the type to the java name for the array class
* eg component.cid4.arg.1.type=[Lcom.rr.core.component.TestPropertyComponentBuilder$IntermediateInterface;
* The [L  must be before the class name and must be terminated with semi-colon
* 
* @NOTE should use contructor args in pref to properties for best perf as var can then be immutable
*/

public class SMTPropertyComponentBuilder {
    
    private static final String DEFAULT_PROPERTIES = "defaultProperties";

    private static final int MAX_RECURSE_CONFIG = 5;

    private static final ErrorCode ERR_COMP = new ErrorCode( "PCB100", "Error building component" );

    private static final int MAX_DEPENDENY_DEPTH = 20;
    
    private final Logger    _log = LoggerFactory.console( SMTPropertyComponentBuilder.class );
    private final AppProps  _props;
    
    private final Map<String, SMTComponent> _components = new LinkedHashMap<String,SMTComponent>();
    private final Map<String, Map<?,?>>     _maps       = new LinkedHashMap<String,Map<?,?>>();
    
    private       boolean _initialised = false;

    private final Map<SMTComponent, Set<SMTComponent>> _dependants = new LinkedHashMap<SMTComponent, Set<SMTComponent>>();

    /**
     * ordered set of components, with all a components dependencies appearing before it
     */
    private final LinkedHashSet<SMTComponent> _orderedComponents = new LinkedHashSet<SMTComponent>();

    private int _nextIdx = 0;
    
    public SMTPropertyComponentBuilder( AppProps props ) {
        super();
        _props     = props;
    }

    public synchronized void init() {
        if ( ! _initialised ) {
            _log.info( "SMTPropertyComponentBuilder.init()" );
    
            loadMaps();
            instantiateComponents();
            findComponentDependencies();
            orderComponentsBasedOnDependencies();
            verify();
        }
        
        _initialised = true;
    }

    @Override
    public String toString() {
        ReusableString s = new ReusableString( 8192 );
        
        for( SMTComponent c : _orderedComponents ) {
            String lc = c.getComponentId();

            s.append( lc ).append( " : " ).append( c.toString() ).append( "\n" );
        }
        
        return s.toString();
    }
    
    private void verify() {
        
        // check for case mismatch on componentIds
        Map<String, SMTComponent> ids = new HashMap<String,SMTComponent>();
        for( SMTComponent c : _orderedComponents ) {
            String lc = c.getComponentId().toLowerCase();

            SMTComponent existing = ids.get( lc );
            
            if ( existing != null ) {
                throw new SMTRuntimeException( "Component id " + lc + " has case mismatched entries, eg " + 
                                               c.getComponentId() + " vs " + existing.getComponentId() );
            }
            
            ids.put( lc, c );
        }

        for( SMTComponent c : _components.values() ) {
            if ( ! _orderedComponents.contains( c ) ) {
                throw new SMTRuntimeException( "Component id " + c.getComponentId() + " is missing in ordered list" );
            }
        }
    }

    private void orderComponentsBasedOnDependencies() {
        
        int level = 1;
        
        LinkedHashSet<SMTComponent> stacked = new LinkedHashSet<SMTComponent>();

        // force all components with no dependencies to appear first
        for( SMTComponent c : _dependants.keySet() ) {
            if ( c == null ) {
                throw new SMTRuntimeException( "Null component : should never happen" );
            }
            
            Set<SMTComponent>   dependencies = _dependants.get( c );
            if ( dependencies == null || dependencies.size() == 0 ) {
                _orderedComponents.add( c );
            }
        }
        
        for( SMTComponent c : _dependants.keySet() ) {
            addComponentAfterDependencies( _orderedComponents, stacked, c, level );
        }
        
        int i=1;
        
        for( SMTComponent c : _orderedComponents ) {
            _log.info( "Component Order # " + (i++) + " : " + c.getComponentId() );
            
            if ( c.getComponentId() == null ) {
                throw new SMTRuntimeException( "Null componentId in class " + c.getClass().getSimpleName() );
            }
        }
    }

    private void addComponentAfterDependencies( LinkedHashSet<SMTComponent> initialiseOrder, LinkedHashSet<SMTComponent> stacked, SMTComponent c, int level ) {
        
        if ( ! _components.values().contains( c ) ) {
            return; // not all SMTComponents are configured some are owned by enclosing components, these sub components must be ignores
        }
        
        if ( initialiseOrder.contains( c ) ) {
            return; // already done this component
        }
        
        Set<SMTComponent>   dependencies = _dependants.get( c );
        
        String dStr = "";
        
        if ( dependencies != null ) {
            for( SMTComponent d : dependencies ) {
                if ( dStr.length() > 0 ) {
                    dStr += ", ";
                }
                
                dStr += d.getComponentId();
            }
        }
        
        if ( dStr.length() == 0 ) dStr = "NOTHING";
        
        _log.info( "Component " + c.getComponentId() + " depends on " + dStr );

        if ( level > MAX_DEPENDENY_DEPTH ) {
            throw new SMTRuntimeException( "Exceeded max recurive depth trying to determine order of components, depth=" + level + " on "  + c.getComponentId() );
        }

        stacked.add( c ); // protect against back references by keeping track of all components in stack 
        
        if ( dependencies != null ) {
            for( SMTComponent d : dependencies ) {
                if ( d == null ) {
                    throw new SMTRuntimeException( "Null Component Dependency .. shouldnt be possible" );
                }
                
                if ( ! initialiseOrder.contains( d ) && d != c && ! stacked.contains( d ) ) {
                    addComponentAfterDependencies( initialiseOrder, stacked, d, level+1 );
                }
            }
        }
        
        stacked.remove( c );
        
        initialiseOrder.add( c );
    }

    private void findComponentDependencies() {
        for( SMTComponent c : _components.values() ) {
            addToDependencies( c );
        }
    }

    private void addToDependencies( SMTComponent c ) {
        
        getDependenciesForComponent( c );
        
        Set<Field> fields = ReflectUtils.getMembers( c );
        
        for( Field f : fields ) {
            Class<?> type = f.getType();
            
            boolean wasAccessable = f.isAccessible();
            try {
                f.setAccessible( true );
                Object val = f.get( c );
                
                if ( val != null && SMTComponent.class.isAssignableFrom( type ) ) {
                    SMTComponent dependantOn = (SMTComponent) val;
                    
                    link( c, dependantOn );
                    
                } else if ( val != null && type.isArray() ) {
                    Object[] vals = (Object[]) f.get( c );
                    
                    for( Object o : vals ) {
                        if ( o instanceof SMTComponent ) {
                            link( c, (SMTComponent) o );
                        }
                    }
                }
                
            } catch( Exception e ) {
                // swallow
            } finally {
                f.setAccessible( wasAccessable );
            }
        }
            
    }

    private void link( SMTComponent c, SMTComponent dependantOn ) {
        if ( c == null ) {
            throw new SMTRuntimeException( "Null component " );
        }
        
        Set<SMTComponent> componentDependencies = getDependenciesForComponent( c );

        componentDependencies.add( dependantOn );
    }

    private Set<SMTComponent> getDependenciesForComponent( SMTComponent c ) {
        Set<SMTComponent> componentDependencies = _dependants.get( c );
        
        if ( componentDependencies == null ) {
            componentDependencies = new LinkedHashSet<SMTComponent>();
            
            _dependants.put( c, componentDependencies );
        }
        
        return componentDependencies;
    }

    private void loadMaps() {
        String base = "map.";
        String[] compIds = _props.getNodesWithCaseIntact( base );

        String    firstId = "";
        Exception first = null;

        for( int i=0 ; i < compIds.length ; ++i ) {
            String id = compIds[i];
            
            try {
                loadMap( id, base + id + "." );
            } catch( Exception e ) {
                _log.error( ERR_COMP, "Map Load " + id + " FAIL " + e.getMessage() );
                
                if ( first == null ) {
                    firstId = id;
                    first = e;
                }
            }
        }

        if ( first != null ) {
            throw new SMTRuntimeException( "FIRST map load failure " + firstId + " : " + first.getMessage(),first );
        }
        
        _log.info( "SMTPropertyComponentBuilder.initialised " + compIds.length + " components" );
    }

    private void loadMap( String id, String baseProps ) {
        String keyType = _props.getProperty( baseProps + "keyType", false, "java.lang.String" );
        String valType = _props.getProperty( baseProps + "valType", false, "java.lang.String" );

        if ( ! keyType.equals( "java.lang.String") || ! valType.equals( "java.lang.String") ) {
            throw new SMTRuntimeException( "Not yet implemented,only support String maps, id=" + id);
        }

        Map<String,String> map = new LinkedHashMap<String,String>();
        
        // map.{mapId}.entry.{idx}={key}|{value}

        String argBase = baseProps + "entry.";
        String[] argIdxs = _props.getNodesWithCaseIntact( argBase );
        
        int numArgs = argIdxs.length + 1;
        
        for( int i=1 ; i < numArgs ; i++ ) {
            int matchIdx = Integer.parseInt( argIdxs[i-1] );
            
            if ( matchIdx != i ) {
                throw new SMTRuntimeException( "Bad config in map " + id + ", next arg expected " + i + ", but got " + matchIdx );
            }
            
            String entry = _props.getProperty( argBase + matchIdx );
            String[] parts = entry.split( "\\|" );
            if ( parts.length != 2 ) {
                throw new SMTRuntimeException( "Bad config in map " + id + ", idx=" + i + ", expected {key}|{val}  not [" + entry + "]" );
            }
            
            map.put( parts[0].trim(), parts[1].trim() );
        }
        
        if ( _maps.put( "map." + id.toLowerCase(), map ) != null ) {
            throw new SMTRuntimeException( "Duplicate map in config " + id );
        }
    }

    private void instantiateComponents() {
        String base = "component.";
        String[] compIds = _props.getNodesWithCaseIntact( base );

        String    firstId = "";
        Exception first = null;

        for( int i=0 ; i < compIds.length ; ++i ) {
            String id = compIds[i];
            
            try {
                init( id, base + id + "." );
            } catch( Exception e ) {
                _log.error( ERR_COMP, "Component " + id + " FAIL " + e.getMessage() );
                
                if ( first == null ) {
                    firstId = id;
                    first = e;
                }
            }
        }

        if ( first != null ) {
            throw new SMTRuntimeException( "FIRST component failure " + firstId + " : " + first.getMessage(),first );
        }
        
        _log.info( "SMTPropertyComponentBuilder.initialised " + compIds.length + " components" );
    }

    private void init( String id, String baseProps ) throws SMTException {
        String className = _props.getProperty( baseProps + "className", false, null );
        
        if ( className != null ) {
            directInstantiate( id, baseProps, className );
        } else {
            String loader = _props.getProperty( baseProps + "loader" );
            
            instantiateViaLoader( id, baseProps, loader );
        }
    }

    private void directInstantiate( String id, String baseProps, String className ) {
        _log.info( "SMTPropertyComponentBuilder.directInstantiate " + nextIdx() + " : " + id );

        /*
            component.componentId2.className=com.rr.core.SomeClass
             # example of arguments for constructor, default type is String
             # type can be   ref|long|int|string|double
             # for type "ref" the value is a componentID
            component.componentId2.arg.1.value=./data/cme/secdef.t1.dat
            component.componentId2.arg.2.type=int
            component.componentId2.arg.2.value=99
             # example of property set via reflection after instantiation
            component.componentId1.properties.someProperty=someValue
         */
        
        String argBase = baseProps + "arg.";
        String[] argIdxs = _props.getNodesWithCaseIntact( argBase );
        
        SMTComponent c = null;
        
        int numArgs = argIdxs.length + 1;
        
        Object[]   argVals    = new Object[ numArgs ];
        Class<?>[] argClasses = new Class<?>[ numArgs ];

        // first constructor argument is for the componentId
        argVals[0]    = id;
        argClasses[0] = String.class;
        
        for( int i=1 ; i < numArgs ; i++ ) {
            int matchIdx = Integer.parseInt( argIdxs[i-1] );
            
            if ( matchIdx != i ) {
                throw new SMTRuntimeException( "Bad config in component " + id + ", next arg expected " + i + ", but got " + matchIdx );
            }
            
            addConstructorArg( false, argVals, argClasses, i, argBase + i + "." );
        }
        
        try {
            c= ReflectUtils.create( className, argClasses, argVals );
            
        } catch( SMTRuntimeException e ) { // failed to instantiate, retry forcing untyped references to use type SMTComponent
            for( int i=1 ; i < numArgs ; i++ ) {
                addConstructorArg( true, argVals, argClasses, i, argBase + i + "." );
            }

            try {
                c= ReflectUtils.create( className, argClasses, argVals );
            } catch( SMTRuntimeException e1 ) {
                throw new SMTRuntimeException( e.getMessage() + "\nAlso failed with : " + e1.getMessage(), e );
            }
        }

        setComponentProperties( c, id,  baseProps + "properties." );
        
        if ( ! c.getComponentId().equalsIgnoreCase( id ) ) {
            throw new SMTRuntimeException( "componentId of component doesnt match config, configId=" + id + ", instanceId=" + c.getComponentId() );
        }

        if ( c instanceof SMTComponentWithPostConstructHook )  {
            ((SMTComponentWithPostConstructHook)c).postConstruction();
        }
        
        addComponent( c );
    }

    private String nextIdx() {
        return " [ " + (++_nextIdx) + " ]";
    }

    private String nextIdxRange( int n ) {
        int nextIdx = ++_nextIdx;
        
        return " [ " + nextIdx + " -> " + (nextIdx+n-1) + " ]";
    }
    
    private void setComponentProperties( Object c, String id, String baseProps ) {
        try {
            setComponentProperties( c, id, baseProps, 1 );
        } catch( SMTRuntimeException e ) {
            throw new SMTRuntimeException( "Error setting component " + id + " : " + e.getMessage(), e );
        }
        
        autoWireMissingProps( c, id, baseProps );
    }
    
    private void setComponentProperties( Object c, String id, String baseProps, int depth ) {
        
        baseProps = baseProps.toLowerCase();
        
        if ( depth > MAX_RECURSE_CONFIG ) {
            throw new SMTRuntimeException( "Exceeded recursion depth in ProperyComponentBuilder.setComponentProperties id=" + id + " depth=" + depth );
        }
        
        String defaultLoaderProps = _props.getProperty( baseProps + DEFAULT_PROPERTIES, false, null );
        
        if ( defaultLoaderProps != null ) {
            String[] sets = defaultLoaderProps.split( "," );
            for( String set : sets ) {
                setComponentProperties( c, id, set.trim() + ".", depth+1 );
            }
        }
        
        setObjectProperties( baseProps, c );
    }

    private void autoWireMissingProps( Object c, String id, String baseProps ) {
        
        baseProps = baseProps.toLowerCase();
        
        autoWireMissingProperties( baseProps, c );
    }

    private void addComponent( SMTComponent c ) {
        
        String id = c.getComponentId().toString().toLowerCase();
        
        if  ( _components.get( id ) != null ) {
            throw new SMTRuntimeException( "Duplicate component in config, id=" + id );
        }
        
        _components.put( id, c );
    }

    @SuppressWarnings( { "boxing", "unchecked", "null" } )
    private void addConstructorArg( boolean forceSMTRefs, Object[] argVals, Class<?>[] argClasses, int argIdx, String baseProps ) {
        // type can be   ref|long|int|string|zstring|double
        // for type "ref" the value is a componentID

        String typeLC    = _props.getProperty( baseProps + "type",  false, "string" ).toLowerCase();
        String type      = _props.getProperty( baseProps + "type",  false, null );
        String val       = _props.getProperty( baseProps + "value", false, null );
        String ref       = _props.getProperty( baseProps + "ref",   false, null );

        boolean isValNull = ("null".equalsIgnoreCase( val ));
        
        Object pVal = null;
        Class<?> pClass = null;
        
        if ( type != null && type.equalsIgnoreCase( "ref" ) ) {
            pVal = getComponent( val.toLowerCase() );
            pClass = (forceSMTRefs) ? SMTComponent.class : pVal.getClass();
        } else if ( ref != null ) {
            if ( isArrayType( baseProps, type ) ) { 
                pClass = findClass( baseProps, type );
                pVal = populateArrayArg( ref, pClass );
            } else {
                pVal = getComponent( ref.toLowerCase() );
    
                if ( type != null && type.length() > 0 ) {
                    pClass = findClass( baseProps, type );
                } else if ( type == null || type.length() == 0 ) {
                    pClass = (forceSMTRefs) ? SMTComponent.class : pVal.getClass();
                }
            }
        } else if ( "string".equals( typeLC ) ) {
            pVal   = val;
            pClass = String.class;
        } else if ( "zstring".equals( typeLC ) ) {
            pVal   = new ViewString( val );
            pClass = ZString.class;
        } else if ( "long".equals( typeLC ) ) {
            pVal   = Long.parseLong( val );
            pClass = long.class;
        } else if ( "int".equals( typeLC ) ) {
            pVal   = Integer.parseInt( val );
            pClass = int.class;
        } else if ( "double".equals( typeLC ) ) {
            pVal   = Double.parseDouble( val );
            pClass = double.class;
        } else {
            if ( type != null && type.length() > 0 )  {
                try {
                    pClass = findClass( baseProps, type );
                } catch( Exception e1 ) {
                    throw new SMTRuntimeException( "PropertyComponentBuilder : unable to load class " + type + ", baseProps=" + baseProps + ", argIdx=" + argIdx );
                }
                if ( Enum.class.isAssignableFrom( pClass ) ) { 
                    if ( !isValNull ) {
                        @SuppressWarnings( { "rawtypes" } )
                        Class<Enum> enumClass = (Class<Enum>) pClass;
        
                        try {
                            pVal = Enum.valueOf( enumClass, val );
                        } catch( Exception e ) {
                            throw new SMTRuntimeException( "PropertyComponentBuilder : invalid ENUM , " + val + " is not valid entry for " + pClass.getCanonicalName() + 
                                                           ", baseProps=" + baseProps + ", argIdx=" + argIdx );
                        }
                    }
                }
            } 
            
            if ( !isValNull && pVal == null ) {

                // didnt find enum, assume its a component reference
                if ( pClass != null && pClass.isArray() ) { 
                    pClass = findClass( baseProps, type );
                    pVal = populateArrayArg( val, pClass );
                } else {
                    try {
                        String[] compIds = val.split( "," );
                        
                        if ( compIds.length > 1 ) { // ARRAY

                            for( int i=0 ; i < compIds.length ; i++ ) {
                                String aVal = compIds[i].trim();
                                
                                pVal = getComponent( aVal );

                                if ( i == 0 ) {
                                    pClass = (forceSMTRefs) ? SMTComponent.class : pVal.getClass();
                                    
                                    try {
                                        pClass = Class.forName( "[L" + pClass.getName() + ";" );
                                    } catch( ClassNotFoundException e ) {
                                        throw new SMTRuntimeException( "PropertyComponentBuilder : property " + baseProps + ", argIdx=" + argIdx + " unable to derive array class " + e.getMessage(), e );
                                    }
                                }
                            }
                            
                        } else {
                            pVal = getComponent( val );
                            
                            if ( pClass != null ) {
                                if ( forceSMTRefs ) pClass = SMTComponent.class; 
                            } else  {
                                pClass = (forceSMTRefs) ? SMTComponent.class : pVal.getClass();
                            }
                        }
                        
                        if ( ! pClass.isAssignableFrom( pVal.getClass() ) ) {
                            throw new SMTRuntimeException( "PropertyComponentBuilder : property " + baseProps + ", argIdx=" + argIdx + " has type " + typeLC + 
                                                           " but that doesnt match type of component " + val + " which is " + val.getClass().getSimpleName() );
                        }
                    } catch( SMTRuntimeException e ) {
                        throw new SMTRuntimeException( "PropertyComponentBuilder : property " + baseProps + ", argIdx=" + argIdx + " has type " + typeLC + 
                                                       " but cant find component matching id " + val + ", check its defined before referenced");
                    }
                }
            }
        }  
        
        if ( isValNull ) {
            pVal = null;
        }
        
        argVals[ argIdx ]     = pVal;
        argClasses[ argIdx ]  = pClass;
    }

    private Object populateArrayArg( String ref, Class<?> pClass ) {
        String ids[] = ref.split( "," );
        List<?> components = getComponents( ids );
        
        Object arr = Array.newInstance(pClass.getComponentType(), components.size());
        for (int i = 0; i < components.size(); i++) {
            Object v = components.get( i );
            Array.set(arr, i, v );
        }
        return arr;
    }

    private boolean isArrayType( String baseProps, String className ) {
        
        if ( className == null ) return false;
        
        Class<?> pClass = null;
        
        try {
            pClass = Class.forName( className );
        } catch( Exception e ) {
            return false;
        }
        
        return pClass.isArray();
    }

    private Class<?> findClass( String baseProps, String className ) {
        Class<?> pClass = null;
        
        try {
            pClass = Class.forName( className );
        } catch( Exception e ) {
            throw new SMTRuntimeException( "PropertyComponentBuilder : Unable to find class " + className + " specified in property " + baseProps + "className" );
        }
        
        return pClass;
    }

    @SuppressWarnings( { "unchecked" } )
    public <T extends SMTComponent> T getComponent( String id ) {
        
        if ( id == null ) return null;
        
        SMTComponent c = _components.get( id.toLowerCase() );
        
        if ( c == null ) {
            throw new SMTRuntimeException( "Attempt to reference unregistered component, id=" + id + ", check define referenced components first" );
        }
        
        return( (T) c );
    }

    @SuppressWarnings( { "unchecked" } )
    public <T extends SMTComponent> List<T> getComponents( String[] ids ) {
        
        if ( ids == null ) return null;
        
        List<T> components = new ArrayList<T>();
        
        for( String id : ids ) {
            
            id = id.trim();
            
            T c = (T) _components.get( id.toLowerCase() );
            
            if ( c == null ) {
                throw new SMTRuntimeException( "Attempt to reference unregistered component, id=" + id + ", check define referenced components first" );
            }
            
            components.add( c );
        }
        
        return( components );
    }

    private void instantiateViaLoader( String id, String baseProps, String loaderClassName ) throws SMTException {

        Object loader = ReflectUtils.create( loaderClassName );

        setComponentProperties( loader, id,  baseProps + "properties." );
        
        if ( loader instanceof SMTMultiComponentLoader ) {
            SMTComponent[] components = ((SMTMultiComponentLoader)loader).create();
    
            _log.info( "SMTPropertyComponentBuilder.instantiateViaLoader " + nextIdxRange(components.length) + " : " + id );

            for( SMTComponent component : components ) {
                addComponent( component );
            }
        } else if ( loader instanceof SMTSingleComponentLoader ) {
            SMTComponent component = ((SMTSingleComponentLoader)loader).create( id );
            
            _log.info( "SMTPropertyComponentBuilder.instantiateViaLoader " + nextIdx() + " : " + id );
            
            addComponent( component );
        }
    }

    public Collection<SMTComponent> getComponents() {
        return _orderedComponents;
    }
    
    /**
     * all entries under baseProps must be valid members of the propertyHolder
     * 
     * @param baseProps
     * @param propHolder instance to set properties
     */
    private void setObjectProperties( String baseProps, Object propHolder ) {
        
        baseProps = ensureLastCharIsPeriod( baseProps );
        
        Set<Field> fields = ReflectUtils.getMembers( propHolder );
        String[] props  = _props.getNodesWithCaseIntact( baseProps );
        
        for ( String propertyEntry : props ) {      // iterate thru all the specified properties
            
            if ( DEFAULT_PROPERTIES.equalsIgnoreCase( propertyEntry ) ) {
                continue; // the recursive link already processed
            }
            
            boolean set = false;
            
            for( Field f : fields ) {               //   iterate thru all the fields in the propObject looking for match 
                String fieldName = f.getName();
                
                if ( fieldName.charAt( 0 ) == '_' ) fieldName = fieldName.substring( 1 );
         
                if ( fieldName.equalsIgnoreCase( propertyEntry ) ) {
                    String value = _props.getProperty( baseProps + propertyEntry, false, null );

                    if ( value != null && value.length() > 0 ) {
                        setProperty( propHolder, f, value );
                    }

                    set = true;
                    break; // DONE - NEXT PROPERTY ENTRY
                }
            }

            if ( !set ) {
                throw new SMTRuntimeException( "Unable to reflect set property [" + propertyEntry + "] as thats not valid member of " + propHolder.getClass().getSimpleName() +
                                               ", baseProps=" + baseProps );
            }
        }
    }

    private void autoWireMissingProperties( String baseProps, Object propHolder ) {
        
        baseProps = ensureLastCharIsPeriod( baseProps );
        
        Set<Field> fields = ReflectUtils.getMembers( propHolder );
        
        for( Field f : fields ) {               //   iterate thru all the fields in the propObject looking for match 
            String fieldName = f.getName();
            
            if ( fieldName.charAt( 0 ) == '_' ) fieldName = fieldName.substring( 1 );

            if ( ! hasProperty( baseProps, 1, fieldName ) ) {
                setMissingRefUsingReflectionIfPossible( propHolder, fieldName, f );
            }
        }
    }

    private boolean hasProperty( String baseProps, int depth, String fieldName ) {

        if ( depth > MAX_RECURSE_CONFIG ) {
            throw new SMTRuntimeException( "Exceeded recursion depth in ProperyComponentBuilder.autoWireMissingProps baseProps=" + baseProps + 
                                           " depth=" + depth + ", field=" + fieldName);
        }
        
        String defaultLoaderProps = _props.getProperty( baseProps + DEFAULT_PROPERTIES, false, null );
        
        if ( defaultLoaderProps != null ) {
            String[] sets = defaultLoaderProps.split( "," );
            for( String set : sets ) {
                if ( hasProperty( set.trim() + ".", depth+1, fieldName ) ) {
                    return true;
                }
            }
        }
        
        String[] props  = _props.getNodesWithCaseIntact( baseProps );
        
        for ( String propertyEntry : props ) {      // iterate thru all the specified properties
            
            if ( DEFAULT_PROPERTIES.equalsIgnoreCase( propertyEntry ) ) {
                continue;
            }
            
            if ( fieldName.equalsIgnoreCase( propertyEntry ) ) { // ALREADY SET
                return true;
            }
        }
        
        return false;
    }

    private void setMissingRefUsingReflectionIfPossible( Object obj, String fieldName, Field f ) {
        String compKey = fieldName.toLowerCase();

        Class<?> type = f.getType();
        
        SMTComponent c = _components.get( compKey );
        
        if ( c != null && SMTComponent.class.isAssignableFrom( type ) ) {
            _log.info( "AUTOWIRE " + obj.getClass().getName() + " : " + fieldName + ", compId=" + compKey );
            
            setProperty( obj, f, compKey );
        }
    }

    private String ensureLastCharIsPeriod( String baseProps ) {
        if ( baseProps.length() > 0 && baseProps.charAt( baseProps.length()-1 ) != '.' ) {
            baseProps += '.';
        }
        return baseProps;
    }

    private void setProperty( Object obj, Field f, String value ) {
        Class<?> type = f.getType();
        
        boolean wasAccessable = f.isAccessible();
        try {
            f.setAccessible( true );
            if ( Map.class.isAssignableFrom( type ) ) {
                Map<?,?> c =  null;
                
                c = _maps.get( value.toLowerCase() );
                
                if ( c == null ) {
                    c = _maps.get( "map." + value.toLowerCase() );
                }
                
                if ( c != null ) {
                    f.set( obj, c );
                } else {
                    throw new SMTRuntimeException( "Property " + f.getName() + " is a map, but " + value + " is not defined as map in config" );
                }
                
            } else if ( SMTComponent.class.isAssignableFrom( type ) || type.isArray() ) {
                if ( type.isArray() ) {
                    String ids[] = value.split( "," );
                    List<?> components = getComponents( ids );
                    
                    Object arr = Array.newInstance(type.getComponentType(), components.size());
                    for (int i = 0; i < components.size(); i++) {
                        Object val = components.get( i );
                        Array.set(arr, i, val );
                    }                    
                    
                    f.set( obj, arr );
                } else {
                    SMTComponent c =  null;
                    
                    try {
                        c = getComponent( value );
                    } catch( SMTRuntimeException e ) {
                        if ( f.isAnnotationPresent( OptionalReference.class ) ) {
                            _log.info( "SMTPropertyComponentBuilder.setProperty() optional reference of " + f.getName() + ", as type " + type.getSimpleName() +
                                                           " is not set" );
                        } else {
                            throw e;
                        }
                    }
                    
                    f.set( obj, c );
                }
            } else {
                ReflectUtils.setMember( obj, f, value );
            }
        } catch( IllegalArgumentException e ) {
            throw new SMTRuntimeException( "SMTPropertyComponentBuilder.setProperty() unable to set field " + f.getName() + ", as type " + type.getSimpleName() +
                                           " not supported", e );
        } catch( IllegalAccessException e ) {
            throw new SMTRuntimeException( "SMTPropertyComponentBuilder.setProperty() unable to set field " + f.getName() + ", as type " + type.getSimpleName() +
                                           " access not allowed", e );
        } finally {
            f.setAccessible( wasAccessable );
        }
    }
}
