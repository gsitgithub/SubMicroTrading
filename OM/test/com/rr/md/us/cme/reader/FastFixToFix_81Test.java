/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.reader;

import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.msgdict.DictComponentFactory;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.md.fastfix.XMLFastFixTemplateLoader;
import com.rr.md.fastfix.meta.MetaTemplate;
import com.rr.md.fastfix.meta.MetaTemplates;
import com.rr.md.fastfix.reader.FastFixToFixReader;
import com.rr.md.fastfix.template.FastFixTemplateClassRegister;
import com.rr.md.fastfix.template.TemplateClassRegister;
import com.rr.md.us.cme.writer.MDIncRefresh_81_Writer;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.type.MDEntryType;
import com.rr.model.generated.internal.type.MDUpdateAction;


public class FastFixToFix_81Test extends BaseTestCase {

    private int chains     = 10;
    private int chainSize  = 10;
    private int extraAlloc = 10;

    private final byte[] buf = new byte[8192];
    
    private FastFixBuilder encoder = new FastFixBuilder( buf, 0 );
    
    private ComponentFactory cf = new DictComponentFactory();
    
    private MDIncRefresh_81_Writer writer = new MDIncRefresh_81_Writer( cf, getName(), 81 );

    private MetaTemplates meta = new MetaTemplates();
    
    
    public FastFixToFix_81Test() {
        // nothing
    }

    private static <T extends Reusable<T>> void presize( Class<T> aclass, int chains, int chainSize, int extraAlloc ) {
        SuperPool<T> sp = SuperpoolManager.instance().getSuperPool( aclass );
        sp.init( chains, chainSize, extraAlloc );
    }

    @Override
    public void setUp() {
        presize( MDIncRefreshImpl.class, chains, chainSize, extraAlloc );
        presize( MDEntryImpl.class, chains, chainSize, extraAlloc );
        
        loadTemplates();
    }
    
    private void loadTemplates() {
        TemplateClassRegister reg = new FastFixTemplateClassRegister();
        
        XMLFastFixTemplateLoader l = new XMLFastFixTemplateLoader( "data/cme/templates.xml" );
        
        l.load( reg, meta );
    }

    public void testCodec3() {
        MDIncRefreshImpl inc = makeUpdate( 3 );

        PresenceMapWriter pMapOut = new PresenceMapWriter( encoder, 0, 1 );

        encoder.clear();
        pMapOut.reset();
        
        writer.write( encoder, pMapOut, inc );
        pMapOut.end();

        MetaTemplate mt = meta.getTemplate( 81 );
        FastFixToFixReader reader = new FastFixToFixReader( mt, "MDIncRefresh_81", 81, (byte) '|' );
        
        reader.init( cf );

        ReusableString destFixMsg = new ReusableString();
        
        FastFixDecodeBuilder decoder = new FastFixDecodeBuilder();
        decoder.start( buf, 0, buf.length );

        PresenceMapReader pMap = new PresenceMapReader();
        pMap.readMap( decoder );
        
        reader.read( decoder, pMap, destFixMsg );

        String expMsg = "1128=9|35=X|49=CME|34=1000000|52=20130915-08:01:02.003|268=3|" +
                            "279=1|1023=1|269=0|22=8|48=12345678|83=1|270=1000.12345|273=800000|271=100|346=0|336=2|"  +
                            "279=1|1023=2|269=1|22=8|48=12345678|83=2|270=1001.12345|273=800001|271=110|346=10|336=2|" +
                            "279=1|1023=3|269=0|22=8|48=12345678|83=3|270=1002.12345|273=800002|271=120|346=20|336=2|";
        
        assertEquals( expMsg, destFixMsg.toString() );
    }

    @SuppressWarnings( "null" )
    private MDIncRefreshImpl makeUpdate( int numMDEntries ) {
        
        MDIncRefreshImpl inc = new MDIncRefreshImpl();
        
        inc.setSendingTime( 20130915080102003L );
        inc.setMsgSeqNum( 1000000 );
        inc.setPossDupFlag( false );

        inc.setNoMDEntries( numMDEntries );
        
        inc.setNoMDEntries( numMDEntries );
        
        MDEntryImpl first = null;  
        MDEntryImpl tmp = null;
        
        for ( int i=0 ; i < numMDEntries ; i++ ) {
            
            if ( first == null ) {
                tmp = first = new MDEntryImpl();
            } else {
                tmp.setNext( new MDEntryImpl() );
                tmp = tmp.getNext();
            }

            tmp.setMdUpdateAction( MDUpdateAction.Change );
            tmp.setSecurityIDSource( SecurityIDSource.ExchangeSymbol );
            tmp.setSecurityID( 12345678 );
            tmp.setRepeatSeq( i+1 );
            tmp.setNumberOfOrders( i*10 );
            tmp.setMdPriceLevel( i+1 );
            if ( i % 2 == 0 ) {
                tmp.setMdEntryType( MDEntryType.Bid );
            } else {
                tmp.setMdEntryType( MDEntryType.Offer );
            }
            tmp.setMdEntryPx( 1000.12345 + i );
            tmp.setMdEntrySize( 100+i*10 );
            tmp.setMdEntryTime( 800000+i );
        }
        
        inc.setMDEntries( first ); 

        return inc;
    }
}
