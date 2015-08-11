/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.properties;

import java.util.HashSet;
import java.util.Set;

public class CoreProps implements PropertyTags {
    public static final String APP_NAME             = "app.name";
    public static final String APP_DEBUG            = "app.debug";
    public static final String APP_TIMEZONE         = "app.timeZone";
    public static final String ID_PREFIX            = "app.genIdPrefix";
    public static final String NUM_PREFIX           = "app.genNumIdPrefix";
    public static final String APP_TAGS             = "app.propertyTags";
    public static final String PERSIST_DIR          = "run.persistDir";
    public static final String CPU_MASK_FILE        = "run.cpuMaskFile";
    public static final String LOG_FILE_NAME        = "run.logFileName";
    public static final String MIN_LOG_FLUSH        = "run.minLogFlushSecs";
    public static final String MAX_LOG_SIZE         = "run.maxLogSize";
    public static final String STARTUP_DELAY        = "run.start.delay";
    public static final String SOCKET_FACTORY       = "run.socketFactoryClass";
    public static final String MAIN_THREAD_PRI      = "run.mainPriority";
    public static final String STATS_CFG_FILE       = "run.statsCfgFile";
    public static final String ADMIN_PORT           = "admin.port";
    
    public static enum Tags implements PropertyTags.Tag {
        name,
        appProps,
        statsCfgFile,
        loader,
        className,
        defaultProperties,
        value,
        delay,
        debug,
        genIdPrefix,
        genNumIdPrefix,
        fileName,
        persistDir,
        cpuMaskFile,
        logFileName,
        minLogFlushSecs,
        maxLogSize,
        port,
        chainSize,
        socketFactoryClass,
        useNativeLinux,
        mainPriority,
        threadPriority,
        recycler,
        delayMS,
        keyType,
        timeZone,
        valType,
        propertyTags;
    }

    private static final Set<String> _set = new HashSet<String>();

    static {
        for ( Tags p : Tags.values() ) {
             _set.add( p.toString().toLowerCase() );
        }
    }

    private static CoreProps _instance = new CoreProps();
    
    public static CoreProps instance() { return _instance; }
    
    @Override
    public String getSetName() {
        return "CoreProps";
    }

    @Override
    public boolean isValidTag( String tag ) {
        if ( tag == null ) return false;
        
        return _set.contains( tag.toLowerCase() );
    }
    
    @Override
    public Tag lookup( String tag ){
        return Tags.valueOf( tag );
    }

    protected CoreProps() {
        //
    }
}
