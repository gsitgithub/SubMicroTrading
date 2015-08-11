#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
# TRADE SERVER
export OM_HOST_FOR_CLIENT=192.168.2.1
export OM_CLIENT_NIC=eth3
export OM_HOST_FOR_EXCHANGE=192.168.2.1
export OM_EXCHANGE_NIC=eth3
# SIM SERVER
export CLIENT_SIM_HOST=192.168.2.2
export CLIENT_SIM_NIC=eth3
export EXCHANGE_SIM_HOST=192.168.2.2
export EXCHANGE_SIM_NIC=eth3
export HUB_HOST=192.168.2.2
export HUB_NIC=eth3

export SIM_PERF="-u"


# DONT USE onload AS ITS ADDING ALOT OF LAQTENCY WITH CLIENT SENDING IN BATCHES
#export PRERUN=onload

source ./dist/setSMTVars.sh



#======================================================================
# Settings for SIM

#(
#      # exchange in CPU5
#      echo "tcp ${OM_HOST_FOR_CLIENT}:${OM_CLIENT_SERVER_PORT} rxq 7"
#      echo "tcp ${OM_HOST_FOR_EXCHANGE}:${OM_TO_EX_SIM_LOCAL_PORT} rxq 8"       
#) | sfcaffinity_tool --exit-on-eof


export SIM_CPU_MASKS=./config/cpumasks.cfg

