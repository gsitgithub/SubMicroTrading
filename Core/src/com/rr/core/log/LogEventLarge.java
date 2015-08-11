/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.log;

import java.nio.ByteBuffer;

import com.rr.core.lang.CoreReusableType;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.lang.ZString;
import com.rr.core.lang.stats.SizeConstants;
import com.rr.core.utils.NumberFormatUtils;

public class LogEventLarge extends BaseLogEvent<LogEventLarge> {

    private static final int EXPECTED_SIZE = SizeConstants.DEFAULT_LOG_EVENT_LARGE;
    private int  _hexStartOffset = -1;
    private int  _estimatedSize  = 0;

    private static final ReusableString _tmpBuf     = new ReusableString( 4096 ); 

    private static final ReusableString _cntBuf     = new ReusableString( 20 ); 
    
    @Override
    public void reset() {
        super.reset();
        _hexStartOffset = -1;
        _estimatedSize  = 0;
    }
    
    @Override
    public int length() {
        return (_hexStartOffset>0) ? _estimatedSize : _buf.length();
    }

    @Override
    protected final int getExpectedMaxEventSize() {
        return EXPECTED_SIZE;
    }

    @Override
    public final ReusableType getReusableType() {
        return CoreReusableType.LogEventLarge;
    }

    public final void set( Level lvl, ZString msg, int hexStartOffset ) {
        _hexStartOffset = hexStartOffset;
        
        set( lvl, msg.getBytes(), msg.getOffset(), msg.length() );
        
        final int bufLen   = _buf.length();
        final int numLines = (bufLen / HEX_LINE_WIDTH) + 2;
        final int hdrSize  = 20;
        
        _estimatedSize = bufLen * 4 + INDEX_LINE_LEN + numLines * (hdrSize + 12);
    }
    
    private static final int             HEX_LINE_WIDTH = 50;
    private static final ReusableString  _hexIndex = new ReusableString(256);
    private static final ReusableString  _asciiIndex = new ReusableString(256);
    private static       int             INDEX_LINE_LEN;
    
    static {
        _asciiIndex.append( "     1        10        20        30        40        50" );
        _hexIndex.append(   "     1  2  3  4  5  6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 " +
                            "26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50" );
        INDEX_LINE_LEN = _hexIndex.length();
    }
    
    @Override
    public void encode( ByteBuffer dest ) {
        if ( _hexStartOffset < 0 ) {
            super.encode( dest );
        } else {
            final int bufLen       = _buf.length();
            final int numLines     = (bufLen / HEX_LINE_WIDTH) + 1;
            final int startDestPos = encodeHeader( dest );
            final int hdrSize      = (dest.position() - startDestPos) + _hexStartOffset;
                  int bufCapacity  = (dest.capacity() - startDestPos) - MAGIC_SPARE;
            
            synchronized( _tmpBuf ) {
                _tmpBuf.reset();
                
                if ( _estimatedSize > bufCapacity ) {
                    truncateHexEncode( dest, hdrSize, bufCapacity );
                } else {
                    // 18:49:54.961 [info]  0 ABC.
                    //                     80 ABC.
                    //                        1        10        20  .. 80     1        10        20  .. 80   
    
                    _tmpBuf.ensureCapacity( _estimatedSize );
                    
                    boolean hexRequired = false;
    
                    dest.put( (byte)0x0A );
                    hexRequired = encodeAscii( dest, numLines );
                    writeAsciiIndex( dest );
                    
                    if ( hexRequired ) {
                        dest.put( (byte)0x0A );
                        encodeAsHex( dest, numLines );
                        writeHexIndex( dest );
                        
                        dest.put( (byte)0x0A );
                        dest.put( "byteCount=".getBytes() );
                        _cntBuf.reset();
                        _cntBuf.append( bufLen - _hexStartOffset );
                        dest.put( _cntBuf.getBytes(), 0, _cntBuf.length()); 
                    }

                    dest.put( (byte)0x0A );
                }
            }
        }
    }

    private void writeHexIndex( ByteBuffer dest ) {
        dest.put( (byte)0x0A );
        dest.put( _hexIndex.getBytes(),   0, _hexIndex.length() );
    }

    private void writeAsciiIndex( ByteBuffer dest ) {
        dest.put( (byte)0x0A );
        dest.put( _asciiIndex.getBytes(),   0, _asciiIndex.length() );
    }

