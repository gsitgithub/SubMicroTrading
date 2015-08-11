/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang.stats;

public enum SizeType {
    DEFAULT_MIN_MEMCPY_LENGTH( 10 ),
	DEFAULT_STRING_LENGTH( 16 ),
    DEFAULT_ACCOUNT_LENGTH( 5 ),
    DEFAULT_CLIENTID_LENGTH( 5 ),
    DEFAULT_CLORDID_LENGTH( 16 ),
    DEFAULT_EXDESTINATION_LENGTH( 3 ),
    DEFAULT_EXECID_LENGTH( 10 ),
    DEFAULT_MARKETORDERID_LENGTH( 12 ),
    DEFAULT_SECURITYID_LENGTH( 5 ),
    DEFAULT_SENDERCOMPID_LENGTH( 5 ),
    DEFAULT_SENDERSUBID_LENGTH( 5 ),
    DEFAULT_ONBEHALFOFID_LENGTH( 10 ),
    DEFAULT_EXECBROKER_LENGTH( 10 ),
    DEFAULT_SYMBOL_LENGTH( 5 ),
    DEFAULT_RIC_LENGTH( 12 ),
    DEFAULT_TARGETCOMPID_LENGTH( 5 ),
    DEFAULT_TARGETSUBID_LENGTH( 5 ),
    DEFAULT_SENDERLOCID_LENGTH( 8 ),
    DEFAULT_TEXT_LENGTH( 2 ),
    DEFAULT_BENCHMARK_LENGTH( 16 ),
    DEFAULT_USERNAME( 10 ),
    DEFAULT_PASSWORD( 10 ),
    DEFAULT_LASTMKT_LENGTH( 3 ),
    DEFAULT_SECURITYEXCH_LENGTH( 3 ),
    DEFAULT_SUBPARTYGRPID_LENGTH( 10 ),
    DEFAULT_MATURITYMONTHYEAR_LENGTH( 6 ),
    DEFAULT_MATURITYDAY_LENGTH( 2 ), 
    DEFAULT_VIEW_NOS_BUFFER( 400 ), // default size of the buffer backing the ViewStrings in a NOS - holds all input msg 
    DEFAULT_MAX_MSG_BUFFER( 480 ), 
    DEFAULT_MAX_SESSION_BUFFER( 8192 ), 
    DEFAULT_LOG_EVENT_SMALL( 100 ), 
    DEFAULT_LOG_EVENT_LARGE( 500 ), 
    DEFAULT_LOG_EVENT_HUGE( 2048 ), 
    DEFAULT_LOG_MAX_QUEUE_SIZE( 100000 ), 
    DEFAULT_CHAIN_SIZE( 100 ), 
    DEFAULT_MAX_LOG_CHAINS( 5000 );  
	
	private int _size;

	private SizeType( int val ) {
		_size = val;
	}
	
	public int getSize() { return _size; }
}
