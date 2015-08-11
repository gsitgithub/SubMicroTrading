/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.SubsReqType;
import com.rr.model.generated.internal.events.interfaces.SymbolRepeatingGrp;
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

public final class MDRequestImpl implements BaseMDRequest, MDRequestWrite, Reusable<MDRequestImpl> {

   // Attrs

    private          MDRequestImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private final ReusableString _mdReqId = new ReusableString( SizeType.MD_REQ_LEN.getSize() );
    private int _marketDepth = Constants.UNSET_INT;
    private int _numRelatedSym = Constants.UNSET_INT;
    private int _msgSeqNum = Constants.UNSET_INT;

    private SubsReqType _subsReqType;
    private SymbolRepeatingGrp _symbolGrp;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final ViewString getMdReqId() { return _mdReqId; }

    @Override public final void setMdReqId( byte[] buf, int offset, int len ) { _mdReqId.setValue( buf, offset, len ); }
    @Override public final ReusableString getMdReqIdForUpdate() { return _mdReqId; }

    @Override public final SubsReqType getSubsReqType() { return _subsReqType; }
    @Override public final void setSubsReqType( SubsReqType val ) { _subsReqType = val; }

    @Override public final int getMarketDepth() { return _marketDepth; }
    @Override public final void setMarketDepth( int val ) { _marketDepth = val; }

    @Override public final int getNumRelatedSym() { return _numRelatedSym; }
    @Override public final void setNumRelatedSym( int val ) { _numRelatedSym = val; }

    @Override public final SymbolRepeatingGrp getSymbolGrp() { return _symbolGrp; }
    @Override public final void setSymbolGrp( SymbolRepeatingGrp val ) { _symbolGrp = val; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _mdReqId.reset();
        _subsReqType = null;
        _marketDepth = Constants.UNSET_INT;
        _numRelatedSym = Constants.UNSET_INT;
        _symbolGrp = null;
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.MDRequest;
    }

    @Override
    public final MDRequestImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( MDRequestImpl nxt ) {
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
        out.append( "MDRequestImpl" ).append( ' ' );
        out.append( ", mdReqId=" ).append( getMdReqId() );
        out.append( ", subsReqType=" ).append( getSubsReqType() );
        out.append( ", marketDepth=" ).append( getMarketDepth() );
        out.append( ", numRelatedSym=" ).append( getNumRelatedSym() );

        SymbolRepeatingGrpImpl tPtrsymbolGrp = (SymbolRepeatingGrpImpl) getSymbolGrp();
        int tIdxsymbolGrp=0;

        while( tPtrsymbolGrp != null ) {
            out.append( " {#" ).append( ++tIdxsymbolGrp ).append( "} " );
            tPtrsymbolGrp.dump( out );
            tPtrsymbolGrp = tPtrsymbolGrp.getNext();
        }

        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
