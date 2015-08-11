/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.reader;

import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.Dictionaries;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntMandReaderCopy;
import com.rr.core.collections.IntHashMap;
import com.rr.core.collections.IntMap;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.utils.HexUtils;
import com.rr.md.fastfix.XMLFastFixTemplateLoader;
import com.rr.md.fastfix.meta.MetaTemplate;
import com.rr.md.fastfix.meta.MetaTemplates;
import com.rr.md.fastfix.reader.FastFixToFixReader;
import com.rr.md.fastfix.template.FastFixTemplateClassRegister;
import com.rr.md.fastfix.template.TemplateClassRegister;


public class CMEFailedMsgsTest extends BaseTestCase {

    private static final String msg1 = "00 36 9A DC 01 DC E3 01 5A 35 DC 23 5E 6C 66 4B 6B 52 D9 5A AA 09 4C 06 D3 80 81 07 56 E8 80 80 80 80";
    private static final String EXP_msg1 = "[3578588] [1]1128=9|35=f|49=CME|34=3578588|52=20120403-13:30:00.025|48=11561|22=8|75=20120403|333=125800.0|";

    private static final String msg2 = 
        "00 36 8B 99 01 C0 E7 01 5A 17 99 23 5E 6C 66 4B 62 20 86 09 4C 06 D3 91 76 78 C0 80 80 B2 80 01 53 BD 02 3B 15 D6 08 4F 90 3F 2C 23 B0 84 80 80 " +
        "81 CE 0E 35 E2 82 20 38 E0 80 80 80 FE 80 80 81 80 0E 35 E3 82 80 20 38 E0 80 80 80 81 80 80 81 80 0E 35 E4 82 80 20 38 E0 80 80 80 81 80 80 81 " +
        "80 0E 35 E5 82 80 20 38 E0 80 80 80 81 80 80 81 80 0E 35 E6 82 80 20 38 E0 80 80 80 81 80 80 81 80 0E 35 E7 82 80 20 38 E0 80 80 80 8A 80 80 81 " +
        "80 0E 35 F1 82 80 20 38 E0 80 80 80 FF 80 80 81 80 0E 35 FA 82 80 20 38 E0 80 80 80 F8 80 80 81 80 0E 35 FB 82 80 50 00 A0 82 B1 80 80 9C 8A 80 " +
        "60 00 A0 80 8B 80 01 FA 09 B8 01 95 80 50 00 A0 81 B0 80 7D ED 7A D5 EB 80 20 00 A0 8A 80 7E B8 06 DC 97 80 00 00 A0 80 01 C8 79 B4 EB 80 00 00 " +
        "A0 80 80 82 82 80 00 00 A0 80 80 8B 82 80 00 00 A0 80 80 BD 82 80";    

    private static final String EXP_msg2 = 
        "[3574681] [1]1128=9|35=X|49=CME|34=3574681|52=20120403-13:28:46.086|75=20120403|268=17|279=0|269=2|22=8|48=27069|83=5163734|270=141200.0|273=132846000|" +
        "271=3|451=-50.0|1020=236257|5797=1|5799=1|279=0|269=2|22=8|48=27069|83=5163735|270=141200.0|273=132846000|271=1|451=-50.0|1020=236258|5797=1|279=0|" +
        "269=2|22=8|48=27069|83=5163736|270=141200.0|273=132846000|271=1|451=-50.0|1020=236259|5797=1|279=0|269=2|22=8|48=27069|83=5163737|270=141200.0|" +
        "273=132846000|271=1|451=-50.0|1020=236260|5797=1|279=0|269=2|22=8|48=27069|83=5163738|270=141200.0|273=132846000|271=1|451=-50.0|1020=236261|5797=1|" +
        "279=0|269=2|22=8|48=27069|83=5163739|270=141200.0|273=132846000|271=1|451=-50.0|1020=236262|5797=1|279=0|269=2|22=8|48=27069|83=5163740|270=141200.0|" +
        "273=132846000|271=10|451=-50.0|1020=236272|5797=1|279=0|269=2|22=8|48=27069|83=5163741|270=141200.0|273=132846000|271=9|451=-50.0|1020=236281|5797=1|" +
        "279=0|269=2|22=8|48=27069|83=5163742|270=141200.0|273=132846000|271=1|451=-50.0|1020=236282|5797=1|279=2|1023=1|269=1|22=8|48=27069|83=5163743|270=141200.0|" +
        "273=132846000|271=28|346=9|336=2|279=0|1023=10|269=1|22=8|48=27069|83=5163744|270=141450.0|273=132846000|271=1235|346=157|336=2|279=1|1023=1|269=0|22=8|" +
        "48=27069|83=5163745|270=141175.0|273=132846000|271=552|346=263|336=2|279=1|1023=9|269=0|22=8|48=27069|83=5163746|270=140975.0|273=132846000|271=1411|" +
        "346=285|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163747|270=141175.0|273=132846000|271=567|346=391|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163748|" +
        "270=141175.0|273=132846000|271=568|346=392|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163749|270=141175.0|273=132846000|271=578|346=393|336=2|279=1|1023=1|" +
        "269=0|22=8|48=27069|83=5163750|270=141175.0|273=132846000|271=638|346=394|336=2|";

