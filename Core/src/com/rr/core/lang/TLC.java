/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang;

import java.util.HashMap;
import java.util.Map;

import com.rr.core.factories.ReusableStringFactory;
import com.rr.core.pool.PoolFactory;
import com.rr.core.pool.Recycler;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.recycler.ReusableStringRecycler;
import com.rr.core.utils.ReflectUtils;
import com.rr.core.utils.SMTRuntimeException;

/**
 * thread local context, contains all the thread local members the app needs
 * so only need 1 map lookup to access
 * 
 * utils safe across threads
 *
 * @author Richard Rose
 */
public class TLC {

    private static final ThreadLocal<TLC>  _context = new ThreadLocal<TLC>() {
                                                            @Override
                                                            public TLC initialValue() {
                                                                return new TLC();
                                                            }
                                                        };
    
    private final ReusableStringFactory         _reusableStringFactory;
    private final ReusableStringRecycler        _reusableStringRecycler;
    
    private final ReusableString                _head;

    private final Map<String, Object>           _instanceByIdMap;
    private final Map<Class<?>, Object>         _instanceMap;
    private final Map<Class<?>, Object>         _factoryMap;
    private final Map<Class<?>, Object>         _recyclerMap;

    
    TLC() {
        SuperpoolManager sp = SuperpoolManager.instance();
        
        _reusableStringFactory  = sp.getFactory(  ReusableStringFactory.class,   ReusableString.class );
        _reusableStringRecycler = sp.getRecycler( ReusableStringRecycler.class,  ReusableString.class );
        
        _head = new ReusableString( "HEAD" );
        
        _instanceByIdMap = new HashMap<String, Object>();
        _instanceMap = new HashMap<Class<?>, Object>();
        _factoryMap = new HashMap<Class<?>, Object>();
        _recyclerMap = new HashMap<Class<?>, Object>();
    }

    /**
     * do NOT hold onto instance returned as it is only for use on the current thread
     * @return
     */
    public static TLC instance()  {
        return _context.get();
    }
    
    public <T> T getInstanceOf( String id, Class<T> aClass, Class<?>[] pClass, Object[] pArgs ) {
        @SuppressWarnings( "unchecked" )
        T instance = (T) _instanceByIdMap.get( id );
        
        if ( instance == null ) {
            try {
                instance = ReflectUtils.create( aClass, pClass, pArgs );
            } catch( Exception e ) {
                throw new SMTRuntimeException( "TLC error instantaiting " + aClass.getSimpleName(), e );
            }
            
            _instanceByIdMap.put( id, instance );
        }
        
        return instance;
    }
    
    public <T> T getInstanceOf( Class<T> aClass ) throws SMTRuntimeException {
        @SuppressWarnings( "unchecked" )
        T instance = (T) _instanceMap.get( aClass );
        
        if ( instance == null ) {
            try {
                instance = aClass.newInstance();
            } catch( Exception e ) {
                throw new SMTRuntimeException( "TLC error instantaiting " + aClass.getSimpleName(), e );
            }
            
            _instanceMap.put( aClass, instance );
        }
        
        return instance;
    }

    public ReusableString getString() {
        return _reusableStringFactory.get();
    }

    public ReusableString pop() {
        ReusableString s = _head.getNext();
        
        if ( s != null ) {
            _head.setNext( s.getNext() );
            
            return s;
        }
        
        // nothing in the threads Q .. get from pool
        
        return _reusableStringFactory.get();
    }

    public void recycle( ReusableString str ) {
        _reusableStringRecycler.recycle( str );
    }
    
    public ReusableStringRecycler getReusableStringRecycleFactory() {
        return _reusableStringRecycler;
    }
    
    /**
     * dont recycle into factory, but keep in local queue
     * 
     * saves alot of allocs over time as invokers normally grow the temp strings
     * 
     * @param str
     */
    public void pushback( ReusableString str ) {
        
        str.reset();
        
        // insert used string at head of LIFO queue
        str.setNext( _head.getNext() );
        _head.setNext( str );
    }

    public void recycleStringMap( Map<ReusableString, ReusableString> map ) {

        for ( Map.Entry<ReusableString, ReusableString>  entry : map.entrySet() ) {
            _reusableStringRecycler.recycle( entry.getKey() );
            _reusableStringRecycler.recycle( entry.getValue() );
        }
        
        map.clear();
    }

    public void recycleChain( ReusableString chain ) {
        ReusableString tmp;
        
        while( chain != null ) {
            tmp = chain;
            chain = chain.getNext();
            _reusableStringRecycler.recycle( tmp );
        }
    }

    /**
     * thread safe pooled copy of a ZString 
     */
    public static ReusableString safeCopy( ZString ric ) {
        return instance().getString().copy( ric ); 
    }

    /**
     * @param poolClass
     * @return pool factory which is allocated to CURRENT thread
     */
    public synchronized <T extends Reusable<T>> PoolFactory<T> getPoolFactory( Class<T> poolClass ) {
        @SuppressWarnings( "unchecked" )
        PoolFactory<T> instance = (PoolFactory<T>) _factoryMap.get( poolClass );
        
        if ( instance == null ) {
            try {
                instance = SuperpoolManager.instance().getPoolFactory( poolClass );
            } catch( Exception e ) {
                throw new SMTRuntimeException( "TLC error instantaiting " + poolClass.getSimpleName(), e );
            }
            
            _factoryMap.put( poolClass, instance );
        }
        
        return instance;
    }

    /**
     * @param poolClass
     * @return pool factory which is allocated to CURRENT thread
     */
    public synchronized <T extends Reusable<T>> Recycler<T> getPoolRecycler( Class<T> poolClass ) {
        @SuppressWarnings( "unchecked" )
        Recycler<T> instance = (Recycler<T>) _recyclerMap.get( poolClass );
        
        if ( instance == null ) {
            try {
                instance = SuperpoolManager.instance().getRecycler( poolClass );
            } catch( Exception e ) {
                throw new SMTRuntimeException( "TLC error instantaiting " + poolClass.getSimpleName(), e );
            }
            
            _recyclerMap.put( poolClass, instance );
        }
        
        return instance;
    }
}
