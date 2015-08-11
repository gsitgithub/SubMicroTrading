/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.reader;

import com.rr.md.fastfix.template.MDIncRefreshFastFixTemplateReader;
import com.rr.md.fastfix.template.MDIncRefreshFastFixTemplateWriter;
import com.rr.md.us.cme.reader.MDIncRefresh_81_Reader;
import com.rr.md.us.cme.writer.MDIncRefresh_81_Writer;
import com.rr.model.generated.internal.events.impl.MDEntryImpl;


public class MDIncRefresh_81Test extends BaseMDIncRefreshTst {

    @Override
    protected MDIncRefreshFastFixTemplateReader makeReader() {
        return new MDIncRefresh_81_Reader( cf, getName(), 81 );
    }
    
    @Override
    protected MDIncRefreshFastFixTemplateWriter makeWriter() {
        return new MDIncRefresh_81_Writer( cf, getName(), 81 );
    }

    @Override
    protected void checkMDEntry( MDEntryImpl expEntry, MDEntryImpl decodedEntry ) {
        super.checkMDEntry( expEntry, decodedEntry );

        assertEquals( expEntry.getMdPriceLevel(),       decodedEntry.getMdPriceLevel() );
        assertEquals( expEntry.getNumberOfOrders(),     decodedEntry.getNumberOfOrders() );
    }
}
