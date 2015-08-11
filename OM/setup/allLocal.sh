#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
export OM_HOST=127.0.0.1
export SIM_HOST=127.0.0.1
export HUB_HOST=127.0.0.1

# NIC the OM uses to talk to simulators
export OM_NIC=lo
# NIC the OM uses to talk to HUB
export HUB_NIC=lo
# NIC the SIMs use to talk to OM
export SIM_NIC=lo

# /OM/config/simCPUMasks.cfg
# if running SIM on same box as OM dont use simCPUMasks !!!
export SIM_CPU_MASKS=./config/cpumasks.cfg

