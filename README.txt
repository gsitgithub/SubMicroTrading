My name is Richard Rose and I am the author of the SubMicroTrading framework. 

SubMicroTrading is a highly concurrent component based algo trading framework almost 5 years in the making that I am preparing for open source with a target date August 2015. Includes components for various market data and trading sessions including ETI, UTP, Millenium, Fix, FastFix, CME MDP.  

I worked for over a year non stop building the framework and it has pretty much consumed all my spare evening and weekends since. 

I love technology and taught myself 6502 assembler back in 1980. I have worked on compilers, real time control systems, telecoms and for the last 18 years investment banks. Alas I am a lousy marketer.   SubMicroTrading originally targetted ultra low latency OMS and exchange connectivity space. But the advent of sponsored access allowing institutions direct connectivity to an exchange pretty much killed the need for that product. The next step in evolution was to implement market data handlers and add support for algorithmic trading. I had hoped that this would be used in colocation for algo trading but while technically ready that alas never got of the ground for other reasons.

I have seen alot of "stuff" written by C++ experts claiming that java cannot be used for ultra low latency. Just because they dont know how doesnt mean its not possible. I have programmed in C and C++ since the 80's and love both languages. My main preferences for Java over C++ are development time and ease of hiring devs, the IDE's are more mature and productive and I make extensive use of RTTI for bootstrapping. 

That said when you are talking about a whole application, the deciding factor on whats fastest is far more down to design ie threading models (concurrency patterns) and memory models (pooling patterns). People that believe single threaded apps are fastest are I believe taking a simplistic view. Exchanges dont have constant traffic, and when burst rate exceeds the rate that a single thread can process an event then concurrent processing should be faster. Please see "Single Threaded Hidden Message Latency" on http://submicrotrading.com/threading-models.html

By open sourcing SubMicroTrading I aim to prove just how good java is for ultra low latency algo trading. 

The source is released under a the Apache 2.0 license. You can opt for a support subscription please see www.SubMicroTrading.com or contact Low Latency Trading support@submicrotrading.com for more details.

	
	Sample benchmark, replaying CME fast fix using tcpreplay at max replay, measured in an independent lab using TipOff.
	
	4 micros average tick to trade, wire to wire at 800,000 ticks/second 
	(2 micros java process internal time)
	
	Its pretty amazing to run the market data back using tcpreplay, the trading application and exchange simulator all on a low power laptop.  
	To see the true power run on tuned CentOS linux with custom NIO and thread affinity configured.
	
	Follow the blog about techniques used to build the system or register on the website for confirmation on the open launch.

		http://submicro.blogspot.com/
		
		http://www.SubMicroTrading.com
	
	

Whats included

"Proper" Object Orientated code with all the benefits that brings... eg can navigate from order to instrument to exchange to exchange session

Current model and generated codecs including ETI, UTP, Millenium, Fix, FastFix, CME MDP
All market data and exchange codecs convert from external wire format to normalised common internal POJO events
Possibly fastest standard Fix engine on planet
Possibly fastest FastFix implementation on planet
Possibly fastest log engine on planet
Possibly fastest memory mapped index paged persistence
Possible fastest Order Management System (OMS) including Trade Corrections/Cancels on planet
Custom exchange session engines for ETI, UTP, Millenium, Fix, FastFix
Exchange trading simulator (works with any of the generated codecs like ETI)
Complete core of SubMicroTrading including thread core affinity
Component architecture for easy configuration of flow pipelines
Ability to extend and customise the source code of any component
CME dynamic on the fly session generation
Book Manager and Book Conflation for optimal concurrent update processing
Exchange and market data agnostic Algo container
STAC-T1 Benchmark Strategy  

Whats excluded

Encoder/Decoder and model generator ... warning note the exchange protocols have probably changed since I last updated the model 
Custom spread strategy (real example which shows how to write ultra low latency strategy)


Getting Started

This is a project for experienced developers, its not an off the shelf product that just plug in and run. 

It would of been nice to include the few third party jars that I use as well as the exchange instrument static I have been using, but this isnt permissible.
So afraid there is some standard stuff for you to download before you can run anything

The project was developed just by me, and I used teamcity and ant as I really dont like Maven

See my blog for more information and examples http://submicro.blogspot.co.uk

1) download required third party jars

SubMicroTrading/Core/exlib/jars

-rwxr--r--+ 1 Richard None  571104 Nov  9  2014 javax.mail.jar
-rwxr--r--+ 1 Richard None  102394 May 27  2014 jmxtools.jar
-rwxr--r--+ 1 Richard None  237047 May 27  2014 junit-4.8.1.jar
-rwxr--r--+ 1 Richard None  481534 May 27  2014 log4j-1.2.16.jar
-rwxr--r--+ 1 Richard None 1229289 May 27  2014 xercesImpl.jar
-rwxr--r--+ 1 Richard None  194354 May 27  2014 xml-apis.jar

SubMicroTrading/Core/exlib/testJars