    private boolean encodeAscii( ByteBuffer dest, final int numLines ) {
              boolean hexOn       = false;
              int     srcIdx      = _buf.getOffset();
        final int     maxIdx      = srcIdx + _buf.length();
        final byte[]  src         = _buf.getBytes();
              byte[]  tmpAscii    = _tmpBuf.getBytes();
              int     tmpAsciiIdx;
              int     lineStart   = 0;  

        if ( _hexStartOffset > 0 ) {
            dest.put( src, srcIdx, _hexStartOffset );
            srcIdx += _hexStartOffset;
        }
        
        int lineMaxIdx  = srcIdx + HEX_LINE_WIDTH;

        for( int line=0 ; line < numLines ; ++line ) {
            tmpAsciiIdx = 0;
            
            dest.put( (byte)0x0A );
    
            NumberFormatUtils.addPositiveIntFixedLength( tmpAscii, tmpAsciiIdx, lineStart, 4 );
            tmpAsciiIdx += 4;
            tmpAscii[ tmpAsciiIdx++ ] = ' ';
            
            while( srcIdx < lineMaxIdx ) {
                if ( srcIdx >= maxIdx ) {
                    ++srcIdx;
                    tmpAscii[ tmpAsciiIdx++ ] = ' ';
                    continue;
                }
                
                final byte val = src[ srcIdx++ ];
                
                if ( val < 0x20 || val > 'z' ) {
                    hexOn = true;
                    tmpAscii[ tmpAsciiIdx++ ] = '.';
                } else {
                    tmpAscii[ tmpAsciiIdx++ ] = val;
                }
            }
    
            dest.put( tmpAscii, 0, tmpAsciiIdx );
            
            lineMaxIdx += HEX_LINE_WIDTH;
            lineStart  += HEX_LINE_WIDTH;
        }
        
        return hexOn;
    }

    private boolean encodeAsHex( ByteBuffer dest, final int numLines ) {
            boolean hexOn       = false;
            int     srcIdx      = _buf.getOffset();
      final int     maxIdx      = srcIdx + _buf.length();
      final byte[]  src         = _buf.getBytes();
            byte[]  tmpHex      = _tmpBuf.getBytes();
            int     tmpHexIdx;
            int     lineStart   = 0;  
            byte    major;
            byte    minor;
    
      if ( _hexStartOffset > 0 ) {
          dest.put( src, srcIdx, _hexStartOffset );
          srcIdx += _hexStartOffset;
      }
      
      int lineMaxIdx  = srcIdx + HEX_LINE_WIDTH;
    
      for( int line=0 ; line < numLines ; ++line ) {
          tmpHexIdx = 0;
          
          dest.put( (byte)0x0A );
    
          NumberFormatUtils.addPositiveIntFixedLength( tmpHex, tmpHexIdx, lineStart, 4 );
          tmpHexIdx += 4;
          
          while( srcIdx < lineMaxIdx ) {
              if ( srcIdx >= maxIdx ) {
                  ++srcIdx;
                  continue;
              }
              
              final byte val = src[ srcIdx++ ];
              
              major = (byte)((0xFF & val) >> 4 ); // upper nybble 
              minor = (byte)((0x0F & val) );      // lower nybble
              
              if ( major <= 9 )   major = (byte)(major + '0');
              else                major = (byte)((major + 'A') - 10);
              
              if ( minor <= 9 )   minor = (byte)(minor + '0');
              else                minor = (byte)((minor + 'A') - 10);
              
              tmpHex[   tmpHexIdx++ ]   = ' '; 
              tmpHex[   tmpHexIdx++ ]   = major;  
              tmpHex[   tmpHexIdx++ ]   = minor; 
          }
    
          tmpHexIdx   = rtrim( tmpHex,   tmpHexIdx );
          
          dest.put( tmpHex,   0, tmpHexIdx );
          
          lineMaxIdx += HEX_LINE_WIDTH;
          lineStart  += HEX_LINE_WIDTH;
      }
      
      return hexOn;
    }

    private int rtrim( byte[] data, int endIdx ) {
        while( --endIdx >= 0 && data[endIdx] == ' ' ) {
            // rtrim
        }
        
        return endIdx+1;
    }

    private int encodeHeader( ByteBuffer dest ) {
        final int startDestPos = dest.position();
        TimeZoneCalculator.instance().utcFullTimeToLocal( dest, _time );
        
        final byte[] levelHdr = _level.getLogHdr();
        dest.put( levelHdr, 0, levelHdr.length );
        return startDestPos;
    }

    private void truncateHexEncode( ByteBuffer dest, final int hdrLen, int bufCapacity ) {
        // not enough space dont bother with hex
        int    copyBytes = bufCapacity - TRUNCATED.length - hdrLen;
        
        _tmpBuf.ensureCapacity( copyBytes );
        
        byte[] src    = _buf.getBytes();
        byte[] tmp    = _tmpBuf.getBytes();
        
        final int    maxIdx = copyBytes + _buf.getOffset();
              int    tmpIdx = 0;
              
        for( int idx=_buf.getOffset() ; idx < maxIdx ; idx++ ) {
            final byte val = src[idx];
            
            tmp[tmpIdx++] = (Character.isISOControl( val )) ? (byte)'.' : val;
        }
        
        dest.put( tmp, 0, copyBytes );
        dest.put( TRUNCATED, 0, TRUNCATED.length );
    }
}
