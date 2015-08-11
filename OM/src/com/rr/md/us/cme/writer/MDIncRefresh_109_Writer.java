/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.writer;

import com.rr.core.codec.binary.fastfix.FastFixBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapWriter;
import com.rr.core.codec.binary.fastfix.common.constant.int32.IntMandWriterConst;
import com.rr.core.codec.binary.fastfix.common.constant.string.StringMandWriterConst;
import com.rr.core.codec.binary.fastfix.common.def.int32.UIntOptWriterDefault;
import com.rr.core.codec.binary.fastfix.common.def.string.StringWriterDefault;
import com.rr.core.codec.binary.fastfix.common.noop.int32.IntMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.UIntMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int32.UIntOptWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.noop.int64.ULongMandWriterNoOp;
import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntMandWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.copy.string.StringWriterCopy;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.delmant.MandDefExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.custom.defexp.delmant.OptDefExpDeltaMantDecimalWriter;
import com.rr.core.codec.binary.fastfix.msgdict.delta.int32.IntOptWriterDelta;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.UIntMandWriterIncrement;
import com.rr.core.codec.binary.fastfix.msgdict.increment.int32.UIntOptWriterIncrement;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.md.fastfix.template.MDIncRefreshFastFixTemplateWriter;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;

/*
 * Hand coded template for CME MDIncRefresh #81
 * 
 * only boxes ints in constructor
 
    <template name="MDIncRefresh_109" id="109" dictionary="109"
        <string name="ApplVerID" id="1128"> <constant value="9" />
        <string name="MessageType" id="35"> <constant value="X" />
        <string name="SenderCompID" id="49"> <constant value="CME" />
        <uInt32 name="MsgSeqNum" id="34"></uInt32>
        <uInt64 name="SendingTime" id="52"></uInt64>
        <string name="PosDupFlag" id="43" presence="optional"> <default />
        <uInt32 name="TradeDate" id="75"></uInt32>
        <sequence name="MDEntries">
            <length name="NoMDEntries" id="268"></length>
            <uInt32 name="MDUpdateAction" id="279"> <copy value="1" />
            <uInt32 name="MDPriceLevel" id="1023" presence="optional"> <increment />
            <string name="MDEntryType" id="269"> <copy value="0" />
            <uInt32 name="SecurityIDSource" id="22"> <constant value="8" />
            <uInt32 name="SecurityID" id="48"> <copy />
            <uInt32 name="RptSeq" id="83"> <increment />
            <string name="QuoteCondition" id="276" presence="optional"> <default />
            <decimal name="MDEntryPx" id="270"> <exponent> <default value="-2" /> <mantissa> <delta />
            <uInt32 name="NumberOfOrders" id="346" presence="optional"> <default />
            <uInt32 name="MDEntryTime" id="273"> <copy />
            <int32 name="MDEntrySize" id="271" presence="optional"> <delta />
            <string name="TradingSessionID" id="336" presence="optional"> <default value="2" />
            <decimal name="NetChgPrevDay" id="451" presence="optional"> <exponent> <default /> <mantissa> <delta />
            <string name="TickDirection" id="274" presence="optional"> <default />
            <uInt32 name="OpenCloseSettleFlag" id="286" presence="optional">
            <uInt32 name="SettlDate" id="64" presence="optional"> <default />
        </sequence>
 */

public final class MDIncRefresh_109_Writer implements MDIncRefreshFastFixTemplateWriter {

    private static final int INIT_SEQ_PMAP_SIZE = 2; 

    private final String _name;
    private final int    _id;
    private final ReusableString _mdEntryType = new ReusableString(2);

    private final PresenceMapWriter seqPMap = new PresenceMapWriter();
    
    private final StringMandWriterConst _f1_ApplVerID;
    private final StringMandWriterConst _f2_MessageType;
    private final StringMandWriterConst _f3_SenderCompID;
    private final UIntMandWriterNoOp    _f4_MsgSeqNum;
    private final ULongMandWriterNoOp   _f5_SendingTime;
    private final StringWriterDefault   _f6_PosDupFlag;
    private final UIntMandWriterNoOp    _f6b_TradeDate;
    
