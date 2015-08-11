/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.fastfix.meta;


public class MetaSequenceEntry extends MetaTemplate {
    private MetaFieldEntry      _lengthField = null;
    private boolean             _optional;

    public MetaSequenceEntry( String name, int id, boolean optionalSeq ) {
        super( name, id, null );
        _optional = optionalSeq;
    }

    @Override
    public String toString() {
        return "\n       SEQUENCE : isOptional=" + isOptional() + ", LENGTH_FLD=" + getLengthField().toString() + "  :  {\n" + super.toString() + "\n}\n";
    }
    
    @Override
    public boolean isOptional() {
        return _optional;
        
//        return _lengthField.isOptional();
    }

    public MetaFieldEntry getLengthField() {
        return _lengthField;
    }

    public void setLengthField( MetaFieldEntry lengthField ) {
        _lengthField = lengthField;
    }
}

