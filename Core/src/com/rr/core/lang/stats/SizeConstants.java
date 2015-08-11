/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang.stats;

public class SizeConstants {

    // on startup the constants are initialised from the StatsMgr ... obviously need be careful of startup order
    
    // only constants which need to be in the persisted stats file need an entry in the stats mgr
    // otherwise just hard code here
    public static final int  MIN_MEMCPY_LENGTH                = StatsMgr.instance().find( SizeType.DEFAULT_MIN_MEMCPY_LENGTH );
    public static final int  DEFAULT_MAX_MSG_BUFFER           = StatsMgr.instance().find( SizeType.DEFAULT_MAX_MSG_BUFFER );
    public static final int  DEFAULT_MAX_SESSION_BUFFER       = StatsMgr.instance().find( SizeType.DEFAULT_MAX_SESSION_BUFFER );
    
    public static final int  DEFAULT_STRING_LENGTH            = StatsMgr.instance().find( SizeType.DEFAULT_STRING_LENGTH );
    public static final int  DEFAULT_ACCOUNT_LENGTH           = StatsMgr.instance().find( SizeType.DEFAULT_ACCOUNT_LENGTH );
    public static final int  DEFAULT_CLIENTID_LENGTH          = StatsMgr.instance().find( SizeType.DEFAULT_CLIENTID_LENGTH );
    public static final int  DEFAULT_CLORDID_LENGTH           = StatsMgr.instance().find( SizeType.DEFAULT_CLORDID_LENGTH );
    public static final int  DEFAULT_EXDESTINATION_LENGTH     = StatsMgr.instance().find( SizeType.DEFAULT_EXDESTINATION_LENGTH );
    public static final int  DEFAULT_EXECID_LENGTH            = StatsMgr.instance().find( SizeType.DEFAULT_EXECID_LENGTH );
    public static final int  DEFAULT_MARKETORDERID_LENGTH     = StatsMgr.instance().find( SizeType.DEFAULT_MARKETORDERID_LENGTH );
    public static final int  DEFAULT_SECURITYID_LENGTH        = StatsMgr.instance().find( SizeType.DEFAULT_SECURITYID_LENGTH );
    public static final int  DEFAULT_SENDERCOMPID_LENGTH      = StatsMgr.instance().find( SizeType.DEFAULT_SENDERCOMPID_LENGTH );
    public static final int  DEFAULT_SENDERSUBID_LENGTH       = StatsMgr.instance().find( SizeType.DEFAULT_SENDERSUBID_LENGTH );
    public static final int  DEFAULT_ONBEHALFOFID_LENGTH      = StatsMgr.instance().find( SizeType.DEFAULT_ONBEHALFOFID_LENGTH );
    public static final int  DEFAULT_EXECBROKER_LENGTH        = StatsMgr.instance().find( SizeType.DEFAULT_EXECBROKER_LENGTH );
    public static final int  DEFAULT_SYMBOL_LENGTH            = StatsMgr.instance().find( SizeType.DEFAULT_SYMBOL_LENGTH );
    public static final int  DEFAULT_RIC_LENGTH               = StatsMgr.instance().find( SizeType.DEFAULT_RIC_LENGTH );
    public static final int  DEFAULT_TARGETCOMPID_LENGTH      = StatsMgr.instance().find( SizeType.DEFAULT_TARGETCOMPID_LENGTH );
    public static final int  DEFAULT_TARGETSUBID_LENGTH       = StatsMgr.instance().find( SizeType.DEFAULT_TARGETSUBID_LENGTH );
    public static final int  DEFAULT_SENDERLOCID_LENGTH       = StatsMgr.instance().find( SizeType.DEFAULT_SENDERLOCID_LENGTH );
    public static final int  DEFAULT_TEXT_LENGTH              = StatsMgr.instance().find( SizeType.DEFAULT_TEXT_LENGTH );
    public static final int  DEFAULT_BENCHMARK_LENGTH         = StatsMgr.instance().find( SizeType.DEFAULT_BENCHMARK_LENGTH );
    public static final int  DEFAULT_USERNAME                 = StatsMgr.instance().find( SizeType.DEFAULT_USERNAME );
    public static final int  DEFAULT_PASSWORD                 = StatsMgr.instance().find( SizeType.DEFAULT_PASSWORD );
    public static final int  DEFAULT_LASTMKT_LENGTH           = StatsMgr.instance().find( SizeType.DEFAULT_LASTMKT_LENGTH );
    public static final int  DEFAULT_SECURITYEXCH_LENGTH      = StatsMgr.instance().find( SizeType.DEFAULT_SECURITYEXCH_LENGTH );
    public static final int  DEFAULT_SUBPARTYGRPID_LENGTH     = StatsMgr.instance().find( SizeType.DEFAULT_SUBPARTYGRPID_LENGTH );
    public static final int  DEFAULT_MATURITYMONTHYEAR_LENGTH = StatsMgr.instance().find( SizeType.DEFAULT_MATURITYMONTHYEAR_LENGTH );
    public static final int  DEFAULT_MATURITYDAY_LENGTH       = StatsMgr.instance().find( SizeType.DEFAULT_MATURITYDAY_LENGTH );
    public static final int  DEFAULT_VIEW_NOS_BUFFER          = StatsMgr.instance().find( SizeType.DEFAULT_VIEW_NOS_BUFFER );

    public static final int  DEFAULT_LOG_EVENT_SMALL          = StatsMgr.instance().find( SizeType.DEFAULT_LOG_EVENT_SMALL );
    public static final int  DEFAULT_LOG_EVENT_LARGE          = StatsMgr.instance().find( SizeType.DEFAULT_LOG_EVENT_LARGE );
    public static final int  DEFAULT_LOG_EVENT_HUGE           = StatsMgr.instance().find( SizeType.DEFAULT_LOG_EVENT_HUGE );

    public static final int  DEFAULT_LOG_MAX_QUEUE_SIZE       = StatsMgr.instance().find( SizeType.DEFAULT_LOG_MAX_QUEUE_SIZE );
    public static final int  DEFAULT_CHAIN_SIZE               = StatsMgr.instance().find( SizeType.DEFAULT_CHAIN_SIZE );
    public static final int  DEFAULT_MAX_LOG_CHAINS           = StatsMgr.instance().find( SizeType.DEFAULT_MAX_LOG_CHAINS );
}
