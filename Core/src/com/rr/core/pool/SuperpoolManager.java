/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.pool;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.rr.core.lang.Reusable;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.utils.ShutdownManager;

public class SuperpoolManager {

    private static final Logger _log = LoggerFactory.console( SuperpoolManager.class );

    private static final SuperpoolManager _instance = new SuperpoolManager();
    
    private final Map<Class<?>,SuperPool<?>> _pools = new HashMap<Class<?>,SuperPool<?>>();
    
    public static SuperpoolManager instance() { return _instance; }
    
    private SuperpoolManager() {
        
        ShutdownManager.instance().register( new ShutdownManager.Callback() {
                            @Override
                            public void shuttingDown() {
                                logStats();
                            }} );
        
    }
    
    @SuppressWarnings( "unchecked" )
    public synchronized <T extends Reusable<T>> SuperPool<T> getSuperPool( Class<T> poolClass ) {
        SuperPool<T> sp = (SuperPool<T>) _instance._pools.get( poolClass );
        
        if ( sp == null ) {
            sp = new SuperPool<T>( poolClass );
            
            _instance._pools.put( poolClass, sp );
        }
        
        return sp;
    }

    protected synchronized void logStats() {
        _log.info( "\n\nLOG POOL STATS\n" );
        
        for ( SuperPool<?> pool : _pools.values() ) {
            pool.logStats();
        }
    }

    public synchronized <T extends Reusable<T>> Recycler<T> getRecycler( Class<T> poolClass ) {
        SuperPool<T> sp = getSuperPool( poolClass );
        
        return sp.getRecycleFactory();
    }

    public synchronized <T extends Reusable<T>> PoolFactory<T> getPoolFactory( Class<T> poolClass ) {
        SuperPool<T> sp = getSuperPool( poolClass );
        
        return sp.getPoolFactory();
    }

    /**
     * allows a specific factory to be used with the superpool for a class (avoiding generics in pool is faster)
     * 
     * @param <F>               factory class
     * @param <T>               pooled object type
     * @param factoryClass
     * @param poolClass
     * @return
     */
    public synchronized <F,T extends Reusable<T>> F getFactory( Class<F> factoryClass, Class<T> poolClass ) {
        SuperPool<T> sp = SuperpoolManager.instance().getSuperPool( poolClass );
        
        try {
            Constructor<F> c = factoryClass.getConstructor( sp.getClass() );
            
            return c.newInstance( sp );
            
        } catch( Exception e ) {
            throw new RuntimeException( "Failed to reflect instantiate " + factoryClass.getSimpleName() );
        }
    }

    /**
     * allows a specific Recycler to be used with the superpool for a class (avoiding generics in pool is faster)
     * 
     * @param <F>               recycler class
     * @param <T>               pooled object type
     * @param recyclerClass
     * @param poolClass
     * @return
     */
    @SuppressWarnings( "boxing" )
    public synchronized <F,T extends Reusable<T>> F getRecycler( Class<F> recyclerClass, Class<T> poolClass ) {
        SuperPool<T> sp = SuperpoolManager.instance().getSuperPool( poolClass );
        
        try {
            Constructor<F> c = recyclerClass.getConstructor( int.class, sp.getClass() );
            
            int chainSize = sp.getChainSize();
            
            return c.newInstance( chainSize, sp );
            
        } catch( Exception e ) {
            throw new RuntimeException( "Failed to reflect instantiate " + recyclerClass.getSimpleName() );
        }
    }

    public void resetPoolStats() {
        _log.info( "\n\n" );
        
        for ( SuperPool<?> pool : _pools.values() ) {
            pool.resetStats(); 
        }
    }
}
