/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.loaders;

import com.rr.core.component.SMTComponent;
import com.rr.core.component.SMTSingleComponentLoader;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Exchange;
import com.rr.core.utils.SMTException;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.inst.FixInstrumentLoader;
import com.rr.inst.InstrumentStore;
import com.rr.inst.MultiExchangeInstrumentStore;
import com.rr.inst.SingleExchangeInstrumentStore;
import com.rr.inst.ThreadsafeInstrumentStore;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.exchange.ExchangeManager;


public class FixInstrumentFileStoreLoader implements SMTSingleComponentLoader {

    private static final Logger _console = LoggerFactory.console( FixInstrumentFileStoreLoader.class );
    
    private String  _type        = "dummy";
    private String  _file        = null;
    private boolean _threadsafe  = false;
    private ZString _rec         = null;   // if using singleExchange this property should be set

    
    @Override
    public SMTComponent create( String id ) throws SMTException {
        
        InstrumentStore instrumentStore = null;

        if ( _file != null && ! _type.equalsIgnoreCase( "dummy" ) ) {
            
            if ( _type.equalsIgnoreCase( "multiExchange" ) ) {
                instrumentStore = new MultiExchangeInstrumentStore( id, 1000 );

                _console.info( "Loading MultiExchangeInstrumentStore with instrument file " + _file );
            } else if ( _type.equalsIgnoreCase( "singleExchange" ) ) {
                Exchange e = ExchangeManager.instance().getByREC( new ViewString(_rec) );
                
                if ( e == null ) {
                    throw new SMTRuntimeException( "Instrument store REC not in exchange manager : [" + _rec + "]" );
                }
                
                instrumentStore = new SingleExchangeInstrumentStore( id, e, 1000 );

                _console.info( "Loading SingleExchangeInstrumentStore with instrument file " + _file + ", srcRec=" + _rec );
            } else {
                throw new SMTRuntimeException( "Unsupported inst.type of " + _type );
            }
            
            // use threadsafe inst store when intraday updates required
            
            if ( _threadsafe ) {
                _console.info( "Wrapping instrument store with ThreadsafeInstrumentStore with instrument file " );

                instrumentStore = new ThreadsafeInstrumentStore( instrumentStore );
            }
            
            FixInstrumentLoader loader = new FixInstrumentLoader( instrumentStore );
            
            loader.loadFromFile( _file, _rec );
            
        } else {
            _console.info( "Using DUMMY instrument store" );
            instrumentStore  = new DummyInstrumentLocator( id );  
        }
        
        return instrumentStore;
    }
}
