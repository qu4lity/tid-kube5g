# Pipeline as Code lab-k8strap

This pac folder contains all the pipelines as code files involved in K8s deployments. It can be seen the separation of demo and development scenarios, in both groovy files, inventories and ssh configuration.

The aim of these pipelines is provision a K8s cluster on baremetal (corenet1 and corenet2 in demo case) by using Ansible playbooks. Once the provisioning is finished, this pipeline should send a message to 5g-hacking SQS AWS queue containing ("k8s-deployed") in order to trigger next steps, such as installing mobile core network functions or deploy Apps.