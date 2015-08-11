#*******************************************************************************
# Copyright (c) 2015 Low Latency Trading Limited  :  Author Richard Rose
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at	http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing,  software distributed under the License 
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
#*******************************************************************************
#!/bin/sh

FILE=$1
WARM=$2

cat $FILE | fgrep '  [' | sed -e 's/^.*  \[//' -e 's/\].*$//' -e 's/,/ /g' | gawk -v WARM=$WARM '
BEGIN {
	MINEX=99999
	MININ=99
	MINOUT=99
	MINB=99
}

{
    REC=REC+1

    if ( REC > WARM ) {
        IN[ REC ]=$1
        EX[ REC ]=$2
        OUT[ REC ]=$3

	BOTH=$1+$3
        B[ REC ]=BOTH

	if ( $1 > MAXIN )  MAXIN = $1
	if ( $1 < MININ )  MININ = $1
	if ( $2 > MAXEX )  MAXEX = $2
	if ( $2 < MINEX )  MINEX = $2
	if ( $3 > MAXOUT ) MAXOUT = $3
	if ( $3 < MINOUT ) MINOUT = $3
	if ( BOTH > MAXB ) MAXB = BOTH
	if ( BOTH < MINB ) MINB = BOTH

	TOTIN=TOTIN+$1
	TOTEX=TOTEX+$2
	TOTOUT=TOTOUT+$3
	TOTB=TOTB+BOTH
    }
}

END {
    ARRAYRECS = REC - WARM

    if ( ARRAYRECS > 1 ) {
	asort( IN )
	asort( EX )
	asort( OUT )
	N = asort( B )

	print "IN       recs=" N ", min=" MININ ", ave=" TOTIN/ARRAYRECS ", max=" MAXIN ", 5p=" percent(IN,N,5) ", 50p=" percent(IN,N,50) ", 90p=" percent(IN,N,90) ", 95p=" percent(IN,N,95) ", 99p=" percent(IN,N,99)
	print "OUT      recs=" N ", min=" MINOUT ", ave=" TOTOUT/ARRAYRECS ", max=" MAXOUT ", 5p=" percent(OUT,N,5) ", 50p=" percent(OUT,N,50) ", 90p=" percent(OUT,N,90) ", 95p=" percent(OUT,N,95) ", 99p=" percent(OUT,N,99)
	print "INOUT    recs=" N ", min=" MINB ", ave=" TOTB/ARRAYRECS ", max=" MAXB ", 5p=" percent(B,N,5) ", 50p=" percent(B,N,50) ", 90p=" percent(B,N,90) ", 95p=" percent(B,N,95) ", 99p=" percent(B,N,99)

	print "EXCHANGE recs=" N ", min=" MINEX ", ave=" TOTEX/ARRAYRECS ", max=" MAXEX ", 5p=" percent(EX,N,5) ", 50p=" percent(EX,N,50) ", 90p=" percent(EX,N,90) ", 95p=" percent(EX,N,95) ", 99p=" percent(EX,N,99)
    }
}

function percent( anARRAY, size, percentile ) {
	idx = size * percentile / 100;

	return( anARRAY[ int(idx) ] )
}'