    private final IntMandWriterNoOp                 _f7_NoMDEntries;
    private final UIntMandWriterCopy                _f8_MDUpdateAction;
    private final UIntOptWriterIncrement            _f9_PriceLevel;
    private final StringWriterCopy                  _f10_MDEntryType;
    private final IntMandWriterConst                _f11_SecurityIDSource; // should be UInt const
    private final UIntMandWriterCopy                _f12_SecurityID;
    private final UIntMandWriterIncrement           _f13_RptSeq;
    private final StringWriterDefault               _f13b_QuoteCondition;
    private final MandDefExpDeltaMantDecimalWriter  _f14_MDEntryPx;
    private final UIntOptWriterDefault              _f14b_NumberOfOrders;
    private final UIntMandWriterCopy                _f15_MDEntryTime;
    private final IntOptWriterDelta                 _f16_MDEntrySize;
    private final StringWriterDefault               _f19_TradingSessionID;
    private final OptDefExpDeltaMantDecimalWriter   _f20_NetChgPrevDay;
    private final StringWriterDefault               _f21_TickDirection;
    private final UIntOptWriterNoOp                 _f22_OpenCloseSettleFlag;
    private final UIntOptWriterDefault              _f23_SettlDate;
    
    @SuppressWarnings( "boxing" )
    public MDIncRefresh_109_Writer( ComponentFactory cf, String name, int id ) {
        _name = name;
        _id = id;
        
        _f1_ApplVerID           = cf.getWriter( StringMandWriterConst.class,            "ApplVerID",        1128,   new ReusableString("9") );
        _f2_MessageType         = cf.getWriter( StringMandWriterConst.class,            "MessageType",      35,     new ReusableString("X") );
        _f3_SenderCompID        = cf.getWriter( StringMandWriterConst.class,            "SenderCompID",     49,     new ReusableString("CME") );
        _f4_MsgSeqNum           = cf.getWriter( UIntMandWriterNoOp.class,               "MsgSeqNum",        49 );
        _f5_SendingTime         = cf.getWriter( ULongMandWriterNoOp.class,              "SendingTime",      52 );
        _f6_PosDupFlag          = cf.getWriter( StringWriterDefault.class,              "PosDupFlag",       43,     new ReusableString("") );
        _f6b_TradeDate          = cf.getWriter( UIntMandWriterNoOp.class,               "TradeDate",        75 );
        _f7_NoMDEntries         = cf.getWriter( IntMandWriterNoOp.class,                "NoMDEntries",      268 );
        _f8_MDUpdateAction      = cf.getWriter( UIntMandWriterCopy.class,               "MDUpdateAction",   279,    1 );
        _f9_PriceLevel          = cf.getWriter( UIntOptWriterIncrement.class,           "PriceLevel",       1023 );
        _f10_MDEntryType        = cf.getWriter( StringWriterCopy.class,                 "MDEntryType",      269,    new ReusableString("0") );
        _f11_SecurityIDSource   = cf.getWriter( IntMandWriterConst.class,               "SecurityIDSource", 22,     8 );
        _f12_SecurityID         = cf.getWriter( UIntMandWriterCopy.class,               "SecurityID",       48 );
        _f13_RptSeq             = cf.getWriter( UIntMandWriterIncrement.class,          "RptSeq",           83 );
        _f13b_QuoteCondition    = cf.getWriter( StringWriterDefault.class,              "QuoteCondition",   276,    new ReusableString("") );
        
        _f14_MDEntryPx          = cf.getWriter( MandDefExpDeltaMantDecimalWriter.class,  cf, "MDEntryPx",   270,    -2, 0L );
        _f14b_NumberOfOrders    = cf.getWriter( UIntOptWriterDefault.class,             "NumberOfOrders",   346 );
        
        _f15_MDEntryTime        = cf.getWriter( UIntMandWriterCopy.class,               "MDEntryTime",      273 );
        _f16_MDEntrySize        = cf.getWriter( IntOptWriterDelta.class,                "MDEntrySize",      271 );
        _f19_TradingSessionID   = cf.getWriter( StringWriterDefault.class,              "TradingSessionID", 336,    new ReusableString("2") );

        _f20_NetChgPrevDay          = cf.getWriter( OptDefExpDeltaMantDecimalWriter.class,  cf, "NetChgPrevDay",    270,    Constants.UNSET_INT, 0L );
        _f21_TickDirection          = cf.getWriter( StringWriterDefault.class,                  "TickDirection",    336,    new ReusableString("") );
        _f22_OpenCloseSettleFlag    = cf.getWriter( UIntOptWriterNoOp.class,                    "OpenCloseSettleFlag",      271 );
        _f23_SettlDate              = cf.getWriter( UIntOptWriterDefault.class,                 "SettlDate",        271 );
    }

