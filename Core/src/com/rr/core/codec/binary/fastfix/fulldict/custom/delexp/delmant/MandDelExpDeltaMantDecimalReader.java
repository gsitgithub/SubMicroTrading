/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec.binary.fastfix.fulldict.custom.delexp.delmant;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.DoubleFieldValWrapper;
import com.rr.core.codec.binary.fastfix.common.FastFixDecimal;
import com.rr.core.codec.binary.fastfix.common.FieldUtils;
import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.fulldict.custom.CustomDecimalFieldReader;
import com.rr.core.codec.binary.fastfix.fulldict.delta.int32.IntMandReaderDelta;
import com.rr.core.codec.binary.fastfix.fulldict.delta.int64.LongMandReaderDelta;
import com.rr.core.codec.binary.fastfix.fulldict.entry.DictEntry;

/**
 * CME decimal are all default exponent of 2 with delta mantissa
 *
 * @author Richard Rose
 */
public class MandDelExpDeltaMantDecimalReader extends CustomDecimalFieldReader {

    private final int  _initExp;
    private final long _initMant;
    
    private final FastFixDecimal _prevDecimal = new FastFixDecimal();

    private final IntMandReaderDelta  _exp;
    private final LongMandReaderDelta _mant;
    
    public MandDelExpDeltaMantDecimalReader( ComponentFactory cf, String name, int id, String initExp, String initMant ) {
        this( cf, name, id, FieldUtils.parseInt(initExp), FieldUtils.parseLong(initMant) );
    }
    
    @SuppressWarnings( "boxing" )
    public MandDelExpDeltaMantDecimalReader( ComponentFactory cf, String name, int id, int initExp, long initMant ) {
        super( name, id, false );
        
        String expName = name + "Exp";
        String mantName = name + "Mant";
        
        DictEntry prevExp  = cf.getPrevFieldValInt32Wrapper( expName,  initExp );
        DictEntry prevMant = cf.getPrevFieldValInt64Wrapper( mantName, initMant );

        _exp  = cf.getReader( IntMandReaderDelta.class,   expName,  id, prevExp );
        _mant = cf.getReader( LongMandReaderDelta.class,  mantName, id, prevMant );
        
        _initExp  = initExp;
        _initMant = initMant;

        reset();
    }

    @Override
    public void reset() {
        _prevDecimal.set( _initExp, _initMant );
        _exp.reset();
        _mant.reset();
    }
    
    /**
     * write the field, note the code could easily be extracted and templated but then would get autoboxing and unable to optimise inlining  
     * 
     * @param encoder
     * @param mapWriter
     * @param value
     */
    public double read( final FastFixDecodeBuilder encoder, final PresenceMapReader mapReader ) {
        final int exp   = _exp.read( encoder );
        final long mant = _mant.read( encoder );

        return _prevDecimal.set( exp, mant );
    }
    
    @Override
    public void read( FastFixDecodeBuilder decoder, PresenceMapReader mapReader, DoubleFieldValWrapper dest ) {
        dest.setVal( read( decoder, mapReader ) );
    }
    
    public double getPreviousValue() {
        return _prevDecimal.get();
    }
    
    public double getInitValue( FastFixDecimal t ) {
        return t.set( _exp.getInitValue(), _mant.getInitValue() );
    }
    
    @Override
    public boolean requiresPMap() {
        return _exp.requiresPMap() || _mant.requiresPMap();
    }
}
