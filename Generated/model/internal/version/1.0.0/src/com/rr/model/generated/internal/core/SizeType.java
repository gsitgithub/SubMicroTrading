/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.core;


import javax.annotation.Generated;

@Generated( "com.rr.model.generated.internal.core.SizeType" )

public enum SizeType {
    ACCOUNT_LENGTH( 5 ),
    CLIENTID_LENGTH( 5 ),
    CLORDID_LENGTH( 20 ),
    SRC_LINKID_LENGTH( 20 ),
    CURRENCY_LENGTH( 3 ),
    ORDERID_LENGTH( 10 ),
    EXDESTINATION_LENGTH( 3 ),
    EXECID_LENGTH( 10 ),
    MARKETORDERREF_LENGTH( 10 ),
    SECURITYID_LENGTH( 12 ),
    ONBEHALFOFID_LENGTH( 10 ),
    EXECBROKER_LENGTH( 10 ),
    SYMBOL_LENGTH( 12 ),
    COMPID_LENGTH( 10 ),
    SENDERLOCID_LENGTH( 8 ),
    TEXT_LENGTH( 4 ),
    TRADE_TEXT_LENGTH( 4 ),
    BENCHMARK_LENGTH( 16 ),
    USERNAME( 10 ),
    PASSWORD( 10 ),
    LASTMKT_LENGTH( 4 ),
    SECURITYEXCH_LENGTH( 4 ),
    SUBPARTYGRPID_LENGTH( 10 ),
    MATURITYMONTHYEAR_LENGTH( 6 ),
    MATURITYDAY_LENGTH( 2 ),
    VIEW_NOS_BUFFER( 400 ),
    MIN_ENCODE_BUFFER( 128 ),
    TESTRECID_LENGTH( 16 ),
    USERNAME_LENGTH( 15 ),
    TAG_LEN( 4 ),
    MD_REQ_LEN( 9 ),
    ALGO_ID_LEN( 30 ),
    INST_FEED_TYPE_LENGTH( 9 ),
    INST_SEC_GRP_LEN( 9 ),
    INST_SEC_DESC_LENGTH( 9 ),
    INST_CFI_CODE_LENGTH( 9 ),
    INST_APPL_ID_LENGTH( 9 ),
    UTP_TEXT_LENGTH( 11 ),
    UTP_REJECT_TEXT_LENGTH( 40 ),
    UTP_MIC_LEN( 4 ),
    UTP_CCY_LEN( 3 ),
    UTP_SYMBOL_LEN( 12 ),
    UTP_COMP_ID_LEN( 11 ),
    UTP_GIVE_UP_FIRM_LEN( 8 ),
    UTP_ACCOUNT_LEN( 12 ),
    UTP_NOS_FREE_TEXT1_LEN( 18 ),
    UTP_INST_CLASS_LEN( 2 ),
    UTP_INST_CLASS_STATUS_LEN( 4 ),
    UTP_TRADING_SESSION_ID_LEN( 4 ),
    MILL_USER_NAME_LENGTH( 25 ),
    MILL_PASSWORD_LENGTH( 25 ),
    MILL_PASSWORD_EXPIRY_LENGTH( 30 ),
    MILL_REASON_LENGTH( 20 ),
    MILL_REJECT_REASON_LENGTH( 30 ),
    MILL_CLORDID_LENGTH( 20 ),
    MILL_ORDERID_LENGTH( 12 ),
    MILL_ACCOUNT_LENGTH( 10 ),
    MILL_EXECID_LENGTH( 12 ),
    MILL_CPARTY_LENGTH( 11 ),
    ETI_PASSWORD_LENGTH( 32 ),
    ETI_INTERFACE_VERSION_LENGTH( 30 ),
    ETI_APP_SYS_NAME_LENGTH( 30 ),
    ETI_APP_SYS_VER_LENGTH( 30 ),
    ETI_APP_SYS_VENDOR_LENGTH( 30 ),
    ETI_APP_MSG_ID_LENGTH( 16 ),
    ETI_VAR_TEXT_LENGTH( 128 ),
    ETI_ACCOUNT_LENGTH( 2 ),
    ETI_LINKID_LENGTH( 24 ),
    CME_SBE_APPLID_LENGTH( 5 ),
    CME_SBE_ASSET_LENGTH( 6 ),
    CME_SBE_CFICODE_LENGTH( 6 ),
    CME_SBE_MDFEEDTYPE_LENGTH( 3 ),
    CME_SBE_QUOTEREQID_LENGTH( 23 ),
    CME_SBE_SECURITYEXCHANGE_LENGTH( 4 ),
    CME_SBE_SECURITYGROUP_LENGTH( 6 ),
    CME_SBE_SECURITYSUBTYPE_LENGTH( 5 ),
    CME_SBE_SECURITYTYPE_LENGTH( 6 ),
    CME_SBE_CURRENCY_LENGTH( 3 ),
    CME_SBE_SYMBOL_LENGTH( 20 ),
    CME_SBE_UNITOFMEASURE_LENGTH( 30 );

    private int _size;

    private SizeType( int val ) {
        _size = val;
    }

    public int getSize() { return _size; }
}
