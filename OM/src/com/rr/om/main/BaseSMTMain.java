/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rr.core.admin.AdminAgent;
import com.rr.core.codec.BinaryDecoder;
import com.rr.core.codec.BinaryEncoder;
import com.rr.core.codec.CodecFactory;
import com.rr.core.codec.Decoder;
import com.rr.core.codec.Encoder;
import com.rr.core.codec.FixDecoder;
import com.rr.core.codec.FixEncoder;
import com.rr.core.codec.binary.fastfix.FastFixDecoder;
import com.rr.core.codec.binary.fastfix.FastFixEncoder;
import com.rr.core.codec.binary.fastfix.NonBlockingFastFixSocketSession;
import com.rr.core.collections.BlockingSyncQueue;
import com.rr.core.collections.ConcLinkedMsgQueueSingle;
import com.rr.core.collections.JavaConcMessageQueue;
import com.rr.core.collections.MessageQueue;
import com.rr.core.collections.NonBlockingSyncQueue;
import com.rr.core.collections.RingBufferMsgQueue1C1P;
import com.rr.core.collections.RingBufferMsgQueueSingleConsumer;
import com.rr.core.collections.SimpleMessageQueue;
import com.rr.core.dispatch.DirectDispatcher;
import com.rr.core.dispatch.MessageDispatcher;
import com.rr.core.dispatch.ThreadedDispatcher;
import com.rr.core.lang.Reusable;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.lang.stats.StatsCfgFile;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.LogEventLarge;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.ClientProfile;
import com.rr.core.model.Exchange;
import com.rr.core.model.MessageHandler;
import com.rr.core.os.NativeHooksImpl;
import com.rr.core.persister.DummyIndexPersister;
import com.rr.core.persister.IndexPersister;
import com.rr.core.persister.Persister;
import com.rr.core.persister.PersisterException;
import com.rr.core.persister.memmap.IndexMMPersister;
import com.rr.core.persister.memmap.MemMapPersister;
import com.rr.core.pool.SuperPool;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.properties.AppProps;
import com.rr.core.properties.CoreProps;
import com.rr.core.properties.PropertyGroup;
import com.rr.core.properties.PropertyTags.Tag;
import com.rr.core.session.AbstractSession;
import com.rr.core.session.MessageRouter;
import com.rr.core.session.MultiSessionDispatcher;
import com.rr.core.session.MultiSessionReceiver;
import com.rr.core.session.MultiSessionThreadedDispatcher;
import com.rr.core.session.MultiSessionThreadedReceiver;
import com.rr.core.session.PassThruRouter;
import com.rr.core.session.Session;
import com.rr.core.session.SessionException;
import com.rr.core.session.SessionThreadedDispatcher;
import com.rr.core.session.SimpleExecutableElement;
import com.rr.core.session.socket.SeqNumSession;
import com.rr.core.session.socket.SocketConfig;
import com.rr.core.thread.ControlThread;
import com.rr.core.thread.ControlThreadType;
import com.rr.core.thread.DualElementControlThread;
import com.rr.core.thread.SingleElementControlThread;
import com.rr.core.thread.TriElementControlThread;
import com.rr.core.utils.FileException;
import com.rr.core.utils.FileUtils;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;
import com.rr.core.warmup.WarmupRegistry;
import com.rr.inst.FixInstrumentLoader;
import com.rr.inst.InstrumentStore;
import com.rr.inst.MultiExchangeInstrumentStore;
import com.rr.inst.SingleExchangeInstrumentStore;
import com.rr.inst.ThreadsafeInstrumentStore;
import com.rr.md.fastfix.FastSocketConfig;
import com.rr.md.us.cme.CMEConfig;
import com.rr.md.us.cme.CMEFastFixSession;
import com.rr.md.us.cme.CMENonBlockingFastFixSession;
import com.rr.md.us.cme.XMLCMEConfigLoader;
import com.rr.md.us.cme.builder.CMERoundRobinSessionBuilder;
import com.rr.md.us.cme.reader.CMEFastFixDecoder;
import com.rr.md.us.cme.writer.CMEFastFixEncoder;
import com.rr.model.generated.fix.codec.CMEEncoder;
import com.rr.model.generated.fix.codec.CodecFactoryPopulator;
import com.rr.model.generated.fix.codec.CodecId;
import com.rr.model.generated.internal.events.factory.EventRecycler;
import com.rr.model.generated.internal.events.impl.ClientCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.ClientCancelledImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.MarketCancelledImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderSingleImpl;
import com.rr.om.client.ClientProfileManager;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;
import com.rr.om.emea.exchange.eti.gateway.ETIGatewayController;
import com.rr.om.emea.exchange.eti.trading.ETISocketConfig;
import com.rr.om.emea.exchange.eti.trading.ETISocketSession;
import com.rr.om.emea.exchange.millenium.MilleniumSocketConfig;
import com.rr.om.emea.exchange.millenium.MilleniumSocketSession;
import com.rr.om.emea.exchange.millenium.SequentialPersister;
import com.rr.om.emea.exchange.millenium.recovery.MilleniumRecoveryController;
import com.rr.om.emea.exchange.utp.UTPSocketConfig;
import com.rr.om.emea.exchange.utp.UTPSocketSession;
import com.rr.om.exchange.CodecLoader;
import com.rr.om.exchange.ExchangeManager;
import com.rr.om.exchange.loader.XMLExchangeLoader;
import com.rr.om.main.OMProps.Tags;
import com.rr.om.model.id.DailyLongIDGenerator;
import com.rr.om.order.OrderImpl;
import com.rr.om.order.OrderVersion;
import com.rr.om.order.collections.HashEntry;
import com.rr.om.session.SessionManager;
import com.rr.om.session.SessionType;
import com.rr.om.session.fixsocket.FixSocketConfig;
import com.rr.om.session.fixsocket.FixSocketSession;
import com.rr.om.session.fixsocket.NonBlockingFixSocketSession;
import com.rr.om.session.jmx.JMXSession;
import com.rr.om.warmup.FixTestUtils;
import com.rr.om.warmup.WarmupCMEFastFixSession;
import com.rr.om.warmup.WarmupCMENonBlockFastFixSession;
import com.rr.om.warmup.WarmupETIEurexSocketSession;
import com.rr.om.warmup.WarmupFixSocketSession;
import com.rr.om.warmup.WarmupMilleniumSocketSession;
import com.rr.om.warmup.WarmupMultiFixSocketSession;
import com.rr.om.warmup.sim.WarmupUtils;
import com.rr.om.warmup.units.WarmupCodecs;
import com.rr.om.warmup.units.WarmupHeartbeat;
import com.rr.om.warmup.units.WarmupJavaSpecific;
import com.rr.om.warmup.units.WarmupLogger;
import com.rr.om.warmup.units.WarmupRecycling;
import com.rr.om.warmup.units.WarmupRouters;

public abstract class BaseSMTMain {

    private static final Logger         _console             = LoggerFactory.console( BaseSMTMain.class );
    private static       ViewString     _persistFileNameBase = new ViewString( "./persist/daily" );

    private final String                _appName;
    private final String                _idPrefix;
    private final SessionManager        _sessMgr;
    private final ClientProfileManager  _clientProfileMgr;
    
    private final int                   _persistDatPageSize;
    private final long                  _persistIdxPreSize;
    private final long                  _persistDatPreSize;
    
    protected     InstrumentStore       _instrumentStore;

    private final byte[]                _dateStrBytes;
    private final String                _dateStr;
    private final boolean               _isTrace;

    private final int                   _warmupPortOffset;
    private final boolean               _warmupSpinLocks;
    
    private final Map<String,ControlThread>                  _controllerThreads    = new HashMap<String,ControlThread>();
    private final Map<String,MultiSessionThreadedDispatcher> _multiSessDispatchers = new HashMap<String,MultiSessionThreadedDispatcher>();
    private final Map<String,MultiSessionThreadedReceiver>   _multiSessReceivers   = new HashMap<String,MultiSessionThreadedReceiver>();
    
    protected CodecFactory _codecFactory;

    public static void prepare( String[] args, ThreadPriority main ) throws Exception {
        if ( args.length != 1 ){
            _console.info( "Error : missing property file argument" );
            _console.info( "Usage: {prog} propertyFile" );
            System.exit( -1 );
        }

        String propFile = args[0];

        AppProps.instance().init( propFile, OMProps.instance() );
        
        AppProps props = AppProps.instance();
        String appName = props.getProperty( CoreProps.APP_NAME ); 
        String baseDir = props.getProperty( CoreProps.PERSIST_DIR, false, "./persist/daily/" );
        _persistFileNameBase = new ViewString( baseDir + appName );
        
        StatsMgr.setStats( new StatsCfgFile() );
        StatsMgr.instance().initialise();

        ThreadUtils.init( props.getProperty( CoreProps.CPU_MASK_FILE ) );
        ThreadUtils.setPriority( Thread.currentThread(), main );
        
        LoggerFactory.setDebug( false );
        LoggerFactory.initLogging( props.getProperty( CoreProps.LOG_FILE_NAME, false, "./logs/" + appName ), 
                                   props.getIntProperty( CoreProps.MAX_LOG_SIZE, false, 10000000 ) );        

        LoggerFactory.setMinFlushPeriodSecs( props.getIntProperty( CoreProps.MIN_LOG_FLUSH, false, 30 ) );
        setupExchangeManager( props );
        
        AdminAgent.init( props.getIntProperty( CoreProps.ADMIN_PORT, false, 8000 )  );

        int warmupCount = props.getIntProperty( OMProps.WARMUP_COUNT, false, 3000 );
        WarmupRegistry.instance().setWarmupCount( warmupCount );
    }

