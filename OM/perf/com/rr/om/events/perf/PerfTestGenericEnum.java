/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.events.perf;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.Chainable;
import com.rr.core.lang.ReusableCategory;
import com.rr.core.lang.ReusableCategoryEnum;
import com.rr.core.lang.ReusableType;
import com.rr.core.lang.ReusableTypeIDFactory;

/**
 * test the performance difference between getReusableType returning generic enum and interface for switch statement
 *
 * A = getReusableType returns ReusableType interface
 * B = getReusableType returns enum generic extending ReusableType
 * C = getReusableType returns specific enum 
 * 
 * @author Richard Rose
 */

// TODO check impact of replacing all the small (<=8 byte) with SmallString

public class PerfTestGenericEnum extends BaseTestCase {

    private int _dontOpt;
    
    public static enum ModelReusableTypes implements ReusableType {

        NewOrderSingle( ReusableCategoryEnum.Event ),
        CancelReplaceRequest( ReusableCategoryEnum.Event ),
        CancelRequest( ReusableCategoryEnum.Event ),
        CancelReject( ReusableCategoryEnum.Event ),
        NewOrderAck( ReusableCategoryEnum.Event ),
        TradeNew( ReusableCategoryEnum.Event ),
        Rejected( ReusableCategoryEnum.Event ),
        Cancelled( ReusableCategoryEnum.Event ),
        Replaced( ReusableCategoryEnum.Event ),
        DoneForDay( ReusableCategoryEnum.Event ),
        Stopped( ReusableCategoryEnum.Event ),
        Expired( ReusableCategoryEnum.Event ),
        Suspended( ReusableCategoryEnum.Event ),
        Restated( ReusableCategoryEnum.Event ),
        TradeCorrect( ReusableCategoryEnum.Event ),
        TradeCancel( ReusableCategoryEnum.Event ),
        OrderStatus( ReusableCategoryEnum.Event );

        private final int              _id;
        private final ReusableCategory _cat;

        private ModelReusableTypes( ReusableCategory cat ) {
            _cat = cat;
            _id  = ReusableTypeIDFactory.nextId( cat );
        }

        @Override
        public int getId() { return _id; }

        @Override
        public int getSubId() {
            return _id;
        }

        @Override
        public ReusableCategory getReusableCategory() { return _cat; }
    }
    
    public interface ReusableA<T> extends Chainable<T> {

        public void reset();

        public ReusableType getReusableType();
    }
    
    public interface ReusableB<T> extends Chainable<T> {

        public void reset();

        public Enum<? extends ReusableType> getReusableType();
    }
    
    public interface ReusableC<T> extends Chainable<T> {

        public void reset();

        public ModelReusableTypes getReusableType();
    }
    
    public static class ReusableGenericA implements ReusableA<ReusableGenericA> {

        private ReusableGenericA _nxt;

        public ReusableGenericA( ReusableGenericA nxt ) { _nxt = nxt; }
        
        @Override
        public ReusableType getReusableType() { return ModelReusableTypes.TradeNew; }

        @Override
        public void reset() { _nxt = _nxt._nxt; }

        @Override
        public ReusableGenericA getNext() { return _nxt; }

        @Override
        public void setNext( ReusableGenericA nxt ) { _nxt = nxt; }
    }
    
    public static class ReusableGenericB implements ReusableB<ReusableGenericB> {

        private ReusableGenericB _nxt;

        public ReusableGenericB( ReusableGenericB nxt ) { _nxt = nxt; }
        
        @Override
        public Enum<? extends ReusableType> getReusableType() { return ModelReusableTypes.TradeNew; }

        @Override
        public void reset() { _nxt = _nxt._nxt; }

        @Override
        public ReusableGenericB getNext() { return _nxt; }

        @Override
        public void setNext( ReusableGenericB nxt ) { _nxt = nxt; }
    }
    
    public static class ReusableGenericC implements ReusableC<ReusableGenericC> {

        private ReusableGenericC _nxt;

        public ReusableGenericC( ReusableGenericC nxt ) { _nxt = nxt; }
        
        @Override
        public ModelReusableTypes getReusableType() { return ModelReusableTypes.TradeNew; }

        @Override
        public void reset() { _nxt = _nxt._nxt; }

        @Override
        public ReusableGenericC getNext() { return _nxt; }

        @Override
        public void setNext( ReusableGenericC nxt ) { _nxt = nxt; }
    }
    
    
    public void testGenerics() {
    
        int runs = 5;
        int iterations = 100000000;
        
        doRun( runs, iterations );
    }

    private void doRun( int runs, int iterations ) {
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long reusableType = testA( iterations );
            long enumGeneric  = testB( iterations );
            long explicit     = testC( iterations );
            
            System.out.println( "Run " + idx + "GET reusableType=" + reusableType + ", enumGen=" + enumGeneric+ ", explicit=" + explicit );
        }
        
