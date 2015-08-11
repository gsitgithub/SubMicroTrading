/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.inst;

import com.rr.core.lang.ViewString;
import com.rr.core.model.Instrument;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.utils.FileException;
import com.rr.om.BaseOMTestCase;


public class TestFixInstrumentLoader extends BaseOMTestCase {

    static {
        loadExchanges();
    }
    
    public void testLoadOne() throws FileException {
        MultiExchangeInstrumentStore instStore = new MultiExchangeInstrumentStore( 1000 );
        FixInstrumentLoader loader = new FixInstrumentLoader( instStore );
        loader.loadFromFile( "./data/cme/secdef.one.dat", new ViewString("2") );

        Instrument inst = instStore.getInstrumentByID( new ViewString("2"),  27069 );
        
        assertNotNull( inst );
        
        assertEquals( 27069, inst.getLongSymbol() );
        assertEquals( new ViewString("27069"), inst.getExchangeSymbol() );
    }

    public void testT1Inst() throws FileException {
        MultiExchangeInstrumentStore instStore = new MultiExchangeInstrumentStore( 1000 );
        FixInstrumentLoader loader = new FixInstrumentLoader( instStore );
        loader.loadFromFile( "./data/cme/secdef.t1.dat", new ViewString("2") );

        Instrument inst = instStore.getInstrumentByID( new ViewString("2"),  27069 );
        
        assertNotNull( inst );
        
        assertEquals( 27069, inst.getLongSymbol() );
        assertEquals( new ViewString("27069"), inst.getExchangeSymbol() );
    }

    public void testAutocertInst() throws FileException {
        MultiExchangeInstrumentStore instStore = new MultiExchangeInstrumentStore( 1000 );
        FixInstrumentLoader loader = new FixInstrumentLoader( instStore );
        loader.loadFromFile( "./data/cme/secdef.autocert.dat", new ViewString("2") );

        Instrument inst1 = instStore.getInstrumentByID( new ViewString("2"),  407417 );
        assertNotNull( inst1 );

        Instrument inst2 = instStore.getInstrument( new ViewString("407417"), SecurityIDSource.ExchangeSymbol, new ViewString("2"), new ViewString("2"), null ); 
                                        
        assertNotNull( inst2 );
        assertSame( inst1, inst2 );
        
        assertEquals( 407417, inst1.getLongSymbol() );
        assertEquals( new ViewString("407417"), inst1.getExchangeSymbol() );

        Instrument inst3 = instStore.getInstrumentBySecurityDesc( new ViewString("0GEU4") ); 
        assertNotNull( inst3 );
    }

    public void testGetByTag107() throws FileException {
        // 1128=99=45935=d49=CME34=92452=2013122917050035815=GBP22=848=12212355=JF107=EBPM4200=201406202=0207=CMED461=FFCXSX462=4562=1731=1827=2864=2865=5866=201305281145=213000000865=7866=201406161145=150000000870=4871=24872=1871=24872=3871=24872=4871=24872=7947=GBP969=5996=EUR1140=25001141=21022=GBX264=51022=GBI264=21142=K1143=3001144=31146=51147=1000001148=01150=836051151=EBP1180=701300=865796=201312279787=0.000019850=010=021

        MultiExchangeInstrumentStore instStore = new MultiExchangeInstrumentStore( 1000 );
        FixInstrumentLoader loader = new FixInstrumentLoader( instStore );
        loader.loadFromFile( "./data/cme/secdef.t1.and.ebpm.dat", new ViewString("2") );

        Instrument inst = instStore.getInstrumentBySymbol( new ViewString("EBPM4"), new ViewString("2"), null, null );

        assertNotNull( inst );
        
        assertEquals( 122123, inst.getLongSymbol() );
        assertEquals( new ViewString("122123"), inst.getExchangeSymbol() );
    }
}
