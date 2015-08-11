#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
#!/bin/sh

T1FILE=$1
WARM=$2

cat $T1FILE | grep -i gap | grep -v WARM
GAPS=`cat $T1FILE | grep -i gap | grep -v WARM | wc -l`
echo "number of gaps $GAPS"

cat $T1FILE | grep 35=D | tr -d '[' | tr -d ']' | gawk -v WARM=$WARM '
BEGIN {
        MIN=99999
}

{
        ++REC
        if ( REC > WARM ) {

                TIMES[ REC ] = $7

                if ( $7 > MAX ) MAX = $7
                if ( $7 < MIN ) MIN = $7

                TOT=TOT+$7
        }
}

END {

    ARRAYRECS = REC - WARM

    if ( ARRAYRECS > 1 ) {
        N = asort( TIMES )

        print "InternalTickToTrade (usecs)     recs=" N ", min=" MIN ", ave=" TOT/ARRAYRECS ", max=" MAX ", 5p=" percent(TIMES,N,5) ", 50p=" percent(TIMES,N,50) ", 90p=" percent(TIMES,N,90) ", 95p=" percent(TIMES,N,95) ", 99p=" percent(TIMES,N,99) ", 99p.9=" percent(TIMES,N,99.9)

    }
}

function percent( anARRAY, size, percentile ) {
        idx = size * percentile / 100;

        return( anARRAY[ int(idx) ] )
}'

