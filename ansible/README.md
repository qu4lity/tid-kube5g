# ubuntu-ansible

Deploy Ubuntu VMs with different profiles defined in hosts file.

An example hosts file is provided as hosts.example

Then launch playbook:
ansible-playbook bootstrap.yml

## Caveat:
- For Calico and MetalLB roles you need to pip3 install --user openshift