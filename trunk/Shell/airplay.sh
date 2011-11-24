#!/bin/sh
#--keepalive --keepalive-time 100

#while getopts put: o
#do	case "$o" in
#	t)	theme="$OPTARG";;
#	s)	skipthemes="1";;
#	u)	skipuploads="1";;
#	p)	skipplugins="1";;
#	[?])	echo $usage
#		exit 1;;
#	esac
#done
filename = ''
curl -D test.txt -X PUT --data-binary @$filename -H 'Connection: keep-alive' -H 'User-Agent: MediaControl/1.0' -H 'X-Apple-Transition: None' -H 'Content-Type:' -H 'Accept:' -H 'Host:' -H 'Expect:' http://Apple-TV.local:7000/photo