    private static void setupExchangeManager( AppProps props ) {
        int idNumPrefix = props.getIntProperty( CoreProps.NUM_PREFIX, false, 10 );
        DailyLongIDGenerator numIdGen = new DailyLongIDGenerator( idNumPrefix, 19 ); // to fit ENX numeric ID format
        ExchangeManager.instance().register( numIdGen );
        XMLExchangeLoader loader = new XMLExchangeLoader( props.getProperty( OMProps.EXCHANGE_XML, false, "./config/exchange.xml" ) );
        loader.load();
    }
    
    public BaseSMTMain()  {
        AppProps props = AppProps.instance();
        String appName = props.getProperty( CoreProps.APP_NAME );
        
        _appName      = appName;
        _idPrefix     = props.getProperty( CoreProps.ID_PREFIX ); 
        _isTrace      = props.getBooleanProperty( CoreProps.APP_DEBUG, false, false );
        _codecFactory = new CodecFactory();
        
        int expOrders = props.getIntProperty( OMProps.EXPECTED_ORDERS, false, 1024 * 1024 );
        
        long estDataSize = 256l * expOrders * 8;
        
        int defPageSize = 10000000;
        long estPageSize = 4096l + Math.abs( estDataSize / 10);
        
        // get defaults for persistence sizes
        _persistDatPageSize = (estPageSize < defPageSize) ? (int)estPageSize : defPageSize;
        _persistIdxPreSize  = 1024l * 1024l + (expOrders*8*16);
        _persistDatPreSize  = 1024l * 1024l + estDataSize;
        
        _clientProfileMgr = new ClientProfileManager();
        _sessMgr          = new SessionManager( "sessMgr" );
        _instrumentStore  = new DummyInstrumentLocator(); // @TODO make inst locator plugable 

        _dateStrBytes = TimeZoneCalculator.instance().getDateLocal();
        _dateStr      = new String( _dateStrBytes );
        
        _warmupPortOffset = props.getIntProperty( OMProps.WARMUP_PORT_OFFSET, false, 0 );
        _warmupSpinLocks  = props.getBooleanProperty( OMProps.SEND_SPINLOCKS, false, false );

        CodecFactoryPopulator pop = new CodecLoader();
        pop.register( _codecFactory );
        
        WarmupUtils.setCodecFactory( _codecFactory );
    }
    
    public String getAppName() {
        return _appName;
    }
    
    protected abstract Logger log();

    protected String getIdPrefix() {
        return _idPrefix;
    }
    
    protected final SessionManager getSessionManager() {
        return _sessMgr;
    }
    
    /**
     * @throws SessionException
     * @throws FileException
     * @throws PersisterException
     */
    protected void init() throws SessionException, FileException, PersisterException {
        initWarmup();
        loadSessionSharedComponents();
        loadInstrumentStore();
    }

    /*
     * add lockable wrapper IF property inst.useLocks
     * type=Dummy (default ..  if no file) | SingleExchange | MultiExchange
     * inst.exchange=CME (if missing use Multi)
     */
    private void loadInstrumentStore() throws FileException {
        PropertyGroup instGroup = new PropertyGroup( "inst.", null, null );

        String storeType = instGroup.getProperty( OMProps.Tags.type, false, "dummy" );
        String instFile  = instGroup.getProperty( OMProps.Tags.file,  false, null );

        if ( instFile != null && ! storeType.equalsIgnoreCase( "dummy" ) ) {
            ZString exchangeREC = new ViewString( instGroup.getProperty( OMProps.Tags.REC ) );
            
            if ( storeType.equalsIgnoreCase( "multiExchange" ) ) {
                _instrumentStore = new MultiExchangeInstrumentStore( 1000 );

                _console.info( "Loading MultiExchangeInstrumentStore with instrument file " + instFile );
            } else if ( storeType.equalsIgnoreCase( "singleExchange" ) ) {
                Exchange e = ExchangeManager.instance().getByREC( new ViewString(exchangeREC) );
                
                if ( e == null ) {
                    throw new SMTRuntimeException( "Instrument store REC not in exchange manager : [" + exchangeREC + "]" );
                }
                
                _instrumentStore = new SingleExchangeInstrumentStore( e, 1000 );

                _console.info( "Loading SingleExchangeInstrumentStore with instrument file " + instFile + ", srcRec=" + exchangeREC );
            } else {
                throw new SMTRuntimeException( "Unsupported inst.type of " + storeType );
            }
            
            // use threadsafe inst store when intraday updates required
            
            boolean useThreadsafe = instGroup.getBoolProperty( OMProps.Tags.threadsafe,  false, false );

            if ( useThreadsafe ) {
                _console.info( "Wrapping instrument store with ThreadsafeInstrumentStore with instrument file " );

                _instrumentStore = new ThreadsafeInstrumentStore( _instrumentStore );
            }
            
            FixInstrumentLoader loader = new FixInstrumentLoader( _instrumentStore );
            loader.loadFromFile( instFile, exchangeREC );
            
        } else {
            _console.info( "Using DUMMY instrument store" );
        }
    }

    protected void initWarmup() {
        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        WarmupRegistry.instance().register( new WarmupCodecs( warmupCount ) );
        WarmupRegistry.instance().register( new WarmupRecycling( warmupCount ) );
        WarmupRegistry.instance().register( new WarmupLogger( warmupCount ) );
        WarmupRegistry.instance().register( new WarmupRouters( warmupCount ) );
        WarmupRegistry.instance().register( new WarmupJavaSpecific( warmupCount ) );
    }
    
    protected void warmup() {
        WarmupRegistry.instance().warmAll();
    }

    protected void commonSessionConnect() {
        for( Map.Entry<String,MultiSessionThreadedDispatcher> entry : _multiSessDispatchers.entrySet() ) {
            log().info( "Starting MultiSessionThreadedDispatcher " + entry.getKey() );
            MultiSessionThreadedDispatcher dis = entry.getValue();
            dis.start();
        }
        for( Map.Entry<String,MultiSessionThreadedReceiver> entry : _multiSessReceivers.entrySet() ) {
            log().info( "Starting MultiSessionThreadedReceiver " + entry.getKey() + ", this will kick off configured connections");
            MultiSessionThreadedReceiver rcv = entry.getValue();
            rcv.start();
        }
    }
    
    protected void presize( int expOrders ) {
        int orders              = Math.max( expOrders, 20000 );
        int recycledMax         = Math.min( expOrders, 50000 ); // allowing 100000 per second, assume in second get time to recycle
        
        int chainSize           = SizeConstants.DEFAULT_CHAIN_SIZE;
        int orderChains         = orders / chainSize ;
        int recycledEventChains = recycledMax / chainSize;
        int logChains           = Math.min( orderChains, SizeConstants.DEFAULT_MAX_LOG_CHAINS );
        int extraAlloc          = 50;

        log().info( "Presize based on " + orders + " orders will have orderChains=" + orderChains + ", chainSize=" + chainSize );
        
        presize( ClientNewOrderSingleImpl.class, orderChains,         chainSize, extraAlloc );
        presize( MarketNewOrderSingleImpl.class, recycledEventChains, chainSize, extraAlloc );
        presize( ClientNewOrderAckImpl.class,    recycledEventChains, chainSize, extraAlloc );
        presize( MarketNewOrderAckImpl.class,    recycledEventChains, chainSize, extraAlloc );
        presize( ClientCancelRequestImpl.class,  recycledEventChains, chainSize, extraAlloc );
        presize( MarketCancelRequestImpl.class,  recycledEventChains, chainSize, extraAlloc );
        presize( ClientCancelledImpl.class,      recycledEventChains, chainSize, extraAlloc );
        presize( MarketCancelledImpl.class,      recycledEventChains, chainSize, extraAlloc );

        presize( ReusableString.class,           10 * orderChains, chainSize, extraAlloc );
        presize( HashEntry.class,                2  * orderChains, chainSize, extraAlloc );

        presize( OrderImpl.class,                orderChains, chainSize, extraAlloc );
        presize( OrderVersion.class,             2 * orderChains, chainSize, extraAlloc );

        presize( OrderImpl.class,                orderChains, chainSize, extraAlloc );

        presize( LogEventLarge.class,            logChains, chainSize, 100 );
    }
    
