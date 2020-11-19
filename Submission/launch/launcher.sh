#!/bin/bash

# Change this to your netid
netid=jmw150330

# Root directory of your project
PROJDIR=/home/012/j/jm/jmw150330/Documents/AOS/Projects/Project2

# Directory where the config file is located on your local system
#CONFIGLOCAL=$HOME/launch/config.txt
CONFIGLOCAL=$HOME/Documents/AOS/Projects/Project2/launch/config.txt

CONFIG=$PROJDIR/launch/config.txt

# Directory your java classes are in
BINDIR=$PROJDIR/bin

# Your main project class
#PROG=/home/012/j/jm/jmw150330/Documents/AOS/Projects/Project2/bin/Application
PROG=Application

n=0
cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" | sed -e 's/\r$//g' |
(
	# Read first line
    read line
	echo $line
	# Get number of nodes
	i=$( echo $line | awk '{ print $1}' )
	echo $i
	# While n is less than number of nodes 
    while [[ $n -lt $i ]]
    do
		# Read line from config file
    	read line
		# Print line 
		echo $line
		# Get node ID
    	p=$( echo $line | awk '{ print $1 }' )
		# Get hostname 
        host=$( echo $line | awk '{ print $2 }' )
		
	# java -cp $BINDIR $PROG $p -> execute java.exe with custom classpath $BINDIR and execute program $PROG with argument nodeID $p and config file location $CONFIG
	gnome-terminal -- ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host "hostname; java -cp $BINDIR $PROG $p $PROJDIR; exec bash" &

        n=$(( n + 1 ))
    done
)
