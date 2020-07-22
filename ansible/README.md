# ubuntu-ansible

Deploy Ubuntu VMs with different profiles defined in hosts file.

An example hosts file is provided as hosts.example

Then launch playbook:
ansible-playbook bootstrap.yml

## Workflow

These folders can be used as part of K8s-deployment step in 5g-hack demo. Furthermore, these files can be used as part of a K8s-tester using KVM hosts.
The folder structure is based on a role structure where a main YAML file (bootstrap.yml in this case) triggers other YAML roles for the installation. That means making the code more modular and human-readable.

In demo case, target hosts are physical servers, such as corenet1 and corenet2 according to the inventory used. The result is a K8s installation (corenet1 as master and corenet2 as worker) ready to deploy mobile core functions (UPF) or third party Apps on top of it.

On the other hand, in KVM case the result is a 4 KVM nodes K8s deployment with a master (k8s) and three workers (k8s-worker{1,2,3}).

## Caveat:
- For Calico and MetalLB roles you need to pip3 install --user openshift