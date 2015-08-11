/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.reader;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.binary.fastfix.FastFixDecoder;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.md.us.cme.FastFixTstUtils;
import com.rr.md.us.cme.writer.CMEFastFixEncoder;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;


public class MDIncRefreshCMECodecTest extends BaseTestCase {

    private static final Logger       _log = LoggerFactory.create( MDIncRefreshCMECodecTest.class );
    
    private Decoder            _decoder   = new CMEFastFixDecoder( "CMETstReader", "data/cme/templates.xml", -1, true );
    private CMEFastFixEncoder  _encoder   = new CMEFastFixEncoder( "CMETstWriter", "data/cme/templates.xml", true );
    
    private final byte[]       _buf = _encoder.getBytes();
    
    private String             _dateStr = "20120403";
    private TimeZoneCalculator _calc;

    
    @Override
    public void setUp(){
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _decoder.setTimeZoneCalculator( _calc );
    }
    
    public MDIncRefreshCMECodecTest() {
        // nothing
    }

    public void testOneMDEntry() {
        MDIncRefreshImpl inc = FastFixTstUtils.makeMDIncRefresh( 1 );

        // encode to String
        _encoder.encode( inc );
        
        //  encode String to fastfix
        
        _decoder.parseHeader( _buf, 0, _encoder.getLength() );
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        
        FastFixTstUtils.checkEqualsA( inc, dec );
    }
    
    public void testTopMDEntry() {
        MDIncRefreshImpl inc = FastFixTstUtils.makeTOPBookIncRefresh();

        // encode to String
        _encoder.encode( inc, 83, (byte)0 );
        
        ReusableString destFixMsg = new ReusableString();
        destFixMsg.reset();
        destFixMsg.append( "MSG T83  " );
        destFixMsg.appendHEX( _encoder.getBytes(), _encoder.getOffset(), _encoder.getLength() );

        _log.info( destFixMsg );

        //  encode String to fastfix
        
        ((FastFixDecoder)_decoder).setNextDummy();
        _decoder.parseHeader( _buf, 0, _encoder.getLength() );
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        
        FastFixTstUtils.checkEqualsA( inc, dec );
    }
    
    public void testFiveMDEntry() {
        MDIncRefreshImpl inc = FastFixTstUtils.makeMDIncRefresh( 5 );

        _encoder.encode( inc );
        
        _decoder.parseHeader( _buf, 0, _encoder.getLength() );
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        
        FastFixTstUtils.checkEqualsA( inc, dec );
    }

    public void testFiveMDEntryT81() {
        MDIncRefreshImpl inc = FastFixTstUtils.makeMDIncRefresh( 5 );
        _encoder.encode( inc, 81, (byte) 3 );
        _decoder.parseHeader( _buf, 0, _encoder.getLength() );
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        FastFixTstUtils.checkEqualsA( inc, dec );
    }
    
    public void testFiveMDEntryT83() {
        MDIncRefreshImpl inc = FastFixTstUtils.makeMDIncRefresh( 5 );
        _encoder.encode( inc, 83, (byte) 3 );
        _decoder.parseHeader( _buf, 0, _encoder.getLength() );
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        FastFixTstUtils.checkEqualsA( inc, dec );
    }

    public void testFiveMDEntryT84() {
        MDIncRefreshImpl inc = FastFixTstUtils.makeMDIncRefresh( 5 );
        _encoder.encode( inc, 84, (byte) 3 );
        _decoder.parseHeader( _buf, 0, _encoder.getLength() );
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        FastFixTstUtils.checkEqualsB( inc, dec );
    }

    public void testFiveMDEntryT103() {
        MDIncRefreshImpl inc = FastFixTstUtils.makeMDIncRefresh( 5 );
        _encoder.encode( inc, 103, (byte) 3 );
        _decoder.parseHeader( _buf, 0, _encoder.getLength() );
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        FastFixTstUtils.checkEqualsA( inc, dec );
    }

    public void testFiveMDEntryT109() {
        MDIncRefreshImpl inc = FastFixTstUtils.makeMDIncRefresh( 5 );
        _encoder.encode( inc, 109, (byte) 3 );
        _decoder.parseHeader( _buf, 0, _encoder.getLength() );
        MDIncRefreshImpl dec = (MDIncRefreshImpl) _decoder.decode( _buf, 0, _encoder.getLength() );
        FastFixTstUtils.checkEqualsB( inc, dec );
    }    
}
