# Pipes
This repo is meant to serve as for bootstraping k8s on top on vms in order to quickly get a k8s-learning playground.

## Baremetal

* create bridge using systemd-neworkd

create bridge and map it to phy nic underneat

```
cat /etc/systemd/network/*
```

```
[NetDev]
Name=5gbr0
Kind=bridge
[Match]
Name=5gbr0

[Network]
IPForward=yes
Address=172.30.0.71/16
Gateway=172.30.0.1
DNS=8.8.8.8
[Match]
Name=ens5f0

[Network]
Bridge=5gbr0
```

* create network using libvirt

```
<network>
 <name>5gphysical</name>
 <forward mode="bridge"/>
 <bridge name="5gbr0"/>
</network>
```

run `virsh net-create FILE`


* Create two ifaces per machine:

Ubuntu1804 xml file and confirm they have two ifaces in the same way:

```
<interface type='direct' trustGuestRxFilters='yes'>
     <mac address='52:54:00:b6:cd:34'/>
     <source dev='5gbr0' mode='bridge'/>
     <model type='virtio'/>
     <address type='pci' domain='0x0000' bus='0x01' slot='0x00' function='0x0'/>
   </interface>

<interface type='network'>
     <mac address='52:54:00:81:77:68'/>
     <source network='5gphysical'/>
     <model type='virtio'/>
     <address type='pci' domain='0x0000' bus='0x09' slot='0x00' function='0x0'/>
</interface>
```

* ToFix:

- assure net-defined and net-start of the network 

```
virsh net-list --
 Name         State    Autostart   Persistent
-----------------------------------------------
 5gphysical   active   no          no
 default      active   yes         yes
```
To better detail in the doc:

- assure want_multus and want_multinic are true and want_multinic is
    a pre-requirement of want_multus

- assure ufw is disabled in master and worker nodes:

- assure IPForwarding is activated in the systemd-network

- asssure to put example of macvlan to be tuned and use samplepod default file