    protected static <T extends Reusable<T>> void presize( Class<T> aclass, int chains, int chainSize, int extraAlloc ) {
        
        SuperPool<T> sp = SuperpoolManager.instance().getSuperPool( aclass );
        
        sp.init( chains, chainSize, extraAlloc );
    }
    
    protected static <T extends Enum<T>> T getProperty( PropertyGroup prop, 
                                                        Tag           propTag, 
                                                        Class<T>      enumClass ) {
        String val = prop.getProperty( propTag );
          
        T tVal;
        try {
            tVal = Enum.valueOf( enumClass, val );
        } catch( Exception e ) {
            throw new SMTRuntimeException( "BadProperty, " + prop.getPropertyGroup() + "." + propTag.toString() + " value (" + val + 
                                           ") is not valid entry for " + enumClass.getCanonicalName() );
        }
          
        return tVal;
    }

    protected static <T extends Enum<T>> T getProperty( String property, Class<T> enumClass, T defVal ) {
        String val = AppProps.instance().getProperty( property, false, null );
          
        if ( val == null ) return defVal;
        
        T tVal;
        try {
            tVal = Enum.valueOf( enumClass, val );
        } catch( Exception e ) {
            throw new SMTRuntimeException( "BadProperty, " + property + " value (" + val + 
                                           ") is not valid entry for " + enumClass.getCanonicalName() );
        }
          
        return tVal;
    }

    protected void loadSessionSharedComponents() {
        String[] multiControlThreads = AppProps.instance().getNodes( "multi.controlthread." );
        for( String controlId : multiControlThreads ) {
            
            controlId = controlId.toLowerCase();
            
            PropertyGroup propGroup = new PropertyGroup( "multi.controlthread." + controlId, null, null );

            ThreadPriority    priority  = getProperty( propGroup, Tags.threadPriority,  ThreadPriority.class );
            ControlThreadType type      = getProperty( propGroup, Tags.type,  ControlThreadType.class );
            
            ControlThread ctlThread = null;
            
            switch( type ) {
            case SingleElement:
                ctlThread = new SingleElementControlThread( controlId, priority );
                break;
            case DualElement:
                ctlThread = new DualElementControlThread( controlId, priority );
                break;
            case TriElement:
                ctlThread = new TriElementControlThread( controlId, priority );
                break;
            default:
                throw new SMTRuntimeException( "ControlThreadType " + type + " not supported" );
            }
            
            _controllerThreads.put( controlId, ctlThread );
        }
        
        String[] multifixNames = AppProps.instance().getNodes( "multifix." );
        for( String multifixId : multifixNames ) {
            PropertyGroup propGroup = new PropertyGroup( "multifix." + multifixId + ".controlthread", null, null );

            String inThreadControllerId  = propGroup.getProperty( Tags.in ).toLowerCase();
            String outThreadControllerId = propGroup.getProperty( Tags.out, false, null );
            
            ControlThread inThreadController  = _controllerThreads.get( inThreadControllerId );
            
            MultiSessionThreadedReceiver   rcv;
            rcv = new MultiSessionThreadedReceiver(   "MultiSessReceiver"   + multifixId, inThreadController );
            _multiSessReceivers.put( multifixId, rcv );

            if ( outThreadControllerId != null ) {
                outThreadControllerId = outThreadControllerId.toLowerCase();
                
                ControlThread outThreadController = _controllerThreads.get( outThreadControllerId );
    
                MultiSessionThreadedDispatcher dis;
                dis = new MultiSessionThreadedDispatcher( "MultiSessDispatcher" + multifixId, outThreadController ); 
                _multiSessDispatchers.put( multifixId, dis );
            }
        }
        
        NativeHooksImpl.instance(); // force native library load if appropriate
    }

    protected MessageDispatcher getProcessorDispatcher( PropertyGroup  propGroup, 
                                                        MessageQueue   queue,
                                                        String         name,
                                                        ThreadPriority priority ) {

        String controlThread = propGroup.getProperty( Tags.controlthread, false, null );

        if ( controlThread != null ) {
            ControlThread threadController  = _controllerThreads.get( controlThread );

            SimpleExecutableElement dis = new SimpleExecutableElement( name, threadController, queue ); 
            
            return dis;
        }
        
        return new ThreadedDispatcher( name, queue, priority );
    }

    
    protected IndexPersister createInboundPersister( String id, PropertyGroup base, ThreadPriority priority ) throws FileException {

        boolean dummyPersistence = base.getBoolProperty( Tags.dummyPersister, false, false );
        
        if ( dummyPersistence ) {
            _console.info( "Using dummy inbound persister for " + id );
            return new DummyIndexPersister();
        }

        int  persistDatPageSize = base.getIntProperty(  Tags.persistDatPageSize, false, _persistDatPageSize );
        long persistDatPreSize  = base.getLongProperty( Tags.persistDatPreSize,  false, _persistDatPreSize );
        long persistIdxPreSize  = base.getLongProperty( Tags.persistIdxPreSize,  false, _persistIdxPreSize );
        
        ReusableString fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/in/" ).append( id ).append( ".dat" );
        boolean removePersistence = base.getBoolProperty( Tags.forceRemovePersistence, false, false );
        if ( removePersistence ) FileUtils.rm( fileName.toString() );
        MemMapPersister persister = new MemMapPersister( new ViewString( id ), 
                                                         fileName, 
                                                         persistDatPreSize,
                                                         persistDatPageSize, 
                                                         priority );
        
        fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/in/" ).append( id ).append( ".idx" );
        if ( removePersistence ) FileUtils.rm( fileName.toString() );
        IndexPersister indexPersister = new IndexMMPersister( persister, 
                                                              new ViewString( "IDX_" + id ), 
                                                              fileName, 
                                                              persistIdxPreSize,
                                                              priority );
        
        return indexPersister;
    }

    protected IndexPersister createOutboundPersister( String id, PropertyGroup base, ThreadPriority priority ) throws FileException {

        boolean dummyPersistence = base.getBoolProperty( Tags.dummyPersister, false, false );
        
        if ( dummyPersistence ) {
            _console.info( "Using dummy outbound persister for " + id );
            return new DummyIndexPersister();
        }

        int  persistDatPageSize = base.getIntProperty(  Tags.persistDatPageSize, false, _persistDatPageSize );
        long persistDatPreSize  = base.getLongProperty( Tags.persistDatPreSize,  false, _persistDatPreSize );
        long persistIdxPreSize  = base.getLongProperty( Tags.persistIdxPreSize,  false, _persistIdxPreSize );
        
        ReusableString fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/out/" ).append( id ).append( ".dat" );
        boolean removePersistence = base.getBoolProperty( Tags.forceRemovePersistence, false, false );
        if ( removePersistence ) FileUtils.rm( fileName.toString() );
        MemMapPersister persister = new MemMapPersister( new ViewString( id ), 
                                                         fileName, 
                                                         persistDatPreSize,
                                                         persistDatPageSize, 
                                                         priority );
        
        fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/out/" ).append( id ).append( ".idx" );
        if ( removePersistence ) FileUtils.rm( fileName.toString() );
        IndexPersister indexPersister = new IndexMMPersister( persister, 
                                                              new ViewString( "IDX_" + id ), 
                                                              fileName, 
                                                              persistIdxPreSize,
                                                              priority );
        
        return indexPersister;
    }
    
