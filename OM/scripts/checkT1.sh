#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
#!/bin/sh


EXFILE=$1
WARM=$2

# exchangeSim
# 23:04:00.048 [info]  NOS_clOrdId 27069_1007583 103307091850188 103307091859188


cat $EXFILE | grep NOS_clOrdId | grep -v CLORDID | gawk -v WARM=$WARM '
BEGIN {
        MIN=99999
}

/NOS_clOrdId/ {
        TIME = ($6 - $5)
        ++REC
		if ( REC > WARM ) {
			if ( TIME > MAX ) MAX = TIME
			if ( TIME < MIN ) MIN = TIME

			TOT=TOT+TIME

			TIMES[ $4 ] = TIME 
		}
}

END {

    ARRAYRECS = REC - WARM

    if ( ARRAYRECS > 1 ) {
        N = asort( TIMES )

        print "TickToTrade (usecs)     recs=" N ", min=" MIN ", ave=" TOT/ARRAYRECS ", max=" MAX ", 5p=" percent(TIMES,N,5) ", 50p=" percent(TIMES,N,50) ", 90p=" percent(TIMES,N,90) ", 95p=" percent(TIMES,N,95) ", 99p=" percent(TIMES,N,99) ", 99.9p=" percent(TIMES,N,99.9)

    }
}

function percent( anARRAY, size, percentile ) {
        idx = size * percentile / 100;

        return( anARRAY[ int(idx) ] )
}'

