/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.utils;

import com.rr.core.codec.binary.BinaryBigEndianEncoderUtils;
import com.rr.core.codec.binary.BinaryBigEndianDecoderUtils;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Constants;

public class TestBinaryCodecUtils extends BaseTestCase {

    public void testBoolean() {
        byte[] buf = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklnopqrstuvwxyz0123456789".getBytes();
        BinaryBigEndianEncoderUtils beu = new BinaryBigEndianEncoderUtils( buf, 3 );
        
        beu.start();
        beu.encodeBool( true );
        beu.encodeBool( false );
        beu.end();
        
        assertEquals( 2, beu.getLength() );
        
        BinaryBigEndianDecoderUtils bdu = new BinaryBigEndianDecoderUtils();
        bdu.start( buf, 3, buf.length );
        assertEquals( true, bdu.decodeBool() );
        assertEquals( false, bdu.decodeBool() );
        bdu.end();
        
        assertEquals( 'C', buf[2] );
        assertEquals( 'B', buf[1] );
    }

    public void testInteger() {
        byte[] buf = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklnopqrstuvwxyz0123456789".getBytes();
        BinaryBigEndianEncoderUtils beu = new BinaryBigEndianEncoderUtils( buf, 3 );
        
        beu.start();
        beu.encodeInt( 0 );
        beu.encodeInt( Integer.MIN_VALUE );
        beu.encodeInt( Integer.MAX_VALUE );
        beu.encodeInt( 123 );
        beu.end();
        
        assertEquals( 16, beu.getLength() );
        
        BinaryBigEndianDecoderUtils bdu = new BinaryBigEndianDecoderUtils();
        bdu.start( buf, 3, buf.length );
        assertEquals( 0, bdu.decodeInt() );
        assertEquals( 0, bdu.decodeInt() ); // Integer.MIN_VALUE used as UNSET_VALUE which is encoded as zero
        assertEquals( Integer.MAX_VALUE, bdu.decodeInt() );
        assertEquals( 123, bdu.decodeInt() );
        bdu.end();
    }

    public void testLong() {
        byte[] buf = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklnopqrstuvwxyz0123456789".getBytes();
        BinaryBigEndianEncoderUtils beu = new BinaryBigEndianEncoderUtils( buf, 3 );
        
        beu.start();
        beu.encodeLong( 0l );
        beu.encodeLong( Long.MIN_VALUE );
        beu.encodeLong( Long.MAX_VALUE );
        beu.encodeLong( 123456789054321l );
        beu.end();
        
        assertEquals( 32, beu.getLength() );
        
        BinaryBigEndianDecoderUtils bdu = new BinaryBigEndianDecoderUtils();
        bdu.start( buf, 3, buf.length );
        assertEquals( 0l, bdu.decodeLong() );
        assertEquals( 0l, bdu.decodeLong() ); // MIN_VALUE used as UNSET_VALUE which is encoded as zero
        assertEquals( Long.MAX_VALUE, bdu.decodeLong() );
        assertEquals( 123456789054321l, bdu.decodeLong() );
        bdu.end();
    }

    public void testPrice() {
        byte[] buf = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789ABCDFGH".getBytes();
        BinaryBigEndianEncoderUtils beu = new BinaryBigEndianEncoderUtils( buf, 3 );
        
        beu.start();
        beu.encodePrice( 0.0 );
        beu.encodePrice( Double.MIN_VALUE );
        beu.encodePrice( Double.MAX_VALUE );
        beu.encodePrice( Constants.UNSET_DOUBLE );
        beu.encodePrice( 0.000009 );
        beu.encodePrice( 123456789.000009 );
        beu.encodePrice( 123456789.0 );
        beu.encodePrice( 12345.6789 );
        beu.end();
        
        assertEquals( 64, beu.getLength() );
        
        BinaryBigEndianDecoderUtils bdu = new BinaryBigEndianDecoderUtils();
        bdu.start( buf, 3, buf.length );
        assertEquals( 0.0, bdu.decodePrice(), Constants.WEIGHT );
        assertEquals( Double.MIN_VALUE, bdu.decodePrice(), Constants.WEIGHT ); //MIN_VALUE used as UNSET_VALUE which is encoded as zero
        assertEquals( Double.MAX_VALUE, bdu.decodePrice(), Constants.WEIGHT );
        assertEquals( 0.0, bdu.decodePrice(), Constants.WEIGHT );              // UNSET encoded as 0.0
        assertEquals( 0.000009, bdu.decodePrice(), Constants.WEIGHT );
        assertEquals( 123456789.000009, bdu.decodePrice(), Constants.WEIGHT );
        assertEquals( 123456789.0, bdu.decodePrice(), Constants.WEIGHT );
        assertEquals( 12345.6789, bdu.decodePrice(), Constants.WEIGHT );
        bdu.end();

        assertEquals( 'G', buf[67] );
    }
}