    private static final String msg3 = 
        "00 36 99 F9 01 C0 E7 01 5A 33 F9 23 5E 6C 66 4B 69 07 AD 09 4C 06 D3 83 76 78 C0 80 80 B2 80 01 53 BD 02 3B 3F FE 08 4E F7 3F 33 06 C8 84 80 80 81 7F " +
        "B5 0E 4B 85 83 20 38 E0 80 80 80 83 80 80 81 80 0E 4B 8A 83 80 50 00 A0 81 B0 80 80 02 E9 00 E7 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    private static final String EXP_msg3 = 
        "[3578361] [1]1128=9|35=X|49=CME|34=3578361|52=20120403-13:29:57.613|75=20120403|268=3|279=0|269=2|22=8|48=27069|83=5169150|270=141175.0|273=132957000|" +
        "271=3|451=-75.0|1020=238980|5797=2|5799=1|279=0|269=2|22=8|48=27069|83=5169151|270=141175.0|273=132957000|271=5|451=-75.0|1020=238985|5797=2|279=1|1023=1|" +
        "269=0|22=8|48=27069|83=5169152|270=141175.0|273=132957000|271=365|346=102|336=2|";

    private static final String msg4 = "00 36 9A DC 01 DC E3 01 5A 35 DC 23 5E 6C 66 4B 6B 52 D9 5A AA 09 4C 06 D3 80 81 07 56 E8 80 80 80 80";
    private static final String EXP_msg4 = "[3578588] [1]1128=9|35=f|49=CME|34=3578588|52=20120403-13:30:00.025|48=11561|22=8|75=20120403|333=125800.0|"; 
    
    
    private static final String msg5 = "00 36 8B E2 01 C0 D3 01 5A 17 E2 23 5E 6C 66 4B 62 20 90 09 4C 06 D3 90 BE 84 B1 3F 2C 23 B0 01 53 BD 02 3B 16 DB 08 4F F4 0D 9C 01 F2 B0 82 B0 7F 83 79 C0 7F B1 A0 82 80 FF FF E6 80 81 4F 81 03 0A A2 76 85 79 AE 7E DF E0 81 83 CE 81 80 B6 82 B1 01 53 BD 02 3B 16 DE 0A F8 08 D9 01 AE E6 82 81 4F 81 03 0A A4 76 85 77 A6 7E D2 C0 81 B2 8A 81 B6 81 B0 01 53 BD 02 3B 16 DF 09 97 B6 90 A0 81 80 81 81 A0 81 80 85 81 80 E7 06 8B 01 8D B0 81 B1 B2 05 83 CC B0 87 B0 7E D1 7F B5 00 CC A0 81 01 96 75 BE 7E DC 80 E7 06 81 01 8B";
    private static final String EXP_msg5 = "[3574754] [1]1128=9|35=X|49=CME|34=3574754|52=20120403-13:28:46.096|75=20120403|268=16|279=1|1023=4|269=1|273=132846000|22=8|48=27069|83=5163867|270=141300.0|271=1692|346=242|336=2|279=1|1023=2|269=0|273=132846000|22=8|48=27069|83=5163868|270=141175.0|271=860|346=16547|336=2|279=1|1023=2|269=0|273=132846000|22=8|48=27069|83=5163869|270=141175.0|271=859|346=16674|336=2|279=0|1023=1|269=0|273=132846000|22=8|48=10113|83=50466|270=139900.0|271=9|346=32897|336=2|279=1|1023=3|269=0|273=132846000|22=8|48=10113|83=50467|270=139850.0|271=10|346=32897|336=2|279=1|1023=2|269=1|273=132846000|22=8|48=27069|83=5163870|270=141250.0|271=1123|346=33071|336=2|279=2|1023=1|269=1|273=132846000|22=8|48=10113|83=50468|270=139975.0|271=9|346=49281|336=2|279=1|1023=2|269=1|273=132846000|22=8|48=10113|83=50469|270=140025.0|271=19|346=49282|336=2|279=1|1023=1|269=0|273=132846000|22=8|48=27069|83=5163871|270=141200.0|271=73|346=49298|336=2|279=1|1023=1|269=0|273=132846000|22=8|48=27069|83=5163872|270=141200.0|271=74|346=49299|336=2|279=1|1023=1|269=0|273=132846000|22=8|48=27069|83=5163873|270=141200.0|271=79|346=49300|336=2|279=1|1023=2|269=0|273=132846000|22=8|48=27069|83=5163874|270=141175.0|271=858|346=49441|336=2|279=1|1023=1|269=1|273=132846000|22=8|48=27069|83=5163875|270=141225.0|271=1501|346=49517|336=2|279=1|1023=7|269=0|273=132846000|22=8|48=27069|83=5163876|270=141050.0|271=1426|346=49593|336=2|279=1|1023=1|269=0|273=132846000|22=8|48=27069|83=5163877|270=141200.0|271=80|346=65813|336=2|279=1|1023=2|269=0|273=132846000|22=8|48=27069|83=5163878|270=141175.0|271=849|346=65952|336=2|";

