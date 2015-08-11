/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.mdadapters;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import junit.framework.Assert;

import com.rr.core.algo.mdadapters.BookDepthTableModel.BookTableColumnNames;
import com.rr.core.dummy.warmup.DummyAppProperties;
import com.rr.core.dummy.warmup.DummyMessageHandler;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.book.UnsafeL2Book;
import com.rr.model.generated.internal.type.Side;
import com.rr.om.dummy.warmup.DummyInstrument;

/**
 * listens to changes in book and logs them
 */
@SuppressWarnings( { "synthetic-access", "serial" } )
public final class JPanelBook extends JPanel implements ActionListener {

    private final JTable              _booksTable;
    private final BookDepthTableModel _booksModel;
    private final JPanelOrderTicket   _orderTicket;
    private final Book                _book;

    private static final List<JPanelBook> _panels = new ArrayList<JPanelBook>();


    private JButton                   _closeBtn;
    private JFrame                    _frame;
    private JPanel                    _buttonPanel;

    private class CloseButtonListener implements ActionListener {

        @Override
        public void actionPerformed( ActionEvent event ) {
            setVisible( false );
        }
    }

    private class BookMouseAdapter implements MouseListener {

        @Override
        public void mousePressed( MouseEvent me ) {
            JTable table = (JTable) me.getSource();
            Point p = me.getPoint();
            if ( me.getClickCount() == 2 ) {
                int row = table.rowAtPoint( p );
                int column = table.columnAtPoint( p );

                newTicket( row, column );
            }
        }

        @Override
        public void mouseClicked( MouseEvent e ) { /* nothing */}

        @Override
        public void mouseReleased( MouseEvent e ) { /* nothing */}

        @Override
        public void mouseEntered( MouseEvent e ) { /* nothing */}

        @Override
        public void mouseExited( MouseEvent e ) { /* nothing */}
    }

    public JPanelBook( Book book, BookDepthTableModel bookModel, JPanelOrderTicket ticket ) {
        super();
        _book = book;
        _booksModel = bookModel;
        _orderTicket = ticket;
        _booksTable = new JTable( _booksModel );
    }

    public void open() {
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

        _booksTable.setPreferredScrollableViewportSize( new Dimension( 500, 200 ) );
        _booksTable.setFillsViewportHeight( true );
        _booksTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        add( new JScrollPane( _booksTable ) );

        _booksTable.addMouseListener( new BookMouseAdapter() );

        addButtons();

        javax.swing.SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                createAndShowGUI();
            }
        } );
    }

    @Override
    public void setVisible( boolean visible ) {
        _frame.setVisible( visible );
        super.setVisible( visible );
        
        if ( visible ) {
            _panels.add( this );
        } else {
            _panels.remove( this );
        }
    }

    private void addButtons() {
        // close and submit buttons
        _buttonPanel = new JPanel();
        _closeBtn = new JButton( "Close" );
        _buttonPanel.add( _closeBtn );

        add( _buttonPanel, BorderLayout.SOUTH );

        ActionListener closeListener = new CloseButtonListener();
        _closeBtn.addActionListener( closeListener );
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be invoked from the event-dispatching thread.
     */
    private void createAndShowGUI() {
        UIManager.put( "swing.boldMetal", Boolean.FALSE );

        _frame = new JFrame( "Book " + _book.getInstrument().getSecurityDesc() );
        _frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        setOpaque( true ); // content panes must be opaque
        _frame.setContentPane( this );

        _frame.pack();

        JPanelBook lastPanel = null;

        int i = _panels.size();
        while( i > 0 ) {
            lastPanel = _panels.get( --i );
            
            if ( lastPanel != this ) break;
        }
        
        _frame.setLocationRelativeTo( lastPanel );

        _frame.setVisible( true );

        _frame.setAlwaysOnTop( true );
        
        _panels.add( this );
    }

    private void newTicket( int row, int column ) {

        if ( row == -1 || column == -1 )
            return;

        BookDepthTableModel.BookTableColumnNames colId = _booksModel.getColumn( column );

        if ( !_orderTicket.isVisible() ) {
            if ( colId == BookTableColumnNames.AskPx || colId == BookTableColumnNames.AskQty ) {
                _orderTicket.openTicket( _book, Side.Buy );
            } else if ( colId == BookTableColumnNames.BidPx || colId == BookTableColumnNames.BidQty ) {
                _orderTicket.openTicket( _book, Side.Sell );
            }
        }
    }

    @Override
    public void actionPerformed( ActionEvent event ) {
        // nothing
    }

    /**
     * main program only for use in TESTING the app without market data
     */
    public static void main( String[] args ) {
        LoggerFactory.setDebug( true );

        StatsMgr.setStats( new TestStats() );

        Map<String, String> p = new HashMap<String, String>();

        try {
            DummyAppProperties.testInit( p );
        } catch( Exception e ) {
            Assert.fail( e.getMessage() );
        }

        javax.swing.SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                MessageHandler dummySession = new DummyMessageHandler();
                ExecTableModel execModel = new ExecTableModel( "execModel" );
                OrderTableModel orderModel = new OrderTableModel( "orderModel", execModel );
                JPanelOrderTicket ticket = new JPanelOrderTicket( "orderTicketPanel", dummySession, orderModel );

                Instrument inst = DummyInstrument.DUMMY;
                UnsafeL2Book book = new UnsafeL2Book( inst, 10 );
                book.setLevel( 0, 100, 56, false, 300, 57.5, false );

                BookDepthTableModel bookModel = new BookDepthTableModel( book );
                JPanelBook blotter = new JPanelBook( book, bookModel, ticket );
                bookModel.changed();

                ticket.init( null );
                blotter.open();

                ticket.createGUI();
            }
        } );

    }

}
