#!/bin/bash


terraform destroy -target=aws_internet_gateway.igw
terraform destroy -target=aws_route_table_association.public
terraform destroy -target=aws_route.public_internet_gateway
terraform destroy -target=aws_route_table_association.public
terraform destroy -target=aws_internet_gateway.igw
terraform destroy -target=aws_vpc.main
terraform destroy
