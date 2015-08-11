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
import com.rr.md.us.cme.reader.MDIncRefresh_109_Reader;
import com.rr.md.us.cme.writer.MDIncRefresh_109_Writer;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;


public class MDIncRefresh_109Test extends BaseMDIncRefreshTst {

    private final UIntMandReaderCopy    _templateIdReader = new UIntMandReaderCopy( "TemplateId", 0, 0 );

    public void testFailed109b() {
        
        String hexMsg = "C0 ED 01 6A 5D B7 23 5E 6C 66 4F 3C 00 A8 09 4C 06 D3 83 5F B0 80 B1 01 3B A2 03 B9 C3 80 08 49 89 43 06 04 80 80 80 80 11 90 CF 80 80 80 80 80 31 C0 82 B1 80 80 82 8B 80";

        byte[] binaryMsg = HexUtils.hexStringToBytes( hexMsg );
        byte[] buf = new byte[8192];
        System.arraycopy( binaryMsg, 0, buf, 0, binaryMsg.length );
        
        PresenceMapReader pMapIn  = new PresenceMapReader();

        MDIncRefreshImpl last = null;
        
        decoder.start( buf, 0, binaryMsg.length );
        pMapIn.readMap( decoder );
        int templateId = _templateIdReader.read( decoder, pMapIn ); 
        assertEquals( 109, templateId );
        
        last = reader.read( decoder, pMapIn );     

        assertEquals( 3845815, last.getMsgSeqNum() );
        assertEquals( 3, last.getNoMDEntries() );
    }
    
    public void testFailed109() {
        
        String hexMsg = "C0 ED 01 5A 18 FF 23 5E 6C 66 4B 62 27 AB 09 4C 06 D3 81 2D E0 82 01 53 BD 02 3B 18 DC 80 08 4F 90 9C 3F 2C 2B 98 AB 80";

        byte[] binaryMsg = HexUtils.hexStringToBytes( hexMsg );
        byte[] buf = new byte[8192];
        System.arraycopy( binaryMsg, 0, buf, 0, binaryMsg.length );
        
        PresenceMapReader pMapIn  = new PresenceMapReader();

        MDIncRefreshImpl last = null;
        
        decoder.start( buf, 0, binaryMsg.length );
        pMapIn.readMap( decoder );
        int templateId = _templateIdReader.read( decoder, pMapIn ); 
        assertEquals( 109, templateId );
        
        last = reader.read( decoder, pMapIn );     

        assertEquals( 3574911, last.getMsgSeqNum() );
        assertEquals( 1, last.getNoMDEntries() );
    }
    
    @Override
    protected MDIncRefreshFastFixTemplateReader makeReader() {
        return new MDIncRefresh_109_Reader( cf, getName(), 109 );
    }
    
    @Override
    protected MDIncRefreshFastFixTemplateWriter makeWriter() {
        return new MDIncRefresh_109_Writer( cf, getName(), 109 );
    }
}
