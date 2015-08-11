/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.mdadapters;

import javax.swing.table.AbstractTableModel;

import com.rr.core.model.Book;
import com.rr.core.model.book.ApiMutatableBook;
import com.rr.core.model.book.DoubleSidedBookEntry;
import com.rr.core.model.book.DoubleSidedBookEntryImpl;
import com.rr.core.model.book.UnsafeL2Book;

/**
 * listens to changes in book and logs them
 */
@SuppressWarnings( { "serial", "boxing" } )
public final class BookDepthTableModel extends AbstractTableModel {

    public enum BookTableColumnNames {
        Level, Valid, BidDirty, BidQty, BidPx, AskPx, AskQty, AskDirty
    }

    private final BookTableColumnNames[] _cols       = BookTableColumnNames.values();
    private final int                    _numColumns = _cols.length;
    private final Book                   _book;

    private DoubleSidedBookEntry         _curEntry   = new DoubleSidedBookEntryImpl();
    private DoubleSidedBookEntry         _lastEntry  = new DoubleSidedBookEntryImpl();

    private ApiMutatableBook             _curSnap;
    private ApiMutatableBook             _lastSnap;

    public BookDepthTableModel( Book book ) {
        _book = book;
        _curSnap = new UnsafeL2Book( book.getInstrument(), book.getMaxLevels() );
        _lastSnap = new UnsafeL2Book( book.getInstrument(), book.getMaxLevels() );
    }

    @Override
    public int getColumnCount() {
        return _numColumns;
    }

    @Override
    public int getRowCount() {
        return _book.getMaxLevels();
    }

    @Override
    public String getColumnName( int col ) {
        return _cols[col].name();
    }

    public BookTableColumnNames getColumn( int col ) {
        return _cols[col];
    }

    @Override
    public Object getValueAt( int row, int col ) {
        _book.getLevel( row, _curEntry );

        BookTableColumnNames c = _cols[col];

        switch( c ) {
        case Level:
            return row;
        case AskPx:
            return _curEntry.getAskPx();
        case AskQty:
            return _curEntry.getAskQty();
        case BidPx:
            return _curEntry.getBidPx();
        case BidQty:
            return _curEntry.getBidQty();
        case Valid:
            return _curEntry.isValid();
        case BidDirty:
            return _curEntry.isBuyIsDirty();
        case AskDirty:
            return _curEntry.isSellIsDirty();
        default:
            break;
        }

        return null;
    }

    @Override
    public Class<?> getColumnClass( int c ) {
        return getValueAt( 0, c ).getClass();
    }

    @Override
    public boolean isCellEditable( int row, int col ) {
        return false;
    }

    public void changed() {

        if ( _book != null ) {
            _book.snap( _curSnap );

            for ( int row = 0 ; row < _book.getMaxLevels() ; ++row ) {
                _curSnap.getLevel( row, _curEntry );
                _lastSnap.getLevel( row, _lastEntry );

                for ( int col = 0 ; col < _cols.length ; ++col ) {
                    if ( ! _curEntry.equals( _lastEntry ) ) {
                        fireTableRowsUpdated( row, row );
                    }
                }
            }

            ApiMutatableBook tmp = _lastSnap;
            _lastSnap = _curSnap;
            _curSnap = tmp;
        }
    }
}