    @Override
    public void write( final FastFixBuilder encoder, final PresenceMapWriter pMap, MDIncRefreshImpl dest ) {
        
        _f1_ApplVerID.write( encoder );                        // pass null to skip field
        _f2_MessageType.write( encoder );
        _f3_SenderCompID.write( encoder );
        
        _f4_MsgSeqNum.write( encoder, dest.getMsgSeqNum() );
        _f5_SendingTime.write( encoder, dest.getSendingTime() );
        
        _f6_PosDupFlag.write( encoder, pMap, null );                 // skip pos dup as we dont care
        _f6b_TradeDate.write( encoder, 0 );
        
        final int noMDEntries = dest.getNoMDEntries();
        _f7_NoMDEntries.write( encoder, noMDEntries );    
        
        int curEntry = 0;
        
        if ( noMDEntries > 0 ) { 
            MDEntryImpl tmp = (MDEntryImpl) dest.getMDEntries();
            
            do {
                seqPMap.set( encoder, encoder.getCurrentIndex(), INIT_SEQ_PMAP_SIZE );
                
                // encode fields
                
                _f8_MDUpdateAction.write( encoder, seqPMap, tmp.getMdUpdateAction().getVal()-'0' );
                _f9_PriceLevel.write( encoder, seqPMap, tmp.getMdPriceLevel() );
                
                _mdEntryType.reset();
                _mdEntryType.append( tmp.getMdEntryType().getVal() );

                _f10_MDEntryType.write( encoder, seqPMap, _mdEntryType ); 
                
                _f11_SecurityIDSource.write();
                _f12_SecurityID.write( encoder, seqPMap, (int)tmp.getSecurityID() );
                
                _f13_RptSeq.write( encoder, seqPMap, tmp.getRepeatSeq() );
                _f13b_QuoteCondition.write( encoder, seqPMap, null );
                
                _f14_MDEntryPx.write( encoder, seqPMap, tmp.getMdEntryPx() );
                _f14b_NumberOfOrders.write( encoder, seqPMap, Constants.UNSET_INT );
                _f15_MDEntryTime.write( encoder, seqPMap, tmp.getMdEntryTime() );
                _f16_MDEntrySize.write( encoder, tmp.getMdEntrySize() );

                _f19_TradingSessionID.write( encoder, seqPMap, null );
                _f20_NetChgPrevDay.write( encoder, seqPMap, Constants.UNSET_DOUBLE );
                _f21_TickDirection.write( encoder, seqPMap, null );
                _f22_OpenCloseSettleFlag.write( encoder, Constants.UNSET_INT );
                _f23_SettlDate.write( encoder, seqPMap, Constants.UNSET_INT );

                seqPMap.end();
                
                if ( ++curEntry == noMDEntries ) {
                    break;
                }
                
                tmp = tmp.getNext();
                
            } while( tmp != null );
        }
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public int getId() {
        return _id;
    }

    public void reset() {
        _f4_MsgSeqNum.reset();
        _f5_SendingTime.reset();
        _f6_PosDupFlag.reset();
        _f7_NoMDEntries.reset();
        _f8_MDUpdateAction.reset();
        _f9_PriceLevel.reset();
        _f10_MDEntryType.reset();
        _f12_SecurityID.reset();
        _f13_RptSeq.reset();
        _f13b_QuoteCondition.reset();
        _f14_MDEntryPx.reset();
        _f14b_NumberOfOrders.reset();
        _f15_MDEntryTime.reset();
        _f16_MDEntrySize.reset();
        _f19_TradingSessionID.reset();
        _f20_NetChgPrevDay.reset();
        _f21_TickDirection.reset();
        _f22_OpenCloseSettleFlag.reset();
        _f23_SettlDate.reset();
    }
}
