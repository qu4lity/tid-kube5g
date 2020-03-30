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

* Till here the config of the baremetal

