/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.admin;

import javax.management.ObjectInstance;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.utils.Utils;

public class TestAdminAgent extends BaseTestCase {

    public void testAgent() throws AdminException {
        try {
            AdminAgent.init( 8000 );
            DummyAdminCommand beanA = new DummyAdminCommand( "AAA", "one" );
            DummyAdminCommand beanB = new DummyAdminCommand( "BBB", "two" );
            AdminAgent.register( beanA );
            AdminAgent.register( beanB );
            ObjectInstance mbA = AdminAgent.find( beanA.getName() );
            ObjectInstance mbB = AdminAgent.find( beanB.getName() );
            assertNotSame( mbA, mbB );
            assertNotNull( mbA );
            assertNotNull( mbB );
            Object aVal = AdminAgent.getAttribute( beanA.getName(), "Message" );
            assertEquals( beanA.getMessage(), aVal );
        } finally {
            AdminAgent.close();
        }
    }
    
    public static void main( String args[] ) {

        TestAdminAgent a = new TestAdminAgent();
        try {
            a.testAgent();
            
            System.out.println( "Start JConsole to test" );
            while( true ) {
                Utils.delay( 1000 );
            }
        } catch( AdminException e ) {
            System.out.println( "Failed" );
        }
    }
}
