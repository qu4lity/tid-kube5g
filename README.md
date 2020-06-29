# lab-k8strap
This repo is meant to serve as for bootstraping k8s on top on vms in order to quickly get a k8s-learning playground.

## Repo Structure

- [vmtools](tools/)
A bunch of vm management helper scripts used to bootstrap the virtual infra.

- [prepare-vms](prepare-vms)
Ansible scripts to automate the configuration and provisioning of the ubuntu VMs.

## Getting Started

### Pre-requirement

Tested on ubuntu 18.04 bionic.

### Install

`vmtools/vminstall` is a quick way to generate a minimum image that can act as
worker and master. See:
[vmtools/README.md](vmtools)
for all the info on the tools to manage vms. *TBA*

Example:
`vminstall base` will generate a qcow image named `base`
this image can later be cloned with:
`vmclone k8s base`
`vmclone k8sworker1 base`

these vms can later be provisioned with the minimal necessary to run k8s:
`prepare-vms/bootstrap.yml` is the ansible-playbook that gets you most of what
you need to set up a minimal environment to start playing with k8s.

### Quickstart

Remember to configure `bridge_name` `bridge_gw` and `bridge_ip` ansible variable in the inventory before provisioning the network setup on the baremetal machine.

```console
ansible-playbook -i inventory  kvm.yaml
```

The `kvm.yaml` playbook install the minimum necessary packages and runs the role kvmhost that prepares the pipes on the server (bridge, and libvirt interfaces) in order to launch the cluster. The machine will reboot and then you are ready to initiate the k8s cluster bootstrap setup.

Then we bootstrap playbook deploys the kubertelconetes cluster (intel-multus+flannel):

```console
ansible-playbook -i inventory  bootstrap.yaml
```
