/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.fastfix.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.common.DoubleFieldValWrapper;
import com.rr.core.codec.binary.fastfix.common.FieldReader;
import com.rr.core.codec.binary.fastfix.common.FieldValWrapper;
import com.rr.core.codec.binary.fastfix.common.FixFieldReader;
import com.rr.core.codec.binary.fastfix.common.IntFieldValWrapper;
import com.rr.core.codec.binary.fastfix.common.LongFieldValWrapper;
import com.rr.core.codec.binary.fastfix.common.StringFieldValWrapper;
import com.rr.core.codec.binary.fastfix.msgdict.ReaderFieldClassLookup;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.md.fastfix.meta.DecimalMetaFieldEntry;
import com.rr.md.fastfix.meta.MetaBaseEntry;
import com.rr.md.fastfix.meta.MetaFieldEntry;
import com.rr.md.fastfix.meta.MetaGroupEntry;
import com.rr.md.fastfix.meta.MetaSequenceEntry;
import com.rr.md.fastfix.meta.MetaTemplate;
import com.rr.md.fastfix.template.FastFixTemplateReader;

/*
 * Fast Fix to Fix Reader
 * 
 
    <template name="MDIncRefresh_81" id="81" dictionary="81"
        <string name="ApplVerID" id="1128"> <constant value="9" /> </string>
        <string name="MessageType" id="35"> <constant value="X" /> </string>
        <string name="SenderCompID" id="49"> <constant value="CME" /> </string>
        <uInt32 name="MsgSeqNum" id="34"></uInt32>
        <uInt64 name="SendingTime" id="52"></uInt64>
        <string name="PosDupFlag" id="43" presence="optional"> <default /> </string>
        <sequence name="MDEntries">
            <length name="NoMDEntries" id="268"></length>
            <uInt32 name="MDUpdateAction" id="279" presence="optional"> <copy value="0" /> </uInt32>
            <uInt32 name="MDPriceLevel" id="1023" presence="optional"> <default value="1" /> </uInt32>
            <string name="MDEntryType" id="269"> <copy value="J" /> </string>
            <uInt32 name="SecurityIDSource" id="22" presence="optional"> <constant value="8" /> </uInt32>
            <uInt32 name="SecurityID" id="48" presence="optional"> <copy /> </uInt32>
            <uInt32 name="RptSeq" id="83" presence="optional"> <increment /> </uInt32>
            <decimal name="MDEntryPx" id="270" presence="optional"> <exponent> <default value="0" /> <mantissa> <delta /> 
            <uInt32 name="MDEntryTime" id="273"> <copy />
            <int32 name="MDEntrySize" id="271" presence="optional"> <delta />
            <string name="QuoteCondition" id="276" presence="optional"> <default />
            <uInt32 name="NumberOfOrders" id="346" presence="optional"> <delta />
            <string name="TradingSessionID" id="336" presence="optional"> <default value="2" />
        </sequence>
 */

public final class FastFixToFixReader implements FastFixTemplateReader {

    private static final int  MAX_SEQUENCE_OCC = 1024;

    static final boolean _debug = false;

    private final String _name;
    private final int _id;
    
    private interface ReadWrapper {
        public void read( ReusableString log, FastFixDecodeBuilder decoder, PresenceMapReader pMap );

        public void reset();
    }
    
    private static class DateTimeFieldReadWrapper<T extends FixFieldReader<LongFieldValWrapper>> extends StandardFieldReadWrapper<T,LongFieldValWrapper> {

        private ReusableString _tmpTime = new ReusableString();
        
        public DateTimeFieldReadWrapper( MetaBaseEntry metaEntry, T rdr, LongFieldValWrapper valWrapper, byte delim ) {
            super( metaEntry, rdr, valWrapper, delim );
        }
        
        @Override
        protected void logValue( ReusableString log ) {
            // 20120403 19 43 42 222
            // 20090107-18:15:16
            _tmpTime.reset();
            _tmpTime.append( getValWrapper().getVal() );
            log.append( _tmpTime, 0, 8 );
            log.append( '-' );
            log.append( _tmpTime, 8, 2 );
            log.append( ':' );
            log.append( _tmpTime, 10, 2 );
            log.append( ':' );
            log.append( _tmpTime, 12, 2 );
            if ( _tmpTime.length() == 17 ) {
                log.append( '.' );
                log.append( _tmpTime, 14, 3 );
            }
        }
    }
    
