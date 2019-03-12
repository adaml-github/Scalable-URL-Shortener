#!/bin/bash

#error handle - stops script (hopefully) if command returns non-zero value
set -e
set -o pipefail #this one is bash only and helps for pipelines

CWD="`pwd`";

##num arg check and if test name correct; doesn't test for host ints
#echo $#
#echo $1

usage(){
    #print how to run the script
    echo "./testKill.sh [test] [HOSTS]"
    echo "      -[test] : url, dbw, dwr, all"
    echo "      -[HOSTS] : list of hosts as numerals seperated by spaces, eg. 1 2 3 4"
    exit
}

kill_url(){
    echo "Running kill_url..."
    #Kill 1 URL shortner
    for host in $2
    do
        ssh dh2026pc$host.utm.utoronto.ca "cd \"$CWD\"; kill `cat out/service$host.pid`;"
        echo $host
    done

    #test both read and write
    python readTest.py
    python writeTest.py


    sh ../heartbeat.sh

    #Kill ALL URL shortner
    for host in $2 $3 $4 $5
    do
        ssh dh2026pc$host.utm.utoronto.ca "cd \"$CWD\"; kill `cat out/service$host.pid`;"
        echo $host
    done

    python readTest.py
    python writeTest.py
}


kill_dbw(){
    echo "Running kill_dbw..."
    #Kill 1 db writer
    for host in $2
    do
    ssh dh2026pc$host.utm.utoronto.ca "cd \"$CWD\"; kill `cat out/dbwriter$host.pid`;"
    echo $host
    done

    #only need to test writing to db
    python writeTest.py


    sh ../heartbeat.sh

    #Kill BOTH db writer
    for host in $2 $3 $4 $5
    do
    ssh dh2026pc$host.utm.utoronto.ca "cd \"$CWD\"; kill `cat out/dbwriter$host.pid`;"
    echo $host
    done

    python writeTest.py
}

kill_dbr(){
    echo "Running kill_dbr..."
    #Kill 1 db reader
    for host in $2
    do
    ssh dh2026pc$host.utm.utoronto.ca "cd \"$CWD\"; kill `cat out/dbreader$host.pid`;"
    echo $host
    done

    #only need to test reading to db
    python readTest.py


    sh ../heartbeat.sh

    #Kill BOTH db reader
    for host in $2 $3 $4 $5
    do
    ssh dh2026pc$host.utm.utoronto.ca "cd \"$CWD\"; kill `cat out/dbreader$host.pid`;"
    echo $host
    done

    python readTest.py
}

#check for at least two args - one test name and at least one host??
if [ $# -lt 2 ]; then
    usage
fi

#start up the servers and db with heartbeat
sh ../heartbeat.sh


if [ $1 == "url" ]; then
    kill_url
elif [ $1 == "dbw" ]; then
    kill_dbw
elif [ $1 == "dbr" ]; then
    kill_dbr
elif [ $1 == "all" ]; then
    kill_url
    kill_dbw
    kill_dbr
else
    usage
fi

#stop all runnning with stop script
sh ../stop