    private static final String msg6 = "00 98 A9 FC 01 80 04 62 53 FC 5F 64 1B 36 36 33 BE 80 81 2F 80 83 3F 2C 33 80 01 53 BD 3D 3F DF 81 1C 66 34 B2 07 ED 01 A9 A7 AF F8 01 C1 45 1A 24 F8 02 AA C8 75 8E 80 80 51 10 80 81 B0";
    private static final String EXP_msg6 = "[10004988] [1]1128=9|35=X|49=CME|34=10004988|52=42125775-42:86:52|75=0|268=1|279=1|1023=3|269=0|273=132848000|22=8|48=27069|83=1007583|270=603981300.0|271=1005|346=169|336=2|";
    
    private static final String msg7 = "00 00 00 06 01 5F F3 CF 86 23 5E 6C 66 0C 61 7A B5 90 83 86 09 4B 3C C5 3F 35 56 C1 83 00 4E 96 81 81 7A F6 B1 81 00 60 BE 81 7F 15 AE 45 D3 45 D3 45 53 55 32 2D 45 53 5A B2 01 6D AE 46 4D 49 58 53 D8 B5 58 43 4D C5 80 81 80 80 83 B0 81 80 55 53 C4 82 27 89 55 53 C4 80 82 80 8A C6 45 D1 80 35 B0 B0 85 C0 98 B1 C0 80 B4 C0 80 31 B1 C0 80 31 B4 0C 23 FA 81 85 81 80 80 80 81 83 30 CC 81 1A A6 80 B2 80 80 80 80 80 34 DB 80 80 B7 80 80 80 80 43 54 52 43 D4 80 80 80 80 80 09 4C 06 D3 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String EXP_msg7 = "[6] [1]1128=9|35=d|49=CME|34=6|52=20120403-00:07:20.693|911=15|864=2|865=5|866=20110916|1145=133000000|865=7|866=20120921|1145=133000000|1150=-650.0|731=1|1149=12350.0|1148=-13650.0|1151=ES|55=ES|107=ESU2-ESZ2|48=30381|22=8|461=FMIXSX|462=5|207=XCME|9850=0.0|827=2|1147=0|202=0.0|947=USD|562=1|1140=5000|15=USD|1141=1|1022=GBX|264=10|1142=F|762=EQ|1143=50|1144=0|870=4|871=24|872=1|871=24|872=4|871=24|872=11|871=24|872=14|200=201209|969=5.0|1146=0.0|9787=0.01|555=2|600=[N/A]|623=1|602=3366|603=8|624=2|600=[N/A]|623=1|602=10113|603=8|624=1|1180=7|996=CTRCT|5796=20120402|";
    
