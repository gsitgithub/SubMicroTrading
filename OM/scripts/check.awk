BEGIN {
	MININ=99
	MB=99
	MINB=99
}

{
    REC=REC+1

    if ( REC > WARM ) {
        IN[ REC ]=$1
        OUT[ REC ]=$3

	BOTH=$1+$3
        B[ REC ]=BOTH

	if ( $1 > MAXIN ) MAXIN = $1
	if ( $1 < MININ ) MININ = $1
	if ( $3 > MAXOUT ) MAXOUT = $3
	if ( $3 < MAXOUT ) MAXOUT = $3
	if ( BOTH > MAXB ) MAXB = BOTH
	if ( BOTH < MINB ) MINB = BOTH

	TOTIN=TOTIN+$1
	TOTOUT=TOTOUT+$3
	TOTB=TOTB+BOTH
    }
}

END {
    ARRAYRECS = REC - WARM

    if ( ARRAYRECS > 100 ) {
	asort( IN )
	asort( OUT )
	N = asort( B )

	print "IN    recs=" N ", min=" MININ ", ave=" TOTIN/ARRAYRECS ", max=" MAXIN ", 10p=" percent(IN,N,10) ", 50p=" percent(IN,N,50) ", 90p=" percent(IN,N,90) ", 95p=" percent(IN,N,95) ", 99p=" percent(IN,N,99)
	print "OUT   recs=" N ", min=" MB ", ave=" TOTOUT/ARRAYRECS ", max=" MAXOUT ", 10p=" percent(OUT,N,10) ", 50p=" percent(OUT,N,50) ", 90p=" percent(OUT,N,90) ", 95p=" percent(OUT,N,95) ", 99p=" percent(OUT,N,99)
	print "INOUT recs=" N ", min=" MINB ", ave=" TOTB/ARRAYRECS ", max=" MAXB ", 10p=" percent(B,N,10) ", 50p=" percent(B,N,50) ", 90p=" percent(B,N,90) ", 95p=" percent(B,N,95) ", 99p=" percent(B,N,99)
    }
}

function percent( anARRAY, size, percentile ) {
	idx = size * percentile / 100;

	return( anARRAY[ int(idx) ] )
}

