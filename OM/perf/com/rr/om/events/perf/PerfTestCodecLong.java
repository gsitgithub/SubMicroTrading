/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import com.rr.core.codec.AbstractFixDecoder;
import com.rr.core.codec.FixEncodeBuilder;
import com.rr.core.codec.FixEncodeBuilderImpl;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Message;
import com.rr.core.utils.Utils;
import com.rr.om.dummy.warmup.DummyInstrumentLocator;


/**
 * perf test using long instead of int for codec
 *
 * @author Richard Rose
 */
public class PerfTestCodecLong extends BaseTestCase {

    private static final Logger _log = LoggerFactory.console( PerfTestCodecLong.class );
    
    private static class DummyDecoder extends AbstractFixDecoder {

        public DummyDecoder( byte major, byte minor ) {
            super( major, minor );
            setInstrumentLocator( new DummyInstrumentLocator() );
        }

        public void setup( final byte[] fixMsg, final int offset, final int maxIdx ) {
            _fixMsg = fixMsg;
            _offset = offset;
            _idx    = offset;
            _maxIdx = maxIdx;
        }
        
        public int decodeInt() {
            return getIntVal();
        }

        public long decodeLong() {
            return getLongVal();
        }
        
        public int decodeTag() {
            return getTag();
        }

        @Override
        protected Message doMessageDecode() {
            return null;
        }
    }
    
    private int count = 1000000;

    public PerfTestCodecLong() {
        // nothing
    }

    public void testEncoding() {
        doTest( 5, count, 1 );
        doTest( 5, count, 100 );
        doTest( 5, count, 54321 );
        doTest( 5, count, 987654321 );
    }
    

    private void doTest( int iter, int max, int num ) {
        for ( int i=0 ; i < iter ; ++i ) {
            runTest( max, num );
        }
    }

    private void runTest( int max, int num ) {
        
        final byte[] bufInt  = new byte[512];
        final byte[] bufLong = new byte[512];
        
        FixEncodeBuilder  encoderInt  = new FixEncodeBuilderImpl( bufInt,  0, (byte)'4', (byte)'4' );
        FixEncodeBuilder  encoderLong = new FixEncodeBuilderImpl( bufLong, 0, (byte)'4', (byte)'4' );
        
        DummyDecoder decoder = new DummyDecoder( (byte)'4', (byte)'4' );
        
        for ( int i=0 ; i < 10 ; i++ ){
            encoderInt.encodeInt( 38, num );
            encoderLong.encodeLong( 38, num );
        }
        
        _log.info(  "START TEST ==================" );

        @SuppressWarnings( "unused" )
        long tmp;
        @SuppressWarnings( "unused" )
        int tag;
        
        long start = Utils.nanoTime();
        for( int i=0 ; i < max ; i++ ) {
            decoder.setup( bufInt, 0, bufInt.length );
            for( int j=0 ; j < 10 ;  ++j ) {
                tag = decoder.decodeTag();
                tmp = decoder.decodeInt();
            }
        }
        long end = Utils.nanoTime();
        System.out.println( "DECODE INT " + num + ", cnt=" + max + ", time=" + ((end - start)/max) );

        start = Utils.nanoTime();
        for( int i=0 ; i < max ; i++ ) {
            decoder.setup( bufLong, 0, bufLong.length );
            for( int j=0 ; j < 10 ;  ++j ) {
                tag = decoder.decodeTag();
                tmp = decoder.decodeLong();
            }
        }
        end = Utils.nanoTime();
        System.out.println( "DECODE LONG " + num + ", cnt=" + max + ", time=" + ((end - start)/max) );

        start = Utils.nanoTime();
        for( int i=0 ; i < max ; i++ ) {
            encoderInt.start();
            for( int j=0 ; j < 10 ;  ++j ) {
                encoderInt.encodeInt( 38, num );
            }
        }
        end = Utils.nanoTime();
        System.out.println( "ENCODE INT " + num + ", cnt=" + max + ", time=" + ((end - start)/max) );

        start = Utils.nanoTime();
        for( int i=0 ; i < max ; i++ ) {
            encoderLong.start();
            for( int j=0 ; j < 10 ;  ++j ) {
                encoderLong.encodeLong( 38, num );
            }
        }
        end = Utils.nanoTime();
        System.out.println( "ENCODE LONG, cnt=" + max + ", time=" + ((end - start)/max) );
    }
}