    private static class StandardFieldReadWrapper<T extends FixFieldReader<V>, V extends FieldValWrapper> implements ReadWrapper {
        private final MetaBaseEntry _metaEntry;
        private final T             _rdr;
        private final V             _valWrapper;
        private final byte          _delim;
        
        public StandardFieldReadWrapper( MetaBaseEntry metaEntry, T rdr, V valWrapper, byte delim ) {
            super();
            _metaEntry = metaEntry;
            _rdr = rdr;
            _valWrapper = valWrapper;
            _delim = delim;
        }

        @Override
        public void reset() {
            _rdr.reset();
        }
        
        @Override
        public String toString() {
            ReusableString buf = new ReusableString();

            _valWrapper.log( buf );

            String s = _metaEntry.toString() + ", hasValue=" + _valWrapper.hasValue() + " : " + buf.toString();
            
            return s;
        }
        
        @Override
        public void read( ReusableString log, FastFixDecodeBuilder decoder, PresenceMapReader pMap ) {
            
            _valWrapper.reset();
            
            _rdr.read( decoder, pMap, _valWrapper );
            
            if ( getValWrapper().hasValue() ) {
                log.append( _metaEntry.getId() ).append('=');
                
                logValue( log );
                
                log.append( _delim );
                
                if ( _debug ) {
                    log.append( "  idx=" ).append( decoder.getCurrentIndex() ).append( "  : " );
                    pMap.trace( log );
                    log.append( "\n" );
                }
            }
        }

        protected void logValue( ReusableString log ) {
            getValWrapper().log( log );
        }

        public V getValWrapper() {
            return _valWrapper;
        }
    }
    
    private static class SequenceFieldReadWrapper implements ReadWrapper {
        private final MetaSequenceEntry _metaEntry;
        private final String _name;
        private final PresenceMapReader seqPMap = new PresenceMapReader();
        private final boolean _requiresPMap; 
        
        private StandardFieldReadWrapper<FixFieldReader<IntFieldValWrapper>, IntFieldValWrapper> _lengthWrapper;
        
        private final List<ReadWrapper> _fieldReaders = new ArrayList<ReadWrapper>();
        
        @SuppressWarnings( "unchecked" )
        public SequenceFieldReadWrapper( String name, MetaSequenceEntry metaEntry, ComponentFactory cf, byte delim ) {
            super();
            _name = name;
            _metaEntry = metaEntry;
            
            ReadWrapper addField = addField( metaEntry.getLengthField(), cf, delim );
            
            _lengthWrapper = (StandardFieldReadWrapper<FixFieldReader<IntFieldValWrapper>, IntFieldValWrapper>) addField;
            
            Iterator<MetaBaseEntry> it = _metaEntry.getEntryIterator();
            
            boolean requiresPMap = false;
            
            while( it.hasNext() ) {
                MetaBaseEntry e = it.next();
                
                ReadWrapper w = addField( e, cf, delim );
                
                _fieldReaders.add( w );
                
                requiresPMap |= e.requiresPresenceBit();
            }
            
            _requiresPMap = requiresPMap;
        }

        @Override
        public void reset() {
            resetFields( _fieldReaders );
        }

        @Override
        public String toString() {
            return "Sequence " + _name + " " + _metaEntry.toString() + ", lenWrap=" + _lengthWrapper.toString();
        }

        @Override
        public void read( ReusableString log, FastFixDecodeBuilder decoder, PresenceMapReader pMap ) {
            if ( _debug ) {
                log.append( "\nSEQUENCE {\n" );
            }
            
            _lengthWrapper.read( log, decoder, pMap );

            int seqLen = _lengthWrapper.getValWrapper().getVal();
            
            if ( seqLen == Constants.UNSET_INT ) return;
            
            if ( seqLen < 0 || seqLen > MAX_SEQUENCE_OCC ) {
                decoder.throwDecodeException( "Bad sequence length " + seqLen + ", in template " + _name );
            }
            
            for ( int i=0 ; i < seqLen ; i++ ) {
                if ( _debug ) {
                    log.append( "sequence iter=" ).append( i ).append( "\n" );
                }

                if ( _requiresPMap ) {
                    seqPMap.readMap( decoder );
                    readFields( decoder, seqPMap, log, _fieldReaders );
                } else { 
                    readFields( decoder, pMap, log, _fieldReaders );
                }

                if ( _debug ) {
                    log.append( "\n" );
                }
            }
        }
    }
    
