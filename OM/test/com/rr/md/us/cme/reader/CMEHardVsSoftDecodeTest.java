/*******************************************************************************
 * Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,  software distributed under the License 
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
package com.rr.md.us.cme.reader;

import com.rr.core.codec.Decoder;
import com.rr.core.codec.FixField;
import com.rr.core.codec.binary.fastfix.FastFixDecodeBuilder;
import com.rr.core.codec.binary.fastfix.PresenceMapReader;
import com.rr.core.codec.binary.fastfix.common.Dictionaries;
import com.rr.core.codec.binary.fastfix.msgdict.copy.int32.UIntMandReaderCopy;
import com.rr.core.lang.BaseTestCase;
import com.rr.core.lang.ReusableString;
import com.rr.core.lang.TimeZoneCalculator;
import com.rr.core.model.Message;
import com.rr.core.utils.HexUtils;
import com.rr.md.fastfix.XMLFastFixTemplateLoader;
import com.rr.md.fastfix.meta.MetaTemplate;
import com.rr.md.fastfix.meta.MetaTemplates;
import com.rr.md.fastfix.reader.FastFixToFixReader;
import com.rr.md.fastfix.template.FastFixTemplateClassRegister;
import com.rr.md.fastfix.template.TemplateClassRegister;
import com.rr.md.us.cme.FastFixTstUtils;
import com.rr.model.generated.fix.codec.MD44Decoder;
import com.rr.model.generated.internal.events.impl.MDIncRefreshImpl;


public class CMEHardVsSoftDecodeTest extends BaseTestCase {

    private static final MetaTemplates meta = new MetaTemplates();

    private static final String msg2 = 
        "00 36 8B 99 01 C0 E7 01 5A 17 99 23 5E 6C 66 4B 62 20 86 09 4C 06 D3 91 76 78 C0 80 80 B2 80 01 53 BD 02 3B 15 D6 08 4F 90 3F 2C 23 B0 84 80 80 " +
        "81 CE 0E 35 E2 82 20 38 E0 80 80 80 FE 80 80 81 80 0E 35 E3 82 80 20 38 E0 80 80 80 81 80 80 81 80 0E 35 E4 82 80 20 38 E0 80 80 80 81 80 80 81 " +
        "80 0E 35 E5 82 80 20 38 E0 80 80 80 81 80 80 81 80 0E 35 E6 82 80 20 38 E0 80 80 80 81 80 80 81 80 0E 35 E7 82 80 20 38 E0 80 80 80 8A 80 80 81 " +
        "80 0E 35 F1 82 80 20 38 E0 80 80 80 FF 80 80 81 80 0E 35 FA 82 80 20 38 E0 80 80 80 F8 80 80 81 80 0E 35 FB 82 80 50 00 A0 82 B1 80 80 9C 8A 80 " +
        "60 00 A0 80 8B 80 01 FA 09 B8 01 95 80 50 00 A0 81 B0 80 7D ED 7A D5 EB 80 20 00 A0 8A 80 7E B8 06 DC 97 80 00 00 A0 80 01 C8 79 B4 EB 80 00 00 " +
        "A0 80 80 82 82 80 00 00 A0 80 80 8B 82 80 00 00 A0 80 80 BD 82 80";    

    //    private static final String EXP_msg2 = 
    //        "[3574681] [1]1128=9|35=X|49=CME|34=3574681|52=20120403-13:28:46.086|75=20120403|268=17|279=0|269=2|22=8|48=27069|83=5163734|270=141200.0|273=132846000|" +
    //        "271=3|451=-50.0|1020=236257|5797=1|5799=1|279=0|269=2|22=8|48=27069|83=5163735|270=141200.0|273=132846000|271=1|451=-50.0|1020=236258|5797=1|279=0|" +
    //        "269=2|22=8|48=27069|83=5163736|270=141200.0|273=132846000|271=1|451=-50.0|1020=236259|5797=1|279=0|269=2|22=8|48=27069|83=5163737|270=141200.0|" +
    //        "273=132846000|271=1|451=-50.0|1020=236260|5797=1|279=0|269=2|22=8|48=27069|83=5163738|270=141200.0|273=132846000|271=1|451=-50.0|1020=236261|5797=1|" +
    //        "279=0|269=2|22=8|48=27069|83=5163739|270=141200.0|273=132846000|271=1|451=-50.0|1020=236262|5797=1|279=0|269=2|22=8|48=27069|83=5163740|270=141200.0|" +
    //        "273=132846000|271=10|451=-50.0|1020=236272|5797=1|279=0|269=2|22=8|48=27069|83=5163741|270=141200.0|273=132846000|271=9|451=-50.0|1020=236281|5797=1|" +
    //        "279=0|269=2|22=8|48=27069|83=5163742|270=141200.0|273=132846000|271=1|451=-50.0|1020=236282|5797=1|279=2|1023=1|269=1|22=8|48=27069|83=5163743|270=141200.0|" +
    //        "273=132846000|271=28|346=9|336=2|279=0|1023=10|269=1|22=8|48=27069|83=5163744|270=141450.0|273=132846000|271=1235|346=157|336=2|279=1|1023=1|269=0|22=8|" +
    //        "48=27069|83=5163745|270=141175.0|273=132846000|271=552|346=263|336=2|279=1|1023=9|269=0|22=8|48=27069|83=5163746|270=140975.0|273=132846000|271=1411|" +
    //        "346=285|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163747|270=141175.0|273=132846000|271=567|346=391|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163748|" +
    //        "270=141175.0|273=132846000|271=568|346=392|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163749|270=141175.0|273=132846000|271=578|346=393|336=2|279=1|1023=1|" +
    //        "269=0|22=8|48=27069|83=5163750|270=141175.0|273=132846000|271=638|346=394|336=2|";

    //1128=9|35=X|49=CME|34=3574681|52=20120403-13:28:46.086|75=20120403|268=17|
    //279=0|269=2|22=8|48=27069|83=5163734|270=141200.0|273=132846000|271=3|451=-50.0|1020=236257|5797=1|5799=1|
    //279=0|269=2|22=8|48=27069|83=5163735|270=141200.0|273=132846000|271=1|451=-50.0|1020=236258|5797=1|
    //279=0|269=2|22=8|48=27069|83=5163736|270=141200.0|273=132846000|271=1|451=-50.0|1020=236259|5797=1|
    //279=0|269=2|22=8|48=27069|83=5163737|270=141200.0|273=132846000|271=1|451=-50.0|1020=236260|5797=1|279=0|269=2|22=8|48=27069|83=5163738|270=141200.0|273=132846000|271=1|451=-50.0|1020=236261|5797=1|279=0|269=2|22=8|48=27069|83=5163739|270=141200.0|273=132846000|271=1|451=-50.0|1020=236262|5797=1|279=0|269=2|22=8|48=27069|83=5163740|270=141200.0|273=132846000|271=10|451=-50.0|1020=236272|5797=1|279=0|269=2|22=8|48=27069|83=5163741|270=141200.0|273=132846000|271=9|451=-50.0|1020=236281|5797=1|279=0|269=2|22=8|48=27069|83=5163742|270=141200.0|273=132846000|271=1|451=-50.0|1020=236282|5797=1|279=2|1023=1|269=1|22=8|48=27069|83=5163743|270=141200.0|273=132846000|271=28|346=9|336=2|279=0|1023=10|269=1|22=8|48=27069|83=5163744|270=141450.0|273=132846000|271=1235|346=157|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163745|270=141175.0|273=132846000|271=552|346=263|336=2|279=1|1023=9|269=0|22=8|48=27069|83=5163746|270=140975.0|273=132846000|271=1411|346=285|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163747|270=141175.0|273=132846000|271=567|346=391|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163748|270=141175.0|273=132846000|271=568|346=392|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163749|270=141175.0|273=132846000|271=578|346=393|336=2|279=1|1023=1|269=0|22=8|48=27069|83=5163750|270=141175.0|273=132846000|271=638|346=394|336=2|
    
    private static final String msg3 = 
        "00 36 99 F9 01 C0 E7 01 5A 33 F9 23 5E 6C 66 4B 69 07 AD 09 4C 06 D3 83 76 78 C0 80 80 B2 80 01 53 BD 02 3B 3F FE 08 4E F7 3F 33 06 C8 84 80 80 81 7F " +
        "B5 0E 4B 85 83 20 38 E0 80 80 80 83 80 80 81 80 0E 4B 8A 83 80 50 00 A0 81 B0 80 80 02 E9 00 E7 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    //    private static final String EXP_msg3 = 
    //        "[3578361] [1]1128=9|35=X|49=CME|34=3578361|52=20120403-13:29:57.613|75=20120403|268=3|279=0|269=2|22=8|48=27069|83=5169150|270=141175.0|273=132957000|" +
    //        "271=3|451=-75.0|1020=238980|5797=2|5799=1|279=0|269=2|22=8|48=27069|83=5169151|270=141175.0|273=132957000|271=5|451=-75.0|1020=238985|5797=2|279=1|1023=1|" +
    //        "269=0|22=8|48=27069|83=5169152|270=141175.0|273=132957000|271=365|346=102|336=2|";

    private static final String msg5 = "00 36 8B E2 01 C0 D3 01 5A 17 E2 23 5E 6C 66 4B 62 20 90 09 4C 06 D3 90 BE 84 B1 3F 2C 23 B0 01 53 BD 02 3B 16 DB 08 4F F4 0D 9C 01 F2 B0 82 B0 7F 83 79 C0 7F B1 A0 82 80 FF FF E6 80 81 4F 81 03 0A A2 76 85 79 AE 7E DF E0 81 83 CE 81 80 B6 82 B1 01 53 BD 02 3B 16 DE 0A F8 08 D9 01 AE E6 82 81 4F 81 03 0A A4 76 85 77 A6 7E D2 C0 81 B2 8A 81 B6 81 B0 01 53 BD 02 3B 16 DF 09 97 B6 90 A0 81 80 81 81 A0 81 80 85 81 80 E7 06 8B 01 8D B0 81 B1 B2 05 83 CC B0 87 B0 7E D1 7F B5 00 CC A0 81 01 96 75 BE 7E DC 80 E7 06 81 01 8B";
    //  private static final String EXP_msg5 = "[3574754] [1]1128=9|35=X|49=CME|34=3574754|52=20120403-13:28:46.096|75=20120403|268=16|279=1|1023=4|269=1|273=132846000|22=8|48=27069|83=5163867|270=141300.0|271=1692|346=242|336=2|279=1|1023=2|269=0|273=132846000|22=8|48=27069|83=5163868|270=141175.0|271=860|346=16547|336=2|279=1|1023=2|269=0|273=132846000|22=8|48=27069|83=5163869|270=141175.0|271=859|346=16674|336=2|279=0|1023=1|269=0|273=132846000|22=8|48=10113|83=50466|270=139900.0|271=9|346=32897|336=2|279=1|1023=3|269=0|273=132846000|22=8|48=10113|83=50467|270=139850.0|271=10|346=32897|336=2|279=1|1023=2|269=1|273=132846000|22=8|48=27069|83=5163870|270=141250.0|271=1123|346=33071|336=2|279=2|1023=1|269=1|273=132846000|22=8|48=10113|83=50468|270=139975.0|271=9|346=49281|336=2|279=1|1023=2|269=1|273=132846000|22=8|48=10113|83=50469|270=140025.0|271=19|346=49282|336=2|279=1|1023=1|269=0|273=132846000|22=8|48=27069|83=5163871|270=141200.0|271=73|346=49298|336=2|279=1|1023=1|269=0|273=132846000|22=8|48=27069|83=5163872|270=141200.0|271=74|346=49299|336=2|279=1|1023=1|269=0|273=132846000|22=8|48=27069|83=5163873|270=141200.0|271=79|346=49300|336=2|279=1|1023=2|269=0|273=132846000|22=8|48=27069|83=5163874|270=141175.0|271=858|346=49441|336=2|279=1|1023=1|269=1|273=132846000|22=8|48=27069|83=5163875|270=141225.0|271=1501|346=49517|336=2|279=1|1023=7|269=0|273=132846000|22=8|48=27069|83=5163876|270=141050.0|271=1426|346=49593|336=2|279=1|1023=1|269=0|273=132846000|22=8|48=27069|83=5163877|270=141200.0|271=80|346=65813|336=2|279=1|1023=2|269=0|273=132846000|22=8|48=27069|83=5163878|270=141175.0|271=849|346=65952|336=2|";

    //    MDIncRefreshImpl , sendingTime=20120403132846096, received=0, noMDEntries=16 {#1} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163867, numberOfOrders=242, mdPriceLevel=4, mdEntryType=Offer, mdEntryPx=141300.0, mdEntrySize=1692, mdEntryTime=132846000, tradingSessionID=TS3 {#2} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163868, numberOfOrders=16547, mdPriceLevel=2, mdEntryType=Bid, mdEntryPx=141175.0, mdEntrySize=860, mdEntryTime=132846000, tradingSessionID=TS3 {#3} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163869, numberOfOrders=16674, mdPriceLevel=2, mdEntryType=Bid, mdEntryPx=141175.0, mdEntrySize=859, mdEntryTime=132846000, tradingSessionID=TS3 {#4} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=10113, mdUpdateAction=New, repeatSeq=50466, numberOfOrders=32897, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=139900.0, mdEntrySize=9, mdEntryTime=132846000, tradingSessionID=TS3 {#5} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=10113, mdUpdateAction=Change, repeatSeq=50467, numberOfOrders=32897, mdPriceLevel=3, mdEntryType=Bid, mdEntryPx=139850.0, mdEntrySize=10, mdEntryTime=132846000, tradingSessionID=TS3 {#6} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163870, numberOfOrders=33071, mdPriceLevel=2, mdEntryType=Offer, mdEntryPx=141250.0, mdEntrySize=1123, mdEntryTime=132846000, tradingSessionID=TS3 {#7} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=10113, mdUpdateAction=Delete, repeatSeq=50468, numberOfOrders=49281, mdPriceLevel=1, mdEntryType=Offer, mdEntryPx=139975.0, mdEntrySize=9, mdEntryTime=132846000, tradingSessionID=TS3 {#8} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=10113, mdUpdateAction=Change, repeatSeq=50469, numberOfOrders=49282, mdPriceLevel=2, mdEntryType=Offer, mdEntryPx=140025.0, mdEntrySize=19, mdEntryTime=132846000, tradingSessionID=TS3 {#9} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163871, numberOfOrders=49298, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=141200.0, mdEntrySize=73, mdEntryTime=132846000, tradingSessionID=TS3 {#10} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163872, numberOfOrders=49299, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=141200.0, mdEntrySize=74, mdEntryTime=132846000, tradingSessionID=TS3 {#11} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163873, numberOfOrders=49300, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=141200.0, mdEntrySize=79, mdEntryTime=132846000, tradingSessionID=TS3 {#12} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163874, numberOfOrders=49441, mdPriceLevel=2, mdEntryType=Bid, mdEntryPx=141175.0, mdEntrySize=858, mdEntryTime=132846000, tradingSessionID=TS3 {#13} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163875, numberOfOrders=49517, mdPriceLevel=1, mdEntryType=Offer, mdEntryPx=141225.0, mdEntrySize=1501, mdEntryTime=132846000, tradingSessionID=TS3 {#14} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163876, numberOfOrders=49593, mdPriceLevel=7, mdEntryType=Bid, mdEntryPx=141050.0, mdEntrySize=1426, mdEntryTime=132846000, tradingSessionID=TS3 {#15} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163877, numberOfOrders=65813, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=141200.0, mdEntrySize=80, mdEntryTime=132846000, tradingSessionID=TS3 {#16} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163878, numberOfOrders=65952, mdPriceLevel=2, mdEntryType=Bid, mdEntryPx=141175.0, mdEntrySize=849, mdEntryTime=132846000, tradingSessionID=TS3, msgSeqNum=3574754, possDupFlag=N
    //    MDIncRefreshImpl , sendingTime=20120403132846096, received=0, noMDEntries=16 {#1} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163867, numberOfOrders=242, mdPriceLevel=4, mdEntryType=Offer, mdEntryPx=141300.0, mdEntrySize=1692, mdEntryTime=132846000, tradingSessionID=null {#2} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163868, numberOfOrders=16547, mdPriceLevel=2, mdEntryType=Bid, mdEntryPx=141175.0, mdEntrySize=860, mdEntryTime=132846000, tradingSessionID=null {#3} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163869, numberOfOrders=16674, mdPriceLevel=2, mdEntryType=Bid, mdEntryPx=141175.0, mdEntrySize=859, mdEntryTime=132846000, tradingSessionID=null {#4} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=10113, mdUpdateAction=New, repeatSeq=50466, numberOfOrders=32897, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=139900.0, mdEntrySize=9, mdEntryTime=132846000, tradingSessionID=null {#5} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=10113, mdUpdateAction=Change, repeatSeq=50467, numberOfOrders=32897, mdPriceLevel=3, mdEntryType=Bid, mdEntryPx=139850.0, mdEntrySize=10, mdEntryTime=132846000, tradingSessionID=null {#6} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163870, numberOfOrders=33071, mdPriceLevel=2, mdEntryType=Offer, mdEntryPx=141250.0, mdEntrySize=1123, mdEntryTime=132846000, tradingSessionID=null {#7} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=10113, mdUpdateAction=Delete, repeatSeq=50468, numberOfOrders=49281, mdPriceLevel=1, mdEntryType=Offer, mdEntryPx=139975.0, mdEntrySize=9, mdEntryTime=132846000, tradingSessionID=null {#8} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=10113, mdUpdateAction=Change, repeatSeq=50469, numberOfOrders=49282, mdPriceLevel=2, mdEntryType=Offer, mdEntryPx=140025.0, mdEntrySize=19, mdEntryTime=132846000, tradingSessionID=null {#9} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163871, numberOfOrders=49298, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=141200.0, mdEntrySize=73, mdEntryTime=132846000, tradingSessionID=null {#10} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163872, numberOfOrders=49299, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=141200.0, mdEntrySize=74, mdEntryTime=132846000, tradingSessionID=null {#11} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163873, numberOfOrders=49300, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=141200.0, mdEntrySize=79, mdEntryTime=132846000, tradingSessionID=null {#12} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163874, numberOfOrders=49441, mdPriceLevel=2, mdEntryType=Bid, mdEntryPx=141175.0, mdEntrySize=858, mdEntryTime=132846000, tradingSessionID=null {#13} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163875, numberOfOrders=49517, mdPriceLevel=1, mdEntryType=Offer, mdEntryPx=141225.0, mdEntrySize=1501, mdEntryTime=132846000, tradingSessionID=null {#14} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163876, numberOfOrders=49593, mdPriceLevel=7, mdEntryType=Bid, mdEntryPx=141050.0, mdEntrySize=1426, mdEntryTime=132846000, tradingSessionID=null {#15} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163877, numberOfOrders=65813, mdPriceLevel=1, mdEntryType=Bid, mdEntryPx=141200.0, mdEntrySize=80, mdEntryTime=132846000, tradingSessionID=null {#16} MDEntryImpl , securityIDSource=ExchangeSymbol, securityID=27069, mdUpdateAction=Change, repeatSeq=5163878, numberOfOrders=65952, mdPriceLevel=2, mdEntryType=Bid, mdEntryPx=141175.0, mdEntrySize=849, mdEntryTime=132846000, tradingSessionID=null, msgSeqNum=3574754, possDupFlag=N

    private CMEFastFixDecoder  _decoder   = new CMEFastFixDecoder( "CMETstReader", "data/cme/templates.xml", -1, true );

    private String             _dateStr = "20120403";
    private TimeZoneCalculator _calc;

    private final Dictionaries _dictionaries = new Dictionaries();
    private final FastFixDecodeBuilder  _binDecodeBuilder   = new FastFixDecodeBuilder();
    private final PresenceMapReader     _pMap               = new PresenceMapReader();
    private final ReusableString        _destFixMsg         = new ReusableString();
    
    private final UIntMandReaderCopy    _templateIdReader = new UIntMandReaderCopy( "TemplateId", 0, 0 );

    private final Decoder               _softDecoder    = new MD44Decoder();
    
    
    @Override
    public void setUp(){
        _calc = new TimeZoneCalculator();
        _calc.setDate( _dateStr );
        _decoder.setTimeZoneCalculator( _calc );
        
        TemplateClassRegister reg = new FastFixTemplateClassRegister();
        XMLFastFixTemplateLoader l = new XMLFastFixTemplateLoader( "data/cme/templates.xml" );
        l.load( reg, meta );
    }
    
    public CMEHardVsSoftDecodeTest() {
        // nothing
    }

    public void testMsg2() {
        doTest( msg2 );
    }
    
    public void testMsg3() {
        doTest( msg3 );
    }
        
    public void testMsg5() {
        doTest( msg5 );
    }
    
    public void doTest( String hexMsg ) {
        
        MDIncRefreshImpl softInc = softDecode( hexMsg );
        MDIncRefreshImpl hardInc = hardDecode( hexMsg );
        
        System.out.println( "SOFT : " + softInc.toString() );
        System.out.println( "HARD : " + hardInc.toString() );
        
        FastFixTstUtils.checkEqualsA( softInc, hardInc );
    }

    private MDIncRefreshImpl hardDecode( String hexMsg ) {
        byte[] binaryMsg = HexUtils.hexStringToBytes( hexMsg );
        byte[] buf = new byte[8192];
        System.arraycopy( binaryMsg, 0, buf, 0, binaryMsg.length );
        
        int numBytes = binaryMsg.length;

        return (MDIncRefreshImpl) _decoder.decode( buf, 0, numBytes );
    }

    private MDIncRefreshImpl softDecode( String hexMsg ) {
        byte[] binaryMsg = HexUtils.hexStringToBytes( hexMsg );
        byte[] buf = new byte[8192];
        System.arraycopy( binaryMsg, 0, buf, 0, binaryMsg.length );
        
        int numBytes = binaryMsg.length;

        _destFixMsg.reset();
        
        _binDecodeBuilder.start( buf, 0, numBytes );

        // 4 byte sequence , 1 byte channel
        
        _binDecodeBuilder.decodeSeqNum();
        _binDecodeBuilder.decodeChannel();

        _pMap.readMap( _binDecodeBuilder );
        
        int templateId = _templateIdReader.read( _binDecodeBuilder, _pMap ); 
        
        FastFixToFixReader reader = getReader( templateId );

        Message m = null;
        
        if ( reader != null ) {
            reader.reset();

            reader.read( _binDecodeBuilder, _pMap, _destFixMsg );
            
            m = _softDecoder.decode( _destFixMsg.getBytes(), 0, _destFixMsg.length() );
        } 

        return (MDIncRefreshImpl) m;
    }

    private FastFixToFixReader getReader( int templateId ) {
        MetaTemplate mt = meta.getTemplate( templateId );
        if ( mt == null ) return null;

        System.out.println( "TEMPLATE #" + templateId );
        
        FastFixToFixReader reader = new FastFixToFixReader( mt, "T" + templateId, templateId, FixField.FIELD_DELIMITER );
            
        reader.init( _dictionaries.getMsgTypeDictComponentFactory( mt.getDictionaryId() ) );
        
        return reader;
    }
}
