/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;

/**
 * Debug proxy wrapper of the decode builder
 * 
 * @NOTE unsigned numbers will appear signed
 */
public final class DebugBinaryDecodeBuilder<T extends AbstractBinaryDecoderUtils> implements BinaryDecodeBuilder {

    private static final Logger       _log = LoggerFactory.create( DebugBinaryDecodeBuilder.class );
    
    private final ReusableString _dump;
    
    private final T _builder;

    private int _startIdx;

    public DebugBinaryDecodeBuilder( ReusableString dumpStr, T builder ) {
        super();
        _dump = dumpStr;
        _builder = builder;
    }

    @Override
    public void start( final byte[] msg, final int offset, final int maxIdx ) {
        _builder.start( msg, offset, maxIdx );
        _dump.reset();
        _dump.append( "\n\n{    DECODE " );
    }

    @Override
    public final int getMaxIdx() {
        return _builder.getMaxIdx();
    }

    @Override
    public void end() {
        _builder.end();
        _dump.append( "\n}\n" );
        _log.infoLarge( _dump );
        _dump.reset();
    }
    
    @Override
    public int decodeTimestampUTC() {
        startTrace( "timestampUTC" );
        int val = _builder.decodeTimestampUTC();
        _dump.append( val );
        endTrace();
        return val;
    }
    
    @Override
    public double decodePrice() {
        startTrace( "price" );
        double val = _builder.decodePrice();
        _dump.append( val );
        endTrace();
        return val;
    }
    
    @Override
    public long  decodeLong() {
        startTrace( "long" );
        long val = _builder.decodeLong();
        _dump.append( val );
        endTrace();
        return val;
    }
    
    @Override
    public long decodeULong() {
        startTrace( "ulong" );
        long val = _builder.decodeULong();
        _dump.append( val );
        endTrace();
        return val;
    }
    
    @Override
    public int   decodeInt() {
        startTrace( "int" );
        int val = _builder.decodeInt();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public int decodeUInt() {
        startTrace( "uint" );
        int val = _builder.decodeUInt();
        _dump.append( val );
        endTrace();
        return val;
    }
    
    @Override
    public short decodeShort() {
        startTrace( "short" );
        short val = _builder.decodeShort();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public short decodeUShort() {
        startTrace( "ushort" );
        short val = _builder.decodeUShort();
        _dump.append( val );
        endTrace();
        return val;
    }
    
    @Override
    public byte decodeByte() {
        startTrace( "byte" );
        byte val = _builder.decodeByte();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public byte decodeUByte() {
        startTrace( "ubyte" );
        byte val = _builder.decodeUByte();
        _dump.append( val );
        endTrace();
        return val;
    }
    
    @Override
    public byte decodeChar() {
        startTrace( "char" );
        byte val = _builder.decodeChar();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public void decodeString( final ReusableString dest ) {
        startTrace( "string" );
        _builder.decodeString( dest );
        _dump.append( dest );
        endTrace();
    }

    @Override
    public boolean decodeBool() {
        startTrace( "bool" );
        boolean val = _builder.decodeBool();
        _dump.append( val );
        endTrace();
        return val;
    }
    
    
    /**
     * encodes a fixed width string, string must be null padded and null terminated
     * @param str
     * @param fixedDataSize
     */
    @Override
    public final void decodeStringFixedWidth( final ReusableString dest, final int len ) {
        startTrace( "stringFixedWidth" );
        _builder.decodeStringFixedWidth( dest, len );
        _dump.append( dest );
        endTrace();
    }
    
    @Override
    public final void decodeData( final ReusableString dest, final int len ) {
        startTrace( "data" );
        _builder.decodeData( dest, len );
        endTrace();
    }

    private void startTrace( String type ) {
        _dump.append( type ).append( ' ' );
        _startIdx = _builder.getCurrentIndex();
    }

    private void endTrace() {
        int endIdx = _builder.getCurrentIndex();
        int bytes = endIdx - _startIdx;
        _dump.append( ",  bytes=" ).append( bytes ).append( ", offset=" ).append( _startIdx - _builder.getOffset() ).append( ", raw=[" );
        _dump.appendHEX( _builder.getBuffer(), _startIdx, bytes );
        _dump.append( " ] " );
    }

    @Override
    public void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        _builder.setTimeZoneCalculator( calc );
    }

    @Override
    public void setMaxIdx( int maxIdx ) {
        _builder.setMaxIdx( maxIdx );
    }

    @Override
    public void clear() {
        _builder.clear();
    }

    @Override
    public int getNextFreeIdx() {
        return _builder.getNextFreeIdx();
    }

    @Override
    public int getOffset() {
        return _builder.getOffset();
    }

    @Override
    public int getLength() {
        return _builder.getLength();
    }

    @Override
    public byte[] getBuffer() {
        return _builder.getBuffer();
    }

    @Override
    public int getCurrentIndex() {
        return _builder.getCurrentIndex();
    }

    @Override
    public void skip( int size ) {
        startTrace( "skip" );
        _builder.skip( size );
        endTrace();
    }

    @Override
    public double decodeDecimal() {
        startTrace( "decimal" );
        double val = _builder.decodeDecimal();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public int decodeQty() {
        startTrace( "qty" );
        int val = _builder.decodeQty();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public int decodeTimeLocal() {
        startTrace( "timeLocal" );
        int val = _builder.decodeTimeLocal();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public int decodeDate() {
        startTrace( "date" );
        int val = _builder.decodeDate();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public int decodeTimeUTC() {
        startTrace( "timeUTC" );
        int val = _builder.decodeTimeUTC();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public int decodeTimestampLocal() {
        startTrace( "timestampLocal" );
        int val = _builder.decodeTimestampLocal();
        _dump.append( val );
        endTrace();
        return val;
    }

    @Override
    public void decodeLongToString( ReusableString dest ) {
        startTrace( "longToString" );
        _builder.decodeLongToString( dest );
        _dump.append( dest );
        endTrace();
    }

    @Override
    public void decodeIntToString( ReusableString dest ) {
        startTrace( "intToString" );
        _builder.decodeIntToString( dest );
        _dump.append( dest );
        endTrace();
    }

    @Override
    public void decodeZStringFixedWidth( ReusableString dest, int len ) {
        startTrace( "zStringFixedWidth" );
        _builder.decodeZStringFixedWidth( dest, len );
        _dump.append( dest );
        endTrace();
    }
}
