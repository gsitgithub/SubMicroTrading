#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
#!/bin/bash

source systest.sh

#LOG_COMPILE="$JITTER"

# PROGRAM ARGS=========================================================================

PROG_ARGS="./config/systestMulti/systestMultiSMT.properties"

# JVM  ARGS=========================================================================
#PROFILE="-agentpath:/home/smt/jprofiler6/bin/linux-x64/libjprofilerti.so=port=8849" 
#DEBUG="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"


JVM_ARGS="$DEBUG $PROFILE $LOG_COMPILE -Xmx8g -Xms8g $BASE_JVM_ARGS"


# JDK1.7 -XX:+PrintInlining  -XX:ReservedCodeCacheSize=64m"


setClasspath
echo $CLASSPATH

echo "RUN: $PRERUN java $JVM_ARGS -classpath $CLASSPATH com.rr.om.main.SMTMain $PROG_ARGS"

nohup $PRERUN java $JVM_ARGS -classpath $CLASSPATH com.rr.om.main.SMTMain $PROG_ARGS >logs/server.log 2>&1 &


