/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.processor;

import com.rr.core.codec.FixDecoder;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.model.generated.internal.events.impl.ClientNewOrderSingleImpl;
import com.rr.model.generated.internal.events.impl.ClientTradeNewImpl;
import com.rr.model.generated.internal.events.impl.MarketNewOrderAckImpl;
import com.rr.model.generated.internal.events.interfaces.TradeNew;
import com.rr.model.generated.internal.type.OrdStatus;
import com.rr.om.order.Order;
import com.rr.om.warmup.FixTestUtils;


public class TestTradeNewForSpreadOrder extends BaseProcessorTestCase {
    
    @Override
    protected FixDecoder createDecoder() {
        return FixTestUtils.getDecoderCME42();
    }
    
    public void testPartialOnOpenState() {

        String nos = "8=FIX.4.2|9=217|35=D|34=279|49=1T1234N|52=20130812-09:24:42.032|56=CME|1=4895|11=ABC//P1000001|21=1|38=5|40=2|44=13.000000|50=1234|54=1|55=EBPM5-EBPH5|57=G|59=0|60=20130812-09:24:42|107=EBPM5-EBPH5|142=IN|167=FUT|204=1|1028=N|9702=1|10=088|";
        String ack = "8=FIX.4.2|9=285|35=8|34=289|369=279|52=20130812-09:26:58.064|49=CME|50=G|56=1T1234N|57=1234|143=IN|1=4895|6=0|11=ABC//P1000001|14=0|17=82213:266456|20=0|37=82129307769|38=5|39=0|40=2|41=0|44=13|48=998520|54=1|55=90|59=0|60=20130812-09:26:58.065|107=EBPM5-EBPH5|150=0|151=5|167=FUT|432=20130812|1028=N|10=105";
        String fill = "8=FIX.4.2|9=384|35=8|34=290|369=279|52=20130812-09:26:58.079|49=CME|50=G|56=1T1234N|57=1234|143=IN|1=4895|6=0|11=ABC//P1000001|14=3|17=82213:266458TN0000005|20=0|31=13|32=3|37=82129307769|38=5|39=1|40=2|41=0|44=13|48=998520|54=1|55=90|59=0|60=20130812-09:26:58.079|75=20130812|107=EBPM5-EBPH5|150=1|151=0|167=FUT|337=TRADE|375=CME000A|393=2|432=20130812|442=3|527=I1T51k89ylq5201308125|1028=N|1057=N|10=000";
        String fillLeg1 = "8=FIX.4.2|9=332|35=8|34=291|369=279|52=20130812-09:26:58.079|49=CME|50=G|56=1T1234N|57=1234|143=IN|1=4895|6=0|11=ABC//P1000001|14=3|17=82213:266460TN0000013|20=0|31=9950|32=3|37=82129307769|39=1|40=2|41=0|48=998490|54=1|55=90|60=20130812-09:26:58.079|75=20130812|107=EBPM5|150=1|167=FUT|337=TRADE|375=CME000A|442=2|527=I1T51k89ylq5201308125|1028=N|10=095";
        String fillLeg2 = "8=FIX.4.2|9=332|35=8|34=292|369=279|52=20130812-09:26:58.079|49=CME|50=G|56=1T1234N|57=1234|143=IN|1=4895|6=0|11=ABC//P1000001|14=3|17=82213:266462TN0000006|20=0|31=9937|32=3|37=82129307769|39=1|40=2|41=0|48=998500|54=2|55=90|60=20130812-09:26:58.079|75=20130812|107=EBPH5|150=1|167=FUT|337=TRADE|375=CME000A|442=2|527=I1T51k89ylq5201308125|1028=N|10=106";
        
        ZString mktOrderId   = new ViewString("82129307769");
        ZString fillExecId   = new ViewString("TN0000005");
        ZString fillExecIdL1 = new ViewString("TN0000013");
        ZString fillExecIdL2 = new ViewString("TN0000006");
        
       // send NOS to mkt
        ClientNewOrderSingleImpl cnos = FixTestUtils.getMessage( nos, _decoder, _upMsgHandler );
        _proc.handle( cnos );
        clearQueues();

       // synth mkt ack
        MarketNewOrderAckImpl mack = FixTestUtils.getMessage( ack, _decoder, null );
        _proc.handle( mack );
        clearQueues();
       
       // MACK is now recycled !
        
       // order now open send partial fill and get client fill
        
        TradeNew mfill = FixTestUtils.getMessage( fill, _decoder, null );
        
        int lastQty = mfill.getLastQty();
        double lastPx = mfill.getLastPx();
        int leavesQty = cnos.getOrderQty() - lastQty; 
        
        _proc.handle( mfill );
        checkQueueSize( 1, 0, 0 );
        ClientTradeNewImpl cfill = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );
        assertEquals( "EBPM5-EBPH5", cfill.getSecurityDesc().toString() );

        Order order = _proc.getOrder( cnos.getClOrdId() );

        assertSame( _proc.getStateOpen(),       order.getState() );
        assertSame( order.getLastAckedVerion(), order.getPendingVersion() );

        checkTradeNew( order, cnos, cfill, mktOrderId, fillExecId, lastQty, lastPx, lastQty, lastPx * lastQty, OrdStatus.PartiallyFilled );

        TradeNew mfillLeg1 = FixTestUtils.getMessage( fillLeg1, _decoder, null );
        _proc.handle( mfillLeg1 );
        checkQueueSize( 1, 0, 0 );
        ClientTradeNewImpl cfillL1 = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );
        assertEquals( "EBPM5", cfillL1.getSecurityDesc().toString() );
        checkTradeNew( order, cnos, cfillL1, mktOrderId, fillExecIdL1, lastQty, 9950.0, lastQty, lastPx * lastQty, OrdStatus.PartiallyFilled );

        TradeNew mfillLeg2 = FixTestUtils.getMessage( fillLeg2, _decoder, null );
        _proc.handle( mfillLeg2 );
        checkQueueSize( 1, 0, 0 );
        ClientTradeNewImpl cfillL2 = (ClientTradeNewImpl) getMessage( _upQ, ClientTradeNewImpl.class );
        assertEquals( "EBPH5", cfillL2.getSecurityDesc().toString() );
        
        checkTradeNew( order, cnos, cfillL2, mktOrderId, fillExecIdL2, lastQty, 9937, lastQty, lastPx * lastQty, OrdStatus.PartiallyFilled );
        
        assertEquals( leavesQty, order.getLastAckedVerion().getLeavesQty() );
    }
}
