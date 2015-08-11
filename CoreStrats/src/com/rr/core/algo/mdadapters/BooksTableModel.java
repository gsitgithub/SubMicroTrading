/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.mdadapters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.rr.core.component.SMTComponent;
import com.rr.core.lang.ReusableString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.book.DoubleSidedBookEntry;
import com.rr.core.model.book.DoubleSidedBookEntryImpl;
import com.rr.md.book.l2.L2BookListener;

/**
 * listens to changes in book and logs them
 */
@SuppressWarnings( { "serial", "boxing" } )
public final class BooksTableModel extends AbstractTableModel implements L2BookListener<Book>, SMTComponent {

    private static final class BookWrapper {
        private final Book _book;

        private BookDepthTableModel _depthModel = null;
        
        private int _bookRowIdx = -1;
        
        BookWrapper( Book book ) {
            _book = book;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_book == null) ? 0 : _book.hashCode());
            return result;
        }

        @Override
        public boolean equals( Object obj ) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            BookWrapper other = (BookWrapper) obj;
            if ( _book == null ) {
                if ( other._book != null )
                    return false;
            } else if ( !_book.equals( other._book ) )
                return false;
            return true;
        }
        
        public int getBookRowIdx() {
            return _bookRowIdx;
        }
        
        public void setBookRowIdx( int bookRowIdx ) {
            _bookRowIdx = bookRowIdx;
        }

        // lazily construct book model
        BookDepthTableModel getDepthModel() {
            return _depthModel;
        }

        // lazily construct book model
        BookDepthTableModel createDepthModel() {
            if ( _depthModel == null ) {
                _depthModel = new BookDepthTableModel( _book );
            }
            
            return _depthModel;
        }
    }
    
    private static Comparator<? super Book> _bookComparator = new Comparator<Book>() {

                                                                @Override
                                                                public int compare( Book o1, Book o2 ) {
                                                                    return o1.getInstrument().getSecurityDesc()
                                                                             .compareTo( o2.getInstrument().getSecurityDesc() );
                                                                }
                                                            };

    private static final Logger             _log            = LoggerFactory.create( BooksTableModel.class );

    public enum BookTableColumnNames {
        Symbol, SecurityId, TickId, ValidBBO, BidQty, BidPx, AskPx, AskQty
    }

    private final BookTableColumnNames[] _cols       = BookTableColumnNames.values();
    private final ReusableString         _debugMsg   = new ReusableString();
    private final int                    _numColumns = _cols.length;
    private final Map<Book,BookWrapper>  _bookMap    = new HashMap<Book,BookWrapper>();
    private final List<Book>             _bookList   = new ArrayList<Book>();
    private final String                 _id;
    private final DoubleSidedBookEntry   _curBBO     = new DoubleSidedBookEntryImpl();

    private long                         _changes;

    public BooksTableModel( String id ) {
        _id = id;
    }

    @Override
    public void clear() {
        // nothing
    }

    public long getChangeCount() {
        return _changes;
    }

    @Override
    public int getColumnCount() {
        return _numColumns;
    }

    @Override
    public int getRowCount() {
        return _bookList.size();
    }

    @Override
    public String getColumnName( int col ) {
        return _cols[col].name();
    }

    public Book getBook( int row ) {
        return _bookList.get( row );
    }

    public BookTableColumnNames getColumn( int col ) {
        return _cols[col];
    }

    @Override
    public Object getValueAt( int row, int col ) {
        synchronized( _bookList ) { // protect against corruption during table rebuild/sort
            
            final Book book = _bookList.get( row );
    
            BookTableColumnNames c = _cols[col];
    
            book.getLevel( 0, _curBBO );
    
            switch( c ) {
            case AskPx:
                return _curBBO.getAskPx();
            case AskQty:
                return _curBBO.getAskQty();
            case BidPx:
                return _curBBO.getBidPx();
            case BidQty:
                return _curBBO.getBidQty();
            case SecurityId:
                return book.getInstrument().getLongSymbol();
            case Symbol:
                return book.getInstrument().getSecurityDesc();
            case TickId:
                return book.getTickCount();
            case ValidBBO:
                return book.isValidBBO();
            default:
                break;
            }
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

    @Override
    public void changed( final Book book ) {
        ++_changes;
        _debugMsg.reset();
        book.dump( _debugMsg );
        _log.info( _debugMsg );

        final BookWrapper bw = _bookMap.get( book );
        
        if ( bw == null ) {
            if ( book.isValidBBO() ) { // only add entry when have valid BBO
                
                synchronized( _bookList ) { // only allow 1 book update thread to rebuild table at a time
                    
                    _bookMap.put( book, new BookWrapper(book) );
                    
                    _bookList.add( book );

                    _bookList.sort( _bookComparator );
                    
                    rebuild();
                }

                fireTableDataChanged();
            }
        } else {
            int row = bw.getBookRowIdx();

            fireTableRowsUpdated( row, row );
            
            BookDepthTableModel depthModel = bw.getDepthModel();
            
            if ( depthModel != null ) {
                depthModel.changed();
            }
        }
    }

    private void rebuild() {
        for( int i=0 ; i < _bookList.size() ; i++ ) {
            final Book book = _bookList.get( i );
            
            final BookWrapper bw = _bookMap.get( book );
            
            bw.setBookRowIdx( i );
        }
    }

    @Override
    public String id() {
        return _id;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    public BookDepthTableModel getDepthModel( int row, Book book ) {
        final BookWrapper bw = _bookMap.get( book );
        
        return bw.createDepthModel();
    }
}