    private static class GroupFieldReadWrapper implements ReadWrapper {
        private final MetaGroupEntry        _metaEntry;
        private final String                _name;
        private final PresenceMapReader     _seqPMap = new PresenceMapReader();
        private final boolean               _requiresPMap; 
        
        private final List<ReadWrapper> _fieldReaders = new ArrayList<ReadWrapper>();
        
        public GroupFieldReadWrapper( String name, MetaGroupEntry metaEntry, ComponentFactory cf, byte delim ) {
            super();
            _name = name;
            _metaEntry = metaEntry;
            
            Iterator<MetaBaseEntry> it = _metaEntry.getEntryIterator();
            
            boolean requiresPMap = false;
            
            while( it.hasNext() ) {
                MetaBaseEntry e = it.next();
                
                ReadWrapper w = addField( e, cf, delim );
                
                _fieldReaders.add( w );
                
                requiresPMap |= e.requiresPresenceBit();
            }
            
            _requiresPMap = requiresPMap;
        }
        
        @Override
        public String toString() {
            return "Group " + _name + " : " + _metaEntry.toString();
        }
        
        @Override
        public void reset() {
            resetFields( _fieldReaders );
        }

        @Override
        public void read( ReusableString log, FastFixDecodeBuilder decoder, PresenceMapReader pMap ) {
            if ( _debug ) {
                log.append( "\nGROUP {\n" );
            }
            
            if ( _debug ) {
                log.append( _name ).append( "\n" );
            }

            if ( !_metaEntry.isOptional() || pMap.isNextFieldPresent() ) {
                if ( _requiresPMap ) {
                    _seqPMap.readMap( decoder );
                    readFields( decoder, _seqPMap, log, _fieldReaders );
                } else { 
                    readFields( decoder, pMap, log, _fieldReaders );
                }
    
                if ( _debug ) {
                    log.append( "\n" );
                }
            }
        }
    }
    
    private final MetaTemplate _mt;
    
    private final List<ReadWrapper> _fieldReaders = new ArrayList<ReadWrapper>();

    private final byte _delim; 
    
    
    public FastFixToFixReader( MetaTemplate mt, String name, int id, byte delim ) {
        _name = name;
        _id = id;
        _mt = mt;
        _delim = delim;
    }
    
    public MetaTemplate getMetaTemplate() {
        return _mt;
    }
    
    public void init( ComponentFactory cf ) {
        
        try {
            Iterator<MetaBaseEntry> it = _mt.getEntryIterator();
            
            while( it.hasNext() ) {
                MetaBaseEntry e = it.next();
                
                ReadWrapper w = addField( e, cf, _delim );
                
                _fieldReaders.add( w );
            }
        } catch( Exception e ) {
            throw new SMTRuntimeException( "Exception initialising template name=" + getName() + ", id=" + getId() + " : " + e.getMessage(), e );
        }
    }

    static ReadWrapper addField( MetaBaseEntry e, ComponentFactory cf, byte delim ) {
        ReadWrapper w = null;
        
        if ( e.getId() == 52 ) {
            w = addDateTimeField( (MetaFieldEntry)e, cf, delim );
        } else if ( e.getClass() == MetaGroupEntry.class ) {
            w = addGroup( (MetaGroupEntry)e, cf, delim );
        } else if ( e.getClass() == MetaSequenceEntry.class ) {
            w = addSequence( (MetaSequenceEntry)e, cf, delim );
        } else if ( e.getClass() == MetaFieldEntry.class ) {
            w = addNormalField( (MetaFieldEntry)e, cf, delim );
        } else if ( e.getClass() == DecimalMetaFieldEntry.class ) {
            w = addDecimalField( (DecimalMetaFieldEntry)e, cf, delim );
        } else {
            throw new SMTRuntimeException( "Unsupported fast fix meta class " + e );
        }
        
        return w;
    }

    private static ReadWrapper addGroup( MetaGroupEntry e, ComponentFactory cf, byte delim ) {
        GroupFieldReadWrapper w = new GroupFieldReadWrapper( e.getName(),  e, cf, delim );
        return w;
    }

