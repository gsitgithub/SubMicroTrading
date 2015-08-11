#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
# TRADE SERVER
export OM_HOST_FOR_CLIENT=192.168.10.1
export OM_CLIENT_NIC=eth2
export OM_HOST_FOR_EXCHANGE=192.168.12.1
export OM_EXCHANGE_NIC=eth3
# SIM SERVER
export CLIENT_SIM_HOST=192.168.10.2
export CLIENT_SIM_NIC=eth2
export EXCHANGE_SIM_HOST=192.168.12.2
export EXCHANGE_SIM_NIC=eth3
export HUB_HOST=192.168.0.2
export HUB_NIC=eth2

export PRERUN="onload --profile=latency "
export EF_ACCEPT_INHERIT_NONBLOCK=1
export EF_NETIF_COUNT=2
export EF_NONAGLE_INFLIGHT_MAX=-1
export EF_STACK_PER_THREAD=1
export EF_FDS_MT_SAFE=1
export EF_TCP_CLIENT_LOOPBACK=1

# recomended by SF, worked well for pingpong but high spikes in SMT
#export EF_TCP_FASTSTART=0
#export EF_POLL_USEC=100000
#export EF_TCP_FASTSTART_IDLE=0


#export EF_NO_FAIL=0


#export PRERUN=onload
#export EF_POLL_USEC=1000
#export EF_ACCEPT_INHERIT_NONBLOCK=1
#export EF_FDS_MT_SAFE=1
#export EF_NETIF_COUNT=2
#export EF_NONAGLE_INFLIGHT_MAX=-1
#export EF_HELPER_USEC=100
#export EF_HELPER_PRIME_USEC=50

#export EF_SPIN_USEC=2000
#export EF_TCP_RECV_SPIN=1

export SIM_PERF="-u"


source ../setSMTVars.sh



#======================================================================
# Settings for SIM

#(
#      # exchange in CPU5
#      echo "tcp ${CLIENT_SIM_HOST}:${SIM_CLIENT_TO_OM_LOCAL_PORT} rxq 6"
#      echo "tcp ${EXCHANGE_SIM_HOST}:${EX_SIM_SERVER_PORT} rxq 8"
#) | sfcaffinity_tool --exit-on-eof


export SIM_CPU_MASKS=./config/simCPUMasks.cfg



