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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import junit.framework.Assert;

import com.rr.core.algo.mdadapters.OrderTableModel.MiniOrderRequest;
import com.rr.core.component.SMTControllableComponent;
import com.rr.core.component.SMTStartContext;
import com.rr.core.dummy.warmup.DummyAppProperties;
import com.rr.core.dummy.warmup.DummyMessageHandler;
import com.rr.core.dummy.warmup.TestStats;
import com.rr.core.lang.Constants;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.stats.StatsMgr;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Book;
import com.rr.core.model.Instrument;
import com.rr.core.model.MessageHandler;
import com.rr.core.model.book.DoubleSidedBookEntry;
import com.rr.core.model.book.DoubleSidedBookEntryImpl;
import com.rr.core.model.book.UnsafeL2Book;
import com.rr.core.swing.SpringLayoutUtilities;
import com.rr.model.generated.internal.events.impl.RecoveryCancelReplaceRequestImpl;
import com.rr.model.generated.internal.events.impl.RecoveryCancelRequestImpl;
import com.rr.model.generated.internal.events.impl.RecoveryNewOrderSingleImpl;
import com.rr.model.generated.internal.type.OrdType;
import com.rr.model.generated.internal.type.Side;
import com.rr.model.generated.internal.type.TimeInForce;
import com.rr.om.dummy.warmup.DummyInstrument;

/**
 * listens to changes in book and logs them
 */
@SuppressWarnings( { "synthetic-access", "serial" } )
public final class JPanelOrderTicket extends JPanel implements SMTControllableComponent, ActionListener {

    private final MessageHandler       _tradingSession;

    private final OrderTableModel      _orderModel;

    private Instrument                 _instrument;
    private JTextField                 _secDesc;
    private JTextField                 _secId;
    private JTextField                 _price;
    private JTextField                 _qty;
    private JTextField                 _clOrdId;

    private JComboBox<OrdType>         _ordType;
    private JComboBox<TimeInForce>     _tif;

    private JButton                    _submitBtn;
    private JButton                    _closeNosBtn;

    private ButtonGroup                _sideGroup;

    private String                     _id;
    private JFrame                     _frame;

    private JRadioButton               _buyButton;
    private JRadioButton               _sellButton;

    private JPanel                     _nosButtonPanel;
    private JPanel                     _amendButtonPanel;
    private JButton                    _amendBtn;
    private JButton                    _cancelBtn;
    private JButton                    _closeAmendBtn;

    private ReusableString             _orderId     = new ReusableString();
    private int                        _nextClOrdId = seedId();

    private final DoubleSidedBookEntry _bbo         = new DoubleSidedBookEntryImpl();

    class SubmitButtonListener implements ActionListener {

        @Override
        public void actionPerformed( ActionEvent event ) {
            RecoveryNewOrderSingleImpl nos = new RecoveryNewOrderSingleImpl();

            nos.setSide( (_buyButton.isSelected()) ? Side.Buy : Side.Sell );
            nos.setOrderQty( Integer.parseInt( _qty.getText() ) );
            nos.getClOrdIdForUpdate().append( _clOrdId.getText() );
            nos.setInstrument( _instrument );
            nos.setOrdType( (OrdType) _ordType.getSelectedItem() );
            nos.setTimeInForce( (TimeInForce) _tif.getSelectedItem() );

            if ( nos.getOrdType() != OrdType.Market ) {
                nos.setPrice( Double.parseDouble( _price.getText() ) );
            }

            _orderModel.handle( nos );

            _tradingSession.handle( nos );

            setVisible( false );
        }
    }

    class AmendButtonListener implements ActionListener {

        @Override
        public void actionPerformed( ActionEvent event ) {
            RecoveryCancelReplaceRequestImpl amend = new RecoveryCancelReplaceRequestImpl();

            amend.setSide( (_buyButton.isSelected()) ? Side.Buy : Side.Sell );
            amend.setPrice( Double.parseDouble( _price.getText() ) );
            amend.setOrderQty( Integer.parseInt( _qty.getText() ) );
            amend.getOrigClOrdIdForUpdate().append( _clOrdId.getText() );
            amend.getClOrdIdForUpdate().append( "" + nextClOrdId() );
            amend.getOrderIdForUpdate().copy( _orderId );
            amend.setInstrument( _instrument );
            amend.setOrdType( (OrdType) _ordType.getSelectedItem() );
            amend.setTimeInForce( (TimeInForce) _tif.getSelectedItem() );

            _orderModel.handle( amend );

            _tradingSession.handle( amend );

            setVisible( false );
        }
    }

