/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.main;

import java.util.HashSet;
import java.util.Set;

import com.rr.core.properties.CoreProps;
import com.rr.core.properties.PropertyTags;

/**
 * only the final tag of a property is validated, eg property xxx.yyy.ZZZ, only ZZZ must be a member of Tags
 * this is because the set of complete property names is dynamic and can vary across instances
 */

public class OMProps extends CoreProps {
    public static final String EXPECTED_ORDERS     = "run.expectedOrders";
    public static final String WARMUP_COUNT        = "run.warmUpCount";
    public static final String WARMUP_PORT_OFFSET  = "run.warmUpPortOffset";
    public static final String EXCHANGE_XML        = "run.exchangeXML";
    public static final String SEND_SPINLOCKS      = "run.enableSendSpinLock";
    public static final String PROC_ROUTER         = "proc.router";
    
    public static final String DEFAULT_CPU_WARM_MISS_COUNT = "fastfix.default.cpuWarmMissCount";
    
    
    public enum Tags implements PropertyTags.Tag {
        // app properties
        numCorePerCPU,
        lockToSocketOne,
        useNativeLinux,
        useLinuxNonLockingNIOSockets,
        mainPriority,
        threadPriority,
        exchangeXML,
    
        expectedOrders,
        warmUpCount,
        warmUpPortOffset,
        forceRemovePersistence,
        enabled,
        
        configFile,
        env,
        
        // processor validator
        maxAgeMS,
        expTrades,
        forceCancelUnknownExexId,
        
        // processor order map
        mapType, 
        loadFactor, 
        segments,
        
        // general session properties
        trace,
        userName,
        userId,
        disableNanoStats,
        logEvents,
        logPojoEvents,
        logKeepWarmEvents,
        logStats,
        enableReceiverSpinLock,
        enableSendSpinLock,
        router,
        dispatcher,
        queue,
        queuePresize,
        soDelayMS,
        throttleSender,
        throttlerClass,
        sessionDirection,
        
        // socket properties
        server,
        localPort,
        useNIO,
        nic,
        hostname,
        port, 
        altPort,                    // some sessions require two ports, one for trading and one for connection/recovery
        logDelayedWriteNanos,
        disableLoopback,
        maxMsgsPerSecond,
        ttl,
        qos,

        // fix session properties
        type,
        multifix,
        codecId,
        inThreadPriority,
        outThreadPriority,
        persistThreadPriority,
        senderCompId,
        senderSubId,
        senderLocationId,
        targetCompId,
        targetSubId,
        persistDatPageSize, 
        persistIdxPreSize, 
        persistDatPreSize,
        isRecoverFromLoginSeqNumTooLow,
        heartBeatIntSecs,
        dummyPersister,
        rawData,
        encryptMethod,
        isGapFillAllowed,
        disconnectOnSeqGap,
        maxResendRequestSize,
        
        // binary session properties
        sessionTrace,
        isCancelOnDisconnect,
        
        // multifix
        controlthread,
        in,
        out,
        inboundRouter,
        

        // client profile limits
        defaultClientProfile,
        useDummyProfile,
        clientProfiles,
        clientName,
        lowThresholdPercent,
        medThresholdPercent,
        highThresholdPercent,
        maxTotalQty,
        maxTotalOrderValueUSD,
        maxSingleOrderValueUSD,
        maxSingleOrderQty,
        maxOrderQty,
        
        // SIM params
        postConnectWaitSecs,
        batchSize,
        sendEvents,
        batchDelayMicros,
        eventTemplateFile,
        logSimulatorEvents,
        logOrderInTS,
        
        // MDS
        presubFile,
        
        // instrument store
        threadsafe,
        file,
        
        // Other
        REC,        // reuters exchange code
        
        // Exchange specific
        partyIDSessionID,
        password,
        traderPassword,
        sessionLogonPassword,
        locationId,
        forceTradingServerLocalhost,
        uniqueClientCode,
        
        // fast fix
        multicast,
        multifixList,
        subChannelMask, 
        multicastGroups,
        tickToTradeRatio,
        bookLevels,
        enqueueIncTicksOnGap, 
        subscriptionFile,
        channelList,
        overrideSubscribeSet,

        // T1 & Book
        allowIntradaySecurityUpdates,
        disableDirtyAllBooksOnPacketGap,
        ignoreDirtyOnGap,
        maxEnqueueIncUpdatesOnGap,
        enqueueIncUpdatesOnGap,
        bookListener,
        logIntermediateFix,

        
        //component references
        ref,
        exchangeRouter,
        sessionManager,
        inboundHandler,
        hubSession,
        sessionConfig,
        gwyConfig,
        clientProfileManager,
        exchangeManager,
        instrumentLocator,
        instrumentStore,
        inboundDispatcher,
        outboundDispatcher,
        warmupControl,
        templateFile
    }

    private static final Set<String> _set = new HashSet<String>();

    static {
        for ( Tags p : Tags.values() ) {
             _set.add( p.toString().toLowerCase() );
        }
    }

    private static OMProps _instance = new OMProps();
    
    public static OMProps instance() { return _instance; }
    
    @Override
    public String getSetName() {
        return "OMProps";
    }

    @Override
    public boolean isValidTag( String tag ) {
        if ( tag == null ) return false;
        
        if ( _set.contains( tag.toLowerCase() )  ) {
            return true;
        }
            
        return super.isValidTag( tag );
    }
    
    @Override
    public Tag lookup( String tag ){
        Tag val = null;
        
        try {
            val = Tags.valueOf( tag );
        } catch( Exception e ) {
            // ignore
        }
        
        if ( val == null ) {
            val = super.lookup( tag );
        }
        
        return val;
    }

    protected OMProps() {
        // protected
    }
}
