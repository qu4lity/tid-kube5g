## Notes on creating persistent (or not that persistent) storage volume

Create NFS for volumes (not the best solution we need to go to iSCSI):

Create dirs in the master node for the case of sgw/pgw/pcrf:

```
sysadmin@k8s:~$ sudo apt-get update && sudo  apt-get install -y nfs-kernel-server   
```

Make and populate a directory to be shared. 

```
sysadmin@k8s:~$ sudo mkdir /opt/volA ; sudo chmod 1777 /opt/volA  
sysadmin@k8s:~$ sudo mkdir /opt/volB ; sudo chmod 1777 /opt/volB

sysadmin@k8s:~$ sudo mkdir /opt/volC ; sudo chmod 1777 /opt/volC

cp files /opt/volA
```

Edit the NFS server to share the directories and export them.
See Below the content of /etc/exports:

```
/opt/volA	*(rw,sync,no_root_squash,subtree_check)
/opt/volB	*(rw,sync,no_root_squash,subtree_check)
/opt/volC	*(rw,sync,no_root_squash,subtree_check)
```



Then mount in the agents and install nfs-common

```
sudo apt-get -y install nfs-common
sudo mount k8s.local:/opt/volA /mnt

sudo apt-get -y install nfs-common
sudo mount k8s.local:/opt/volB /mnt

sudo apt-get -y install nfs-common
sudo mount k8s.local:/opt/volC /mnt
```
