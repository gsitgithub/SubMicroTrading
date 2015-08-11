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
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;


public class DebugBinaryEncodeBuilder<T extends AbstractBinaryEncoderUtils> implements BinaryEncodeBuilder {

    private static final Logger       _log = LoggerFactory.create( DebugBinaryEncodeBuilder.class );
    
    private final ReusableString _dump;
    
    private final T _builder;
    
    private int _startIdx;

    public DebugBinaryEncodeBuilder( ReusableString dumpStr, T builder ) {
        _dump = dumpStr;
        _builder = builder;
    }
    
    @Override
    public void start() {
        _builder.start();
        _dump.reset();
        _dump.append( "\n\n{  ENCODE " );
    }

    @Override
    public void start( int msgType ) {
        _builder.start( msgType );
        _dump.reset();
        _dump.append( "\n\n{  ENCODE msgType=" ).append( msgType );
    }

    public T getBuilder() {
        return _builder;
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
    public int getCurLength() {
        return _builder.getCurLength();
    }

    @Override
    public int end() {
        int len = _builder.end();
        _dump.append( "\n} bytes=" ).append( len ).append( "\n" );
        _log.infoLarge( _dump );
        return len;
    }

    @Override
    public void encodeChar( byte val ) {
        startTrace( "char" );
        _builder.encodeChar( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeString( ZString val ) {
        startTrace( "str" );
        _builder.encodeString( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeString( ZString str, int maxLen ) {
        startTrace( "str(maxLen)" );
        _builder.encodeString( str, maxLen );
        _dump.append( str ).append( "  (maxLen=" ).append( maxLen ).append( ")" );
        endTrace();
    }

    @Override
    public void encodeString( byte[] buf, int offset, int len ) {
        startTrace( "str" );
        _builder.encodeString( buf, offset, len );
        _dump.append( buf, offset, len );
        _dump.append( "  (len=" ).append( len ).append( ")" );
        endTrace();
    }

    @Override
    public void encodeStringFixedWidth( byte[] buf, int offset, int fixedDataSize ) {
        startTrace( "str" );
        _builder.encodeStringFixedWidth( buf, offset, fixedDataSize );
        _dump.append( buf, offset, fixedDataSize );
        _dump.append( "  (maxLen=" ).append( fixedDataSize ).append( ")" );
        endTrace();
    }

    @Override
    public void encodeData( ZString str, int maxLen ) {
        startTrace( "data" );
        _builder.encodeData( str, maxLen );
        _dump.append( str ).append( "  (len=" ).append( maxLen ).append( ")" );
        endTrace();
    }

    @Override
    public void encodeBytes( byte[] buf ) {
        startTrace( "bytes" );
        _builder.encodeBytes( buf );
        _dump.append( "  (len=" ).append( buf.length ).append( ")" );
        endTrace();
    }

    @Override
    public void encodeDecimal( double val ) {
        startTrace( "decimal" );
        _builder.encodeDecimal( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodePrice( double val ) {
        startTrace( "price" );
        _builder.encodePrice( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeLong( long val ) {
        startTrace( "long" );
        _builder.encodeLong( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeInt( int val ) {
        startTrace( "int" );
        _builder.encodeInt( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeQty( int val ) {
        startTrace( "qty" );
        _builder.encodeQty( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeShort( int val ) {
        startTrace( "short" );
        _builder.encodeShort( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeByte( byte val ) {
        startTrace( "byte" );
        _builder.encodeByte( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeZStringFixedWidth( ZString val, int len ) {
        startTrace( "ZStringFixedWidth" );
        _builder.encodeZStringFixedWidth( val, len );
        _dump.append( val );
        _dump.append( "  (len=" ).append( len ).append( ")" );
        endTrace();
    }

    @Override
    public void encodeZStringFixedWidth( byte[] buf, int offset, int fixedDataSize ) {
        startTrace( "str" );
        _builder.encodeZStringFixedWidth( buf, offset, fixedDataSize );
        _dump.append( buf, offset, fixedDataSize );
        _dump.append( "  (maxLen=" ).append( fixedDataSize ).append( ")" );
        endTrace();
    }
    

    @Override
    public void encodeStringFixedWidth( ZString val, int len ) {
        startTrace( "stringFixedWidth" );
        _builder.encodeStringFixedWidth( val, len );
        _dump.append( val );
        _dump.append( "  (len=" ).append( len ).append( ")" );
        endTrace();
    }

    @Override
    public void encodeStringAsLong( ZString val ) {
        startTrace( "stringAsLong" );
        _builder.encodeStringAsLong( val );
        _dump.append( val );
        _dump.append( "  (len=" ).append( val.length() ).append( ")" );
        endTrace();
    }

    @Override
    public void encodeStringAsInt( ZString val ) {
        startTrace( "stringAsInt" );
        _builder.encodeStringAsInt( val );
        _dump.append( val );
        _dump.append( "  (len=" ).append( val.length() ).append( ")" );
        endTrace();
    }

    @Override
    public void encodeULong( long val ) {
        startTrace( "ulong" );
        _builder.encodeULong( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeUInt( int val ) {
        startTrace( "uint" );
        _builder.encodeUInt( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeUShort( short val ) {
        startTrace( "ushort" );
        _builder.encodeUShort( val );
        _dump.append( val );
        endTrace();
    }

    @Override
    public void encodeUByte( byte code ) {
        startTrace( "ubyte" );
        _builder.encodeUByte( code );
        _dump.append( code );
        endTrace();
    }

    @Override
    public void encodeDate( int yyyymmdd ) {
        startTrace( "date" );
        _builder.encodeDate( yyyymmdd );
        _dump.append( yyyymmdd );
        endTrace();
    }

    @Override
    public void encodeTimestampUTC( int msFromStartofDayUTC ) {
        startTrace( "timestampUTC" );
        _builder.encodeTimestampUTC( msFromStartofDayUTC );
        _dump.append( msFromStartofDayUTC );
        endTrace();
    }

    @Override
    public void encodeTimeUTC( int msFromStartofDayUTC ) {
        startTrace( "timeUTC" );
        _builder.encodeTimeUTC( msFromStartofDayUTC );
        _dump.append( msFromStartofDayUTC );
        endTrace();
    }

    @Override
    public void encodeBool( boolean isOn ) {
        startTrace( "bool" );
        _builder.encodeBool( isOn );
        _dump.append( isOn );
        endTrace();
    }

    @Override
    public void clear() {
        _builder.clear();
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
    public void setTimeZoneCalculator( TimeZoneCalculator calc ) {
        _builder.setTimeZoneCalculator( calc );
    }

    @Override
    public void encodeFiller( int len ) {
        startTrace( "filler" );
        _builder.encodeFiller( len );
        _dump.append( " len=" ).append( len );
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
}
