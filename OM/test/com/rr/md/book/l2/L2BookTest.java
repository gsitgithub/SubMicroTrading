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
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;
import com.rr.model.generated.internal.events.impl.MDSnapshotFullRefreshImpl;
import com.rr.model.generated.internal.type.MDEntryType;
import com.rr.model.generated.internal.type.MDUpdateAction;

public class L2BookTest extends BaseL2BookTst {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testSimpleL1BuyOnly() {
        MDIncRefreshImpl event = getBaseEvent( 100 );
        add( event, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Bid, 1000 );

        applyEvent( _book, event );

        double[][] results = { { 100, 15.25, 0, 0.0 } };

        verify( _book, results );
    }

    public void testSimpleL5BuyOnly() {
        MDIncRefreshImpl event = getBaseEvent( 100 );
        add( event, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Bid, 1000 );
        add( event, 1, MDUpdateAction.New, 200, 15.5,  MDEntryType.Bid, 1001 );
        add( event, 2, MDUpdateAction.New, 300, 15.75, MDEntryType.Bid, 1002 );
        add( event, 3, MDUpdateAction.New, 400, 16.0,  MDEntryType.Bid, 1003 );
        add( event, 4, MDUpdateAction.New, 500, 16.25, MDEntryType.Bid, 1004 );

        applyEvent( _book, event );

        double[][] results = { 
                               { 100, 15.25, 0, 0.0 }, 
                               { 200, 15.5,  0, 0.0 },
                               { 300, 15.75, 0, 0.0 }, 
                               { 400, 16.0,  0, 0.0 }, 
                               { 500, 16.25, 0, 0.0 } 
                             };

        verify( _book, results );
    }

    public void testInsertTop() {
        MDIncRefreshImpl event = getBaseEvent( 100 );
        add( event, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Bid, 1000 );
        add( event, 1, MDUpdateAction.New, 200, 15.5,  MDEntryType.Bid, 1001 );
        add( event, 2, MDUpdateAction.New, 300, 15.75, MDEntryType.Bid, 1002 );
        add( event, 3, MDUpdateAction.New, 400, 16.0,  MDEntryType.Bid, 1003 );
        add( event, 4, MDUpdateAction.New, 500, 16.25, MDEntryType.Bid, 1004 );

        applyEvent( _book, event );

        MDIncRefreshImpl event2 = getBaseEvent( 101 );
        add( event2, 0, MDUpdateAction.New, 90,  15.0, MDEntryType.Bid, 1005 );

        applyEvent( _book, event2 );

        double[][] results = { 
                               {  90, 15.00, 0, 0.0 }, 
                               { 100, 15.25, 0, 0.0 }, 
                               { 200, 15.5,  0, 0.0 },
                               { 300, 15.75, 0, 0.0 }, 
                               { 400, 16.0,  0, 0.0 } 
                             };

        verify( _book, results );
    }

    public void testInsertBottom() {
        MDIncRefreshImpl event = getBaseEvent( 100 );
        add( event, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Bid, 1000 );
        add( event, 1, MDUpdateAction.New, 200, 15.5,  MDEntryType.Bid, 1001 );
        add( event, 2, MDUpdateAction.New, 300, 15.75, MDEntryType.Bid, 1002 );
        add( event, 3, MDUpdateAction.New, 400, 16.0,  MDEntryType.Bid, 1003 );
        add( event, 4, MDUpdateAction.New, 500, 16.25, MDEntryType.Bid, 1004 );

        applyEvent( _book, event );

        MDIncRefreshImpl event2 = getBaseEvent( 101 );
        add( event2, 4, MDUpdateAction.New, 450, 16.125, MDEntryType.Bid, 1005 );

        applyEvent( _book, event2 );

        double[][] results = { 
                               { 100, 15.25,  0, 0.0 }, 
                               { 200, 15.5,   0, 0.0 },
                               { 300, 15.75,  0, 0.0 }, 
                               { 400, 16.0,   0, 0.0 }, 
                               { 450, 16.125, 0, 0.0 } 
                             };

        verify( _book, results );
    }

