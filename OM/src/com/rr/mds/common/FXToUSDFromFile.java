/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.mds.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.rr.core.lang.ErrorCode;
import com.rr.core.lang.ViewString;
import com.rr.core.lang.ZString;
import com.rr.core.log.Logger;
import com.rr.core.log.LoggerFactory;
import com.rr.core.model.Currency;

public class FXToUSDFromFile {

    private static final Logger         _log = LoggerFactory.create( FXToUSDFromFile.class );
    
    private static final ErrorCode      CANT_READ_FILE = new ErrorCode( "FXF100", "Unable to read the FX file" );
    
    public  static final ZString        DEFAULT_FX_TO_USD_FILE = new ViewString( "./var/daily/fxrates.txt" );
    
    public static void load( ZString fileName ) {
        if ( fileName == null ) fileName = DEFAULT_FX_TO_USD_FILE;
        
        _log.info( "Setting FX from " + fileName );

        for ( Currency ccy : Currency.values() ) {
            ccy.setUSDFactor( Double.NaN );
        }

        File file = new File( fileName.toString() );
        
        if ( ! file.canRead() ) {
            _log.error( CANT_READ_FILE, fileName );
            
            throw new RuntimeException( "Cant read FX from file" );
        }
        
        try {
            BufferedReader input =  new BufferedReader( new FileReader(file) );

            try {
                String line = null; 
                while( ( line = input.readLine()) != null ){
                    if ( line.charAt( 0 ) == '#' ) continue;
                    
                    String[] entries = line.split( " " );
                    
                    if ( entries.length != 2 ){
                        _log.warn( "FXFile line has too many entries " + line );
                    }
                    
                    String ccyStr = entries[0].trim();                    
                    
                    Currency ccy = Currency.getVal( ccyStr.getBytes(), 0, ccyStr.length() );
                    
                    if ( ccy != null ) {
                        double rate = Double.parseDouble( entries[1] );
                    
                        _log.info( "Setting FX for " + ccy.toString() + " to USD is " + rate );
                        
                        ccy.setUSDFactor( rate );
                    }
                }
                
                Currency.Other.setUSDFactor( 1.0 );
                Currency.Unknown.setUSDFactor( 1.0 );
            }
            finally {
                input.close();
            }
        }
        catch( IOException e ) {
            _log.warn( "Unable to read file " + fileName + " : " + e.getMessage() );
            throw new RuntimeException( e );
        }
        
        for ( Currency ccy : Currency.values() ) {
            if ( ccy.toUSDFactor() == Double.NaN ) {
                _log.warn( "FX to USD missing entry for " + ccy.toString() );
                
                throw new RuntimeException( "FX to USD missing entry for " + ccy.toString() );
            }
        }
    }
}
