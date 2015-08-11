#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
#!/bin/bash

function setClasspath {
	export CLASSPATH="./smt/smt.jar"
#	export CLASSPATH="./smt.jar"

	for JAR in jars/*
	do
		export CLASSPATH="${CLASSPATH};$JAR"
	done
}

export JVM_COMMON="-XX:+UseCompressedOops -Djava.net.preferIPv4Stack=true -XX:-RelaxAccessControlCheck -Xnoclassgc"


# PROGRAM ARGS=========================================================================

PROG_ARGS="./config/corestrats/cme/cmeInstDump_dev.properties"

JVM_ARGS="$DEBUG $PROFILE $LOG_COMPILE -Xmx1g -Xms1g $BASE_JVM_ARGS -Dapp.propertyTags=com.rr.core.algo.base.StratProps -DSOCKET_FACTORY_CLASS=$SOCKET_FACTORY_CLASS"


setClasspath
echo $CLASSPATH

java $JVM_ARGS -classpath $CLASSPATH com.rr.core.component.builder.AntiSpringBootstrap $PROG_ARGS | tee logs/cmeMdDump.out


