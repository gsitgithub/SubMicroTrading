/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.component.builder;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import com.rr.core.admin.AdminAgent;
import com.rr.core.component.PropertyHelper;
import com.rr.core.component.SMTComponent;
import com.rr.core.component.SMTControllableComponent;
import com.rr.core.component.SMTInitialisableComponent;
import com.rr.core.component.SMTStartContext;
import com.rr.core.component.SMTWarmableComponent;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.stats.StatsCfgFile;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.os.NativeHooksImpl;
import com.rr.core.pool.SuperpoolManager;
import com.rr.core.properties.AppProps;
import com.rr.core.properties.CoreProps;
import com.rr.core.tasks.ScheduledEvent;
import com.rr.core.tasks.Scheduler;
import com.rr.core.utils.ShutdownManager;
import com.rr.core.utils.ShutdownManager.Callback;
import com.rr.core.utils.SystemStatus;
import com.rr.core.utils.ThreadPriority;
import com.rr.core.utils.ThreadUtils;
import com.rr.core.utils.Utils;


/**
 * Bootstraps an SMT set of components forming an application from a property file
 * 
 * use -Dapp.forceConsole=true   to force console logging
 * 
 * Sequence is as follows
 * 
 *      instantiate all components wether via loader or direct
 *      
 *      for each component that can : INITIALISE
 *      
 *      for each component that can : PREPARE
 *          (prepare is used for second phase initialisation that requires other components to have had phase 1 initialisation)
 *          components should use this to verify required references have all been populated
 *          
 *      for each component that can : WARMUP
 *          (components may have their own warmup or register with warmupController to avoid duplication)
 *          
 *      for each component that can : START
 */
public class AntiSpringBootstrap {
    private static final Logger    _console         = LoggerFactory.console( AntiSpringBootstrap.class );
    private static final ErrorCode FAILED           = new ErrorCode( "SPB100", "Exception in main" );
            static       Logger    _log;
    
    public static void main( String[] args ) {
        
        try {
            basicSetup( args );

            final SMTPropertyComponentBuilder loader = new SMTPropertyComponentBuilder( AppProps.instance() );
            
            loader.init();
            
            NativeHooksImpl.instance();
            
            initComponents( loader );
            
            prepareComponents( loader );
     
            ShutdownManager.instance().register( new Callback() {
                @Override
                public void shuttingDown() {
                    stopComponents( loader );
                }} );
            
            warmupComponents( loader );

            SuperpoolManager.instance().resetPoolStats();
            
            Utils.invokeGC();
            
            startComponents( loader );
            
            _console.info( "Completed" );
            
            _console.info( "ENTERING MAIN LOOP - ctrl-C to stop program" );

            System.out.flush();
            System.err.flush();
            
            _log.info( "ENTERING MAIN LOOP - ctrl-C to stop program" );
            
            while( true ) {
                try {
                    Utils.delay( 1000 );
                } catch( Throwable t ) {
                    System.out.println( t.getMessage() );
                }
            }
            
        } catch( Exception e ) {
            
            _console.error( FAILED, "", e );
        }
    }

    private static void initComponents( SMTPropertyComponentBuilder loader ) {
        _console.info( "initComponents started" );

        SMTStartContext context = loader.getComponent( "appContext" );
        
        Collection<SMTComponent> components = new HashSet<SMTComponent>( loader.getComponents() );
        
        Set<String> ids = new HashSet<String>();
        
        for( SMTComponent component : components ) {
            if ( component instanceof SMTInitialisableComponent ) {
                _console.info( "initComponents:  init " + component.getComponentId() );

                SMTInitialisableComponent ic = (SMTInitialisableComponent) component;
                
                ic.init( context );
                
                ids.add( component.getComponentId() );
            }
        }

        // init components created in init phase
        
        Collection<SMTComponent> latestComponents = new HashSet<SMTComponent>( loader.getComponents() );
        
        for( SMTComponent component : latestComponents ) {
            if ( ! ids.contains( component.getComponentId() ) ) {
                if ( component instanceof SMTInitialisableComponent ) {
                    _console.info( "initComponents: second change : init " + component.getComponentId() );
    
                    SMTInitialisableComponent ic = (SMTInitialisableComponent) component;
                    
                    ic.init( context );

                    ids.add( component.getComponentId() );
                }
            }
        }
    }

    private static void prepareComponents( SMTPropertyComponentBuilder loader ) {
        _console.info( "prepareComponents start" );

        Collection<SMTComponent> components = loader.getComponents();
        
        for( SMTComponent component : components ) {
            if ( component instanceof SMTInitialisableComponent ) {
                _console.info( "prepareComponents:  prepare " + component.getComponentId() );

                SMTInitialisableComponent ic = (SMTInitialisableComponent) component;
                
                ic.prepare();
            }
        }
    }