    class CancelButtonListener implements ActionListener {

        @Override
        public void actionPerformed( ActionEvent event ) {
            RecoveryCancelRequestImpl cxl = new RecoveryCancelRequestImpl();

            cxl.setSide( (_buyButton.isSelected()) ? Side.Buy : Side.Sell );
            cxl.getOrigClOrdIdForUpdate().append( _clOrdId.getText() );
            cxl.getClOrdIdForUpdate().append( "" + nextClOrdId() );
            cxl.getOrderIdForUpdate().copy( _orderId );
            cxl.setInstrument( _instrument );

            _orderModel.handle( cxl );

            _tradingSession.handle( cxl );

            setVisible( false );
        }
    }

    class CloseButtonListener implements ActionListener {

        @Override
        public void actionPerformed( ActionEvent event ) {
            setVisible( false );
        }
    }

    public JPanelOrderTicket( String id, MessageHandler tradingSession, OrderTableModel orderModel ) {
        super();
        _id = id;
        _tradingSession = tradingSession;
        _orderModel = orderModel;
    }

    private static int seedId() {
        int ms = (int) (System.currentTimeMillis() % Constants.MS_IN_DAY);
        return (ms / 10000) * 10000;
    }

    @Override
    public void prepare() {
        javax.swing.SwingUtilities.invokeLater( new Runnable() {

            @Override
            public void run() {
                createGUI();
            }
        } );
    }

    @Override
    public void init( SMTStartContext ctx ) {
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

        JPanel northPanel = new JPanel();

        northPanel.add( new JLabel( "SIDE" ) );
        _sideGroup = new ButtonGroup();
        _buyButton = addRadio( northPanel, _sideGroup, "Buy" );
        _sellButton = addRadio( northPanel, _sideGroup, "Sell" );
        _buyButton.setSelected( true );

        add( northPanel );

        addOrderFields();

        addNewOrderButtons();

        addAmendOrderButtons();
    }

    private void addNewOrderButtons() {
        // close and submit buttons
        _nosButtonPanel = new JPanel();
        _submitBtn = new JButton( "Submit" );
        _closeNosBtn = new JButton( "Close" );
        _nosButtonPanel.add( _submitBtn );
        _nosButtonPanel.add( _closeNosBtn );

        add( _nosButtonPanel, BorderLayout.SOUTH );

        ActionListener submitListener = new SubmitButtonListener();
        _submitBtn.addActionListener( submitListener );

        ActionListener cancelListener = new CloseButtonListener();
        _closeNosBtn.addActionListener( cancelListener );
    }

    private void addAmendOrderButtons() {
        // close and submit buttons
        _amendButtonPanel = new JPanel();
        _amendBtn = new JButton( "Amend" );
        _cancelBtn = new JButton( "Cancel" );
        _closeAmendBtn = new JButton( "Close" );
        _amendButtonPanel.add( _amendBtn );
        _amendButtonPanel.add( _cancelBtn );
        _amendButtonPanel.add( _closeAmendBtn );

        _amendButtonPanel.setVisible( false );

        add( _amendButtonPanel, BorderLayout.SOUTH );

        ActionListener amendListener = new AmendButtonListener();
        _amendBtn.addActionListener( amendListener );

        ActionListener cancelListener = new CancelButtonListener();
        _cancelBtn.addActionListener( cancelListener );

        ActionListener closeListener = new CloseButtonListener();
        _closeAmendBtn.addActionListener( closeListener );
    }

