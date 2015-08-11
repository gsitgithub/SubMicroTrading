#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
# setup env vars for runCommon.sh
#
# NOTE THE SIM CPU MASKS FILE SHOULD BE cpumasks.cfg IF RUNNING ON SAME HOST AS OM
#
# this script sets up sim host 10.0.0.2 eth2 to OM host 10.0.0.1 eth1
# HUB runs on sim box

export OM_HOST=10.0.0.1
export SIM_HOST=10.0.0.2
export HUB_HOST=10.0.0.2

# NIC the OM uses to talk to simulators
export OM_NIC=eth1
# NIC the OM uses to talk to HUB
export HUB_NIC=eth1
# NIC the SIMs use to talk to OM
export SIM_NIC=eth2

# /OM/config/simCPUMasks.cfg
export SIM_CPU_MASKS=./config/simCPUMasks.cfg

