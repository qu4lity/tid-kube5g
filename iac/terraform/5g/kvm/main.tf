resource null_resource "deploy_kube" {
  connection {
    host = "172.30.0.71"
  }
  provisioner "remote-exec" {
    inline = [
      "sh launch_cluster.sh"
    ]
  }
}
