/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.lang.stats;

import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.RTStartupException;
import com.rr.core.utils.FileException;
import com.rr.core.utils.FileUtils;

public class StatsCfgFileTest extends BaseTestCase {

    public void testStatsCfgFile() {
        
        Stats stats = new StatsCfgFile();
        
        stats.initialise();
        
        int stringDefault = stats.find( SizeType.DEFAULT_STRING_LENGTH );
        
        assertEquals( 10, stringDefault );
    }
    
    public void testPersist() throws FileException {
        
        String tmpFile = "./tmp/testCfgFile.cfg";
        
        try {
            StatsCfgFile stats = new StatsCfgFile();
            
            stats.initialise();
            
            FileUtils.mkDirIfNeeded( tmpFile );
            
            stats.setFile( tmpFile );
            stats.set( SizeType.DEFAULT_STRING_LENGTH, 20 );
            
            stats.store();
            stats.set( SizeType.DEFAULT_STRING_LENGTH, 30 );
            stats.reload();
            
            int stringDefault = stats.find( SizeType.DEFAULT_STRING_LENGTH );
            
            assertEquals( 20, stringDefault );
        } finally {
            FileUtils.rmIgnoreError( tmpFile );
        }
    }
    
    public void testUnknownStat() {
        
        StatsCfgFile stats = new StatsCfgFile();
        
        try {
            int res =  stats.find( SizeType.DEFAULT_STRING_LENGTH );

            assertEquals( SizeType.DEFAULT_STRING_LENGTH.getSize(), res );
            
        } catch( RTStartupException e ) {
            fail( "Expected RTStartupException to be thrown for unknown type" );
        }
    }
}
