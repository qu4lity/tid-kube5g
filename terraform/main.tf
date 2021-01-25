provider "opennebula" {
        endpoint = "http://192.168.83.20:2633/RPC2"
        username = "oneadmin"
        password = "XXXXXX"
}

data "template_file" "one-kube-template" {
        template = "${file("opennebula_kubernetes_template.tmpl")}"
}


resource "opennebula_template" "one-kube-template" {
        name = "terraform-kubernetes-template"
        template = "${data.template_file.one-kube-template.rendered}"
        permissions = "600"
}


resource "opennebula_virtual_machine" "kube-master" {
        name = "tf-kube-master"
        template_id = "${opennebula_template.one-kube-template.id}"
        permissions = "600"
}


resource "opennebula_virtual_machine" "kube-worker" {
        name = "tf-kube-worker${count.index}"
        template_id = "${opennebula_template.one-kube-template.id}"
        permissions = "600"
        # number of cluster nodes
        count = 2
}

resource "opennebula_virtual_machine" "voltha-demo-node" {
        name = "tf-voltha-demo"
        template_id = "${opennebula_template.one-kube-template.id}"
        permissions = "600"
}

output "tf-kube-master-vm_id" {
        value = "${opennebula_virtual_machine.kube-master.id}"
}

output "tf-kube-master-vm_ip" {
        value = "${opennebula_virtual_machine.kube-master.ip}"
}

output "kube-worker-vm_id" {
        value = "${opennebula_virtual_machine.kube-worker.*.id}"
}

output "kube-worker-vm_ip" {
        value = "${opennebula_virtual_machine.kube-worker.*.ip}"
}

output "voltha-demo-node-vm_id" {
        value = "${opennebula_virtual_machine.voltha-demo-node.id}"
}

output "voltha-demo-node-vm_ip" {
        value = "${opennebula_virtual_machine.voltha-demo-node.ip}"
}


