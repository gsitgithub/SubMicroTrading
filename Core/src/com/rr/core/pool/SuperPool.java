/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.pool;

import com.rr.core.lang.Reusable;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;

public class SuperPool<T extends Reusable<T>> {

    private static final Logger _log = LoggerFactory.console( SuperPool.class );
    
    // TODO benchmark to see optimal size
   
    // TODO register shutdown callback to log stats
    
    // TODO add persistence of sizes by writing the pool size in EOD event
    
    private static final int DEFAULT_CHAINS               = 10;
    private static final int DEFAULT_CHAIN_SIZE           = 10;
    private static final int DEFAULT_UNEXPECTED_CHAINSIZE = 10;

    /**
     * PoolNode<T> a wrapper node to hold a chain of type T 
     */
    static class PoolNode<T> {
        
        PoolNode<T> _next  = null;
        T           _chain = null;
    }

    private final Class<T>    _poolClass;

    /**
     * all the PoolNode instances are final, the root of each of these lists is non mutatable so they can be used safely within
     * synchronisation blocks.
     * The first data node is root->next
     * Some lists have a corresponding last pointer, if lastPointer == root then there is no data, 
     * alternatively check for root->next as null which also indicates no data nodes.
     */
    
    /**
     * _readyChains a list of fully populate pool nodes. Represents the available pool capacity for getChain.
     * Allocated initially in init. 
     * Within returnChain the returnedChains are recycled into the readyChains  
     */
    private final PoolNode<T> _readyChains     = new PoolNode<T>(); 

    /**
     * _returnedChains a list of fully populated pool nodes
     * a list used by returnChain to build up a fully populated list of nodes which can be moved back into the readyChains 
     */
    private final PoolNode<T> _returnedChains  = new PoolNode<T>();
    private PoolNode<T> _lastReturnedChain = _returnedChains;
    
    /**
     * _freeReadyNodes list of pool nodes which have empty chain. 
     * getChain gets a node from the readyChains, removes its chain and places the empty node into the end of the freeReadyNodes
     */
    private final PoolNode<T> _freeReadyNodes  = new PoolNode<T>(); 
    private PoolNode<T> _lastFreeReadyNode = _freeReadyNodes;
    
    /**
     * _freeReturnNodes chain of PoolNode's with null chains, used by the returnChain to avoid creation of the wrapper pool nodes
     * This list is created by the init method
     */
    private final PoolNode<T> _freeReturnNodes = new PoolNode<T>();
    
    
    private int         _unexpectedReturnNodeAlloc = 0;
    private int         _unexpectedChainAlloc      = 0;
    
    private int         _numChains                 = 0;
    private int         _chainSize                 = 0;
    private int         _recycledChains            = 0;
    private int         _unexpectedChainAllocSize  = 10;    // chain to create when pool empty

    
    public SuperPool( Class<T> poolClass ) {
        this( poolClass, DEFAULT_CHAINS, DEFAULT_CHAIN_SIZE, DEFAULT_UNEXPECTED_CHAINSIZE );
    }
    
    public SuperPool( Class<T> poolClass, int chains, int chainSize, int unexpectedChainSize ) {
        _poolClass = poolClass;
        
        init( chains, chainSize, unexpectedChainSize );
    }
    
    public PoolFactory<T> getPoolFactory() {
        return new PoolFactoryImpl<T>( this );
    }
    
    public Recycler<T> getRecycleFactory() {
        return new RecyclerImpl<T>( _poolClass, _chainSize, this );
    }
    
