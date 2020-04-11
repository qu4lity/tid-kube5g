# Notes on kubevirt

* Add nested KVM virtualization in all your workers:

 ssh -l sysadmin k8s-worker1.local
 sudo modprobe -r kvm_intel
 sudo modprobe kvm_intel nested=1

 * GET last kubevirt version:

export KUBEVIRT_VERSION=$(curl -s https://api.github.com/repos/kubevirt/kubevirt/releases | grep tag_name | grep -v -- - | sort -V | tail -1 | awk -F':' '{print $2}' | sed 's/,//' | xargs)
    echo $KUBEVIRT_VERSION

* Deploy the kubevirt operator last version 12 and crd:
    ```
    kubectl create -f https://github.com/kubevirt/kubevirt/releases/download/${KUBEVIRT_VERSION}/kubevirt-operator.yaml
    kubectl create -f https://github.com/kubevirt/kubevirt/releases/download/${KUBEVIRT_VERSION}/kubevirt-cr.yaml
    ```

* You Kube virt deployment should look like this:

   ``` kubectl get pods -n kubevirt
    NAME                               READY     STATUS    RESTARTS   AGE
     virt-api-649859444c-fmrb7          1/1       Running   0          2m12s
     virt-api-649859444c-qrtb6          1/1       Running   0          2m12s
     virt-controller-7f49b8f77c-kpfxw   1/1       Running   0          2m12s
     virt-controller-7f49b8f77c-m2h7d   1/1       Running   0          2m12s
     virt-handler-t4fgb                 1/1       Running   0          2m12s
     curl -L -o virtctl     https://github.com/kubevirt/kubevirt/releases/download/${KUBEVIRT_VERSION}/virtctl-${KUBEVIRT_VERSION}-linux-amd64
     ./virtctl 
     ```

* Get one test VM as example:

    ``` kubectl apply -f https://raw.githubusercontent.com/kubevirt/demo/master/manifests/vm.yaml
     ./virtctl start testvm
     kubectl get vm
    ./virtctl describe vm testvm
     kubectl get vm
     ./virtctl start testvm
     kubectl get vm
     kubectl get vm -o wide
     ./virtctl console testvm
     ```

*     And buala you get access to VM:

