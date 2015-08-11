#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
# for SDP if change 192.168.0   ... then add to libsdp.conf

export OM_HOST=192.168.0.1
export SIM_HOST=192.168.0.2
export HUB_HOST=192.168.0.2

# dont  know why these dont work
# export OM_HOST=OM_HOSTNAME
# export SIM_HOST=SIM_HOSTNAME
# export HUB_HOST=HUB_HOSTNAME


# NIC the OM uses to talk to simulators
export OM_NIC=eth0
# NIC the OM uses to talk to HUB
export HUB_NIC=eth0
# NIC the SIMs use to talk to OM
export SIM_NIC=eth0

# /OM/config/simCPUMasks.cfg
export SIM_ARGS=" -u "

# enable sim one for SIM box
#export SIM_CPU_MASKS=./config/simCPUMasks.cfg
#export SIM_CPU_MASKS=./config/cpumasks.cfg