    protected Session createSession( String             sessName, 
                                     ClientProfile      client,
                                     boolean            isDownstream, 
                                     SessionManager     sessMgr, 
                                     MessageHandler     proc, 
                                     Session            hub, 
                                     String             sidePropertyGroup ) throws FileException, SessionException, PersisterException {

        String minorDefaultGroup = "session.default.";
        String majorDefaultGroup = sidePropertyGroup + "default.";       // overrides baseDefaultPropGroup
        String propertyGroup     = sidePropertyGroup + sessName + ".";   // overrides defaults
        
        PropertyGroup propGroup = new PropertyGroup( propertyGroup, majorDefaultGroup, minorDefaultGroup );

        SessionType sessionType = getProperty( propGroup, OMProps.Tags.type,    SessionType.class );
        
        boolean logEvents = propGroup.getBoolProperty( Tags.logEvents, false, true );
        boolean logStats  = propGroup.getBoolProperty( Tags.logStats,  false, true );
        
        Session sess;
        
        if ( sessionType == SessionType.FIX ) {
            CodecId     codecId     = getProperty( propGroup, OMProps.Tags.codecId, CodecId.class );
            ThreadPriority    sendPriority   = getProperty( propGroup, Tags.outThreadPriority, ThreadPriority.class );
            MessageDispatcher sendDispatcher = getSessionDispatcher( propGroup, sessName + "DISPATCHER", sendPriority );
            WarmupRegistry.instance().register( new WarmupHeartbeat( _codecFactory, codecId ) );
            
            sess = createFixSession( codecId, client, sessName, isDownstream, sendDispatcher, sessMgr, proc, hub, propGroup );
        } else if ( sessionType == SessionType.JMX ) {
            
            CodecId     codecId     = getProperty( propGroup, OMProps.Tags.codecId, CodecId.class );
            sess = createJMXSession( codecId, client, sessName, sessMgr, proc, propGroup );

        } else if ( sessionType == SessionType.MultiFIX ) {
            
            CodecId     codecId     = getProperty( propGroup, OMProps.Tags.codecId, CodecId.class );
            WarmupRegistry.instance().register( new WarmupHeartbeat( _codecFactory, codecId ) );
            sess = createMultiFixSession( codecId, client, sessName, isDownstream, sessMgr, proc, hub, propGroup );

        } else if ( sessionType == SessionType.CMEFastFix ) {
            
            ThreadPriority    sendPriority   = getProperty( propGroup, Tags.outThreadPriority, ThreadPriority.class );
            MessageDispatcher sendDispatcher = getSessionDispatcher( propGroup, sessName + "DISPATCHER", sendPriority );
            
            sess = createCMEFastFixSession( sessName, sendDispatcher, sessMgr, proc, propGroup );
            
        } else if ( sessionType == SessionType.CMEMultiFastFix ) {
            
            sess = createCMEMultiFastFixSession( sessName, sessMgr, proc, propGroup );

        } else if ( sessionType == SessionType.UTP ) {
            CodecId           codecId     = getProperty( propGroup, OMProps.Tags.codecId, CodecId.class );
            ThreadPriority    sendPriority   = getProperty( propGroup, Tags.outThreadPriority, ThreadPriority.class );
            MessageDispatcher sendDispatcher = getSessionDispatcher( propGroup, sessName + "DISPATCHER", sendPriority );
            WarmupRegistry.instance().register( new WarmupHeartbeat( _codecFactory, codecId ) );
            sess = createUTPSession( codecId, client, sessName, isDownstream, sendDispatcher, sessMgr, proc, hub, propGroup );
        } else if ( sessionType == SessionType.Millenium ) {
            CodecId     codecId     = getProperty( propGroup, OMProps.Tags.codecId, CodecId.class );
            ThreadPriority    sendPriority   = getProperty( propGroup, Tags.outThreadPriority, ThreadPriority.class );
            MessageDispatcher sendDispatcher = getSessionDispatcher( propGroup, sessName + "DISPATCHER", sendPriority );
            
            MessageDispatcher recoverySendDispatcher = new SessionThreadedDispatcher( sessName + "Recovery", new BlockingSyncQueue(), ThreadPriority.Other );
            
            WarmupRegistry.instance().register( new WarmupHeartbeat( _codecFactory, codecId ) );
            sess = createMilleniumSession( codecId, client, sessName, isDownstream, sendDispatcher, sessMgr, proc, hub, propGroup, 
                                           recoverySendDispatcher );
        } else if ( sessionType == SessionType.ETIBSE ) {
            CodecId           codecId     = getProperty( propGroup, OMProps.Tags.codecId, CodecId.class );
            ThreadPriority    sendPriority   = getProperty( propGroup, Tags.outThreadPriority, ThreadPriority.class );
            MessageDispatcher sendDispatcher = getSessionDispatcher( propGroup, sessName + "DISPATCHER", sendPriority );
            
            MessageDispatcher gwySendDispatcher = new SessionThreadedDispatcher( sessName + "Gateway", new BlockingSyncQueue(), ThreadPriority.Other );
            
            WarmupRegistry.instance().register( new WarmupHeartbeat( _codecFactory, codecId ) );
            sess = createETISession( codecId, client, sessName, isDownstream, sendDispatcher, sessMgr, proc, hub, propGroup, gwySendDispatcher );
        } else if ( sessionType == SessionType.ETIEurex ) {
            CodecId           codecId     = getProperty( propGroup, OMProps.Tags.codecId, CodecId.class );
            ThreadPriority    sendPriority   = getProperty( propGroup, Tags.outThreadPriority, ThreadPriority.class );
            MessageDispatcher sendDispatcher = getSessionDispatcher( propGroup, sessName + "DISPATCHER", sendPriority );
            
            MessageDispatcher gwySendDispatcher = new SessionThreadedDispatcher( sessName + "Gateway", new BlockingSyncQueue(), ThreadPriority.Other );
            
            WarmupRegistry.instance().register( new WarmupHeartbeat( _codecFactory, codecId ) );
            sess = createETISession( codecId, client, sessName, isDownstream, sendDispatcher, sessMgr, proc, hub, propGroup, gwySendDispatcher );
        } else {
            throw new SMTRuntimeException( "SessionType " + sessionType.toString() + " not yet implemented" );
        }
        
        sess.setLogEvents( logEvents );
        sess.setLogStats( logStats );
        
        sessMgr.add( sess, isDownstream );
        
        if ( isDownstream ) {
            registerExchangeSession( sessMgr, propertyGroup, propGroup, sess );
        }
        
        return sess;
    }

    private Session createJMXSession( CodecId        codecId, 
                                      ClientProfile  client, 
                                      String         sessName, 
                                      SessionManager sessMgr, 
                                      MessageHandler proc,
                                      PropertyGroup  propGroup ) {
        
        MessageRouter inboundRouter = new PassThruRouter( proc ); 
        FixDecoder    decoder       = (FixDecoder) getDecoder( codecId, client, true );
        
        FixSocketConfig  config = new FixSocketConfig();
        config.setFixVersion( codecId.getFixVersion() );
        config.setCodecId( codecId );
        
        propGroup.reflectSet( OMProps.instance(), config );
        
        JMXSession sess = new JMXSession( sessName, config, inboundRouter, decoder, _sessMgr );
         
        return sess;
    }

    private void registerExchangeSession( SessionManager sessMgr, String propertyGroup, PropertyGroup propGroup, Session sess ) {
        String exchangeREC = AppProps.instance().getProperty( propertyGroup + OMProps.Tags.REC, false, null );
        
        if ( exchangeREC != null ) {
            Exchange e = ExchangeManager.instance().getByREC( new ViewString(exchangeREC) );
            
            if ( e == null ) {
                throw new SMTRuntimeException( "Session " + propGroup + " exchangeREC=" + exchangeREC + " exchange not registered with manager");
            }
            
            sessMgr.associateExchange( sess, e );
        }
    }

    protected MessageQueue getQueue( PropertyGroup propGroup, String name ) {

        boolean useSpinLocks = propGroup.getBoolProperty( Tags.enableSendSpinLock, false, false );

        String       queueType = propGroup.getProperty( Tags.queue, false, null );
        MessageQueue queue;
        
        if ( queueType != null ) {
            if ( queueType.equals( "NonBlockingSync" ) ) {
                queue = new NonBlockingSyncQueue();
            } else  if ( queueType.equalsIgnoreCase( "UNSAFE" ) ) {
                queue = new SimpleMessageQueue();
            } else  if ( queueType.equalsIgnoreCase( "SunCAS" ) ) {
                queue = new JavaConcMessageQueue();
            } else  if ( queueType.equalsIgnoreCase( "SMTCAS" ) ) {
                queue = new ConcLinkedMsgQueueSingle();
            } else  if ( queueType.equalsIgnoreCase( "BlockingSync" ) ) {
                queue = new BlockingSyncQueue();
            } else  if ( queueType.equalsIgnoreCase( "RingBuffer1P1C" ) ) {
                int queueSize = propGroup.getIntProperty( Tags.queuePresize, true, 65536 );
                queue = new RingBufferMsgQueue1C1P( queueSize );
            } else  if ( queueType.equalsIgnoreCase( "RingBuffer1C" ) ) {
                int queueSize = propGroup.getIntProperty( Tags.queuePresize, true, 65536 );
                queue = new RingBufferMsgQueueSingleConsumer( queueSize );
            } else {
                throw new SMTRuntimeException( "Configured 'queue' property of " + queueType + ", not valid must be one of " +
                                               " [NonBlockingSync|SunCAS|SMTCAS|BlockingSync|RingBuffer1C|RingBuffer1P1C]" );
            }
        } else {
            if ( useSpinLocks ) {
                queue = new ConcLinkedMsgQueueSingle();
            } else {
                queue = new BlockingSyncQueue();
            }
        }
        
        log().info( "QUEUE: Using " + queue.getClass().getSimpleName() + " for " + name );

        return queue;
    }

