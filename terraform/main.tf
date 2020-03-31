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


resource "opennebula_virtual_machine" "kube-node" {
        name = "tf-kube-node${count.index}"
        template_id = "${opennebula_template.one-kube-template.id}"
        permissions = "600"
        # number of cluster nodes
        count = 3
}

output "kube-node-vm_id" {
        value = "${opennebula_virtual_machine.kube-node.*.id}"
}

output "kube-node-vm_ip" {
        value = "${opennebula_virtual_machine.kube-node.*.ip}"
}



