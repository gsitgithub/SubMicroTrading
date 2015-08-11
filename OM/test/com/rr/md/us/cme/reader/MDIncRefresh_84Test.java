/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.reader;

import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntMandReaderCopy;
import com.rr.core.utils.HexUtils;
import com.rr.md.fastfix.template.MDIncRefreshFastFixTemplateReader;
import com.rr.md.fastfix.template.MDIncRefreshFastFixTemplateWriter;
import com.rr.md.us.cme.reader.MDIncRefresh_84_Reader;
import com.rr.md.us.cme.writer.MDIncRefresh_84_Writer;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;


public class MDIncRefresh_84Test extends BaseMDIncRefreshTst {

    private final UIntMandReaderCopy    _templateIdReader = new UIntMandReaderCopy( "TemplateId", 0, 0 );

    public void testFailed84() {
        
        String hexMsg =
                "C0 D4 01 5A 17 96 23 5E 6C 66 4B 62 20 84 09 4C 06 D3 93 18 B0 01 53 BD 02 3B 15 AD 08 4F 90 00 F0 CE 0E 33 C1 3F 2C 23" +
                "B0 82 00 98 80 7F 94 80 84 82 80 00 98 80 81 80 84 82 80 00 98 80 FE 80 82 82 80 00 98 80 81 80 82 82 80 00 98 80 81 80 82 82 80 00 98" +
                "80 81 80 82 82 80 00 98 80 81 80 82 82 80 00 98 80 81 80 82 82 80 00 98 80 81 80 82 82 80 00 98 80 81 80 82 82 80 00 98 80 81 80 82 82" +
                "80 00 98 80 81 80 82 82 80 00 98 80 85 80 86 82 80 00 98 80 FC 80 82 82 80 00 98 80 86 80 87 82 80 00 98 80 FE 80 85 82 80 00 98 80 FD" +
                "80 82 82 80 00 98 80 81 80 82 82 80";

        byte[] binaryMsg = HexUtils.hexStringToBytes( hexMsg );
        byte[] buf = new byte[8192];
        System.arraycopy( binaryMsg, 0, buf, 0, binaryMsg.length );
        
        PresenceMapReader pMapIn  = new PresenceMapReader();

        MDIncRefreshImpl last = null;
        
        decoder.start( buf, 0, binaryMsg.length );
        pMapIn.readMap( decoder );
        int templateId = _templateIdReader.read( decoder, pMapIn ); 
        assertEquals( 84, templateId );
        
        last = reader.read( decoder, pMapIn );     

        assertEquals( 3574678, last.getMsgSeqNum() );
        assertEquals( 19, last.getNoMDEntries() );
    }
    
    @Override
    protected MDIncRefreshFastFixTemplateReader makeReader() {
        return new MDIncRefresh_84_Reader( cf, getName(), 84 );
    }
    
    @Override
    protected MDIncRefreshFastFixTemplateWriter makeWriter() {
        return new MDIncRefresh_84_Writer( cf, getName(), 84 );
    }
}
