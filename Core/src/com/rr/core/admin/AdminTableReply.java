/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.admin;

import com.rr.core.lang.ReusableString;
import com.rr.core.lang.ZString;

public class AdminTableReply implements AdminReply {

    private final int            _cols;
    private       int            _curCol = 0;
    private final ReusableString _res    = new ReusableString();
    
    public AdminTableReply( String[] columns ) {
        _cols = columns.length;

        _res.append( "<table border=\"1\"><tr>" );
        for( int i=0 ; i < _cols ; ++i ) {
            _res.append( "<th>" ).append( columns[i] ).append( "</th>" );
        }
        _res.append( "</tr>" );
    }

    @Override
    public void add( ZString val ) {
        startCol();
        _res.append( val );
        endCol();
    }

    @Override
    public void add( boolean val ) {
        startCol();
        _res.append( (val) ? "true" : "false" );
        endCol();
    }

    @Override
    public void add( int val ) {
        startCol();
        _res.append( val );
        endCol();
    }

    @Override
    public void add( long val ) {
        startCol();
        _res.append( val );
        endCol();
    }

    @Override
    public void add( double val ) {
        startCol();
        _res.append( val );
        endCol();
    }

    @Override
    public void add( String val ) {
        startCol();
        _res.append( val );
        endCol();
    }

    @Override
    public String end() {
        _res.append( "</table>" );
        return _res.toString();
    }

    private void startCol() {
        if ( _curCol++ % _cols == 0 ) {
            _res.append( "<tr>" );
        }
        _res.append( "<td>" );
    }

    private void endCol() {
        _res.append( "</td>" );
        if ( _curCol % _cols == 0 ) {
            _res.append( "</tr>" );
        }
    }
}
