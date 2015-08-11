#!/bin/ksh

function Usage() {
	echo "$1"
	exit 99
}

if [ $# -ne 2 ] 
then
	Usage "usage : ./bseCDXInstGen  {ContractFileDate}   {SpreadFileDate}\nusage : ./bseCDXInstGen  ../common/cdx/raw/BFX_CO080814.csv  ../common/cdx/raw/BFX_SPD_CO120814.csv"
fi

CONTRACT_FILE=$1
SPREAD_FILE=$2

if [ ! -f ${CONTRACT_FILE} ] 
then
	Usage "ERROR $CONTRACT_FILE  doesnt exist ... expected it to be the contract file"
fi

if [ ! -f ${SPREAD_FILE} ] 
then
	Usage "ERROR $SPREAD_FILE  doesnt exist ... expected it to be the spread file"
fi

echo "Processing CONTRACT_FILE $CONTRACT_FILE" >&2
echo "Processing SPREAD_FILE $SPREAD_FILE" >&2

cat $CONTRACT_FILE $SPREAD_FILE | gawk '
BEGIN {
	MONTH[ "JAN" ] = "01" ; MONTH[ "APR" ] = "04" ; MONTH[ "JUL" ] = "07" ; MONTH[ "OCT" ] = "10" ;
	MONTH[ "FEB" ] = "02" ; MONTH[ "MAY" ] = "05" ; MONTH[ "AUG" ] = "08" ; MONTH[ "NOV" ] = "11" ;
	MONTH[ "MAR" ] = "03" ; MONTH[ "JUN" ] = "06" ; MONTH[ "SEP" ] = "09" ; MONTH[ "DEC" ] = "12" ;
}

function makeMaturityDate( DATE ) {
	N = split( DATE, BITS, "-" )
	if ( N != 3 ) { printf( "ERROR ROW %s : date field had unexpected format %s", FNR, DATE ) > "/dev/stderr" ; next }

	YYYY = BITS[ 3 ]
	MMM  = BITS[ 2 ]
	DD   = BITS[ 1 ]

	if ( ! MMM in MONTH ) { printf( "ERROR ROW %s : date field had unexpected month %s", FNR, MMM ) > "/dev/stderr" ; next }
	
	MDATE = YYYY MONTH[ MMM ] DD

	return MDATE
}


function handleSpreadContract( LINE ) {
	INST = ""

	NUM_FIELDS=split( LINE, CONTRACT_FIELDS, "," )

        # 849516,822995,SF,ARVI,ARVIND,30-OCT-2014,72058221103153158,1,,SF,ARVI,,27-NOV-2014,146,ARVION14S,,,,,,1,2000,2000,5,2000,2000,5,1,A,844790,SF,ARVI,5,30-OCT-2014,ARVIOCTNOV14,
	# 1000044,1000627,FUTCUR,USDINR,,26-Aug-2014,72057598332895264,1,,FUTCUR,USDINR,,26-Nov-2014,1,,,0,,,0,0,1,1,25000,1,1,25000,1,N,1003173,FUTCUR,USDINR,5,26-Aug-2014,USDAUGNOV14,

	LEG1_CONTRACT_ID		=	CONTRACT_FIELDS[ 1 ]
	LEG2_CONTRACT_ID		=	CONTRACT_FIELDS[ 2 ]

	ETI_INST_ID			=	CONTRACT_FIELDS[ 7 ]
	PARTITION_ID			=	CONTRACT_FIELDS[ 8 ]
	PRODUCT_ID			=	CONTRACT_FIELDS[ 14 ]

	DELETE_FLAG			=	substr( CONTRACT_FIELDS[ 29 ], 1, 1 )
	CONTRACT_ID			=	CONTRACT_FIELDS[ 30 ]
	INST_AND_PROD_TYPE		=	CONTRACT_FIELDS[ 31 ]
	EXPIRY_DATE			=	CONTRACT_FIELDS[ 34 ]

	INST_NAME			=	CONTRACT_FIELDS[ 35 ]
	
        # ‘IF‘ for Index Futures ‘IO‘ for Index Options ‘SF‘ for Stock Futures ‘SO‘ for Stock Options
	INST_TYPE = substr( INST_AND_PROD_TYPE, 2, 2 )
	if ( INST_TYPE != "F" && INST_TYPE != "O" ) { printf( "ERROR ROW %s : UnExpected instType of [%s] expected F or O\n", FNR, INST_TYPE ) > "/dev/stderr" ; next }
	if ( INST_TYPE == "F" ) INST_TYPE="FUT"
	if ( INST_TYPE == "O" ) INST_TYPE="OPT"


	if ( DELETE_FLAG != "E" && DELETE_FLAG != "A" && DELETE_FLAG != "X" ) 	{ printf( "ERROR ROW %s : Expected deleteFlag of A,E,X not [%s]\n", FNR, DELETE_FLAG ) > "/dev/stderr" ; next }
        if ( DELETE_FLAG == "E" ) DELETE_FLAG="Y" 
        if ( DELETE_FLAG != "Y" ) DELETE_FLAG="N" 

	MATURITY_DATE=makeMaturityDate( EXPIRY_DATE )
	INST_OP = ((DELETE_FLAG=="Y") ? "D" : "A")

	INST = "35=d|22=8|48=" ETI_INST_ID "|55=" INST_NAME "|107=" INST_NAME "|200=" MATURITY_DATE "|167=" INST_TYPE "|1180=" PARTITION_ID "|969=" TICK_SIZE "|980=" INST_OP "|58=" SYMBOL 
	INST = INST "|1300=" UNDERLYING_MKT_ID "|1151=" PRODUCT_ID

	if ( INST_TYPE == "OPT" ) {
		INST = INST "|202=" STRIKE_PRICE 
	} else {
		CFICODE="F"
	}

	INST = INST "|461=" CFICODE 

	INST = INST "|555=2"

	# 1128=9|9=520|35=d|49=CME|34=335|52=20131229170100026|15=USD|22=8|48=335|55=ES|107=ESM4-ESZ4|200=201406|202=0|207=XCME|461=FMIXSX|462=5|555=2|
	# 600=[N/A]|602=8347|603=8|623=1|624=2|
	# 600=[N/A]|602=28095|603=8|623=1|624=1|
	# 562=1|731=1|762=EQ|827=2|864=2|865=5|866=20130920|1145=133000000|865=7|866=20140620|1145=133000000|870=4|871=24|872=1|871=24|872=4|871=24|872=11|871=24|872=14|947=USD|969=5|996=CTRCT|
	# 1140=5000|1141=1|1022=GBX|264=10|1142=F|1143=50|1144=0|1146=0|1147=0|1150=-1350|1151=ES|1180=7|1300=64|5796=20131227|9787=0.01|9850=0|10=110|

	INST = INST "|600=" SEC_DESC[ LEG1_CONTRACT_ID ] "|602=" LEG1_CONTRACT_ID "|603=8|624=2"
	INST = INST "|600=" SEC_DESC[ LEG2_CONTRACT_ID ] "|602=" LEG2_CONTRACT_ID "|603=8|624=1|"

	printf( "%s\n", INST )
}

function handleContract( LINE ) {
	INST = ""

	NUM_FIELDS=split( LINE, CONTRACT_FIELDS, "," )
		
        # 862090,500303,SO,ABNV,ABIRLANUVO,0,16-OCT-2014,147000,CE,2,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,17-SEP-2014,16-OCT-2014,74,0,250,250,5,0,0,0,862090,0,0,0,0,0,0,0,0,0,0,0,16-OCT-2014,16-OCT-2014,0,1,ABNVCOC1470,ABNV14OCT1470.00CEW3,0,0,0,2,0,W,0,0,0,0,0,ABNVOPT,0,18475,A

	CONTRACT_ID		=	CONTRACT_FIELDS[ 1 ]
	UNDERLYING_ASSET_TYPE	=	CONTRACT_FIELDS[ 2 ]
	INST_AND_PROD_TYPE	=	CONTRACT_FIELDS[ 3 ]
	SYMBOL			=	CONTRACT_FIELDS[ 4 ]
	EXPIRY_DATE		=	CONTRACT_FIELDS[ 7 ]
	STRIKE_PRICE		=	CONTRACT_FIELDS[ 8 ]
	OPTION_TYPE		=	CONTRACT_FIELDS[ 9 ]
	PRECISION		=	CONTRACT_FIELDS[ 10 ]
	PARTITION_ID		=	CONTRACT_FIELDS[ 13 ]
	PRODUCT_ID		=	CONTRACT_FIELDS[ 29 ]
	MIN_LOT_SIZE		=	CONTRACT_FIELDS[ 31 ]
	TICK_SIZE		=	CONTRACT_FIELDS[ 33 ]
	ETI_INST_ID		=	CONTRACT_FIELDS[ 37 ]
	LOT_SIZE_MULT		=	CONTRACT_FIELDS[ 52 ]
	INST_NAME		=	CONTRACT_FIELDS[ 54 ]
	UNDERLYING_MKT_ID	=	CONTRACT_FIELDS[ 58 ]
	CONTRACT_TYPE		=	CONTRACT_FIELDS[ 60 ]
	DELETE_FLAG		=	substr( CONTRACT_FIELDS[ 69 ], 1, 1 )

	SEC_DESC[ CONTRACT_ID ] = INST_NAME
	
        # ‘IF‘ for Index Futures ‘IO‘ for Index Options ‘SF‘ for Stock Futures ‘SO‘ for Stock Options
	INST_TYPE = substr( INST_AND_PROD_TYPE, 2, 2 )
	if ( INST_TYPE != "F" && INST_TYPE != "O" ) { printf( "ERROR ROW %s : UnExpected instType of [%s] expected F or O\n", FNR, INST_TYPE ) > "/dev/stderr" ; next }
	if ( INST_TYPE == "F" ) INST_TYPE="FUT"
	if ( INST_TYPE == "O" ) INST_TYPE="OPT"

	# ‘CE‘ for Call Option (European) ‘PE’ for Put Option (European) ‘’ for Futures Contracts

	if ( INST_TYPE == "OPT" && (OPTION_TYPE != "CE" &&  OPTION_TYPE != "PE") ) { printf( "ERROR ROW %s : UnExpected optionType of [%s] expected CE,PE\n", FNR, OPTION_TYPE ) > "/dev/stderr" ; next }

	if ( CONTRACT_TYPE != "W" && CONTRACT_TYPE != "M" &&  CONTRACT_TYPE != "Q" && CONTRACT_TYPE != "H") { 
		printf( "ERROR ROW %s : Expected contractType of W,M,Q,H not [%s]\n", FNR, CONTRACT_TYPE ) > "/dev/stderr" 
		next 
	}

	if ( DELETE_FLAG != "E" && DELETE_FLAG != "A" && DELETE_FLAG != "X" ) 	{ printf( "ERROR ROW %s : Expected deleteFlag of A,E,X not [%s]\n", FNR, DELETE_FLAG ) > "/dev/stderr" ; next }
        if ( DELETE_FLAG == "E" ) DELETE_FLAG="Y" 
        if ( DELETE_FLAG != "Y" ) DELETE_FLAG="N" 

	# 1128=9|9=432|35=d|49=CME|34=594|52=20131229171000352|15=USD|22=8|48=71356|55=YO|107=YOV4|200=201410|202=0|207=XNYM|461=FCAXSX|462=2|562=1|731=1|827=2|864=2|865=5|866=20120928|1145=213000000|865=7|866=20140930|1145=163000000|870=2|871=24|872=1|871=24|872=14|947=USD|969=1|996=LBS|1140=999|1141=2|1022=GBX|264=10|1022=GBI|264=2|1142=F|1143=105|1144=3|1146=11.2|1147=112000|1148=0|1150=1703|1151=YO|1180=32|1300=76|5796=20131227|9787=0.0001|9850=0|10=150|

	# MISSING FIELDS,  CCY(15), 

	MATURITY_DATE=makeMaturityDate( EXPIRY_DATE )
	INST_OP = ((DELETE_FLAG=="Y") ? "D" : "A")

	INST = "35=d|22=8|48=" ETI_INST_ID "|55=" INST_NAME "|107=" INST_NAME "|200=" MATURITY_DATE "|167=" INST_TYPE "|1180=" PARTITION_ID "|969=" TICK_SIZE "|980=" INST_OP "|58=" SYMBOL 
	INST = INST "|1300=" UNDERLYING_MKT_ID "|1151=" PRODUCT_ID "|110=" MIN_LOT_SIZE "|1200=" PRECISION "|231=" LOT_SIZE_MULT


	if ( INST_TYPE == "OPT" ) {
		INST = INST "|202=" STRIKE_PRICE 
	} else {
		CFICODE="F"
	}

	if ( OPTION_TYPE == "CE" ) {
		INST = INST "|201=1" 
		CFICODE="OCE"
	}

	if ( OPTION_TYPE == "PE" ) {
		INST = INST "|201=0" 
		CFICODE="OPE"
	}

	INST = INST "|461=" CFICODE "|"

	printf( "%s\n", INST )
		
}

{
	LINE = $0
	NUM_FIELDS=split( LINE, CONTRACT_FIELDS, "," )

	if ( FNR == 0 || NUM_FIELDS == 0 ) {
		next
	}

	if ( NUM_FIELDS == 3 ) {
		SEG=CONTRACT_FIELDS[ 1 ]
		VER=CONTRACT_FIELDS[ 2 ]

		if ( SEG != 2 ) { printf( "ERROR Expected segment of 1 not %s\n", SEG ) ; exit 101 }
		if ( VER != "1.0.0" ) { printf( "ERROR Expected version of 1.0.0 not %s\n", VER ) ; exit 102 }

		next
	}


	if ( NUM_FIELDS == 69 ) { 
		handleContract( $0 ) 
	} else if ( NUM_FIELDS == 36 ) { 
		handleSpreadContract( $0 ) 
	} else { 
		printf( "ERROR ROW %s : Expected 69 or 36 fields not %s, line=%s\n", FNR, NUM_FIELDS, LINE ) > "/dev/stderr" 
		next 
	}
}

' | tr '|' '\001'

RES=$?

if [ $RES -ne 0 ] 
then
	echo "Failed to process contract file err=$RES"
	exit $RES
fi


