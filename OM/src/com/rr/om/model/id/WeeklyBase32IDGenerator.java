/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.om.model.id;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.rr.core.lang.ZString;

/**
 * in effect the counter will be the number of millisends from one week ago.
 */
public class WeeklyBase32IDGenerator extends AbstractBase32IDGenerator {

    /**
     * 
     * @param processInstanceSeed   
     * @param len                   length of non seed component
     */
    public WeeklyBase32IDGenerator( ZString processInstanceSeed, int len ) {
        super( processInstanceSeed, len );
        
        seed();
    }

    @Override
    protected long seed() {
        Calendar c = new GregorianCalendar();
        
        c.add( Calendar.DAY_OF_MONTH, -7 );
        
        return System.currentTimeMillis() - c.getTimeInMillis();
    }
}
