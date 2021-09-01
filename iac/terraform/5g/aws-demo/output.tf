#############################################
# OUTPUTS
#############################################
output "vpc_id" {
  value       = "${aws_vpc.hack5g_vpc.id}"
  description = "A reference to the created VPC"
}

output "vpc_cidr" {
  value = "${aws_vpc.hack5g_vpc.cidr_block}"
}

output "public_subnet_1_id" {
  value = "${aws_subnet.public_subnet_1.id}"
}

output "public_subnet_1_cidr" {
  value = "${aws_subnet.public_subnet_1.cidr_block}"
}

output "private_subnet_1_id" {
  value = "${aws_subnet.private_subnet_1.id}"
}