    public void testInsertMiddle() {
        MDIncRefreshImpl event = getBaseEvent( 100 );
        add( event, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Bid, 1000 );
        add( event, 1, MDUpdateAction.New, 200, 15.5,  MDEntryType.Bid, 1001 );
        add( event, 2, MDUpdateAction.New, 300, 15.75, MDEntryType.Bid, 1002 );
        add( event, 3, MDUpdateAction.New, 400, 16.0,  MDEntryType.Bid, 1003 );
        add( event, 4, MDUpdateAction.New, 500, 16.25, MDEntryType.Bid, 1004 );

        applyEvent( _book, event );

        MDIncRefreshImpl event2 = getBaseEvent( 101 );
        add( event2, 2, MDUpdateAction.New, 250, 15.675, MDEntryType.Bid, 1005 );

        applyEvent( _book, event2 );

        double[][] results = { 
                               { 100, 15.25,  0, 0.0 }, 
                               { 200, 15.5,   0, 0.0 },
                               { 250, 15.675, 0, 0.0 }, 
                               { 300, 15.75,  0, 0.0 }, 
                               { 400, 16.0,   0, 0.0 } 
                             };

        verify( _book, results );
    }

    public void testSnap() {
        MDSnapshotFullRefreshImpl event = getSnapEvent( 100, 1000 );
        addMDEntry( event, 0, 100, 15.25, MDEntryType.Bid );
        addMDEntry( event, 1, 200, 15.5,  MDEntryType.Bid );
        addMDEntry( event, 2, 300, 15.75, MDEntryType.Bid );
        addMDEntry( event, 3, 400, 16.0,  MDEntryType.Bid );
        addMDEntry( event, 4, 500, 16.25, MDEntryType.Bid );
        addMDEntry( event, 0, 110, 15.15, MDEntryType.Offer );
        addMDEntry( event, 1, 210, 15.0,  MDEntryType.Offer );
        addMDEntry( event, 2, 310, 14.75, MDEntryType.Offer );
        addMDEntry( event, 3, 410, 14.50, MDEntryType.Offer );
        addMDEntry( event, 4, 510, 14.25, MDEntryType.Offer );

        applyEvent( _book, event );

        double[][] results = { 
                               { 100, 15.25, 110, 15.15 }, 
                               { 200, 15.5,  210, 15.0 },
                               { 300, 15.75, 310, 14.75 }, 
                               { 400, 16.0,  410, 14.5 }, 
                               { 500, 16.25, 510, 14.25 } 
                             };

        verify( _book, results );
    }

    public void testSetMiddle() {
        MDIncRefreshImpl event = getBaseEvent( 100 );
        add( event, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Bid, 1000 );
        add( event, 1, MDUpdateAction.New, 200, 15.5,  MDEntryType.Bid, 1001 );
        add( event, 2, MDUpdateAction.New, 300, 15.75, MDEntryType.Bid, 1002 );
        add( event, 3, MDUpdateAction.New, 400, 16.0,  MDEntryType.Bid, 1003 );
        add( event, 4, MDUpdateAction.New, 500, 16.25, MDEntryType.Bid, 1004 );

        applyEvent( _book, event );

        MDIncRefreshImpl event2 = getBaseEvent( 101 );
        add( event2, 2, MDUpdateAction.Overlay, 250, 15.675, MDEntryType.Bid, 1005 );

        applyEvent( _book, event2 );

        double[][] results = { 
                               { 100, 15.25,  0, 0.0 }, 
                               { 200, 15.5,   0, 0.0 },
                               { 250, 15.675, 0, 0.0 }, 
                               { 400, 16.0,   0, 0.0 }, 
                               { 500, 16.25,  0, 0.0 } 
                             };

        verify( _book, results );
    }

    public void testSetMiddleB() {
        MDIncRefreshImpl event = getBaseEvent( 100 );
        add( event, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Bid, 1000 );
        add( event, 1, MDUpdateAction.New, 200, 15.5,  MDEntryType.Bid, 1001 );
        add( event, 2, MDUpdateAction.New, 300, 15.75, MDEntryType.Bid, 1002 );
        add( event, 3, MDUpdateAction.New, 400, 16.0,  MDEntryType.Bid, 1003 );
        add( event, 4, MDUpdateAction.New, 500, 16.25, MDEntryType.Bid, 1004 );
        add( event, 0, MDUpdateAction.New, 110, 15.15, MDEntryType.Offer, 1005 );
        add( event, 1, MDUpdateAction.New, 210, 15.0,  MDEntryType.Offer, 1006 );
        add( event, 2, MDUpdateAction.New, 310, 14.75, MDEntryType.Offer, 1007 );
        add( event, 3, MDUpdateAction.New, 410, 14.50, MDEntryType.Offer, 1008 );
        add( event, 4, MDUpdateAction.New, 510, 14.25, MDEntryType.Offer, 1009 );


        applyEvent( _book, event );

        MDIncRefreshImpl event2 = getBaseEvent( 101 );
        add( event2, 2, MDUpdateAction.Overlay, 250, 14.875, MDEntryType.Offer, 1010 );

        applyEvent( _book, event2 );

        double[][] results = { 
                               { 100, 15.25,  110, 15.15 }, 
                               { 200, 15.5,   210, 15.0  },
                               { 300, 15.75,  250, 14.875 }, 
                               { 400, 16.0,   410, 14.5  }, 
                               { 500, 16.25,  510, 14.25 } 
                             };

        verify( _book, results );
    }

