#!/bin/bash

# $1 bastion hostname (eg: bastion.tid-kube5g.tk)
# $2 VPN endpoint IP (eg: 10.95.164.110)
# $3 VPN endpoint username (eg: labuser)
# $4 VPN endpoint password (eg: <Insert_user_password>)

while true;
do
  #echo "Trying conectivity again: check public IP reachable"
  #sshpass -p 'research1' ssh research@10.95.157.12 "ping -c 1 $1"
  #if [ "$?" -eq 0 ] # so if we have exit status of zero then server is UP
  #then
    echo "connect to public cloud"
    sshpass -p $4 ssh -l $3 $2 'sudo ipsec restart'
	sleep 3
    sshpass -p $4 ssh -l $3 $2 'ping -c 1 10.0.4.10'
	if [ "$?" -eq 0 ]
	then
	  echo "edge connected to public cloud"
	exit 0
	else
	  echo "retrying....."
	fi
  #fi
done
