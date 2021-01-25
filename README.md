# QU4LITY tid-kube5g repository
This repo is meant to help bootstraping K8s automatically on top of Ubuntu 18.04 LTS VMs in order to quickly get a telco K8s-learning playground.

## Repo Structure

- [vmtools](tools/):
A bunch of VM management helper scripts used to bootstrap the virtual infra in KVM

- [ansible](ansible):
Ansible playoboks to automate the deployment of K8s

- [pac](pac):
Pipelines for deployment automation, specific to each project using this repo (eg. 5glab)

- [terraform](terraform):
Preliminary support for deployment in OpenNebula VMs

- [docs](docs):
Miscallaneous documentation regarding networking, load balancer and storage topics

## Getting Started

### KVM Host networking setup

The kvm.yaml playbook supports setting up the networking infrastructure of a KVM host with the bridging necessary for supporting Intel Multus later in the bootstrap playbook.

Remember to configure `bridge_name`, `bridge_gw` and `bridge_ip` ansible variables within inventory file before provisioning the network setup on the baremetal machine.

As with the bootstrap playbook, setting up inventory is needed prior to running the playbook.

```console
ansible-playbook -i inventory kvm.yaml
```

This `kvm.yaml` playbook will install the minimum necessary packages and runs the role kvmhost that prepares the pipes on the server (bridge, and libvirt interfaces) in order to launch the cluster. The machine will reboot and then you are ready to initiate the K8s cluster bootstrap setup.

### Installing K8s

`vmtools/vminstall` is a quick way to generate a minimum KVM image that can act as worker and master.
See [vmtools/README.md](vmtools) to check all details about the tools to manage VMs.

Example:
`vminstall base` will generate a qcow image named `base`,
this image can be later cloned with following commands:
`vmclone k8s base`
`vmclone k8sworker1 base`

These VMs can be later provisioned with the minimal necessary to run K8s:
`ansible/bootstrap.yml` is the ansible-playbook that gets you most of what
you need to set up a minimal environment to start playing with k8s.

**Important Note**: The qcow image (e.g. base) has associated an xml file with the definition associated to the VM when provisioning that image. For kubertelconetes virtual environment we recommend to add two interfaces: one macvtap interface and one interface of type network. This can be done by editing the xml file of the base image.
Example:
`virsh edit --domain base`

At the network-side of the VM config we mapped one macvtap interface:

```console
<interface type='direct' trustGuestRxFilters='yes'>
      <mac address='52:54:00:25:30:3b'/>
      <source dev='5gnow' mode='bridge'/>
      <model type='virtio'/>
      <address type='pci' domain='0x0000' bus='0x01' slot='0x00' function='0x0'/>
    </interface>
```

And we include one additional interface for external k8s networks:

```console
 <interface type='network'>
     <mac address='52:54:00:81:77:68'/>
     <source network='5gphysical'/>
     <model type='virtio'/>
     <address type='pci' domain='0x0000' bus='0x09' slot='0x00' function='0x0'/>
</interface>
```


Prior to run the playbook, the inventory file must be adapted to the needs of the particular deployment. An example is provided at hosts.example file, and basically it works as a feature toggle interface, where the machines to be deployed must be defined in the target group, and any feature specific must be enabled per host. Below the target group lies the general variables that control the features for all hosts defined above, so a sane set of defaults is provided.

To bootstrap the kubertelconetes cluster (intel-multus+flannel):

```console
ansible-playbook -i inventory  bootstrap.yaml
```