    private static final String msg8 = "00 00 00 08 01 C0 D0 88 23 5E 6C 66 4B 61 27 BE 01 5A 15 8A 8F 02 D2 82 28 75 AE 92 94 F4 B0 81 7B 99 04 D8 80 87 80 FC B0 81 FB 7D FE 80 83 8B 80 FC B0 81 FB 7F 92 80 84 86 80 F8 B0 81 FB CE 80 85 80 FC B0 81 FB DD 80 86 89 80 FC B0 81 FB 7F A1 80 87 87 80 FC B0 81 FB E7 80 88 86 80 FC B0 81 FB FB 80 89 85 80 FC B0 81 FB 81 80 8A 83 80 FC B0 81 FB EC 80 8B 82 80 FC B1 81 B2 0C 84 80 82 87 80 FC B1 81 85 78 C5 80 83 88 80 FC B1 81 85 F6 80 84 85 80 FC B1 81 85 7C BE 80 85 84 80 FC B1 81 85 C9 80 86 83 80 FC B1 81 02 B1 EC 80 87 82 80 F8 B1 81 06 E6 D3 80 88 80 FC B7 81 76 D0 80 80 80 80 80 F0 B8 81 80 80 80 80 B1 81 80 80 80 83 81 FB 80 F0 B8 81 7C A5 80 80 80 F0 CE 81 03 F4 80 80 80 F0 CF 81 7B DA 80 80 80 B3 81 03 90 80 80 B3 B0";
    private static final String EXP_msg8 = "[8] [1]35=W|1128=9|49=CME|34=8|52=20120403-13:28:30.654|369=3574410|911=15|83=338|1021=2|48=670382|22=8|1682=17|268=20|269=0|270=-615.0|271=599|1023=1|346=6|269=0|270=-620.0|271=341|1023=2|346=10|269=0|270=-625.0|271=231|1023=3|346=5|269=0|270=-630.0|271=181|1023=4|346=5|269=0|270=-635.0|271=146|1023=5|346=8|269=0|270=-640.0|271=51|1023=6|346=6|269=0|270=-645.0|271=26|1023=7|346=5|269=0|270=-650.0|271=21|1023=8|346=4|269=0|270=-655.0|271=21|1023=9|346=2|269=0|270=-660.0|271=1|1023=10|346=1|269=1|270=-610.0|271=1540|1023=1|346=6|269=1|270=-605.0|271=585|1023=2|346=7|269=1|270=-600.0|271=575|1023=3|346=4|269=1|270=-595.0|271=125|1023=4|346=3|269=1|270=-590.0|271=70|1023=5|346=2|269=1|270=-285.0|271=50|1023=6|346=1|269=1|270=585.0|271=5|1023=7|346=1|269=7|270=-615.0|269=8|270=-615.0|269=2|270=-615.0|1020=2|451=-5.0|";

    private static final String msg9 = "00 00 00 07 01 C0 D0 87 23 5E 6C 66 4B 61 22 CE 01 5A 15 84 8F 07 47 BA 82 1A A6 92 99 F4 B0 81 08 4A 86 BD 80 84 80 FC B0 81 E7 85 80 83 86 80 FC B0 81 E7 DF 80 84 85 80 FC B0 81 E7 FC 80 85 84 80 FC B0 81 E7 FD 80 86 83 80 F8 B0 81 E7 81 80 87 80 F8 B0 81 E7 81 80 88 80 F8 B0 81 E7 02 AB 80 89 80 FC B0 81 E7 7D D4 80 8A 82 80 F8 B0 81 E7 81 80 8B 80 FC B1 81 02 93 AB 80 82 86 80 F8 B1 81 99 FE 80 83 80 F8 B1 81 99 81 80 84 80 FC B1 81 99 DD 80 85 84 80 F8 B1 81 99 FE 80 86 80 FC B1 81 99 FF 80 87 83 80 FC B1 81 99 FE 80 88 82 80 F8 B1 81 99 81 80 89 80 F8 B1 81 99 81 80 8A 80 FC B1 81 99 02 AD 80 8B 83 80 FC B7 81 7F 9C 80 80 80 80 80 F0 B8 81 7C A5 80 80 80 F0 CE 81 03 F4 80 80 80 F0 CF 81 7B DA 80 80 80 B3 81 03 90 80 80 B3 B0 81 CE 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
    private static final String EXP_msg9 = "[7] [1]35=W|1128=9|49=CME|34=7|52=20120403-13:28:30.030|369=3574404|911=15|83=123834|1021=2|48=3366|22=8|1682=17|268=25|269=0|270=140550.0|271=60|1023=1|346=3|269=0|270=140525.0|271=64|1023=2|346=5|269=0|270=140500.0|271=31|1023=3|346=4|269=0|270=140475.0|271=27|1023=4|346=3|269=0|270=140450.0|271=24|1023=5|346=2|269=0|270=140425.0|271=24|1023=6|346=2|269=0|270=140400.0|271=24|1023=7|346=2|269=0|270=140375.0|271=322|1023=8|346=2|269=0|270=140350.0|271=22|1023=9|346=1|269=0|270=140325.0|271=22|1023=10|346=1|269=1|270=140600.0|271=64|1023=1|346=5|269=1|270=140625.0|271=62|1023=2|346=5|269=1|270=140650.0|271=62|1023=3|346=5|269=1|270=140675.0|271=27|1023=4|346=3|269=1|270=140700.0|271=25|1023=5|346=3|269=1|270=140725.0|271=24|1023=6|346=2|269=1|270=140750.0|271=22|1023=7|346=1|269=1|270=140775.0|271=22|1023=8|346=1|269=1|270=140800.0|271=22|1023=9|346=1|269=1|270=140825.0|271=322|1023=10|346=2|269=7|270=140725.0|269=8|270=140250.0|269=N|270=140750.0|269=O|270=140200.0|269=2|270=140600.0|1020=50|274=0|451=-50.0|";


