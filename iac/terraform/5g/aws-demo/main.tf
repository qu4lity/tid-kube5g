#############################################
# VPC
#############################################
resource "aws_vpc" "hack5g_vpc" {
  cidr_block = var.private_cloud_cidr

  tags = {
    Name = "${var.environment_name}"
  }
}

#############################################
# INTERNET GATEWAY
#############################################
resource "aws_internet_gateway" "internet_gateway" {
  vpc_id = aws_vpc.hack5g_vpc.id

  tags = {
    Name = "${var.environment_name}"
  }
}

#############################################
# PUBLIC SUBNETS
#############################################
resource "aws_subnet" "public_subnet_1" {
  vpc_id                  = aws_vpc.hack5g_vpc.id
  cidr_block              = var.public_subnet_1_cidr
  map_public_ip_on_launch = var.map_public_ip
  availability_zone       = "${var.region}${var.availability_zone_1}"

  tags = {
    Name = "${var.environment_name}"
    Type = "Public Subnet (AZ1)"
  }
}

#############################################
# PUBLIC SUBNETS ROUTING
#############################################
resource "aws_route_table" "public_route_table" {
  vpc_id = aws_vpc.hack5g_vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = "${aws_internet_gateway.internet_gateway.id}"
  }

  route {
    cidr_block           = "${var.remote_node_private_subnet_1}"
    network_interface_id = "${aws_instance.bastion.primary_network_interface_id}"
  }

  route {
    cidr_block           = "${var.remote_node_private_subnet_2}"
    network_interface_id = "${aws_instance.bastion.primary_network_interface_id}"
  }

  depends_on = [aws_instance.bastion, aws_internet_gateway.internet_gateway]

  tags = {
    Name = "${var.environment_name}"
  }
}

resource "aws_route_table_association" "public_route_table_association_1" {
  subnet_id      = "${aws_subnet.public_subnet_1.id}"
  route_table_id = "${aws_route_table.public_route_table.id}"
}

#############################################
# PRIVATE SUBNETS
#############################################
resource "aws_subnet" "private_subnet_1" {
  vpc_id                  = "${aws_vpc.hack5g_vpc.id}"
  cidr_block              = "${var.private_subnet_1_cidr}"
  map_public_ip_on_launch = "${var.map_public_ip}"
  availability_zone       = "${var.region}${var.availability_zone_1}"

  tags = {
    Name = "${var.environment_name}"
    Type = "Private Subnet (AZ1)"
  }
}

#############################################
# SECURITY GROUPS
#############################################
resource "aws_security_group" "vpc_security_group" {
  vpc_id      = "${aws_vpc.hack5g_vpc.id}"
  name        = "${var.environment_name}-security-group"
  description = "Enable SSH access via port 22, ping and ipsec ports"

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = "${var.network_all}"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = "${var.network_all}"
  }

  ingress {
    cidr_blocks = "${var.network_all}"
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
  }

  ingress {
    cidr_blocks = "${var.network_all}"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
  }

  ingress {
    cidr_blocks = "${var.network_all}"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
  }

  ingress {
    cidr_blocks = "${var.network_all}"
    from_port   = -1
    to_port     = -1
    protocol    = "icmp"
  }

  ingress {
    cidr_blocks = "${var.network_all}"
    from_port   = 500
    to_port     = 500
    protocol    = "udp"
  }

  egress {
    from_port   = 500
    to_port     = 500
    protocol    = "udp"
    cidr_blocks = "${var.network_all}"
  }

  ingress {
    cidr_blocks = "${var.network_all}"
    from_port   = 4500
    to_port     = 4500
    protocol    = "udp"
  }

  egress {
    from_port   = 4500
    to_port     = 4500
    protocol    = "udp"
    cidr_blocks = "${var.network_all}"
  }

  ingress {
    cidr_blocks = "${var.network_all}"
    from_port   = 50
    to_port     = 50
    protocol    = "tcp"
  }

  egress {
    from_port   = 50
    to_port     = 50
    protocol    = "tcp"
    cidr_blocks = "${var.network_all}"
  }

  ingress {
    cidr_blocks = "${var.network_all}"
    from_port   = 51
    to_port     = 51
    protocol    = "tcp"
  }

  egress {
    from_port   = 51
    to_port     = 51
    protocol    = "tcp"
    cidr_blocks = "${var.network_all}"
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = "${var.network_all}"
  }

  tags = {
    Name = "${var.environment_name} Security Group"
  }
}

