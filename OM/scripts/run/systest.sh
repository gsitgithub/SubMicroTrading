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

	for JAR in jars/*
	do
		export CLASSPATH="${CLASSPATH}:$JAR"
	done
}

#  -Xcomp removed for now
# for jdk 25 use -XX:-DoEscapeAnalysis 
# JIT when not using TieredCompilation
# export JIT="-XX:+BackgroundCompilation -XX:CompileThreshold=1000 -XX:-ClassUnloading -XX:+UseCompilerSafepoints -XX:-UseOnStackReplacement -XX:+UseCounterDecay"
#-XX:PerMethodRecompilationCutoff=4 

#export JIT="-XX:-UseCodeCacheFlushing -Xcomp -XX:PerMethodRecompilationCutoff=1 -XX:-ClassUnloading -XX:+UseCompilerSafepoints"
export JIT="-XX:+BackgroundCompilation -XX:CompileThreshold=1000 -XX:+TieredCompilation -XX:-ClassUnloading -XX:+UseCompilerSafepoints -XX:CompileCommandFile=.hotspot_compiler"
export JITTER="-XX:+PrintCompilation -XX:+CITime -verbose:class -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -XX:+LogCompilation"
export LOGGC="-verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails"
export JVM_COMMON="-XX:+UseCompressedOops -Djava.net.preferIPv4Stack=true -XX:-RelaxAccessControlCheck -Xnoclassgc"

export BASE_JVM_ARGS="-server $JIT -XX:+UseFastAccessorMethods -XX:+UseFastJNIAccessors -XX:+UseThreadPriorities -XX:-UseBiasedLocking -XX:+UseNUMA -DNUM_CORE_PER_CPU=6 -DLOCK_TO_CPU_ONE=false -DUSE_NATIVE_LINUX=true -DSOCKET_FACTORY_CLASS=$SOCKET_FACTORY_CLASS $LOGGC $JVM_COMMON"


# -XX:+TieredCompilation -XX:-ClassUnloading -XX:+UseCompilerSafepoints -XX:+UseCompressedOops -Djava.net.preferIPv4Stack=true -XX:-RelaxAccessControlCheck -Xnoclassgc -server -XX:+UseFastAccessorMethods -XX:+UseFastJNIAccessors -XX:+UseThreadPriorities -XX:-UseBiasedLocking