    // fails but due to bad encoding ?
    // private static final String msg7 = "08 3E EA BD 01 80 41 7B 55 BD 16 36 12 22 C9 80 83 3F 80 81 B1 3F 2C 33 80 01 53 BD 16 D8 82 0B 84 03 ED 9B 20 80 86 08 45 89 0B C6 01 A9 21 80 81 82 77 3A F7 74 BE 7E D8 82 0A CF 01 B2 80 71 10 80 80 80 B2 80 82 FE 75 8E 80 80 21 10 80 80 80 82 80 FF";
    
    
    private MetaTemplates meta = new MetaTemplates();
    final FastFixDecodeBuilder decoder = new FastFixDecodeBuilder();
    final PresenceMapReader pMap = new PresenceMapReader();
    final ReusableString destFixMsg = new ReusableString();
    private final Dictionaries              _dictionaries = new Dictionaries();
    private final IntMap<FastFixToFixReader>_readers = new IntHashMap<FastFixToFixReader>( 200, 0.75f );
    
    
    @Override
    public void setUp() {
        loadTemplates();
    }
    
    public void testFailedMsg1() {
        doTest( EXP_msg1, msg1, 0 );
    }

    public void testFailedMsg2() {
        doTest( EXP_msg2, msg2, 0 );
    }

    public void testFailedMsg3() {
        doTest( EXP_msg3, msg3, 0 );
    }

    public void testFailedMsg4() {
        doTest( EXP_msg4, msg4, 0 );
    }

    public void testFailedMsg5() {
        doTest( EXP_msg5, msg5, 0 );
    }

    public void testFailedMsg6() {
        doTest( EXP_msg6, msg6, 83 );
    }

    public void testFailedMsg7() {
        doTest( EXP_msg7, msg7, 79 );
    }

    public void testFailedMsg8() {
        doTest( EXP_msg8, msg8, 80 );
    }

    public void testFailedMsg9() {
        doTest( EXP_msg9, msg9, 80 );
    }

    private void doTest( String expMsg, String hexMsg, int copyTemplateId ) {
        byte[] binaryMsg = HexUtils.hexStringToBytes( hexMsg );
        byte[] buf = new byte[8192];
        System.arraycopy( binaryMsg, 0, buf, 0, binaryMsg.length );
        
        final UIntMandReaderCopy templateIdReader = new UIntMandReaderCopy( "TemplateId", 0, copyTemplateId );

        int numBytes = binaryMsg.length;

        destFixMsg.reset();
        
        decoder.start( buf, 0, numBytes );

        // 4 byte sequence , 1 byte channel
        
        int hdrSeqNum = decoder.decodeSeqNum();
        byte channel = decoder.decodeChannel();
        
        destFixMsg.append( '[' ).append( hdrSeqNum ).append( "] [" ).append( (int)channel ).append( ']' );
        
        pMap.readMap( decoder );
        
        int templateId = templateIdReader.read( decoder, pMap ); // its only two bytes but int decoder is fine
        
        FastFixToFixReader reader = getReader( templateId );

        if ( reader != null ) {
            System.out.println( reader.getMetaTemplate().toString() );
            
            reader.reset();

            try {
                reader.read( decoder, pMap, destFixMsg );
            } catch( Exception e ) {
                e.printStackTrace();
            }
            System.out.println( destFixMsg );
            assertEquals( expMsg, destFixMsg.toString() );
        } else {
            fail( "No reader available" );
        }
    }

    private FastFixToFixReader getReader( int templateId ) {
        MetaTemplate mt = meta.getTemplate( templateId );
        if ( mt == null ) return null;
        
        FastFixToFixReader reader = _readers.get( templateId );
        
        if ( reader == null ) {
            reader = new FastFixToFixReader( mt, "T" + templateId, templateId, (byte) '|' );
            
           _readers.put( templateId, reader );
           
           reader.init( _dictionaries.getMsgTypeDictComponentFactory( mt.getDictionaryId() ) );
        }
        
        return reader;
    }
    
    private void loadTemplates() {
        TemplateClassRegister reg = new FastFixTemplateClassRegister();
        
        XMLFastFixTemplateLoader l = new XMLFastFixTemplateLoader( "data/cme/templates.xml" );
        
        l.load( reg, meta );
    }
}
