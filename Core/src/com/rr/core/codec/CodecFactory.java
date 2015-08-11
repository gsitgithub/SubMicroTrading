/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.codec;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import com.rr.core.component.SMTComponent;
import com.rr.core.utils.SMTRuntimeException;

public class CodecFactory implements SMTComponent {

    private final Map<CodecName, Constructor<? extends Encoder>> _encoderMap    = new HashMap<CodecName, Constructor<? extends Encoder>>();
    private final Map<CodecName, Constructor<? extends Decoder>> _decoderMap    = new HashMap<CodecName, Constructor<? extends Decoder>>();
    private final Map<CodecName, Constructor<? extends Decoder>> _recDecoderMap = new HashMap<CodecName, Constructor<? extends Decoder>>();
    private final String _id;

    public CodecFactory() {
        this( new String( "CodecFactory") );
    }
    
    public CodecFactory( String id ) {
        _id = id;
    }

    @Override
    public String getComponentId() {
        return _id;
    }
    
    public void register( CodecName id, Class<? extends Encoder> encoder, Class<? extends Decoder> decoder, Class<? extends Decoder> recoveryDecoder ) {
        
        try {
            _encoderMap.put( id, encoder.getConstructor( byte[].class, int.class ) );
            _decoderMap.put( id, decoder.getConstructor() );
            _recDecoderMap.put( id, recoveryDecoder.getConstructor() );
            
        } catch( Exception e ) {
            throw new SMTRuntimeException( "Error in registering codec " + id.toString() + " " + e.getMessage(), e );
        }
    }
        
    public Encoder getEncoder( CodecName id, byte[] buf, int offset ) {
        
        Constructor<? extends Encoder> constructor = _encoderMap.get( id );

        Encoder encoder = null;
        
        if ( constructor != null ) {
            try {
                encoder = constructor.newInstance( buf, new Integer(offset) );
            } catch( Exception e ) {
                throw new SMTRuntimeException( "Error in instantiating encoder id " + id.toString() + " " + e.getMessage(), e );
            }
        }
        
        if ( encoder == null ) {
            throw new SMTRuntimeException( "Unsupported codec id " + id.toString() );
        }

        return encoder;
    }

    public Decoder getDecoder( CodecName id ) {
        Constructor<? extends Decoder> constructor = _decoderMap.get( id );

        Decoder decoder = null;
        
        if ( constructor != null ) {
            try {
                decoder = constructor.newInstance();
            } catch( Exception e ) {
                throw new SMTRuntimeException( "Error in instantiating decoder id " + id.toString() + " " + e.getMessage(), e );
            }
        }
        
        if ( decoder == null ) {
            throw new SMTRuntimeException( "Unsupported codec id " + id.toString() );
        }

        return decoder;
    }

    public Decoder getRecoveryDecoder( CodecName id ) {
        Constructor<? extends Decoder> constructor = _recDecoderMap.get( id );

        Decoder decoder = null;
        
        if ( constructor != null ) {
            try {
                decoder = constructor.newInstance();
            } catch( Exception e ) {
                throw new SMTRuntimeException( "Error in instantiating recovery decoder id " + id.toString() + " " + e.getMessage(), e );
            }
        }
        
        if ( decoder == null ) {
            throw new SMTRuntimeException( "Unsupported codec id " + id.toString() );
        }

        return decoder;
    }
}