    private static void warmupComponents( SMTPropertyComponentBuilder loader ) {
        _console.info( "warmupComponents start" );

        Collection<SMTComponent> components = loader.getComponents();
        
        for( SMTComponent component : components ) {
            if ( component instanceof SMTWarmableComponent ) {
                _console.info( "warmupComponents:  prepare " + component.getComponentId() );

                SMTWarmableComponent wc = (SMTWarmableComponent) component;
                
                wc.warmup();
            }
        }
    }

    private static void startComponents( SMTPropertyComponentBuilder loader ) {
        _console.info( "startComponents start" );

        Collection<SMTComponent> components = loader.getComponents();
        
        for( SMTComponent component : components ) {
            if ( component instanceof SMTControllableComponent ) {
                _console.info( "startComponents:  start " + component.getComponentId() );

                SMTControllableComponent cc = (SMTControllableComponent) component;
                
                cc.startWork();
            }
        }

        SystemStatus.instance().initialised( loader );
    }

    public static void stopComponents( SMTPropertyComponentBuilder loader ) {
        _console.info( "stopComponents stop" );

        Collection<SMTComponent> components = loader.getComponents();
        
        for( SMTComponent component : components ) {
            if ( component instanceof SMTControllableComponent ) {
                _console.info( "stopComponents:  stop " + component.getComponentId() );

                SMTControllableComponent cc = (SMTControllableComponent) component;
                
                cc.stopWork();
            }
        }
    }

    AntiSpringBootstrap() {
        super();
    }
    
    private static void basicSetup( String[] args ) throws Exception {
        if ( args.length != 1 ){
            _console.info( "Error : missing property file argument" );
            _console.info( "Usage: {prog} propertyFile" );
            System.exit( -1 );
        }

        String propFile = args[0];

        AppProps.instance().init( propFile );
        
        AppProps props          = AppProps.instance();
        String appName          = props.getProperty( CoreProps.APP_NAME ); 

        StatsMgr.setStats( new StatsCfgFile( props.getProperty( CoreProps.STATS_CFG_FILE, false, null ) ) );
        StatsMgr.instance().initialise();

        ThreadUtils.init( props.getProperty( CoreProps.CPU_MASK_FILE, false, null ) );

        ThreadPriority priority = PropertyHelper.getProperty( props, CoreProps.MAIN_THREAD_PRI,  ThreadPriority.class, ThreadPriority.Main );
        
        ThreadUtils.setPriority( Thread.currentThread(), priority );
        
        boolean forceConsole = "true".equalsIgnoreCase( System.getProperty( "app.forceConsole" ) );
        
        LoggerFactory.setDebug( forceConsole );
        LoggerFactory.initLogging( props.getProperty( CoreProps.LOG_FILE_NAME, false, "./logs/" + appName ), 
                                   props.getIntProperty( CoreProps.MAX_LOG_SIZE, false, 10000000 ) );        

        LoggerFactory.setMinFlushPeriodSecs( props.getIntProperty( CoreProps.MIN_LOG_FLUSH, false, 30 ) );
        
        AdminAgent.init( props.getIntProperty( CoreProps.ADMIN_PORT, false, 8000 )  );
        
        _log       = LoggerFactory.create( AntiSpringBootstrap.class );

        setScheduler();
    }

    private static void setScheduler() {
        TimeZoneCalculator local = new TimeZoneCalculator();
        Calendar localNextMidnight = Calendar.getInstance( local.getLocalTimeZone() );
        localNextMidnight.set( Calendar.HOUR_OF_DAY, 00 );
        localNextMidnight.set( Calendar.MINUTE,      00 );
        localNextMidnight.set( Calendar.SECOND,      00 );
        localNextMidnight.add( Calendar.DAY_OF_MONTH, 1 );

        // @note when timestamps are not stored as millis from start of UTC today then can remove this event
        TimeZone utc = TimeZone.getTimeZone( "UTC" );
        Calendar utcNextMidnight = Calendar.getInstance( utc );
        utcNextMidnight.set( Calendar.HOUR_OF_DAY, 00 );
        utcNextMidnight.set( Calendar.MINUTE,      00 );
        utcNextMidnight.set( Calendar.SECOND,      00 );
        utcNextMidnight.add( Calendar.DAY_OF_MONTH, 1 );
        
        Scheduler.instance().registerGroupRepeating( ScheduledEvent.UTCDateRoll, utcNextMidnight,   Constants.MS_IN_DAY );
        Scheduler.instance().registerGroupRepeating( ScheduledEvent.EndOfDay,    localNextMidnight, Constants.MS_IN_DAY );
    }
}
