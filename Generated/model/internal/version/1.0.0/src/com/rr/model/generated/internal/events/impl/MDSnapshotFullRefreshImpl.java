/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.core.model.SecurityIDSource;
import com.rr.model.generated.internal.events.interfaces.MDSnapEntry;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.Constants;
import com.rr.core.model.MsgFlag;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.Reusable;
import com.rr.core.model.Message;
import com.rr.core.model.MessageHandler;
import com.rr.model.internal.type.*;
import com.rr.model.generated.internal.core.ModelReusableTypes;
import com.rr.model.generated.internal.core.SizeType;
import com.rr.model.generated.internal.core.EventIds;
import com.rr.model.generated.internal.events.interfaces.*;

@SuppressWarnings( "unused" )

public final class MDSnapshotFullRefreshImpl implements BaseMDResponse, MDSnapshotFullRefreshWrite, Reusable<MDSnapshotFullRefreshImpl> {

   // Attrs

    private          MDSnapshotFullRefreshImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private long _sendingTime = Constants.UNSET_LONG;
    private long _received = Constants.UNSET_LONG;
    private int _lastMsgSeqNumProcessed = Constants.UNSET_INT;
    private int _totNumReports = Constants.UNSET_INT;
    private int _rptSeq = Constants.UNSET_INT;
    private int _mdBookType = Constants.UNSET_INT;
    private long _securityID = Constants.UNSET_LONG;
    private int _mdSecurityTradingStatus = Constants.UNSET_INT;
    private int _noMDEntries = Constants.UNSET_INT;
    private int _msgSeqNum = Constants.UNSET_INT;

    private SecurityIDSource _securityIDSource = SecurityIDSource.ExchangeSymbol;
    private MDSnapEntry _MDEntries;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final long getSendingTime() { return _sendingTime; }
    @Override public final void setSendingTime( long val ) { _sendingTime = val; }

    @Override public final long getReceived() { return _received; }
    @Override public final void setReceived( long val ) { _received = val; }

    @Override public final int getLastMsgSeqNumProcessed() { return _lastMsgSeqNumProcessed; }
    @Override public final void setLastMsgSeqNumProcessed( int val ) { _lastMsgSeqNumProcessed = val; }

    @Override public final int getTotNumReports() { return _totNumReports; }
    @Override public final void setTotNumReports( int val ) { _totNumReports = val; }

    @Override public final int getRptSeq() { return _rptSeq; }
    @Override public final void setRptSeq( int val ) { _rptSeq = val; }

    @Override public final int getMdBookType() { return _mdBookType; }
    @Override public final void setMdBookType( int val ) { _mdBookType = val; }

    @Override public final SecurityIDSource getSecurityIDSource() { return _securityIDSource; }
    @Override public final void setSecurityIDSource( SecurityIDSource val ) { _securityIDSource = val; }

    @Override public final long getSecurityID() { return _securityID; }
    @Override public final void setSecurityID( long val ) { _securityID = val; }

    @Override public final int getMdSecurityTradingStatus() { return _mdSecurityTradingStatus; }
    @Override public final void setMdSecurityTradingStatus( int val ) { _mdSecurityTradingStatus = val; }

    @Override public final int getNoMDEntries() { return _noMDEntries; }
    @Override public final void setNoMDEntries( int val ) { _noMDEntries = val; }

    @Override public final MDSnapEntry getMDEntries() { return _MDEntries; }
    @Override public final void setMDEntries( MDSnapEntry val ) { _MDEntries = val; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _sendingTime = Constants.UNSET_LONG;
        _received = Constants.UNSET_LONG;
        _lastMsgSeqNumProcessed = Constants.UNSET_INT;
        _totNumReports = Constants.UNSET_INT;
        _rptSeq = Constants.UNSET_INT;
        _mdBookType = Constants.UNSET_INT;
        _securityIDSource = SecurityIDSource.ExchangeSymbol;
        _securityID = Constants.UNSET_LONG;
        _mdSecurityTradingStatus = Constants.UNSET_INT;
        _noMDEntries = Constants.UNSET_INT;
        _MDEntries = null;
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.MDSnapshotFullRefresh;
    }

    @Override
    public final MDSnapshotFullRefreshImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( MDSnapshotFullRefreshImpl nxt ) {
        _next = nxt;
    }

    @Override
    public final void detachQueue() {
        _nextMessage = null;
    }

    @Override
    public final Message getNextQueueEntry() {
        return _nextMessage;
    }

    @Override
    public final void attachQueue( Message nxt ) {
        _nextMessage = nxt;
    }

    @Override
    public final MessageHandler getMessageHandler() {
        return _messageHandler;
    }

    @Override
    public final void setMessageHandler( MessageHandler handler ) {
        _messageHandler = handler;
    }


   // Helper methods
    @Override
    public void setFlag( MsgFlag flag, boolean isOn ) {
        _flags = (byte) MsgFlag.setFlag( _flags, flag, isOn );
    }

    @Override
    public boolean isFlagSet( MsgFlag flag ) {
        return MsgFlag.isOn( _flags, flag );
    }

    @Override
    public String toString() {
        ReusableString buf = new ReusableString();
        dump( buf );
        return buf.toString();
    }

    @Override
    public final void dump( ReusableString out ) {
        out.append( "MDSnapshotFullRefreshImpl" ).append( ' ' );
        out.append( ", sendingTime=" ).append( getSendingTime() );
        out.append( ", received=" ).append( getReceived() );
        out.append( ", lastMsgSeqNumProcessed=" ).append( getLastMsgSeqNumProcessed() );
        out.append( ", totNumReports=" ).append( getTotNumReports() );
        out.append( ", rptSeq=" ).append( getRptSeq() );
        out.append( ", mdBookType=" ).append( getMdBookType() );
        out.append( ", securityIDSource=" );
        if ( getSecurityIDSource() != null ) getSecurityIDSource().id( out );
        out.append( ", securityID=" ).append( getSecurityID() );
        out.append( ", mdSecurityTradingStatus=" ).append( getMdSecurityTradingStatus() );
        out.append( ", noMDEntries=" ).append( getNoMDEntries() );

        MDSnapEntryImpl tPtrMDEntries = (MDSnapEntryImpl) getMDEntries();
        int tIdxMDEntries=0;

        while( tPtrMDEntries != null ) {
            out.append( " {#" ).append( ++tIdxMDEntries ).append( "} " );
            tPtrMDEntries.dump( out );
            tPtrMDEntries = tPtrMDEntries.getNext();
        }

        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
