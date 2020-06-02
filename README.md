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

### Before bootstraping the cluster
Before installing the cluster remember to setup the network pipes on the machine hosting the cluster:
Remember to configure the bridge installed and associated to all macvtap and physnet.

```console
ansible-playbook -i inventory  kvm.yaml
```

machine will reboot and then you are ready to initiate the k8s cluster setup

### Bootstraping the cluster

```console
ansible-playbook -i inventory  bootstrap.yaml
```
