/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.asia.bse.reader;

import java.util.Iterator;

import com.rr.core.codec.binary.fastfix.common.ComponentFactory;
import com.rr.core.codec.binary.fastfix.common.FieldReader;
import com.rr.core.codec.binary.fastfix.msgdict.DictComponentFactory;
import com.rr.core.codec.binary.fastfix.msgdict.ReaderFieldClassLookup;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.utils.SMTRuntimeException;
import com.rr.md.fastfix.XMLFastFixTemplateLoader;
import com.rr.md.fastfix.meta.DecimalMetaFieldEntry;
import com.rr.md.fastfix.meta.MetaBaseEntry;
import com.rr.md.fastfix.meta.MetaFieldEntry;
import com.rr.md.fastfix.meta.MetaGroupEntry;
import com.rr.md.fastfix.meta.MetaSequenceEntry;
import com.rr.md.fastfix.meta.MetaTemplate;
import com.rr.md.fastfix.meta.MetaTemplates;
import com.rr.md.fastfix.template.FastFixTemplateClassRegister;
import com.rr.md.fastfix.template.TemplateClassRegister;


public class BSEFastFixToFixDump extends BaseTestCase {

    private ComponentFactory cf = new DictComponentFactory();
    
    private MetaTemplates meta = new MetaTemplates();
    
    
    public BSEFastFixToFixDump() {
        // nothing
    }

    @Override
    public void setUp() {
        loadTemplates();
    }
    
    private void loadTemplates() {
        TemplateClassRegister reg = new FastFixTemplateClassRegister();
        
        XMLFastFixTemplateLoader l = new XMLFastFixTemplateLoader( "../data/bse/common/EMDIFastTemplates-1.1.xml" );
        
        l.load( reg, meta );
    }

    public void testCodec3() {
        MetaTemplate mt = meta.getTemplate( 94 );
        Iterator<MetaBaseEntry> it = mt.getEntryIterator();
        
        int idx = 0;
        
        while( it.hasNext() ) {
            MetaBaseEntry e = it.next();
            
            addField( e, ++idx );
        }
    }

    private void addField( MetaBaseEntry e, int idx ) {
        
        if ( e.getId() == 52 ) {
            logField( (MetaFieldEntry) e, idx );
        } else if ( e.getClass() == MetaGroupEntry.class ) {
            System.out.println( "\n" );

            MetaGroupEntry metaEntry = (MetaGroupEntry)e;
            
            Iterator<MetaBaseEntry> it = metaEntry.getEntryIterator();
            
            int cnt=0;
            
            while( it.hasNext() ) {
                if ( cnt > 0 ) ++idx;
                
                MetaBaseEntry subE = it.next();
                
                addField( subE, idx );
            }
            
            System.out.println( "\n" );
            
        } else if ( e.getClass() == MetaSequenceEntry.class ) {
            System.out.println( "\n" );

            MetaSequenceEntry metaEntry = (MetaSequenceEntry)e;
            
            addField( metaEntry.getLengthField(), idx );
            
            Iterator<MetaBaseEntry> it = metaEntry.getEntryIterator();
            
            while( it.hasNext() ) {
                MetaBaseEntry subE = it.next();
                
                addField( subE, ++idx );
            }
            
            System.out.println( "\n" );
            
        } else if ( e.getClass() == MetaFieldEntry.class ) {
            logField( (MetaFieldEntry) e, idx );
        } else if ( e.getClass() == DecimalMetaFieldEntry.class ) {
            logField( (DecimalMetaFieldEntry) e, idx );
        } else {
            throw new SMTRuntimeException( "Unsupported fast fix meta class " + e );
        }
    }

    @SuppressWarnings( "boxing" )
    private void logField( DecimalMetaFieldEntry e, int idx ) {
        Class<? extends FieldReader> rdrClass = ReaderFieldClassLookup.getCustomReaderClass( e.getExp().getOperator(), e.getMant().getOperator(), e.isOptional() );
        
        FieldReader r = cf.getReader( rdrClass, cf, e.getName(), e.getId(), e.getExp().getInitValue(), e.getMant().getInitValue() );
        
        logEntry( e, idx, r );
    }

    private void logEntry( MetaBaseEntry e, int idx, FieldReader r ) {
        System.out.println( "    private final " + r.getClass().getSimpleName() + " _f" + idx + "_" + e.getName() + ";" );
    }

    private void logField( MetaFieldEntry e, int idx ) {
        Class<? extends FieldReader> rdrClass = ReaderFieldClassLookup.getReaderClass( e.getOperator(), e.getType(), e.isOptional() );
        
        @SuppressWarnings( "boxing" )
        FieldReader r = cf.getReader( rdrClass, e.getName(), e.getId(), e.getInitValue() );
        
        logEntry( e, idx, r );
    }
}
