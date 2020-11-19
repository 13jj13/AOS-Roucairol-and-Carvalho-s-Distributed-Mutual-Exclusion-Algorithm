#!/bin/bash


# Change this to your netid
netid=jmw150330

#
# Root directory of your project
#PROJDIR=$HOME/TestProj
PROJDIR=/home/012/j/jm/jmw150330/Documents/AOS/Projects/Project2

#
# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/Documents/AOS/Projects/Project2/launch/config.txt

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" | sed -e 's/\r$//g' |
(
    # Read first line
    read line
	# Get number of nodes
	i = $( echo $line | awk '{ print $1}' )
	echo $i
    while [[ $n -lt $i ]]
    do
    	read line
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        gnome-terminal -- ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host "killall -u $netid" &
        sleep 1

        n=$(( n + 1 ))
    done
   
)


echo "Cleanup complete"
