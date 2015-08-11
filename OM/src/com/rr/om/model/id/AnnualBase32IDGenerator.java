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
 * in effect the counter will be the number of millisends from start of year to the last midnight.
 * this gives 24 * 60 * 60 * 1000 = 86,400,000 ids per day unique over the year
 * max id is 365 * 86,400,000 = 31,536,000,000 
 * in base 32, a length of seven is enough for 34,359,738,368 combinations
 */
public class AnnualBase32IDGenerator extends AbstractBase32IDGenerator {

    /**
     * @param processInstanceSeed   
     * @param len                   length of non seed component
     */
    public AnnualBase32IDGenerator( ZString processInstanceSeed, int len ) {
        super( processInstanceSeed, len );
    }

    @Override
    protected long seed() {
        Calendar c = new GregorianCalendar();
        c.set( Calendar.HOUR_OF_DAY, 0 );
        c.set( Calendar.MINUTE, 0 );
        c.set( Calendar.SECOND, 0 );
        c.set( Calendar.MILLISECOND, 0 );
        c.set( Calendar.DAY_OF_MONTH, 1 );
        c.set( Calendar.MONTH, 0 );
        
        return System.currentTimeMillis() - c.getTimeInMillis();
    }
}