    public void testChangeQty() {
        MDIncRefreshImpl event = getBaseEvent( 100 );
        add( event, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Bid, 1000 );
        add( event, 1, MDUpdateAction.New, 200, 15.5,  MDEntryType.Bid, 1001 );
        add( event, 2, MDUpdateAction.New, 300, 15.75, MDEntryType.Bid, 1002 );
        add( event, 3, MDUpdateAction.New, 400, 16.0,  MDEntryType.Bid, 1003 );
        add( event, 4, MDUpdateAction.New, 500, 16.25, MDEntryType.Bid, 1004 );
        add( event, 0, MDUpdateAction.New, 110, 15.15, MDEntryType.Offer, 1005 );
        add( event, 1, MDUpdateAction.New, 210, 15.0,  MDEntryType.Offer, 1006 );
        add( event, 2, MDUpdateAction.New, 310, 14.75, MDEntryType.Offer, 1007 );
        add( event, 3, MDUpdateAction.New, 410, 14.50, MDEntryType.Offer, 1008 );
        add( event, 4, MDUpdateAction.New, 510, 14.25, MDEntryType.Offer, 1009 );


        applyEvent( _book, event );

        MDIncRefreshImpl event2 = getBaseEvent( 101 );
        add( event2, 2, MDUpdateAction.Change, 250, Constants.UNSET_DOUBLE, MDEntryType.Offer, 1010 );

        applyEvent( _book, event2 );

        double[][] results = { 
                               { 100, 15.25,  110, 15.15 }, 
                               { 200, 15.5,   210, 15.0  },
                               { 300, 15.75,  250, 14.75 }, 
                               { 400, 16.0,   410, 14.5  }, 
                               { 500, 16.25,  510, 14.25 } 
                             };

        verify( _book, results );
    }

    public void testSetDeleteA() {
        MDIncRefreshImpl event = getBaseEvent( 100 );
        add( event, 0, MDUpdateAction.New, 100, 15.25, MDEntryType.Bid, 1000 );
        add( event, 1, MDUpdateAction.New, 200, 15.5,  MDEntryType.Bid, 1001 );
        add( event, 2, MDUpdateAction.New, 300, 15.75, MDEntryType.Bid, 1002 );
        add( event, 3, MDUpdateAction.New, 400, 16.0,  MDEntryType.Bid, 1003 );
        add( event, 4, MDUpdateAction.New, 500, 16.25, MDEntryType.Bid, 1004 );
        add( event, 0, MDUpdateAction.New, 110, 15.15, MDEntryType.Offer, 1005 );
        add( event, 1, MDUpdateAction.New, 210, 15.0,  MDEntryType.Offer, 1006 );
        add( event, 2, MDUpdateAction.New, 310, 14.75, MDEntryType.Offer, 1007 );
        add( event, 3, MDUpdateAction.New, 410, 14.50, MDEntryType.Offer, 1008 );
        add( event, 4, MDUpdateAction.New, 510, 14.25, MDEntryType.Offer, 1009 );


        applyEvent( _book, event );

        MDIncRefreshImpl event2 = getBaseEvent( 101 );
        add( event2, 2, MDUpdateAction.Delete, 0, 0.0, MDEntryType.Offer, 1010 );
        add( event2, 0, MDUpdateAction.Delete, 0, 0.0, MDEntryType.Bid,   1010 );

        applyEvent( _book, event2 );

        double[][] results = { 
                               { 200, 15.5,  110, 15.15 }, 
                               { 300, 15.75, 210, 15.0  },
                               { 400, 16.0,  410, 14.5 }, 
                               { 500, 16.25, 510, 14.25  }, 
                               { 0,   0.0,     0, 0.0 } 
                             };

        verify( _book, results );
    }
}
