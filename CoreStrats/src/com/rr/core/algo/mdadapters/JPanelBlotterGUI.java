/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.algo.mdadapters;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import junit.framework.Assert;

import com.rr.core.algo.mdadapters.BooksTableModel.BookTableColumnNames;
import com.rr.core.algo.mdadapters.OrderTableModel.MiniOrderRequest;
import com.rr.core.component.SMTControllableComponent;
import com.rr.core.component.SMTStartContext;
import com.rr.core.dummy.warmup.DummyAppProperties;
import com.rr.core.dummy.warmup.DummyMessageHandler;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.Stopable;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.book.UnsafeL2Book;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.RecoveryTradeNewImpl;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.model.internal.type.ExecType;
import com.rr.om.dummy.warmup.DummyInstrument;

/**
 * listens to changes in book and logs them
 */
@SuppressWarnings( { "synthetic-access", "serial" } )
public final class JPanelBlotterGUI extends JPanel implements Stopable, SMTControllableComponent, ActionListener {

    private final String          _id;

    private JTable                _booksTable;
    private JTable                _ordersTable;
    private JTextArea             _output;

    private final BooksTableModel _booksModel;

    @SuppressWarnings( "unused" )
    private final MessageHandler  _tradingSession;

    private final OrderTableModel _orderModel;

    private JPanelOrderTicket     _orderTicket;

    private ExecTableModel        _execModel;

    private JTable _execsTable;

    private Map<Book,JPanelBook> _bookPanels = new HashMap<Book,JPanelBook>();

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

            if ( SwingUtilities.isRightMouseButton( me ) ) {
                int row = table.rowAtPoint( p );
                
                showBook( row );
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

    private class OrdersMouseAdapter implements MouseListener {

        @Override
        public void mousePressed( MouseEvent me ) {
            JTable table = (JTable) me.getSource();
            Point p = me.getPoint();
            if ( me.getClickCount() == 2 ) {
                int row = table.rowAtPoint( p );

                amendTicket( row );
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

    public JPanelBlotterGUI( String id, MessageHandler tradingSession, BooksTableModel bookModel, OrderTableModel orderModel, ExecTableModel execModel,
                             JPanelOrderTicket ticket ) {
        super();
        _id = id;
        _booksModel = bookModel;
        _tradingSession = tradingSession;
        _orderModel = orderModel;
        _execModel = execModel;
        _orderTicket = ticket;
    }

    @Override
    public void prepare() {
        javax.swing.SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                createAndShowGUI();
            }
        } );
    }

    @Override
    public void stop() {
        // nothing
    }

    @Override
    public void init( SMTStartContext ctx ) {
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

        _booksTable = new JTable( _booksModel );
        _booksTable.setPreferredScrollableViewportSize( new Dimension( 1400, 500 ) );
        _booksTable.setFillsViewportHeight( true );
        _booksTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        add( new JScrollPane( _booksTable ) );

        _booksTable.addMouseListener( new BookMouseAdapter() );

        _ordersTable = new JTable( _orderModel );
        _ordersTable.setPreferredScrollableViewportSize( new Dimension( 1400, 150 ) );
        _ordersTable.setFillsViewportHeight( true );
        _ordersTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        _ordersTable.addMouseListener( new OrdersMouseAdapter() );

        add( new JScrollPane( _ordersTable ) );

        _execsTable = new JTable( _execModel );
        _execsTable.setPreferredScrollableViewportSize( new Dimension( 1400, 150 ) );
        _execsTable.setFillsViewportHeight( true );
        _execsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        add( new JScrollPane( _execsTable ) );

        _output = new JTextArea( 2, 100 );
        _output.setEditable( false );
        add( new JScrollPane( _output ) );
    }

    @Override
    public void startWork() {
        // nothing
    }

    @Override
    public void stopWork() {
        // nothing
    }

    public long getNotifyCount() {
        return 0;
    }

    @Override
    public String getComponentId() {
        return _id;
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be invoked from the event-dispatching thread.
     */
    private void createAndShowGUI() {
        UIManager.put( "swing.boldMetal", Boolean.FALSE );

        JFrame frame = new JFrame( "Blotter" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        setOpaque( true ); // content panes must be opaque
        frame.setContentPane( this );

        frame.pack();

        frame.setLocationRelativeTo( null );

        frame.setVisible( true );
    }

    private void newTicket( int row, int column ) {

        if ( row == -1 || column == -1 )
            return;

        BooksTableModel.BookTableColumnNames colId = _booksModel.getColumn( column );

        Book book = _booksModel.getBook( row );

        if ( !_orderTicket.isVisible() ) {
            if ( colId == BookTableColumnNames.AskPx || colId == BookTableColumnNames.AskQty ) {
                _orderTicket.openTicket( book, Side.Buy );
            } else if ( colId == BookTableColumnNames.BidPx || colId == BookTableColumnNames.BidQty ) {
                _orderTicket.openTicket( book, Side.Sell );
            }
        }
    }

    private void showBook( int row ) {

        if ( row == -1 )
            return;

        Book book = _booksModel.getBook( row );

        JPanelBook bookPanel = getPanelForBook( row, book );
        
        if ( ! bookPanel.isVisible() ) {
            bookPanel.setVisible( true );
        }
    }

    private JPanelBook getPanelForBook( int row, Book book ) {
        JPanelBook bookPanel = _bookPanels.get( book );
        
        if ( bookPanel == null ) {
            BookDepthTableModel depthModel = _booksModel.getDepthModel( row, book );
            
            bookPanel = new JPanelBook( book, depthModel, _orderTicket );
            
            bookPanel.open();
        }
        
        return bookPanel;
    }

    private void amendTicket( int row ) {

        if ( row == -1 )
            return;

        MiniOrderRequest order = _orderModel.getOrder( row );

        if ( !_orderTicket.isVisible() ) {
            _orderTicket.amendTicket( order );
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
                book.setLevel( 0, 100, 56,    false, 300, 57.5,  false );
                book.setLevel( 1, 500, 55.75, false, 500, 57.75, false );
                book.setLevel( 2, 20,  55.5,  false, 900, 58,    false );

                BooksTableModel bookModel = new BooksTableModel( "bookModel" );
                bookModel.changed( book );
                JPanelBlotterGUI blotter = new JPanelBlotterGUI( "blotterGUI", dummySession, bookModel, orderModel, execModel, ticket );

                ticket.init( null );
                blotter.init( null );

                ticket.createGUI();
                blotter.createAndShowGUI();
                
                // inject order and fill
                RecoveryNewOrderSingleImpl nos = new RecoveryNewOrderSingleImpl();

                nos.setSide( Side.Buy );
                nos.setPrice( 56.5 );
                nos.setOrderQty( 25 );
                nos.getClOrdIdForUpdate().append( "100" );
                nos.setInstrument( inst );
                nos.setOrdType( OrdType.Limit );
                nos.setTimeInForce( TimeInForce.Day );

                orderModel.handle( nos );
                
                RecoveryTradeNewImpl fill = new RecoveryTradeNewImpl();
                fill.setExecType( ExecType.Trade );
                fill.getClOrdIdForUpdate().copy( nos.getClOrdId() );
                fill.getOrderIdForUpdate().copy( "ORD100" );
                fill.setOrdStatus( OrdStatus.PartiallyFilled );
                fill.setOrderQty( 25 );
                fill.setPrice( 56.5 );
                fill.setLastQty( 5 );
                fill.setLastPx( 56.75 );
                fill.setCumQty( 10 );
                fill.setAvgPx( 56.725 );
                
                orderModel.handle( fill );
            }
        } );

    }
}
