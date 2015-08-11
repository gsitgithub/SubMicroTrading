#!/bin/ksh

echo "START=$1 LAST=$2"
echo "" | awk -v START=$1 -v LAST=$2 '
END{  
	printf("        ") 
	for ( i = START; i <= LAST ; i++ ) { 
		printf("case %i: ",i) 
		if ( (i + 1) % 15 == 0 ) 
			printf("\n        ") 
	}
}'