#############################################
# NETWORK INTERFACES
#############################################
#resource "aws_network_interface" "private_subnet_1_network_interface_1" {
#  subnet_id         = "${aws_subnet.private_subnet_1.id}"
#  security_groups   = ["${aws_security_group.vpc_security_group.id}"]
#  source_dest_check = "false"
#}

#resource "aws_network_interface" "private_subnet_1_network_interface_2" {
#  subnet_id         = "${aws_subnet.private_subnet_1.id}"
#  security_groups   = ["${aws_security_group.vpc_security_group.id}"]
#  source_dest_check = "false"
#}

#resource "aws_network_interface" "private_subnet_1_network_interface_3" {
#  subnet_id         = "${aws_subnet.private_subnet_1.id}"
#  security_groups   = ["${aws_security_group.vpc_security_group.id}"]
#  source_dest_check = "false"
#}

#############################################
# EC2 INSTANCES
#############################################
#############################################
# BASTION INSTANCE
#############################################

resource "aws_instance" "bastion" {
  ami                         = "${var.bastion_ami}"
  instance_type               = "${var.bastion_instance_type}"
  vpc_security_group_ids      = ["${aws_security_group.vpc_security_group.id}"]
  subnet_id                   = "${aws_subnet.public_subnet_1.id}"
  key_name                    = "${var.key_pair_name}"
  associate_public_ip_address = "true"
  source_dest_check           = "false"

  tags = {
    Name = "demo bastion"
    Type = "Demo Bastion instance"
  }
}

#############################################
# BASTION NETWORK INTERFACES
#############################################
#resource "aws_network_interface_attachment" "bastion_eth1" {
#  instance_id          = "${aws_instance.bastion.id}"
#  network_interface_id = "${aws_network_interface.private_subnet_1_network_interface_1.id}"
#  device_index         = 1
#}

#############################################
# VEPC AMI
#############################################
data "aws_ami" "vepc_ami" {
  most_recent = true
  owners      = ["self"]

  filter {
    name   = "name"
    values = ["ubuntu-20.04-vEPC_*"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

#############################################
# VEPC INSTANCE
#############################################
resource "aws_instance" "vOpen5GS_CPF" {
  ami                         = "${data.aws_ami.vepc_ami.id}"
  instance_type               = "${var.vepc_instance_type}"
  vpc_security_group_ids      = ["${aws_security_group.vpc_security_group.id}"]
  subnet_id                   = "${aws_subnet.public_subnet_1.id}"
  private_ip                  = "10.0.4.10"
  key_name                    = "${var.key_pair_name}"
  associate_public_ip_address = "true"
  source_dest_check           = "false"

  tags = {
    Name = "demo open5Gs_CPF"
    Type = "Demo VEPC opensource instance"
  }
}

#############################################
# VEPC NETWORK INTERFACES
#############################################
#resource "aws_network_interface_attachment" "vOpen5GS_CPF_eth1" {
#  instance_id          = "${aws_instance.vOpen5GS_CPF.id}"
#  network_interface_id = "${aws_network_interface.private_subnet_1_network_interface_2.id}"
#  device_index         = 1
#}

#############################################
# ROUTE53_RECORD
#############################################
resource "aws_route53_record" "bastion" {
  zone_id = "Z08307181LZTC1P7XEA9K"
  name    = "bastion.tid-kube5g.tk"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.bastion.public_ip}"]
}

resource "aws_route53_record" "bastion-priv" {
  zone_id = "Z08307181LZTC1P7XEA9K"
  name    = "bastion-priv.tid-kube5g.tk"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.bastion.private_ip}"]
}

resource "aws_route53_record" "vOpen5GS_CPF" {
  zone_id = "Z08307181LZTC1P7XEA9K"
  name    = "vOpen5GS_CPF.tid-kube5g.tk"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.vOpen5GS_CPF.public_ip}"]
}

resource "aws_route53_record" "vOpen5GS_CPF-priv" {
  zone_id = "Z08307181LZTC1P7XEA9K"
  name    = "vOpen5GS_CPF-priv.tid-kube5g.tk"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.vOpen5GS_CPF.private_ip}"]
}
