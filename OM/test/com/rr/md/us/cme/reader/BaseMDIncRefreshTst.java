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
import com.rr.core.lang.Constants;
import com.rr.core.lang.Reusable;
import com.rr.core.model.SecurityIDSource;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.md.fastfix.template.MDIncRefreshFastFixTemplateReader;
import com.rr.md.fastfix.template.MDIncRefreshFastFixTemplateWriter;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.recycle.MDIncRefreshRecycler;
import com.rr.model.generated.internal.type.MDEntryType;
import com.rr.model.generated.internal.type.MDUpdateAction;


public abstract class BaseMDIncRefreshTst extends BaseTestCase {

    protected ComponentFactory cf = new DictComponentFactory();

    private int chains     = 10;
    private int chainSize  = 10;
    private int extraAlloc = 10;

    private SuperPool<MDIncRefreshImpl> mdIncSP      = SuperpoolManager.instance().getSuperPool( MDIncRefreshImpl.class );
    private MDIncRefreshRecycler        mdIncRecyler = new MDIncRefreshRecycler( chainSize, mdIncSP );

    private final byte[] buf = new byte[8192];
    
    protected FastFixDecodeBuilder decoder = new FastFixDecodeBuilder();
    protected FastFixBuilder       encoder = new FastFixBuilder( buf, 0 );
    
    protected MDIncRefreshFastFixTemplateReader reader;
    protected MDIncRefreshFastFixTemplateWriter writer;
    
    
    public BaseMDIncRefreshTst() {
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
        reader = makeReader();
        writer = makeWriter();
    }
    
    protected abstract MDIncRefreshFastFixTemplateReader makeReader();
    protected abstract MDIncRefreshFastFixTemplateWriter makeWriter();

    public void testCodec1() {
        MDIncRefreshImpl inc = makeUpdate( 1 );

        PresenceMapWriter pMapOut = new PresenceMapWriter( encoder, 0, 1 );
        PresenceMapReader pMapIn  = new PresenceMapReader();

        MDIncRefreshImpl last = null;
        
        encoder.clear();
        pMapOut.reset();
        
        writer.write( encoder, pMapOut, inc );
        pMapOut.end();
        
        decoder.start( buf, 0, buf.length );
        pMapIn.readMap( decoder );
        
        last = reader.read( decoder, pMapIn );     
        
        checkEquals( inc, last );
        
        mdIncRecyler.recycle( last );
    }

    public void testCodec3() {
        MDIncRefreshImpl inc = makeUpdate( 3 );

        PresenceMapWriter pMapOut = new PresenceMapWriter( encoder, 0, 1 );
        PresenceMapReader pMapIn  = new PresenceMapReader();

        MDIncRefreshImpl last = null;
        
        encoder.clear();
        pMapOut.reset();
        
        writer.write( encoder, pMapOut, inc );
        pMapOut.end();
        
        decoder.start( buf, 0, buf.length );
        pMapIn.readMap( decoder );
        
        last = reader.read( decoder, pMapIn );     
        
        checkEquals( inc, last );
        
        mdIncRecyler.recycle( last );
    }

    public void checkEquals( MDIncRefreshImpl exp, MDIncRefreshImpl decoded ) {
        assertEquals( exp.getSendingTime(), decoded.getSendingTime() );
        assertEquals( exp.getMsgSeqNum(),   decoded.getMsgSeqNum() );
        assertEquals( exp.getPossDupFlag(), decoded.getPossDupFlag() );
        assertEquals( exp.getMsgSeqNum(),   decoded.getMsgSeqNum() );
        
        int expEntries = exp.getNoMDEntries();
        
        assertEquals( exp.getNoMDEntries(), decoded.getNoMDEntries() );
        
        MDEntryImpl expEntry     = (MDEntryImpl) exp.getMDEntries();
        MDEntryImpl decodedEntry = (MDEntryImpl) decoded.getMDEntries();
        
        for( int i=0 ; i < expEntries ; i++ ) {

            checkMDEntry( expEntry, decodedEntry );
            
            expEntry = expEntry.getNext();
            decodedEntry = decodedEntry.getNext();
        }
        
    }

    protected void checkMDEntry( MDEntryImpl expEntry, MDEntryImpl decodedEntry ) {
        assertEquals( expEntry.getSecurityIDSource(),   decodedEntry.getSecurityIDSource() );
        assertEquals( expEntry.getSecurityID(),         decodedEntry.getSecurityID() );
        assertEquals( expEntry.getMdUpdateAction(),     decodedEntry.getMdUpdateAction() );
        assertEquals( expEntry.getRepeatSeq(),          decodedEntry.getRepeatSeq() );
        assertEquals( expEntry.getMdEntryType(),        decodedEntry.getMdEntryType() );
        assertEquals( expEntry.getMdEntryPx(),          decodedEntry.getMdEntryPx(), Constants.TICK_WEIGHT );
        assertEquals( expEntry.getMdEntrySize(),        decodedEntry.getMdEntrySize() );
        assertEquals( expEntry.getMdEntryTime(),        decodedEntry.getMdEntryTime() );
    }

    @SuppressWarnings( "null" )
    private MDIncRefreshImpl makeUpdate( int numMDEntries ) {
        
        MDIncRefreshImpl inc = new MDIncRefreshImpl();
        
        inc.setSendingTime( System.currentTimeMillis() );
        inc.setMsgSeqNum( 1000000 );
        inc.setPossDupFlag( false );

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
            
            tmp.setSecurityIDSource( SecurityIDSource.ExchangeSymbol );
            tmp.setSecurityID( 12345678 );
            tmp.setMdUpdateAction( MDUpdateAction.New );
            tmp.setRepeatSeq( i+1 );
            tmp.setNumberOfOrders( i*10 );
            tmp.setMdPriceLevel( i+1 );
            tmp.setMdEntryType( MDEntryType.Bid );
            tmp.setMdEntryPx( 1000.12345 + i );
            tmp.setMdEntrySize( 100+i*10 );
            tmp.setMdEntryTime( 800000+i );
        }
        
        inc.setMDEntries( first ); 

        return inc;
    }
}
