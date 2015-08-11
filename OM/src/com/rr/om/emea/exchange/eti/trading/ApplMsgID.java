/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.emea.exchange.eti.trading;

import com.rr.core.codec.RuntimeDecodingException;
import com.rr.core.codec.binary.BinaryBigEndianDecoderUtils;
import com.rr.core.codec.binary.BinaryBigEndianEncoderUtils;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;

/**
 * 16 byte ETI ApplMsgID is an unsigned long long 
 * 
 * java doesnt have unsigned so need to cater for negative equivalents
 */
public final class ApplMsgID {

    public long _upper;
    public long _lower;
    
    public ApplMsgID() {
        // nothing
    }

    public ApplMsgID( long upper, long lower ) {
        _upper = upper;
        _lower = lower;
    }

    public void set( ZString rawBytes ) {
        int len = rawBytes.length();
        
        if ( len == 0 ) {
            _upper = 0;
            _lower = 0;
        } else {
                
            if ( len != 16 ) throw new RuntimeDecodingException( "Bad ApplMsgID expected 16 bytes not " + len );
            
            byte[] data = rawBytes.getBytes();
            int idx = rawBytes.getOffset();
            
            _upper = BinaryBigEndianDecoderUtils.decodeLong( data, idx );
            _lower = BinaryBigEndianDecoderUtils.decodeLong( data, idx+8 );
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (_lower ^ (_lower >>> 32));
        result = prime * result + (int) (_upper ^ (_upper >>> 32));
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        ApplMsgID other = (ApplMsgID) obj;
        if ( _lower != other._lower )
            return false;
        if ( _upper != other._upper )
            return false;
        return true;
    }

    public void set( ApplMsgID other ) {
        this._upper = other._upper;
        this._lower = other._lower;
    }
    
    public boolean isSequential( ApplMsgID prevId ) {
        long upperDiff = this._upper - prevId._upper;
        long lowerDiff = this._lower - prevId._lower;
        
        // check for expected lower to upper boundary
        if ( prevId._lower == -1 ) {
            if ( _lower == 0 ) { // upper increments by 1 and lower goes from -1 to 0
                if ( upperDiff == 1 ) {
                    return true;
                }
            } 
            return false;
        }
        
        if ( upperDiff == 0 && lowerDiff == 1 ) {
            return true; // standard response
        }
        
        if ( prevId._lower == 0 && prevId._upper == 0 ) { // first message, sequence doesnt have to start with 0
            return true;
        }
        
        return false;
    }

    public void toBytes( ReusableString buf ) {
        buf.ensureCapacity( 16 );
        buf.reset();
        byte[] bytes = buf.getBytes();
        
        BinaryBigEndianEncoderUtils.encodeLong( bytes, 0, _upper );
        BinaryBigEndianEncoderUtils.encodeLong( bytes, 8, _lower );
    }

    /**
     * update this id to the new id if its greater than current value
     */
    public void setIfGreater( ZString newId ) {
        byte[] data = newId.getBytes();
        int    idx  = newId.getOffset();
        
        long newUpper = BinaryBigEndianDecoderUtils.decodeLong( data, idx );
        long newLower = BinaryBigEndianDecoderUtils.decodeLong( data, idx+8 );

        long upperDiff = newUpper - this._upper;
        long lowerDiff = newLower - this._lower;

        if ( upperDiff > 0 || (upperDiff==0 && lowerDiff > 0) || (_upper == 0 && _lower == 0) ) {
            _upper = newUpper;
            _lower = newLower;
        }
    }

    @Override
    public String toString() {
        return new String( "[" + _upper + ", " + _lower + "]" );
    }

    public void dump( ReusableString buf ) {
        buf.append( "AppMsgID high=" ).append( _upper ).append( ", low=" ).append( _lower );
    }
}