        for( int idx=0 ; idx < runs ; idx++ ) {
            
            long reusableType = switchA( iterations );
            long enumGeneric  = switchB( iterations );
            long explicit     = switchC( iterations );
            
            System.out.println( "Run " + idx + "SWITCH reusableType=" + reusableType + ", enumGen=" + enumGeneric+ ", explicit=" + explicit );
        }
    }

    private long testA( int iterations ) {
        
        ReusableGenericA r1 = new ReusableGenericA( null );
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        for( int i=0 ; i < iterations ; ++i ) {
            if ( r1.getReusableType() == ModelReusableTypes.TradeNew ) {
                cnt++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }

    private long testB( int iterations ) {
        
        ReusableGenericB r1 = new ReusableGenericB( null );
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        for( int i=0 ; i < iterations ; ++i ) {
            if ( r1.getReusableType() == ModelReusableTypes.TradeNew ) {
                cnt++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }

    private long testC( int iterations ) {
        
        ReusableGenericC r1 = new ReusableGenericC( null );
        
        int cnt = _dontOpt;
        
        long startTime = System.currentTimeMillis();
            
        for( int i=0 ; i < iterations ; ++i ) {
            if ( r1.getReusableType() == ModelReusableTypes.TradeNew ) {
                cnt++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = cnt;
        
        return endTime - startTime;
    }

    private long switchA( int iterations ) {
        
        ReusableGenericA r1 = new ReusableGenericA( null );
        
        int a=0,b=0;
        
        long startTime = System.currentTimeMillis();
            
        for( int x=0 ; x < iterations ; ++x ) {
            switch( r1.getReusableType().getId() )  {
            case 1:
                a++;
                break;
            case 2:
                b++;
                break;
            case 3:
                a++;
                break;
            case 4:
                b++;
                break;
            case 5:
                a++;
                break;
            case 6:
                b++;
                break;
            case 7:
                a++;
                break;
            case 8:
                b++;
                break;
            case 9:
                a++;
                break;
            case 10:
                b++;
                break;
            case 11:
                a++;
                break;
            case 12:
                b++;
                break;
            case 13:
                b++;
                break;
            case 14:
                a++;
                break;
            case 15:
                a++;
                break;
            case 16:
                b++;
                break;
            case 17:
                a++;
                break;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = a - b;
        
        return endTime - startTime;
    }

    private long switchB( int iterations ) {
        ReusableGenericB r1 = new ReusableGenericB( null );
        
        int a=0,b=0;
        
        long startTime = System.currentTimeMillis();
            
        for( int x=0 ; x < iterations ; ++x ) {
            switch( (ModelReusableTypes)r1.getReusableType() )  {
            case NewOrderSingle:
                a++;
                break;
            case CancelReplaceRequest:
                b++;
                break;
            case CancelRequest:
                a++;
                break;
            case CancelReject:
                b++;
                break;
            case NewOrderAck:
                a++;
                break;
            case TradeNew:
                b++;
                break;
            case Rejected:
                a++;
                break;
            case Cancelled:
                b++;
                break;
            case Replaced:
                a++;
                break;
            case DoneForDay:
                b++;
                break;
            case Stopped:
                a++;
                break;
            case Expired:
                b++;
                break;
            case Suspended:
                a++;
                break;
            case Restated:
                b++;
                break;
            case TradeCorrect:
                a++;
                break;
            case TradeCancel:
                b++;
                break;
            case OrderStatus:
                a++;
                break;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = a - b;
        
        return endTime - startTime;
    }

    private long switchC( int iterations ) {
        
        ReusableGenericC r1 = new ReusableGenericC( null );
        
        int a=0,b=0;
        
        long startTime = System.currentTimeMillis();
            
        for( int x=0 ; x < iterations ; ++x ) {
            switch( r1.getReusableType() )  {
            case NewOrderSingle:
                a++;
                break;
            case CancelReplaceRequest:
                b++;
                break;
            case CancelRequest:
                a++;
                break;
            case CancelReject:
                b++;
                break;
            case NewOrderAck:
                a++;
                break;
            case TradeNew:
                b++;
                break;
            case Rejected:
                a++;
                break;
            case Cancelled:
                b++;
                break;
            case Replaced:
                a++;
                break;
            case DoneForDay:
                b++;
                break;
            case Stopped:
                a++;
                break;
            case Expired:
                b++;
                break;
            case Suspended:
                a++;
                break;
            case Restated:
                b++;
                break;
            case TradeCorrect:
                a++;
                break;
            case TradeCancel:
                b++;
                break;
            case OrderStatus:
                a++;
                break;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        _dontOpt = a - b;
        
        return endTime - startTime;
    }
}
