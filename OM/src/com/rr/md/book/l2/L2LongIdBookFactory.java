/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.book.l2;

import com.rr.core.lang.Constants;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Instrument;
import com.rr.core.model.InstrumentLocator;
import com.rr.core.model.book.ApiMutatableBook;
import com.rr.core.model.book.LockableL1Book;
import com.rr.core.model.book.LockableL2Book;
import com.rr.core.model.book.UnsafeL1Book;
import com.rr.core.model.book.UnsafeL2Book;
import com.rr.core.utils.ReflectUtils;
import com.rr.md.book.FixBookLongKeyFactory;
import com.rr.md.book.MutableFixBook;

/**
 * @WARNING the book can only be updated by one thread and it MUST be created on the thread that will apply the updates
 *          otherwise the recyler will be corrupted ! 
 *
 */
public class L2LongIdBookFactory<T extends MutableFixBook> implements FixBookLongKeyFactory<T> {

    private static final Logger         _log = LoggerFactory.create( L2LongIdBookFactory.class );

    public  static final int            DEFAULT_MAX_LEVELS = 10;

    private final boolean               _useThreadSafeBook;
    private final InstrumentLocator     _instrumentLocator;

    private final int                   _bookLevels;

    private final Class<T>              _adapterClass;
    private final T                     _dummyBookInstance;

    public L2LongIdBookFactory( Class<T> adapterClass, boolean useThreadSafeBook, InstrumentLocator locator, int bookLevels ) {
        _useThreadSafeBook = useThreadSafeBook;
        _instrumentLocator = locator;
        _bookLevels        = bookLevels;
        _adapterClass      = adapterClass;
        
        _dummyBookInstance = ReflectUtils.getStaticMember( adapterClass, "DUMMY" );
    }

    /**
     * create the required book, 
     * 
     * @param key
     * @param recyler
     * @return new L2FixBook or DUMMY instance if inst not located
     */
    @Override
    public T create( final long key, final ZString rec ) {
        return create( key, _bookLevels, rec );
    }
    
    @Override
    public T create( final long key, int levels, final ZString rec ) {
        Instrument inst = _instrumentLocator.getInstrumentByID( rec, key );

        if ( inst == null ) {
            _log.warn( "Unable to find instrument " + key + ", rec=" + rec + ", using DUMMY book" );
            
            return _dummyBookInstance;
        }
        
        int instLevels = inst.getBookLevels();

        // check instrument supports the requested number of levels
        if ( instLevels > 0 && instLevels != Constants.UNSET_INT && instLevels < levels ) {
            levels = instLevels;
        }
        
        ApiMutatableBook book = null;
        
        if ( _useThreadSafeBook ) {
            book = (levels == 1) ? new LockableL1Book( inst ) :  new LockableL2Book( inst, levels );            
        } else {
            book = (levels == 1) ? new UnsafeL1Book( inst ) :  new UnsafeL2Book( inst, levels );            
        }
        
        Class<?>[] cargs = { ApiMutatableBook.class };
        Object[]   cvals = { book };
        
        return ReflectUtils.create( _adapterClass, cargs, cvals );
    }
}