    private MessageDispatcher getSessionDispatcher( PropertyGroup propGroup, String name, ThreadPriority priority ) {
        MessageDispatcher dispatcher = null;

        boolean useSpinLocks   = propGroup.getBoolProperty( Tags.enableSendSpinLock, false, false );
        String  dispatcherType = propGroup.getProperty( Tags.dispatcher, false, null );

        if ( dispatcherType != null && dispatcherType.equals( "DirectDispatcher" ) ) {
            dispatcher = new DirectDispatcher();
            log().info( "DISPATCHER: Using " + dispatcher.getClass().getSimpleName() + " for " + name );
        } else  {
            String       queueType = propGroup.getProperty( Tags.queue, false, null );
            MessageQueue queue;
            
            if ( queueType != null ) {
                if ( queueType.equals( "NonBlockingSync" ) ) {
                    queue = new NonBlockingSyncQueue();
                } else  if ( queueType.equals( "SunCAS" ) ) {
                    queue = new JavaConcMessageQueue();
                } else  if ( queueType.equals( "SMTCAS" ) ) {
                    queue = new ConcLinkedMsgQueueSingle();
                } else  if ( queueType.equals( "BlockingSync" ) ) {
                    queue = new BlockingSyncQueue();
                } else  if ( queueType.equalsIgnoreCase( "RingBuffer1P1C" ) ) {
                    int queueSize = propGroup.getIntProperty( Tags.queuePresize, true, 65536 );
                    queue = new RingBufferMsgQueue1C1P( queueSize );
                } else  if ( queueType.equalsIgnoreCase( "RingBuffer1C" ) ) {
                    int queueSize = propGroup.getIntProperty( Tags.queuePresize, true, 65536 );
                    queue = new RingBufferMsgQueueSingleConsumer( queueSize );
                } else {
                    throw new SMTRuntimeException( "Configured 'queue' property of " + queueType + ", not valid must be one of " +
                                                   " [NonBlockingSync|SunCAS|SMTCAS|BlockingSync]" );
                }
            } else {
                if ( useSpinLocks ) {
                    queue = new ConcLinkedMsgQueueSingle();
                } else {
                    queue = new BlockingSyncQueue();
                }
            }
            
            dispatcher = new SessionThreadedDispatcher( name, queue, priority );
            log().info( "DISPATCHER: Using " + dispatcher.getClass().getSimpleName() + " with " + queue.getClass().getSimpleName() + 
                        ", priority=" + priority.toString() + " for " + name );
        }

        return dispatcher;
    }

    protected ClientProfile loadClientProfile( String sessName ) {
        String majorDefaultGroup = "client.default.";
        String propertyGroup     = "client." + sessName + ".";   // overrides defaults
        PropertyGroup clientPropGroup = new PropertyGroup( propertyGroup, majorDefaultGroup, null );
        String clientName  = clientPropGroup.getProperty( Tags.clientName );
        
        ClientProfile prof = _clientProfileMgr.create( new ViewString(sessName), new ViewString(clientName) );
        
        int lowThresholdPercent  = clientPropGroup.getIntProperty( Tags.lowThresholdPercent,  false, ClientProfile.DEFAULT_LOW_THRESHOLD );
        int medThresholdPercent  = clientPropGroup.getIntProperty( Tags.medThresholdPercent,  false, ClientProfile.DEFAULT_MED_THRESHOLD );
        int highThresholdPercent = clientPropGroup.getIntProperty( Tags.highThresholdPercent, false, ClientProfile.DEFAULT_HIGH_THRESHOLD );

        long maxTotalQty            = clientPropGroup.getLongProperty( Tags.maxTotalQty,            false, ClientProfile.DEFAULT_MAX_TOTAL_QTY );
        long maxTotalValueUSD       = clientPropGroup.getLongProperty( Tags.maxTotalOrderValueUSD,  false, ClientProfile.DEFAULT_MAX_TOTAL_VAL );
        long maxSingleOrderValueUSD = clientPropGroup.getLongProperty( Tags.maxSingleOrderValueUSD, false, ClientProfile.DEFAULT_MAX_SINGLE_VAL );
        int  maxSingleOrderQty      = clientPropGroup.getIntProperty(  Tags.maxSingleOrderQty,      false, ClientProfile.DEFAULT_MAX_SINGLE_QTY );
        
        prof.setThresholds( lowThresholdPercent, medThresholdPercent, highThresholdPercent );
        prof.setMaxTotalQty( maxTotalQty );
        prof.setMaxTotalOrderValueUSD( maxTotalValueUSD );
        prof.setMaxSingleOrderValueUSD( maxSingleOrderValueUSD );
        prof.setMaxSingleOrderQty( maxSingleOrderQty );
        
        return prof;
    }

