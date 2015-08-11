/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.core.properties;

public interface PropertyTags {

    public interface Tag {
        @Override
        public String toString();
    }
    
    /**
     * @return name of this tag set
     */
    public String getSetName(); 

    /**
     * @param tag the final part of a property so xxx.yyy.zzz would have a tag of zzz
     * @return true if the tag is a valid property 
     */
    public boolean isValidTag( String tag );
    
    public Tag lookup( String tag );
}