    private JPanel addOrderFields() {
        JPanel panel = new JPanel( new SpringLayout() );

        _secDesc = addTextField( panel, "SecDesc", 20 );
        _secId = addTextField( panel, "SecId", 20 );
        _price = addTextField( panel, "Price", 10 );
        _qty = addTextField( panel, "Quantity", 10 );
        _clOrdId = addTextField( panel, "ClOrdId", 15 );

        _ordType = addComboBox( panel, "OrdType", OrdType.values(), 1 );
        _tif = addComboBox( panel, "TIF", TimeInForce.values(), 0 );

        int rows = 7;
        int columns = 2;
        int gap = 10;
        SpringLayoutUtilities.makeCompactGrid( panel, rows, columns, gap, gap, // init x,y
                                               gap, gap / 2 );// xpad, ypad

        add( panel );

        return panel;
    }

    private <T> JComboBox<T> addComboBox( JPanel panel, String desc, T[] values, int setIdx ) {
        JComboBox<T> cb = new JComboBox<T>( values );
        cb.setSelectedIndex( 1 );

        JLabel label = new JLabel( desc, SwingConstants.TRAILING );
        label.setLabelFor( cb );
        panel.add( label );
        panel.add( cb );

        return cb;
    }

    private JTextField addTextField( JPanel panel, String desc, int cols ) {
        JTextField field = new JTextField();
        field.setColumns( cols );

        JLabel label = new JLabel( desc, SwingConstants.TRAILING );
        label.setLabelFor( field );
        panel.add( label );
        panel.add( field );

        return field;
    }

    @Override
    public void setVisible( boolean visible ) {
        _frame.setVisible( visible );
        super.setVisible( visible );
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
    void createGUI() {
        UIManager.put( "swing.boldMetal", Boolean.FALSE );

        _frame = new JFrame( "Order Ticket" );
        _frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        setOpaque( true ); // content panes must be opaque
        _frame.setContentPane( this );

        _frame.pack();

        _frame.setLocationRelativeTo( null );

        setVisible( false );
    }

    private JRadioButton addRadio( JPanel panel, ButtonGroup group, String text ) {
        JRadioButton b = new JRadioButton( text );
        b.addActionListener( this );
        group.add( b );
        panel.add( b );
        return b;
    }

    @Override
    public void actionPerformed( ActionEvent event ) {
        // possibly could set the price on change of buy/sell button
    }

    public void openTicket( Book book, Side side ) {

        _clOrdId.setText( "" + nextClOrdId() );

        _instrument = book.getInstrument();

        _secId.setText( "" + _instrument.getLongSymbol() );
        _secDesc.setText( _instrument.getSecurityDesc().toString() );

        book.getLevel( 0, _bbo );

        if ( side == Side.Sell ) {
            _sellButton.setSelected( true );
            _price.setText( "" + _bbo.getBidPx() ); // sell now cross to sell
        } else {
            _buyButton.setSelected( true );
            _price.setText( "" + _bbo.getAskPx() ); // buy now cross to ask
        }

        _qty.setText( "1" );

        _tif.setSelectedItem( TimeInForce.Day );
        _ordType.setSelectedItem( OrdType.Limit );

        toggleButtons( true );

        setVisible( true );
    }

    public void amendTicket( MiniOrderRequest order ) {
        _instrument = order.getInstrument();
        _clOrdId.setText( order.getClOrdId().toString() );

        _secId.setText( "" + _instrument.getLongSymbol() );
        _secDesc.setText( _instrument.getSecurityDesc().toString() );

        if ( order.getSide().getIsBuySide() ) {
            _buyButton.setSelected( true );
        } else {
            _sellButton.setSelected( true );
        }

        _price.setText( "" + order.getPrice() ); // buy now cross to ask
        _qty.setText( "" + order.getOrderQty() );

        _tif.setSelectedItem( order.getTimeInForce() );
        _ordType.setSelectedItem( order.getOrdType() );
        _orderId.copy( order.getOrderId() );

        toggleButtons( false );

        setVisible( true );
    }

    private void toggleButtons( boolean isNOS ) {
        _nosButtonPanel.setVisible( isNOS );
        _amendButtonPanel.setVisible( !isNOS );
    }

    private int nextClOrdId() {
        return ++_nextClOrdId;
    }

    /*
     * STATIC MAIN TO ALLOW DECOUPLED MANUAL TESTING
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

                ticket.init( null );

                ticket.createGUI();

                ticket.openTicket( book, Side.Sell );
            }
        } );

    }

}
