/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.properties;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.utils.SMTRuntimeException;

public class TestAppProps extends BaseTestCase {
    
    public static class TestPropertyTags implements PropertyTags {
        public enum Tags implements PropertyTags.Tag {
            Price, Desc, Fuel, Seats;
        }

        private static final Set<String> _set = new HashSet<String>();

        static {
            for ( Tags p : Tags.values() ) {
                 _set.add( p.toString().toLowerCase() );
            }
        }

        private static TestPropertyTags _instance = new TestPropertyTags();
        public  static TestPropertyTags instance() { return _instance; }
        
        @Override public String getSetName() { return "TestProps"; }
        
        @Override public Tag lookup( String tag ){ return Tags.valueOf( tag ); }
        private TestPropertyTags() { /* singleton */ }

        @Override
        public boolean isValidTag( String tag ) {
            if ( tag == null ) return false;
            return _set.contains( tag.toLowerCase() );
        }

        public void add( String validArg ) {
            _set.add( validArg.toLowerCase() );
        }
    }
    
    public static class TestProps extends AppProps {
        private int _lineNo = 0;
        private final Map<String,String> _localProps;

        public TestProps( String validArgs ) {
            this( validArgs, new LinkedHashMap<String,String>() );
        }
        
        public TestProps( String validArgs, Map<String,String> localProps ) {
            _localProps = localProps;
            setPropSet( TestPropertyTags.instance() );
            String[] args = validArgs.split(",");
            for( String arg : args ) {
                arg = arg.trim();
                if ( arg.length()  > 0 ) {
                    TestPropertyTags.instance().add( arg );
                }
            }
            setInit();
        }
        
        public TestProps() {
            this( "" );
        }

        public void add( String line ) throws Exception {
            procLine( line, "n/a", ++_lineNo, _localProps );
        }

        public void resolve() {
            resolveProps();
        }
    }

    public void testSimple() throws Exception {
        TestProps p = new TestProps();
        
        p.add( "car.audi.Q7.price=50000" );
        p.add( "car.audi.Q7.desc=4*4 SUV" );
        
        int price = p.getIntProperty( "car.audi.Q7.price", false, 0 );
        assertEquals( 50000, price );

        String desc = p.getProperty( "car.audi.Q7.desc" );
        assertEquals( "4*4 SUV", desc );

        String fuel = p.getProperty( "car.audi.Q7.fuel", false, "Diesel" );
        assertEquals( "Diesel", fuel);

        try {
            p.getProperty( "car.audi.Q7.seats" );
            fail( "Failed to throw exception on missing mand prop" );
        } catch( SMTRuntimeException e ) {
            //  expected
        }
    }

    public void testCaseIgnored() throws Exception {
        TestProps p = new TestProps();
        
        p.add( "car.audi.Q7.DeSC=4*4 SUV" );
        
        String desc = p.getProperty( "car.audi.Q7.desc" );
        assertEquals( "4*4 SUV", desc );

        desc = p.getProperty( "car.audi.Q7.DESC" );
        assertEquals( "4*4 SUV", desc );
    }

    public void testInvalidProperty() throws Exception {
        TestProps p = new TestProps();
        
        p.add( "car.audi.Q7.desc=4*4 SUV" );
        
        try {
            p.add( "car.audi.Q7.duff=50000" );
            fail( "Failed to throw exception on missing mand prop" );
        } catch( InvalidPropertyException e ) {
            // expected
        }

        try {
            p.getProperty( "car.audi.Q7.duff" );
            fail( "Failed to throw exception on bad prop 'duff'" );
        } catch( SMTRuntimeException e ) {
            //  expected
        }
    }

    public void testGroups() throws Exception {
        TestProps p = new TestProps();
        
        p.add( "car.audi.Q7.price=50000" );
        p.add( "car.audi.q7.desc=Q7 4*4 SUV" );
        p.add( "car.audi.a8.desc=A8 saloon" );
        p.add( "car.audi.A8.price=80000" );
        p.add( "car.audi.A4.desc=A4 saloon" );
        
        String[] carsA = p.getNodes( "car.audi" );
        String[] carsB = p.getNodes( "car.audi." );
        
        assertEquals( 3, carsA.length );
        assertEquals( 3, carsB.length );
        
        for( String car : carsA ) {
            if ( !car.equals( "q7" ) && !car.equals( "a4" ) && !car.equals( "a8" ) ) {
                fail( "bad car : [" + car + "]" );
            }
        }
    }
    
    public void testMacros() throws Exception {
        TestProps p = new TestProps();
        
        p.add( "car.audi.Q7.price=50000" );
        p.add( "car.audi.Q7.desc=Q7 4*4 SUV" );
        p.add( "car.audi.a8.desc=A8 saloon" );
        p.add( "car.audi.desc=${car.audi.Q7.desc} and ${car.audi.a8.desc}" );
        p.add( "car.desc=AUDI ${car.audi.desc}" );
        p.resolve();
        
        String desc = p.getProperty( "car.audi.desc" );
        assertEquals( "Q7 4*4 SUV and A8 saloon", desc );

        desc = p.getProperty( "car.desc" );
        assertEquals( "AUDI Q7 4*4 SUV and A8 saloon", desc );
    }
    
    public void testPropertyGroups() throws Exception {
        TestProps p = new TestProps();
        
        p.add( "car.audi.Q7.price=50000" );
        p.add( "car.audi.q7.desc=Q7 4*4 SUV" );
        p.add( "car.audi.a8.desc=A8 saloon" );
        p.add( "car.audi.A8.price=80000" );
        p.add( "car.audi.desc=AUDI" );
        p.add( "car.audi.A1.price=25000" );
        p.add( "car.fuel=Diesel" );

        PropertyGroup g = new PropertyGroup( p, "car.audi.Q7", "car.audi", "car" );
        
        int price = g.getIntProperty( TestPropertyTags.Tags.Price, false, 0 );
        assertEquals( 50000, price );

        String desc = g.getProperty( TestPropertyTags.Tags.Desc );
        assertEquals( "Q7 4*4 SUV", desc );

        String fuel = g.getProperty( TestPropertyTags.Tags.Fuel );
        assertEquals( "Diesel", fuel );                                 // taken from minor

        g = new PropertyGroup( p, "car.audi.A1", "car.audi", "car" );
        
        price = g.getIntProperty( TestPropertyTags.Tags.Price, false, 0 );
        assertEquals( 25000, price );

        desc = g.getProperty( TestPropertyTags.Tags.Desc );
        assertEquals( "AUDI", desc );                                   // taken from major

        try {
            g.getProperty( TestPropertyTags.Tags.Seats );
            fail( "Mand missing prop should throw exception" );
        } catch( SMTRuntimeException e ) {
            // expected
        }
    }
}