    public void init( int chains, int chainSize, int unexpectedChainSize ) {

        try {
            if ( (chains != _numChains && chains != DEFAULT_CHAINS) || (chainSize != _chainSize && chainSize != DEFAULT_CHAIN_SIZE) ) {
                _log.info( "SuperPool[" + _poolClass.getSimpleName() + "].init chains=" + chains + ", size="+ chainSize + ", unexpChainSize=" + unexpectedChainSize );
            }
            
            _numChains = chains;
            _chainSize = chainSize;
            _unexpectedChainAllocSize = unexpectedChainSize;
            
            synchronized( _readyChains ) {
             
                PoolNode<T> tmpNode = _readyChains._next;
                
                int i = 0;
                
                while ( i < chains && tmpNode != null ) {
                    int countInChain = 0;
    
                    T tmpObj = tmpNode._chain;
                    
                    if ( tmpObj != null ) {
                        while( tmpObj.getNext() != null ){
                            ++countInChain;
                            tmpObj = tmpObj.getNext();
                        }
                    } else {
                        tmpObj = _poolClass.newInstance();
                        tmpNode._chain = tmpObj;
                    }
    
                    // tmpObj is now the last obj in the chain
                    
                    T newObj;
                    
                    while( ++countInChain < chainSize ) {
                        
                        newObj = _poolClass.newInstance();
                        
                        tmpObj.setNext( newObj );
                        
                        tmpObj = newObj;
                    }
                    
                    tmpNode = tmpNode._next;
                    
                    if ( tmpNode != null ) {
                        ++i;
                    }
                }
                
                while ( i < chains ) {
                    PoolNode<T> node = new PoolNode<T>();
                    
                    T tmp = _poolClass.newInstance();                            
                    node._chain = tmp;
                    
                    T tmp2;
                    
                    for( int j=1 ; j < chainSize ; ++j ) {
                        
                        tmp2 = _poolClass.newInstance();
                        
                        tmp.setNext( tmp2 );
                        
                        tmp = tmp2;
                    }
    
                    node._next = _readyChains._next;
                    _readyChains._next = node;
                    
                    i++;
                }
                
                sizeFreeReturnChain( chains );
            }
        } catch( Exception e ) {
            throw new RuntimeAllocException( "Failed to alloc for " + _poolClass.getSimpleName() + " : "  + e.getMessage(), e );
        }
    }

    private void sizeFreeReturnChain( int chains ) {
        
        int cnt=0;
        PoolNode<T> node = _freeReturnNodes;        
        
        while( node._next != null ) {
            node = node._next;
            cnt++;
        }
        
        while( cnt++ < chains ) {
            node._next = new PoolNode<T>();
            
            node = node._next;
        }
    }

    public T getChain() {
        T chain;

        PoolNode<T> node;
        PoolNode<T> returnEmptyNodes = null;
        PoolNode<T> lastReturnEmptyNodes = null;
        
        synchronized( _readyChains ) {
            
            node = _readyChains._next;
            
            if ( node != null ) { // ok we have chain available
                
                _readyChains._next = node._next;
            
                node._next = null;
    
                _lastFreeReadyNode._next = node; 
                _lastFreeReadyNode = node;
                
                chain = node._chain;
                node._chain = null;
                
                return chain;
            }
       
            /**
             * while we have the readyChains lock, grab the empty freeReadyNodes list, ready to be returned to the _freeReturnNodes list
             */
            returnEmptyNodes = _freeReadyNodes._next;
            lastReturnEmptyNodes = _lastFreeReadyNode;
            _freeReadyNodes._next = null;
            _lastFreeReadyNode = _freeReadyNodes;
        }
        
        PoolNode<T> lastPopulatedChainNode = null;

        synchronized( _returnedChains ) {
            
            /**
             * no chains within the readyChains list, 
             * so lets grab the populated chains built up by returnChains held within the _returnedChains list 
             */
            
            lastPopulatedChainNode = _lastReturnedChain;
            
            node = _returnedChains._next;
            _returnedChains._next = null;
            _lastReturnedChain = _returnedChains;
            
            if ( returnEmptyNodes != null ) { 
                /**
                 * while we have the returnedChains lock, insert the empty freeReadyNodes into the start of the _freeReturnNodes list
                 */
                lastReturnEmptyNodes._next = _freeReturnNodes._next;
                _freeReturnNodes._next     = returnEmptyNodes._next;
            }
        }
            
        if ( node == null ) { // no return chains handy must allocate
    
            ++_unexpectedChainAlloc;
            
            try {
                chain = _poolClass.newInstance();                            
                T tmp;
                
                for( int i=1 ; i < _unexpectedChainAllocSize ; ++i ) {
                    tmp = _poolClass.newInstance();
                    tmp.setNext( chain );
                    chain = tmp;
                }
            } catch( Exception e ) {
                throw new RuntimeAllocException( "Failed to alloc for " + _poolClass.getSimpleName() + " : "  + e.getMessage(), e );
            }
            
            return chain;
        }

        synchronized( _readyChains ) {
            /**
             * ok we have a returned node list
             * put the "next" node into the ready chain
             * chain will be extracted from node which is then put into the end of the _lastFreeReadyNode list 
             */

            if ( lastPopulatedChainNode != node ) {
                // more than one node available to use
                lastPopulatedChainNode._next = _readyChains._next;
                
                _readyChains._next = node._next;
            }
            
            node._next = null;
            
            _lastFreeReadyNode._next = node; 
            _lastFreeReadyNode = node;
            
            chain = node._chain;
            node._chain = null;
            
            return chain;
        }
    }

