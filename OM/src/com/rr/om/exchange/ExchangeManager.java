/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.exchange;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import com.rr.core.component.SMTComponent;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TLC;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Exchange;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.md.book.l3.L3IntIdBookFactoryLSE;
import com.rr.model.generated.codec.ItchLSEDecoder;
import com.rr.om.asia.bse.BSEExchange;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.emea.exchange.bats.BATSExchange;
import com.rr.om.emea.exchange.chix.CHIXExchange;
import com.rr.om.emea.exchange.eti.eurex.ETIMessageValidatorHFT;
import com.rr.om.emea.exchange.eti.eurex.EurexETIExchange;
import com.rr.om.emea.exchange.millenium.lse.LSEExchange;
import com.rr.om.emea.exchange.utp.enx.ENXExchange;
import com.rr.om.emea.exchange.utp.liffe.LiffeExchange;
import com.rr.om.model.id.IDGenerator;
import com.rr.om.model.instrument.ExchangeSession;
import com.rr.om.us.cme.CMEExchange;

public class ExchangeManager implements SMTComponent {

    private static final Logger                _log      = LoggerFactory.create( ExchangeManager.class );
    private static final ExchangeManager       _instance = new ExchangeManager( "ExchangeManagerSingleton" );

    private static final int NUM_SYMBOLS = 1024; // TODO get num symbols from config
    
    private       String                _id;
    private final Map<ZString,Exchange> _micMap   = new ConcurrentHashMap<ZString,Exchange>( 32, 0.75f, 2 );
    private final Map<ZString,Exchange> _recMap   = new ConcurrentHashMap<ZString,Exchange>( 32, 0.75f, 2 );
    
    private       IDGenerator           _mktClOrdIdGen;  // some exchanges require specific IDGenerator
    
    public ExchangeManager( String id ) {
        _id = id;
    }

    public void setId( String id ) {
        _id = id;
    }
    
    public static ExchangeManager instance() { return _instance; }
    
    public void register( IDGenerator mktClOrdIdGen ) {
        _mktClOrdIdGen = mktClOrdIdGen;
    }

    public void register( ZString         micCode, 
                          TimeZone        timezone, 
                          List<Long>      halfDays, 
                          Calendar        eodExpireEventSend, 
                          ExchangeSession session ) {
        
        Exchange exchange = null;
        
        if ( micCode.equals( "XLON" ) ) {
            ItchLSEDecoder.setBookFactory( L3IntIdBookFactoryLSE.class, NUM_SYMBOLS ); 
            L3IntIdBookFactoryLSE.setInstrumentLocator( new DummyInstrumentLocator() );
            exchange = new LSEExchange( micCode, timezone, session, eodExpireEventSend, halfDays );
        } else if ( micCode.equals( "XEUR" ) ) { // reuters mnemonic "d"
            // TODO need clean way of specifying use HFT or LFT message validator
            exchange = new EurexETIExchange( micCode, new ViewString("d"), timezone, session, _mktClOrdIdGen, eodExpireEventSend, halfDays, new ETIMessageValidatorHFT() );
        } else if ( micCode.equals( "XMAT" ) ) {
            exchange = new ENXExchange( micCode, new ViewString("MAT"), timezone, session, _mktClOrdIdGen, eodExpireEventSend, halfDays );
        } else if ( micCode.equals( "XLIS" ) ) {
            exchange = new ENXExchange( micCode, new ViewString("ENL"), timezone, session, _mktClOrdIdGen, eodExpireEventSend, halfDays );
        } else if ( micCode.equals( "XAMS" ) ) {
            exchange = new ENXExchange( micCode, new ViewString("ENA"), timezone, session, _mktClOrdIdGen, eodExpireEventSend, halfDays );
        } else if ( micCode.equals( "XPAR" ) ) {
            exchange = new ENXExchange( micCode, new ViewString("ENP"), timezone, session, _mktClOrdIdGen, eodExpireEventSend, halfDays );
        } else if ( micCode.equals( "XLIF" ) ) {
            exchange = new LiffeExchange( micCode, new ViewString("3"), timezone, session, _mktClOrdIdGen, eodExpireEventSend, halfDays );
        } else if ( micCode.equals( "XCHI" ) ) {
            exchange = new CHIXExchange( micCode, timezone, session, eodExpireEventSend, halfDays );
        } else if ( micCode.equals( "BATE" ) ) {
            exchange = new BATSExchange( micCode, timezone, session, eodExpireEventSend, halfDays );
        } else if ( micCode.equals( "XBOM" ) ) {
            exchange = new BSEExchange( micCode, timezone, session, _mktClOrdIdGen, eodExpireEventSend, halfDays );
        } else if ( micCode.equals( "XCME" ) ) {
            exchange = new CMEExchange( micCode, timezone, session, _mktClOrdIdGen, eodExpireEventSend, halfDays );
        } else {
            throw new SMTRuntimeException( "ExchangeManager attempt to add unsupported exchange mic=" + micCode );
        }
        
        register( exchange );
    }

    private void register( Exchange exchange ) {
        if ( _micMap.containsKey( exchange.getMIC() ) ) {
            throw new SMTRuntimeException( "ExchangeManager attempt to add duplicate exchange mic=" + exchange.getMIC() );
        }
        
        if ( _recMap.containsKey( exchange.getRecCode() ) ) {
            throw new SMTRuntimeException( "ExchangeManager attempt to add duplicate exchange rec=" + exchange.getRecCode() );
        }
        
        _micMap.put( exchange.getMIC(),     exchange );
        _recMap.put( exchange.getRecCode(), exchange );
        
        ReusableString msg = TLC.instance().pop();
        msg.append( "Exchange Registered :\n[" );
        exchange.toString( msg );
        _log.info( msg );
        TLC.instance().pushback( msg );
    }


    @Override
    public String getComponentId() {
        return _id;
    }

    public Exchange getByMIC( ZString mic ) {
        return _micMap.get( mic );
    }

    public Exchange getByREC( ZString rec ) {
        return _recMap.get( rec );
    }

    public void clear() {
        _log.warn( "Clearing ExchangeManager map" );
        
        _recMap.clear();
        _micMap.clear();
    }
}
