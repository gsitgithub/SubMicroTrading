#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
#!/bin/bash

typeset -x LD_LIBRARY_PATH=./lib:/home/smt/tools/jdk1.6.0_27/lib:/home/smt/tools/hwloc/lib:/home/smt/tools/jdk1.6.0_27/jre/lib/amd64

source systest.sh

#LOG_COMPILE="$JITTER -XX:LogFile=jit_client.txt"

# PROGRAM ARGS=========================================================================

# msgsToSend  delayUSEC NIC  cpuMask  spin{1|0}  disableLoopback{1|0}  debug{1|0}   qos  msgSize
PROG_ARGS="1000 1000 192.168.10.2 128 1 1 0 8 16"

# JVM  ARGS=========================================================================
#PROFILE="-agentpath:/home/smt/jprofiler6/bin/linux-x64/libjprofilerti.so=port=8849" 
#DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"


JVM_ARGS="$DEBUG $PROFILE $LOG_COMPILE -Xmx4g -Xms4g $BASE_JVM_ARGS"


setClasspath
echo $CLASSPATH

echo "RUN: $PRERUN java $JVM_ARGS -classpath $CLASSPATH com.rr.core.socket.MulticastPingServer $PROG_ARGS"

$PRERUN java $JVM_ARGS -classpath $CLASSPATH com.rr.core.socket.MulticastPingServer $PROG_ARGS


