/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.model.generated.internal.events.impl;

import com.rr.model.generated.internal.type.ETIOrderProcessingType;
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

public final class ETISessionLogonRequestImpl implements BaseETIRequest, ETISessionLogonRequestWrite, Reusable<ETISessionLogonRequestImpl> {

   // Attrs

    private          ETISessionLogonRequestImpl _next = null;
    private volatile Message        _nextMessage    = null;
    private          MessageHandler _messageHandler = null;
    private int _heartBtIntMS = Constants.UNSET_INT;
    private int _partyIDSessionID = Constants.UNSET_INT;
    private final ReusableString _defaultCstmApplVerID = new ReusableString( SizeType.ETI_INTERFACE_VERSION_LENGTH.getSize() );
    private final ReusableString _password = new ReusableString( SizeType.ETI_PASSWORD_LENGTH.getSize() );
    private boolean _orderRoutingIndicator = false;
    private final ReusableString _applicationSystemName = new ReusableString( SizeType.ETI_APP_SYS_NAME_LENGTH.getSize() );
    private final ReusableString _applicationSystemVer = new ReusableString( SizeType.ETI_APP_SYS_VER_LENGTH.getSize() );
    private final ReusableString _applicationSystemVendor = new ReusableString( SizeType.ETI_APP_SYS_VENDOR_LENGTH.getSize() );
    private int _msgSeqNum = Constants.UNSET_INT;

    private ETIOrderProcessingType _applUsageOrders;
    private ETIOrderProcessingType _applUsageQuotes;

    private byte           _flags          = 0;

   // Getters and Setters
    @Override public final int getHeartBtIntMS() { return _heartBtIntMS; }
    @Override public final void setHeartBtIntMS( int val ) { _heartBtIntMS = val; }

    @Override public final int getPartyIDSessionID() { return _partyIDSessionID; }
    @Override public final void setPartyIDSessionID( int val ) { _partyIDSessionID = val; }

    @Override public final ViewString getDefaultCstmApplVerID() { return _defaultCstmApplVerID; }

    @Override public final void setDefaultCstmApplVerID( byte[] buf, int offset, int len ) { _defaultCstmApplVerID.setValue( buf, offset, len ); }
    @Override public final ReusableString getDefaultCstmApplVerIDForUpdate() { return _defaultCstmApplVerID; }

    @Override public final ViewString getPassword() { return _password; }

    @Override public final void setPassword( byte[] buf, int offset, int len ) { _password.setValue( buf, offset, len ); }
    @Override public final ReusableString getPasswordForUpdate() { return _password; }

    @Override public final ETIOrderProcessingType getApplUsageOrders() { return _applUsageOrders; }
    @Override public final void setApplUsageOrders( ETIOrderProcessingType val ) { _applUsageOrders = val; }

    @Override public final ETIOrderProcessingType getApplUsageQuotes() { return _applUsageQuotes; }
    @Override public final void setApplUsageQuotes( ETIOrderProcessingType val ) { _applUsageQuotes = val; }

    @Override public final boolean getOrderRoutingIndicator() { return _orderRoutingIndicator; }
    @Override public final void setOrderRoutingIndicator( boolean val ) { _orderRoutingIndicator = val; }

    @Override public final ViewString getApplicationSystemName() { return _applicationSystemName; }

    @Override public final void setApplicationSystemName( byte[] buf, int offset, int len ) { _applicationSystemName.setValue( buf, offset, len ); }
    @Override public final ReusableString getApplicationSystemNameForUpdate() { return _applicationSystemName; }

    @Override public final ViewString getApplicationSystemVer() { return _applicationSystemVer; }

    @Override public final void setApplicationSystemVer( byte[] buf, int offset, int len ) { _applicationSystemVer.setValue( buf, offset, len ); }
    @Override public final ReusableString getApplicationSystemVerForUpdate() { return _applicationSystemVer; }

    @Override public final ViewString getApplicationSystemVendor() { return _applicationSystemVendor; }

    @Override public final void setApplicationSystemVendor( byte[] buf, int offset, int len ) { _applicationSystemVendor.setValue( buf, offset, len ); }
    @Override public final ReusableString getApplicationSystemVendorForUpdate() { return _applicationSystemVendor; }

    @Override public final int getMsgSeqNum() { return _msgSeqNum; }
    @Override public final void setMsgSeqNum( int val ) { _msgSeqNum = val; }


    @Override public final boolean getPossDupFlag() { return isFlagSet( MsgFlag.PossDupFlag ); }
    @Override public final void setPossDupFlag( boolean val ) { setFlag( MsgFlag.PossDupFlag, val ); }

   // Reusable Contract

    @Override
    public final void reset() {
        _heartBtIntMS = Constants.UNSET_INT;
        _partyIDSessionID = Constants.UNSET_INT;
        _defaultCstmApplVerID.reset();
        _password.reset();
        _applUsageOrders = null;
        _applUsageQuotes = null;
        _orderRoutingIndicator = false;
        _applicationSystemName.reset();
        _applicationSystemVer.reset();
        _applicationSystemVendor.reset();
        _msgSeqNum = Constants.UNSET_INT;
        _flags = 0;
        _next = null;
        _nextMessage = null;
        _messageHandler = null;
    }

    @Override
    public final ReusableType getReusableType() {
        return ModelReusableTypes.ETISessionLogonRequest;
    }

    @Override
    public final ETISessionLogonRequestImpl getNext() {
        return _next;
    }

    @Override
    public final void setNext( ETISessionLogonRequestImpl nxt ) {
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
        out.append( "ETISessionLogonRequestImpl" ).append( ' ' );
        out.append( ", heartBtIntMS=" ).append( getHeartBtIntMS() );
        out.append( ", partyIDSessionID=" ).append( getPartyIDSessionID() );
        out.append( ", defaultCstmApplVerID=" ).append( getDefaultCstmApplVerID() );
        out.append( ", password=" ).append( getPassword() );
        out.append( ", applUsageOrders=" ).append( getApplUsageOrders() );
        out.append( ", applUsageQuotes=" ).append( getApplUsageQuotes() );
        out.append( ", orderRoutingIndicator=" ).append( getOrderRoutingIndicator() );
        out.append( ", applicationSystemName=" ).append( getApplicationSystemName() );
        out.append( ", applicationSystemVer=" ).append( getApplicationSystemVer() );
        out.append( ", applicationSystemVendor=" ).append( getApplicationSystemVendor() );
        out.append( ", msgSeqNum=" ).append( getMsgSeqNum() );
        out.append( ", possDupFlag=" ).append( getPossDupFlag() );
    }

}