    protected Session createFixSession( CodecId              codecId,
                                        ClientProfile        client, 
                                        String               sessName, 
                                        boolean              isDownstream, 
                                        MessageDispatcher    sendDispatcher,
                                        SessionManager       sessMgr, 
                                        MessageHandler       proc, 
                                        Session              hub,
                                        PropertyGroup        propGroup ) throws FileException, SessionException, PersisterException {

        FixSocketConfig  socketConfig = new FixSocketConfig();
        socketConfig.setFixVersion( codecId.getFixVersion() );
        socketConfig.setCodecId( codecId );
        
        propGroup.reflectSet( OMProps.instance(), socketConfig );
        
        socketConfig.validate();
        
        boolean trace = propGroup.getBoolProperty( Tags.trace, false, false );

        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( sessName, false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder          encoder           = (FixEncoder) getEncoder( codecId, outBuf, logHdrOut, trace ); 
        FixDecoder          decoder           = (FixDecoder) getDecoder( codecId, client, trace );
        FixDecoder          recoveryDecoder   = (FixDecoder) getRecoveryDecoder( codecId, client );
        ThreadPriority      receiverPriority  = getProperty( propGroup, Tags.inThreadPriority,       ThreadPriority.class );
        ThreadPriority      memmapPriority    = getProperty( propGroup, Tags.persistThreadPriority,  ThreadPriority.class );

        String dir = isDownstream ? "DOWN" : "UP";
        
        decoder.setValidateChecksum( true );
        recoveryDecoder.setValidateChecksum( true );
        
        socketConfig.setInboundPersister(  createInboundPersister(  sessName + "_" + dir + "_IN",  propGroup, memmapPriority ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( sessName + "_" + dir + "_OUT", propGroup, memmapPriority ) ); 
        
        FixSocketSession sess = new FixSocketSession( sessName, inboundRouter, socketConfig, sendDispatcher, encoder, decoder, 
                                                      recoveryDecoder, receiverPriority );

        sess.setChainSession( hub );
        sendDispatcher.setHandler( sess );

        postSessionCreate( encoder, sess, codecId );
        
        boolean disableNanoStats = propGroup.getBoolProperty( Tags.disableNanoStats, false, false );
        if ( disableNanoStats ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }
        
        registerWarmupFixSocketSession( codecId );
        
        return sess;
    }

    private void postSessionCreate( Encoder encoder, SeqNumSession sess, CodecId codecId ) {
        if ( codecId == CodecId.CME ) {
            ((CMEEncoder)encoder).setSession( sess );
        }
    }

    protected Session createMultiFixSession( CodecId              codecId,
                                             ClientProfile        client, 
                                             String               sessName, 
                                             boolean              isDownstream, 
                                             SessionManager       sessMgr, 
                                             MessageHandler       proc, 
                                             Session              hub,
                                             PropertyGroup        propGroup ) throws FileException, SessionException, PersisterException {

        FixSocketConfig  socketConfig = new FixSocketConfig();
        socketConfig.setFixVersion( codecId.getFixVersion() );
        socketConfig.setCodecId( codecId );
        
        propGroup.reflectSet( OMProps.instance(), socketConfig );
        
        socketConfig.validate();
        
        boolean trace = propGroup.getBoolProperty( Tags.trace, false, false );
        
        MessageRouter       inboundRouter     = new PassThruRouter( proc ); // currently only using single processor 
        int                 logHdrOut         = AbstractSession.getDataOffset( sessName, false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        FixEncoder          encoder           = (FixEncoder) getEncoder( codecId, outBuf, logHdrOut, trace ); 
        FixDecoder          decoder           = (FixDecoder) getDecoder( codecId, client, trace );
        FixDecoder          recoveryDecoder   = (FixDecoder) getRecoveryDecoder( codecId, client );
        ThreadPriority      memmapPriority    = getProperty( propGroup, Tags.persistThreadPriority,  ThreadPriority.class );

        String dir = isDownstream ? "DOWN" : "UP";

        decoder.setValidateChecksum( true );
        recoveryDecoder.setValidateChecksum( true );
        
        socketConfig.setInboundPersister(  createInboundPersister(  sessName + "_" + dir + "_IN",  propGroup, memmapPriority ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( sessName + "_" + dir + "_OUT", propGroup, memmapPriority ) ); 
        
        MultiSessionThreadedDispatcher dispatcher = findMultiFixDispatcher( propGroup );
        MultiSessionThreadedReceiver   receiver   = findMultiFixReceiver( propGroup );
        
        NonBlockingFixSocketSession sess;
        MessageQueue queue = getQueue( propGroup, sessName );
        
        sess = createNonBlockingFixSession( socketConfig, inboundRouter, encoder, decoder, recoveryDecoder, sessName, dispatcher, receiver, queue );

        sess.setChainSession( hub );
        dispatcher.addSession( sess );

        boolean disableNanoStats = propGroup.getBoolProperty( Tags.disableNanoStats, false, false );
        if ( disableNanoStats ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }
        
        postSessionCreate( encoder, sess, codecId );
        
        registerWarmupMultiSession( codecId );
        
        return sess;
    }

    protected Session createCMEFastFixSession( String               sessName, 
                                               MessageDispatcher    sendDispatcher,
                                               SessionManager       sessMgr, 
                                               MessageHandler       proc, 
                                               PropertyGroup        propGroup ) {

        int port = propGroup.getIntProperty( Tags.port, true, 0 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, port );
        
        propGroup.reflectSet( OMProps.instance(), socketConfig );

        ZString[] grps = formMulticastGroups( propGroup.getProperty( Tags.multicastGroups, true, null ) );
        socketConfig.setMulticast( true );
        socketConfig.setMulticastGroups( grps );

        socketConfig.validate();
        
        boolean trace = propGroup.getBoolProperty( Tags.trace, false, false );

        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 

        long                subChannelMask    = propGroup.getLongProperty( Tags.subChannelMask, false, -1L );

        CMEFastFixEncoder   encoder           = new CMEFastFixEncoder( "CMEWriter"+sessName, "data/cme/templates.xml", trace );
        Decoder             decoder           = new CMEFastFixDecoder( "CMEReader"+sessName, "data/cme/templates.xml", subChannelMask, trace );
        
        ThreadPriority      receiverPriority  = getProperty( propGroup, Tags.inThreadPriority,       ThreadPriority.class );

        CMEFastFixSession sess = new CMEFastFixSession( sessName, inboundRouter, socketConfig, sendDispatcher, encoder, decoder, receiverPriority );

        sendDispatcher.setHandler( sess );

        boolean disableNanoStats = propGroup.getBoolProperty( Tags.disableNanoStats, false, false );
        if ( disableNanoStats ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        } else {
            decoder.setNanoStats( true );
        }
        
        boolean enableEventPojoLogging = propGroup.getBoolProperty( Tags.logPojoEvents, false, false );
        sess.setLogPojos( enableEventPojoLogging );

        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        warmupCount = propGroup.getIntProperty( Tags.warmUpCount, false, warmupCount );
                
        registerWarmupCMEFastFixSocketSession( warmupCount );
        
        return sess;
    }

    protected Session createCMEMultiFastFixSession( String               sessName, 
                                                    SessionManager       sessMgr, 
                                                    MessageHandler       proc, 
                                                    PropertyGroup        propGroup ) {

        int port = propGroup.getIntProperty( Tags.port, true, 0 );
        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, port );
        
        propGroup.reflectSet( OMProps.instance(), socketConfig );
        
        ZString[] grps = formMulticastGroups( propGroup.getProperty( Tags.multicastGroups, true, null ) );
        socketConfig.setMulticast( true );
        socketConfig.setMulticastGroups( grps );

        socketConfig.validate();
        
        boolean trace = propGroup.getBoolProperty( Tags.trace, false, false );
        
        MessageRouter       inboundRouter     = new PassThruRouter( proc ); // currently only using single processor 

        long                subChannelMask    = propGroup.getLongProperty( Tags.subChannelMask, false, -1L );

        CMEFastFixEncoder   encoder           = new CMEFastFixEncoder( "CMEWriter"+sessName, "data/cme/templates.xml", trace );
        FastFixDecoder      decoder           = new CMEFastFixDecoder( "CMEReader"+sessName, "data/cme/templates.xml", subChannelMask, trace );

        MultiSessionThreadedDispatcher dispatcher = findMultiFixDispatcher( propGroup );
        MultiSessionThreadedReceiver   receiver   = findMultiFixReceiver( propGroup );
        
        CMENonBlockingFastFixSession sess;
        MessageQueue queue = getQueue( propGroup, sessName );
        
        sess = createCMENonBlockingFixSession( sessName, inboundRouter, socketConfig, dispatcher, receiver, encoder, decoder, queue );

        dispatcher.addSession( sess );

        boolean disableNanoStats = propGroup.getBoolProperty( Tags.disableNanoStats, false, false );
        if ( disableNanoStats ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        } else {
            decoder.setNanoStats( true );
        }
        
        boolean enableEventPojoLogging = propGroup.getBoolProperty( Tags.logPojoEvents, false, false );
        sess.setLogPojos( enableEventPojoLogging );

        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        warmupCount = propGroup.getIntProperty( Tags.warmUpCount, false, warmupCount );

        registerWarmupCMEMultiSession( warmupCount );
        
        return sess;
    }

    public static ZString[] formMulticastGroups( String grps ) {
        String[] grpArr = grps.split( "," );
        ZString[] zstrs = new ZString[ grpArr.length ];
        int i=0;
        for( String grp : grpArr ) {
            zstrs[i++] = new ViewString( grp.trim() );
        }
        return zstrs;
    }
    
    protected Persister createSequentialPersister( String id, String direction, PropertyGroup base, ThreadPriority priority ) throws FileException {

        int  persistDatPageSize = base.getIntProperty(  Tags.persistDatPageSize, false, _persistDatPageSize );
        long persistDatPreSize  = base.getLongProperty( Tags.persistDatPreSize,  false, _persistDatPreSize );
        
        ReusableString fileName = new ReusableString( _persistFileNameBase );
        fileName.append( '/' ).append( id.toLowerCase() ).append( "/" ).append( direction ).append( "/" ).append( id ).append( ".dat" );
        boolean removePersistence = base.getBoolProperty( Tags.forceRemovePersistence, false, false );
        if ( removePersistence ) FileUtils.rm( fileName.toString() );
        SequentialPersister persister = new SequentialPersister( new ViewString( id ), 
                                                               fileName, 
                                                               persistDatPreSize,
                                                               persistDatPageSize, 
                                                               priority );
        
        return persister;
    }

    protected Session createMilleniumSession( CodecId              codecId,
                                              ClientProfile        client, 
                                              String               sessName, 
                                              boolean              isDownstream, 
                                              MessageDispatcher    sendDispatcher,
                                              SessionManager       sessMgr, 
                                              MessageHandler       proc, 
                                              Session              hub,
                                              PropertyGroup        propGroup, 
                                              MessageDispatcher    recSendDispatcher ) throws FileException, SessionException, PersisterException {

        MilleniumSocketConfig  socketConfig = new MilleniumSocketConfig();
        propGroup.reflectSet( OMProps.instance(), socketConfig );
        socketConfig.validate();

        boolean trace = propGroup.getBoolProperty( Tags.trace, false, false );
        
        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( sessName, false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder             encoder           = getEncoder( codecId, outBuf, logHdrOut, trace ); 
        Decoder             decoder           = getDecoder( codecId, client, trace );
        Decoder             recoveryDecoder   = getRecoveryDecoder( codecId, client );
        ThreadPriority      receiverPriority  = getProperty( propGroup, Tags.inThreadPriority,       ThreadPriority.class );
        ThreadPriority      memmapPriority    = getProperty( propGroup, Tags.persistThreadPriority,  ThreadPriority.class );

        String dir = isDownstream ? "DOWN" : "UP";
        
        socketConfig.setInboundPersister(  createSequentialPersister( sessName + "_" + dir + "_IN",  "in",  propGroup, memmapPriority ) ); 
        socketConfig.setOutboundPersister( createSequentialPersister( sessName + "_" + dir + "_OUT", "out", propGroup, memmapPriority ) );
        socketConfig.setRecoverySession( false );
        
        // setup for recovery session
        String recoverySessName = sessName + "Recovery";
        MilleniumSocketConfig  recoverySocketConfig = new MilleniumSocketConfig();
        propGroup.reflectSet( OMProps.instance(), recoverySocketConfig );
        recoverySocketConfig.setHostname( socketConfig.getRecoveryHostname() );
        recoverySocketConfig.setPort( socketConfig.getRecoveryPort() );        
        recoverySocketConfig.setRecoverySession( true );

        MessageRouter       recInboundRouter     = new PassThruRouter( proc ); 
        int                 recLogHdrOut         = AbstractSession.getDataOffset( recoverySessName, false );
        byte[]              recOutBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder             recEncoder           = getEncoder( codecId, recOutBuf, recLogHdrOut, trace ); 
        Decoder             recDecoder           = getDecoder( codecId, client, trace );
        Decoder             recRecoveryDecoder   = getRecoveryDecoder( codecId, client );
        ThreadPriority      lowPriority          = ThreadPriority.Other;

        recoverySocketConfig.setInboundPersister(  createSequentialPersister( recoverySessName + "_" + dir + "_IN",  "in",  propGroup, lowPriority ) ); 
        recoverySocketConfig.setOutboundPersister( createSequentialPersister( recoverySessName + "_" + dir + "_OUT", "out", propGroup, lowPriority ) );
        
        // create reccovery session
        MilleniumSocketSession recSess = new MilleniumSocketSession( recoverySessName, recInboundRouter, recoverySocketConfig, recSendDispatcher, 
                                                                     recEncoder, recDecoder, recRecoveryDecoder, lowPriority );

        // create reccovery session
        MilleniumSocketSession sess = new MilleniumSocketSession( sessName, inboundRouter, socketConfig, sendDispatcher, encoder, decoder, 
                                                                  recoveryDecoder, receiverPriority );

        sess.getController().setRecoveryController( (MilleniumRecoveryController) recSess.getController() );
        
        sess.setChainSession( hub );
        sendDispatcher.setHandler( sess );
        
        recSess.setChainSession( hub );
        recSendDispatcher.setHandler( recSess );

        boolean disableNanoStats = propGroup.getBoolProperty( Tags.disableNanoStats, false, false );
        if ( disableNanoStats ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }
        
        boolean enableEventPojoLogging = propGroup.getBoolProperty( Tags.logPojoEvents, false, false );
        sess.setLogPojos( enableEventPojoLogging );
        recSess.setLogPojos( enableEventPojoLogging );
        
        recSess.setLogStats( false );
        
        registerWarmupMilleniumSession();
        
        return sess;
    }

    protected Session createETISession( CodecId              codecId,
                                        ClientProfile        client, 
                                        String               sessName, 
                                        boolean              isDownstream, 
                                        MessageDispatcher    sendDispatcher,
                                        SessionManager       sessMgr, 
                                        MessageHandler       proc, 
                                        Session              hub,
                                        PropertyGroup        propGroup, 
                                        MessageDispatcher    recSendDispatcher ) throws FileException, SessionException, PersisterException {

        ETISocketConfig  socketConfig = new ETISocketConfig();
        propGroup.reflectSet( OMProps.instance(), socketConfig );
        socketConfig.validate();

        boolean trace = propGroup.getBoolProperty( Tags.trace, false, false );

        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( sessName, false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder             encoder           = getEncoder( codecId, outBuf, logHdrOut, trace ); 
        Decoder             decoder           = getDecoder( codecId, client, trace );
        Decoder             recoveryDecoder   = getRecoveryDecoder( codecId, client );
        ThreadPriority      receiverPriority  = getProperty( propGroup, Tags.inThreadPriority,       ThreadPriority.class );
        ThreadPriority      memmapPriority    = getProperty( propGroup, Tags.persistThreadPriority,  ThreadPriority.class );
        
        String dir = isDownstream ? "DOWN" : "UP";
        
        socketConfig.setInboundPersister(  createSequentialPersister( sessName + "_" + dir + "_IN",  "in",  propGroup, memmapPriority ) ); 
        socketConfig.setOutboundPersister( createSequentialPersister( sessName + "_" + dir + "_OUT", "out", propGroup, memmapPriority ) );
        socketConfig.setGatewaySession( false );
        
        // setup for gateway session
        String gwySessName = sessName + "Gateway";
        ETISocketConfig  gwyConfig = new ETISocketConfig();
        propGroup.reflectSet( OMProps.instance(), gwyConfig );
        gwyConfig.setHostname( socketConfig.getHostname() );
        gwyConfig.setGatewaySession( true );

        gwyConfig.setPort( propGroup.getIntProperty( Tags.port, true, 0 ) );         
        socketConfig.setPort( socketConfig.getEmulationTestPort() );         
        
        MessageRouter       recInboundRouter     = new PassThruRouter( proc ); 
        int                 recLogHdrOut         = AbstractSession.getDataOffset( gwySessName, false );
        byte[]              recOutBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder             recEncoder           = getEncoder( codecId, recOutBuf, recLogHdrOut, trace ); 
        Decoder             recDecoder           = getDecoder( codecId, client, trace );
        Decoder             recRecoveryDecoder   = getRecoveryDecoder( codecId, client );
        ThreadPriority      lowPriority          = ThreadPriority.Other;

        gwyConfig.setInboundPersister( new DummyIndexPersister() ); 
        gwyConfig.setOutboundPersister( new DummyIndexPersister() );
        
        // create reccovery session
        ETISocketSession gwySess = new ETISocketSession( gwySessName, recInboundRouter, gwyConfig, recSendDispatcher, 
                                                                     recEncoder, recDecoder, recRecoveryDecoder, lowPriority );

        // create reccovery session
        ETISocketSession tradingSess = new ETISocketSession( sessName, inboundRouter, socketConfig, sendDispatcher, encoder, decoder, 
                                                                  recoveryDecoder, receiverPriority );

        ((ETIGatewayController)gwySess.getController()).setTradingSession( tradingSess );
        
        tradingSess.setChainSession( hub );
        sendDispatcher.setHandler( tradingSess );
        
        gwySess.setChainSession( hub );
        recSendDispatcher.setHandler( gwySess );

        boolean disableNanoStats = propGroup.getBoolProperty( Tags.disableNanoStats, false, false );
        if ( disableNanoStats ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            tradingSess.setLogStats( false );
        }
        
        boolean enableEventPojoLogging = propGroup.getBoolProperty( Tags.logPojoEvents, false, false );
        tradingSess.setLogPojos( enableEventPojoLogging );
        gwySess.setLogPojos( enableEventPojoLogging );
        
        gwySess.setLogStats( false );
        
        tradingSess.setGatewaySession( gwySess );
        
        registerWarmupETISession();
        
        return tradingSess;
    }

    protected Session createUTPSession( CodecId              codecId,
                                        ClientProfile        client, 
                                        String               sessName, 
                                        boolean              isDownstream, 
                                        MessageDispatcher    sendDispatcher,
                                        SessionManager       sessMgr, 
                                        MessageHandler       proc, 
                                        Session              hub,
                                        PropertyGroup        propGroup ) throws FileException, SessionException, PersisterException {

        UTPSocketConfig  socketConfig = new UTPSocketConfig();
        
        propGroup.reflectSet( OMProps.instance(), socketConfig );
        
        socketConfig.validate();
        
        boolean trace = propGroup.getBoolProperty( Tags.trace, false, false );
        
        MessageRouter       inboundRouter     = new PassThruRouter( proc ); 
        int                 logHdrOut         = AbstractSession.getDataOffset( sessName, false );
        byte[]              outBuf            = new byte[ SizeConstants.DEFAULT_MAX_SESSION_BUFFER ];
        Encoder             encoder           = getEncoder( codecId, outBuf, logHdrOut, trace ); 
        Decoder             decoder           = getDecoder( codecId, client, trace );
        Decoder             recoveryDecoder   = getRecoveryDecoder( codecId, client );
        ThreadPriority      receiverPriority  = getProperty( propGroup, Tags.inThreadPriority,       ThreadPriority.class );
        ThreadPriority      memmapPriority    = getProperty( propGroup, Tags.persistThreadPriority,  ThreadPriority.class );

        String dir = isDownstream ? "DOWN" : "UP";
        
        socketConfig.setInboundPersister(  createInboundPersister(  sessName + "_" + dir + "_IN",  propGroup, memmapPriority ) ); 
        socketConfig.setOutboundPersister( createOutboundPersister( sessName + "_" + dir + "_OUT", propGroup, memmapPriority ) ); 
        
        UTPSocketSession sess = new UTPSocketSession( sessName, inboundRouter, socketConfig, sendDispatcher, encoder, decoder, 
                                                      recoveryDecoder, receiverPriority );

        sess.setChainSession( hub );
        sendDispatcher.setHandler( sess );

        boolean disableNanoStats = propGroup.getBoolProperty( Tags.disableNanoStats, false, false );
        if ( disableNanoStats ) {
            encoder.setNanoStats( false );
            decoder.setNanoStats( false );
            sess.setLogStats( false );
        }

        boolean enableEventPojoLogging = propGroup.getBoolProperty( Tags.logPojoEvents, false, false );
        sess.setLogPojos( enableEventPojoLogging );
        
        registerWarmupUTPSession();
        
        return sess;
    }

    protected NonBlockingFixSocketSession createNonBlockingFixSession( FixSocketConfig socketConfig, 
                                                                       MessageRouter                  inboundRouter, 
                                                                       FixEncoder                     encoder,
                                                                       FixDecoder                     decoder, 
                                                                       FixDecoder                     recoveryDecoder, 
                                                                       String                         name,
                                                                       MultiSessionThreadedDispatcher dispatcher, 
                                                                       MultiSessionThreadedReceiver   receiver,
                                                                       MessageQueue                   dispatchQueue )throws SessionException, PersisterException {
        
        NonBlockingFixSocketSession sess = new NonBlockingFixSocketSession( name, 
                                                                            inboundRouter, 
                                                                            socketConfig, 
                                                                            dispatcher,
                                                                            receiver,
                                                                            encoder, 
                                                                            decoder, 
                                                                            recoveryDecoder,
                                                                            dispatchQueue );
        return sess;
    }

    protected CMENonBlockingFastFixSession createCMENonBlockingFixSession( String                          name, 
                                                                           MessageRouter                   inboundRouter, 
                                                                           SocketConfig                    config, 
                                                                           MultiSessionDispatcher          dispatcher,
                                                                           MultiSessionReceiver            receiver,
                                                                           FastFixEncoder                  encoder,
                                                                           FastFixDecoder                  decoder, 
                                                                           MessageQueue                    dispatchQueue ) {
    
        CMENonBlockingFastFixSession sess = new CMENonBlockingFastFixSession( name, 
                                                                              inboundRouter, 
                                                                              config, 
                                                                              dispatcher,
                                                                              receiver,
                                                                              encoder, 
                                                                              decoder, 
                                                                              dispatchQueue );
        return sess;
    }

    protected void loadTradesFromFile( String fileName, List<byte[]> templateRequests ) throws IOException {
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader( new FileReader( fileName ) );
            for ( String line = reader.readLine() ; line != null ; line = reader.readLine() ) {

                String req = line.trim();

                if ( req.length() > 0 ) {
                    byte[] msg = FixTestUtils.semiColonToFixDelim( req );

                    templateRequests.add( msg );

                    if ( _isTrace )
                        log().info( "Template Request '" + line + "' len=" + msg.length );
                }
            }
        } finally {
            FileUtils.close( reader );
        }
    }

    /**
     * session.builder.cme.channelList=7,9,10
     * session.builder.cme.configFile=./config/cme/autocert/config.xml
     * session.builder.cme.env=autocert 
     * session.builder.cme.multifixList=up
     */
    protected void createCMEFastFixSessionsViaBuilder( MessageHandler[] handlers ) {
        String minorDefaultGroup = "session.default.";
        String majorDefaultGroup = "session.builder.default.";       // overrides baseDefaultPropGroup
        String propertyGroup     = "session.builder.cme.";           // overrides defaults
        
        PropertyGroup propGroup = new PropertyGroup( propertyGroup, majorDefaultGroup, minorDefaultGroup );

        String channelList = propGroup.getProperty( OMProps.Tags.channelList, false, null );

        if ( channelList == null || channelList.length() == 0 ) {
        
            _console.info( "No channelList entry, skip CME fast fix session generation" );
            return;
        }
        
        String cmeConfigFile = propGroup.getProperty( OMProps.Tags.configFile );
        
        XMLCMEConfigLoader cfgLoader = new XMLCMEConfigLoader( cmeConfigFile );
        CMEConfig cmeCfg = cfgLoader.load();
        
        boolean logEvents         = propGroup.getBoolProperty( Tags.logEvents, false, false );
        boolean logSoftFix        = propGroup.getBoolProperty( Tags.trace, false, false );
        boolean disableNanoStats  = propGroup.getBoolProperty( Tags.disableNanoStats,  false, true );

        FastSocketConfig  socketConfig = new FastSocketConfig( EventRecycler.class, false, new ViewString("localhost"), null, 0 );
        propGroup.reflectSet( OMProps.instance(), socketConfig );
        socketConfig.setMulticast( true );

        long subChannelMask = propGroup.getLongProperty( Tags.subChannelMask, false, -1L );
        String multifixList = propGroup.getProperty( OMProps.Tags.multifixList );
        
        MultiSessionThreadedReceiver[] multiplexReceivers = makeMultiplexReceivers( multifixList );
        CMERoundRobinSessionBuilder b = new CMERoundRobinSessionBuilder( "CMESessBldr", cmeCfg, channelList, _sessMgr, handlers, multiplexReceivers, 
                                                                         socketConfig, null );

        b.setDisableNanoStats( disableNanoStats );
        b.setEnableEventPojoLogging( logEvents );
        b.setTrace( logSoftFix );

        b.setSubChannelMask( subChannelMask );
        
        List<NonBlockingFastFixSocketSession> sessions = b.create();
        
        _console.info( "Created " + sessions.size() + " CME fast fix sessions" );
    }
    
    private MultiSessionThreadedReceiver[] makeMultiplexReceivers( String multifixList ) {
        String[] parts = multifixList.split( "," );
        
        MultiSessionThreadedReceiver[] multiplexReceivers = new MultiSessionThreadedReceiver[ parts.length ];
        
        int next = 0;
        
        for( String multiFixId : parts ) {
            MultiSessionThreadedReceiver rcv = _multiSessReceivers.get( multiFixId.trim() );
            
            if ( rcv == null ) {
                throw new SMTRuntimeException( "Invalid multiFixReceiverId " + multiFixId + " in list [" + multifixList + "] which is not configured");
            }
            
            multiplexReceivers[ next++ ] = rcv;
        }
        
        return multiplexReceivers;
    }

    public static void loadSampleData( String fileName, List<byte[]> templateRequests, int minLineLen ) throws IOException {
        BufferedReader reader = null;
        
        try {
            reader = new BufferedReader( new FileReader( fileName ) );
            for ( String line = reader.readLine() ; line != null ; line = reader.readLine() ) {

                String req = line.trim();

                if ( req.length() > minLineLen ) {
                    byte[] msg = req.getBytes();

                    templateRequests.add( msg );
                }
            }
        } finally {
            FileUtils.close( reader );
        }
    }
    
    private MultiSessionThreadedReceiver findMultiFixReceiver( PropertyGroup propGroup ) {
        String rcvId = propGroup.getProperty( Tags.multifix );
        MultiSessionThreadedReceiver rcv = _multiSessReceivers.get( rcvId );
        
        if ( rcv == null ) {
            throw new SMTRuntimeException( "MultiFixSession " + propGroup + " specifies multiFixReceiver " + rcvId + " which is not configured");
        }
        
        return rcv;
    }

    protected MultiSessionThreadedDispatcher findMultiFixDispatcher( PropertyGroup propGroup ) {
        String disId = propGroup.getProperty( Tags.multifix );
        MultiSessionThreadedDispatcher dis = _multiSessDispatchers.get( disId );
        
        if ( dis == null ) {
            throw new SMTRuntimeException( "MultiFixSession " + propGroup + " specifies multiFixDispatcher " + disId + 
                                           " which is not configured" );
        }
        
        return dis;
    }

    private Decoder getDecoder( CodecId id, ClientProfile client, boolean debug ) {
        Decoder decoder = _codecFactory.getDecoder( id );
        decoder.setClientProfile( client );
        decoder.setInstrumentLocator( _instrumentStore );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setDate( _dateStr );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        if ( decoder instanceof BinaryDecoder ) {
            ((BinaryDecoder)decoder).setDebug( debug );
        }
        return decoder;
    }

    private Decoder getRecoveryDecoder( CodecId id, ClientProfile client ) {
        Decoder decoder = _codecFactory.getRecoveryDecoder( id );
        decoder.setClientProfile( client );
        decoder.setInstrumentLocator( _instrumentStore );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setDate( _dateStr );
        decoder.setTimeZoneCalculator( calc );
        decoder.setReceived( Utils.nanoTime() );
        return decoder;
    }

    private Encoder getEncoder( CodecId id, byte[] buf, int offset, boolean debug ) {
        Encoder encoder = _codecFactory.getEncoder( id, buf, offset );
        TimeZoneCalculator calc = new TimeZoneCalculator();
        calc.setDate( _dateStr );
        encoder.setTimeZoneCalculator( calc );
        if ( encoder instanceof BinaryEncoder ) {
            ((BinaryEncoder)encoder).setDebug( debug );
        }
        return encoder;
    }
    
    protected final void registerWarmupFixSocketSession( CodecId codecId ) {
        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        WarmupFixSocketSession warmSess = WarmupFixSocketSession.create( _appName, _warmupPortOffset, _warmupSpinLocks, warmupCount, codecId );
        
        WarmupRegistry.instance().register( warmSess );
    }

    protected final void registerWarmupCMEFastFixSocketSession( int warmUpCount ) {
        WarmupCMEFastFixSession warmSess = WarmupCMEFastFixSession.create( _appName, _warmupPortOffset, _warmupSpinLocks, warmUpCount, _instrumentStore );
        WarmupRegistry.instance().register( warmSess );
    }

    private void registerWarmupCMEMultiSession( int warmUpCount ) {
        WarmupCMENonBlockFastFixSession warmSess = WarmupCMENonBlockFastFixSession.create( _appName, _warmupPortOffset + 80, _warmupSpinLocks, warmUpCount, _instrumentStore );
        WarmupRegistry.instance().register( warmSess );
    }

    private void registerWarmupMultiSession( CodecId codecId ) {
        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        WarmupMultiFixSocketSession warmSess = WarmupMultiFixSocketSession.create( _appName, _warmupPortOffset + 50, _warmupSpinLocks, warmupCount, codecId );
        // @TODO remove magic 50 with port manager, config should define port range for app
        
        WarmupRegistry.instance().register( warmSess );
    }

    private void registerWarmupMilleniumSession() {
        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        WarmupMilleniumSocketSession warmSess = WarmupMilleniumSocketSession.create( _appName, _warmupPortOffset, _warmupSpinLocks, warmupCount );
        
        WarmupRegistry.instance().register( warmSess );
    }

    private void registerWarmupETISession() {
        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        WarmupETIEurexSocketSession warmSess = WarmupETIEurexSocketSession.create( _appName, _warmupPortOffset, _warmupSpinLocks, warmupCount );
        
        WarmupRegistry.instance().register( warmSess );
    }

    private void registerWarmupUTPSession() {
        int warmupCount = WarmupRegistry.instance().getWarmupCount();
        WarmupMilleniumSocketSession warmSess = WarmupMilleniumSocketSession.create( _appName, _warmupPortOffset, _warmupSpinLocks, warmupCount );
        
        WarmupRegistry.instance().register( warmSess );
    }
}