    public void returnChain( T list ) {

        PoolNode<T> node;
        PoolNode<T> fullChainsStartNode;
        PoolNode<T> fullChainLastNode;
        
        _recycledChains++;
        
        synchronized( _returnedChains ) {
            
            node = _freeReturnNodes._next;

            if ( node != null ) {
                
                _freeReturnNodes._next = node._next;
                
                node._chain = list;
                
                _lastReturnedChain._next = node; 
                _lastReturnedChain = node;
                node._next = null;
                
                return;
            }
            
            // no free nodes available ... give full chains to ready list
            
            fullChainsStartNode   = _returnedChains._next;
            fullChainLastNode     = _lastReturnedChain;
            _returnedChains._next = null;
            _lastReturnedChain    = _returnedChains;
        }

        PoolNode<T> emptyNodeChain = null;
        PoolNode<T> lastEmptyNode  = null;

        // no empty nodes to use
            
        synchronized( _readyChains ) {

            if ( fullChainsStartNode != null ) { // return the fully populated chains to the ready list
                fullChainLastNode._next = _readyChains._next;
                _readyChains._next = fullChainsStartNode;
            }
            
            // get the free nodes from the free alloc list
            emptyNodeChain = _freeReadyNodes._next;
            lastEmptyNode  = _lastFreeReadyNode;
            
            _freeReadyNodes._next = null;
            _lastFreeReadyNode = _freeReadyNodes;
            
            if ( emptyNodeChain == null ) {
                ++_unexpectedReturnNodeAlloc ;
                node = new PoolNode<T>();
            } else {
                node = emptyNodeChain;
                emptyNodeChain = node._next;
            }
            
            node._chain = list;
            node._next = null;
        }
        
        // now need to get return lock again to put back the free nodes
        
        synchronized( _returnedChains ) {
            if ( emptyNodeChain != null ) {
                lastEmptyNode._next = _freeReturnNodes._next;
                _freeReturnNodes._next = emptyNodeChain;
            }
            
            _lastReturnedChain._next = node; 
            _lastReturnedChain = node;
        }
    }

    public void logStats() {
        if ( _unexpectedChainAlloc > (_numChains / 4) ) {
            _log.warn( "SuperPool " + _poolClass.getSimpleName() + " GREW more than 25% initialChains=" + _numChains + ", extraChains=" + _unexpectedChainAlloc +
                       ", chainSize="           + _chainSize + 
                       ", recycledChains="      + _recycledChains + 
                       ", runtimeChainSize="    + _unexpectedChainAllocSize + 
                       ", unexpectedNodeAlloc=" + _unexpectedReturnNodeAlloc  );
        } else {
            if ( _unexpectedChainAlloc > 0 || _recycledChains > 10 ) {
                _log.info( "SuperPool " + _poolClass.getSimpleName() + " initialChains=" + _numChains + ", extraChains=" + _unexpectedChainAlloc +
                           ", chainSize="           + _chainSize + 
                           ", recycledChains="      + _recycledChains + 
                           ", runtimeChainSize="    + _unexpectedChainAllocSize + 
                           ", unexpectedNodeAlloc=" + _unexpectedReturnNodeAlloc  );
            }
        }
    }

    public int getCountExtraChains() {
        return _unexpectedChainAlloc;
    }

    public int getCountRecycledChains() {
        return _recycledChains;
    }

    public int getChainSize() {
        return _chainSize;
    }

    public void resetStats() {
        _numChains += _unexpectedChainAlloc;
        _unexpectedChainAlloc = 0;
        _unexpectedReturnNodeAlloc = 0;
    }

    /**
     * @NOTE ONLY FOR USED IN TESTING WHERE MEMORY IS A CONSTRAINT
     */
    public void deleteAll() {
        _log.info( "SuperPool " + _poolClass.getSimpleName() + " deleteAll invoked" );
        
        _freeReadyNodes._next  = null; 
        _freeReturnNodes._next = null;
        _readyChains._next     = null;
        _returnedChains._next  = null;

        _numChains = 0;
        _chainSize = 0;
        _unexpectedChainAllocSize = 0;
    }
}