    private static ReadWrapper addSequence( MetaSequenceEntry e, ComponentFactory cf, byte delim ) {
        SequenceFieldReadWrapper w = new SequenceFieldReadWrapper( e.getName(),  e, cf, delim );
        return w;
    }

    @SuppressWarnings( { "boxing", "rawtypes", "unchecked" } )
    private static ReadWrapper addDecimalField( DecimalMetaFieldEntry e, ComponentFactory cf, byte delim ) {
        Class<? extends FieldReader> rdrClass = ReaderFieldClassLookup.getCustomReaderClass( e.getExp().getOperator(), e.getMant().getOperator(), e.isOptional() );
        
        FieldReader r = cf.getReader( rdrClass, cf, e.getName(), e.getId(), e.getExp().getInitValue(), e.getMant().getInitValue() );

        DoubleFieldValWrapper v = new DoubleFieldValWrapper(); 
        StandardFieldReadWrapper w = new StandardFieldReadWrapper( e, (FixFieldReader<DoubleFieldValWrapper>)r, v, delim );
        
        return w;
    }

    @SuppressWarnings( { "boxing", "rawtypes", "unchecked" } )
    private static ReadWrapper addNormalField( MetaFieldEntry e, ComponentFactory cf, byte delim ) {
        Class<? extends FieldReader> rdrClass = ReaderFieldClassLookup.getReaderClass( e.getOperator(), e.getType(), e.isOptional() );
        
        FieldReader r = cf.getReader( rdrClass, e.getName(), e.getId(), e.getInitValue() );

        FieldValWrapper v = null;
        StandardFieldReadWrapper w = null;    
        
        switch ( e.getType() ) {
        case length:
        case uInt32:
        case int32:
            v = new IntFieldValWrapper(); 
            w = new StandardFieldReadWrapper( e, (FixFieldReader<IntFieldValWrapper>)r, v, delim );    
            break;
        case uInt64:
        case int64:
            v = new LongFieldValWrapper(); 
            w = new StandardFieldReadWrapper( e, (FixFieldReader<LongFieldValWrapper>)r, v, delim );    
            break;
        case string:
            v = new StringFieldValWrapper(); 
            w = new StandardFieldReadWrapper( e, (FixFieldReader<StringFieldValWrapper>)r, v, delim );    
            break;
        case sequence:
            break;
        case decimal: // decimal fall thru to exception as should be catered for in add decimal
        default:
            throw new SMTRuntimeException( "Unsupported type .. must change code for support of new type" );
        }

        return w;
    }

    @SuppressWarnings( { "unchecked", "rawtypes", "boxing" } )
    private static ReadWrapper addDateTimeField( MetaFieldEntry e, ComponentFactory cf, byte delim ) {
        Class<? extends FieldReader> rdrClass = ReaderFieldClassLookup.getReaderClass( e.getOperator(), e.getType(), e.isOptional() );
        
        FieldReader r = cf.getReader( rdrClass, e.getName(), e.getId(), e.getInitValue() );

        LongFieldValWrapper v = new LongFieldValWrapper();
        DateTimeFieldReadWrapper w = new DateTimeFieldReadWrapper( e, (FixFieldReader<LongFieldValWrapper>)r, v, delim );        

        return w;
    }

    public void read( final FastFixDecodeBuilder decoder, final PresenceMapReader pMap, final ReusableString destFixMsg ) {
        readFields( decoder, pMap, destFixMsg, _fieldReaders );
    }

    static void readFields( FastFixDecodeBuilder decoder, PresenceMapReader pMap, ReusableString destFixMsg, List<ReadWrapper> fieldReaders ) {
        final int numFields = fieldReaders.size();
        
        for ( int i=0 ; i < numFields ; i++ ) {
            final ReadWrapper rdWrap = fieldReaders.get( i );

            rdWrap.read( destFixMsg, decoder, pMap );
        }
    }

    public String getName() {
        return _name;
    }

    public int getId() {
        return _id;
    }

    @Override
    public void reset() {
        resetFields( _fieldReaders );
    }

    static final void resetFields( List<ReadWrapper> fieldReaders ) {
        final int numFields = fieldReaders.size();
        
        for ( int i=0 ; i < numFields ; i++ ) {
            final ReadWrapper rdWrap = fieldReaders.get( i );

            rdWrap.reset();
        }
    }
}
