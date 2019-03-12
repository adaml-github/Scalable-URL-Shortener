#!/bin/bash
CWD="`pwd`"
mkdir -p "out"
rm "out/*"
rm "hosts"
#Replace db reference to User
sed -i "s/username/$USER/g" DatabaseThread.java
javac *.java

if [ "$#" -ne "1" ]
then
	echo "1 host parameters required"
	exit 1
fi
host2=$(($1+1)) 
host3=$(($1+2)) 
host4=$(($1+3))
startUp=true
#Start Database Controller
while true; do

	for host in $1 $host2 $host3 $host4
	do
		ip="`ssh "dh2026pc$host.utm.utoronto.ca" hostname -I | awk '{print $1}'`"
	        if !(nc -z $ip 3030) && !(nc -z $ip 5050)
	        then
	                echo "Database Controller is down, restarting..."
	                ssh dh2026pc$host.utm.utoronto.ca "cd \"$CWD\"; ./startDatabaseController $host $startUp;"
	                echo "Restarted Host :)"
	                if [ $host -eq $host4 ] 
	                then
	                	echo "Restore backup from $host4 to $1"
	        			scp -p dh2026pc$1.utm.utoronto.ca:/virtual/$USER/databaseBackup.txt dh2026pc$host4.utm.utoronto.ca:/virtual/$USER/database.txt
	        		else
	        			num=$(($host + 1))
	        			echo "Restore backup from $num to $host"
	        			scp -p dh2026pc$num.utm.utoronto.ca:/virtual/$USER/databaseBackup.txt dh2026pc$host.utm.utoronto.ca:/virtual/$USER/database.txt
	        		fi        	

	        fi
	done
	startUp=false
	#Start URL Shortner
	for host in $1 $host2 $host3 $host4
	do
		ip="`ssh "dh2026pc$host.utm.utoronto.ca" hostname -I | awk '{print $1}'`"
	        if !(nc -z $ip 4040)
	        then
	                echo "URL shortner $host is down, restarting..."
	                ssh dh2026pc$host.utm.utoronto.ca "cd \"$CWD\"; ./startURLShortner $host;"
	                echo "Restarted Host :)"
	        fi
	done

	#Start Load Simple Proxy
	for host in $1
	do
		ip="`ssh "dh2026pc$host.utm.utoronto.ca" hostname -I | awk '{print $1}'`"
		if !(nc -z $ip 2020)
		then
			echo "Simple proxy $host is down, restarting..."
	        ssh dh2026pc$host.utm.utoronto.ca "cd \"$CWD\"; ./startSimpleProxy $host;"
	        echo "Restarted Host :)"
		fi
	done
	echo "Backup all databases into host+1"
	for host in $1 $host2 $host3 $host4
	do
		ip="`ssh "dh2026pc$host.utm.utoronto.ca" hostname -I | awk '{print $1}'`"
	        if !(nc -z $ip 3030) && !(nc -z $ip 5050)
	        then
	        	echo "Don't overwrite db"
	        else
	                if [ $host -eq $host4 ] 
	                then
	                	echo "Backup $host4 to $1"
	        			scp -p dh2026pc$host4.utm.utoronto.ca:/virtual/$USER/database.txt dh2026pc$1.utm.utoronto.ca:/virtual/$USER/databaseBackup.txt
	        		else
	        			num=$(($host + 1))
	        			echo "Backup $host to $num"
	        			scp -p dh2026pc$host.utm.utoronto.ca:/virtual/$USER/database.txt dh2026pc$num.utm.utoronto.ca:/virtual/$USER/databaseBackup.txt
	        		fi        	

	        fi
	done
	
	sleep 15;
done
