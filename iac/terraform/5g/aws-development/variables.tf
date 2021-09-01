#############################################
# GENERIC VARIABLES
#############################################
variable "environment_name" {
  description = "Environment name "
  default     = "tid-kube5g-development"
}

variable "region" {
  default = "eu-west-3"
}

variable "private_cloud_cidr" {
  default = "10.0.0.0/16"
}

variable "private_subnet_1_cidr" {
  default = "10.0.6.0/24"
}

variable "public_subnet_1_cidr" {
  default = "10.0.4.0/24"
}

variable "remote_node_private_subnet_1" {
  default = "172.30.0.0/16"
}

variable "remote_node_private_subnet_2" {
  default = "192.168.50.0/24"
}

variable "network_all" {
  type    = list
  default = ["0.0.0.0/0"]
}

variable "map_public_ip" {
  default = true
}

variable "availability_zone_1" {
  default = "a"
}

variable "availability_zone_2" {
  default = "b"
}


variable "bastion_instance_type" {
  description = "Instance type"
  default     = "t2.micro"
}

variable "vepc_instance_type" {
  description = "Instance type"
  default     = "t2.small"
}

variable "vepc_commercial_instance_type" {
  description = "Instance type"
  default     = "t2.small"
}
#############################################
# SPECIFIC VARIABLES
#############################################

variable "key_pair_name" {
  description = "EC2 Key pair name"
  default     = "tid-kube5gkey"
}

variable "bastion_ami" {
  description = "Ubuntu 18.04 AMI"
  default     = "ami-03bca18cb3dc173c9"
}
