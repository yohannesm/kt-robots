# ----------------------------------------
# Create a ecs service using fargate
# ----------------------------------------
provider "aws" {
  version = ">= 2.52"
  region  = var.region
}
data "aws_caller_identity" "current" {}

# ----------------------------------------
# get the main vpc and subnets
# Note that this will not account for
# private subnets
# ----------------------------------------
data "aws_vpc" "main" {
  default = true
}

data "aws_subnet_ids" "main" {
  vpc_id = data.aws_vpc.main.id
}


# ----------------------------------------
#  Create security group rule for the task
# ----------------------------------------
resource "aws_security_group_rule" "task_ingress_8080" {
  security_group_id        = module.fargate.service_sg_id
  type                     = "ingress"
  protocol                 = "tcp"
  from_port                = 8080
  to_port                  = 8080
  cidr_blocks              = ["0.0.0.0/0"]
  ipv6_cidr_blocks         = ["::/0"]
}

# ----------------------------------------
# Create a fargate cluster
# ----------------------------------------
resource "aws_ecs_cluster" "cluster" {
  name = "${var.name_prefix}-cluster"
  capacity_providers = ["FARGATE_SPOT"]
  default_capacity_provider_strategy {
    capacity_provider = "FARGATE_SPOT"
  }
}

# ----------------------------------------
# Define an ECR repository where we are
# going to host the server image
# This also executes a script that
# pushes the image to the repository
# ----------------------------------------
resource "aws_ecr_repository" "ktrobots_server" {
  name                 = "ktrobots-server"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }

  provisioner "local-exec" {
    command = "./build-image.sh ${data.aws_caller_identity.current.account_id} ${var.region}"
    working_dir = "../"
  }
}

# ----------------------------------------
# Create an on-demand dynamo table that
# will be used to store the game state
# ----------------------------------------
resource "aws_dynamodb_table" "basic-dynamodb-table" {
  name           = "KT-Robots-Server-GameTable"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "primaryKey"

  attribute {
    name = "primaryKey"
    type = "S"
  }
}

# ----------------------------------------
# Module to create and start a fargate
# service and the task that we created
# above
# ----------------------------------------
module "fargate" {
  source  = "telia-oss/ecs-fargate/aws"
  version = "3.3.0"

  name_prefix          = var.name_prefix
  vpc_id               = data.aws_vpc.main.id
  private_subnet_ids   = data.aws_subnet_ids.main.ids
  cluster_id           = aws_ecs_cluster.cluster.id
  task_container_image = "${aws_ecr_repository.ktrobots_server.repository_url}:latest"

  // public ip is needed for default vpc, default is false
  task_container_assign_public_ip = true

  // port, default protocol is HTTP
  task_container_port = 8080

  desired_count = 1
  // See the combination of possible values here:
  // https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-cpu-memory-error.html
  task_definition_cpu = 512
  //task_definition_cpu = 2048
  task_definition_memory = 1024
  //task_definition_memory = 4096

  task_container_environment = {
    ENV = "dev"
  }

  health_check = {
    port = "traffic-port"
    path = "/"
  }

  tags = {
    environment = "dev"
    terraform   = "True"
  }
}

# ----------------------------------------
# WARNING: The following commands attach
# ADMIN policies to the SERVER task
# and should be avoided in any production
# environment
# ----------------------------------------
resource "null_resource" "fargate" {
  provisioner "local-exec" {
    command = "aws iam attach-role-policy --policy-arn arn:aws:iam::aws:policy/AdministratorAccess --role-name ${module.fargate.task_role_name}"
  }
  provisioner "local-exec" {
    when = destroy
    command = "aws iam detach-role-policy --policy-arn arn:aws:iam::aws:policy/AdministratorAccess --role-name ${module.fargate.task_role_name}"
  }
}