-rwxr--r--+ 1 Richard None  45024 May 27  2014 hamcrest-core-1.3.jar
-rwxr--r--+ 1 Richard None 245039 May 27  2014 junit-4.11.jar

2) Setup new eclipse workspace (even if you use IntelliJ pls start with eclipse to get correct warning settings)
   Set tab to 4 spaces conversion ON

3) import existing projects into new workspace ... this sets up coding standards and compiler warning/error levels
   
4) All should compile, zero warnings

5) Run unit tests in Core / OM .... note smtopt unit tests were covered by implementation of first real strategy in the strategy project which is excluded from the open source offering.

6) Import the eclipse launchers from the ./launchers directory 

Note SubMicroTrading used to use hardwired bootstrapping (extending BaseSMTMain). This is old style and new programs now use the AntiSpring property bootstrap. I have used Spring for many years and some huge projects and I really dislike it. For ultra low latency you dont want indirect proxies and can do without the pain of innocuous error messages.

7) Check the test GUI runs ... run the JPanelBlotterGUI launcher ... it includes a main purely for test use, try double click a price (should bring up ticket), right click for book in popup

8) Try STAC-T1 benchmark 

Run the "T1 - CME exchange sim"
Run the "T1 - SMT"

Check the log file and that the T1 algo client fix session connects to the exchange simulator server fix session.
run the T1 benchmark by replaying canned market data with tcpreplay

... try running the JMX admin command and injecting a fix order

9) Project overview

Core

Heart of all SubMicroTrading components with collections, thread multiplexing, core session code, base for generated classes like AbstractFixDecoder, fast fix implementation, entities references in generated code like Instrument
Look at ReusableString, RingBufferMsgQueue1C1P, SuperPoolManager, IndexPersister, AbstractControlThread, AntiSpringBootstrap

Generated

Pojos for internal model .... eg NewOrderSingle (ClientNewOrderSingleImpl, MarketNewOrderSingleImpl, RecoveryNewOrderSingle ... future blog will explain differences)
Codecs for translation between internal pojo and external wire format ... look at implementations of Encoder, Decoder interfaces eg CMEFastFixDecoder, ETIBSEEncoder

OM

Full market order management system and exchange session customisations (OMS + line handlers)
Also contains first implementation of T1Algo 
See CMEOnDemandFastFixSessionBuilder for dynamic CME session management .... create sessions on the fly as needed

SMTOpt

Optional package with simplest NIO socket implementation with no thread safety and no temporary object creation.

CoreStrats

Exchange agnostic algo / strategy container .... can rapidly prototype ultra low latency strategies without worrying about exchange specific idiosyncrasies.
MKtDataController and Book

Scalability via the highly concurrent MarketDataController and Book snapping.
In async trading application you must protect against the bid/ask changing mid read .... the book snapping will occur at most once per thread ... and is driven by consumption so you will skip old tick updates

See the "ALGO T1 (New Bootstrap)" for example first algo container strategy implementation 
note this will be slower than the T1 - SMT bencmark class as its fully thread safe and intended for highly concurrent strategies.

See algoT1.properties, StrategyT1, CMEMarketDataControllerLoader, MarketDataController


10) download hwloc

If you want to get the best benefit then compile on windows and use the dist script to produce a tar to install on linux
Install hwloc on linux ... you dont need it for windows development only to get core thread affinity


11) build linux libraries 

Best performance requires two linux libraries, the Core/Native library and the optional smtopt library

build smtopt library ... contains alternative NIO implementation with no thread safety or temp object creation .... 

Building smtopt library, you will need to setup some header files from openjdk (I was using open jdk 1.6)

smtopt/native/core/sun

jlong.h
jlong_md.h
jvm.h
nio.h
typedefs.h
typedefs_md.h
jvm_md.h
jni_util.h
net_util.h
net_util_md.h
nio_util.h

You can generate the following :-

java_net_NetworkInterface.h
java_net_SocketOptions.h
sun_nio_ch_IOStatus.h

With :-

javah -force -classpath ..\..\bin sun.nio.ch.IOStatus
javah -force -classpath ..\..\bin java.net.SocketOptions
javah -force -classpath ..\..\bin java.net.NetworkInterface

Then run the makefile to create the library and copy it to LDD path ... sample shell scripts for setup are present and should be simple enough to work out.


NOTES

1) Coding Style

   please keep the same coding style (spacing, tab converted to 4 spaces, CR, underscore field prefix), the eclipse settings have been exported

   import EclipseCodeFormatterExport.xml into eclipse code format

2) ZERO warnings

   all projects should have ZERO warnings ... you should use the eclipse settings which I will put on a blog
   
   Note that "Deprecated and Restricted API :  Forbidden Reference"  must be set to IGNORE

Rules for Change Submission

Improvements are welcomed, but all code changes must

a) have unit test
b) be in the SubMicroTrading coding and naming style .... the code must look consistent

Changes that dont have unit test or are not in the coding standard will be dropped without any review.



