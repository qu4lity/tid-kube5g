resource null_resource "deploy_kube" {
  connection {
    host = "10.95.164.110"
    user = "sysadmin"
    port = 20021
    #20021 to access quantadu.lab
  }
  provisioner "remote-exec" {
    inline = [
      "sh launch_cluster.sh"
    ]
  }
}
