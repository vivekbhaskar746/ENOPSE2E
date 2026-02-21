resource "aws_ecs_cluster" "app" {
  name = "app-cluster"
}

resource "aws_security_group" "ecs_sg" {
  name   = "ecs-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    security_groups = [module.alb.security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "ecs_backend_sg" {
  name   = "ecs-backend-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    security_groups = [module.alb.security_group_id]
  }

  egress {
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    cidr_blocks     = [module.vpc.vpc_cidr_block]
  }
}

resource "aws_ecs_task_definition" "frontend" {
  family                   = "frontend"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu    = 256
  memory = 512
  execution_role_arn = aws_iam_role.ecs_exec.arn

  container_definitions = jsonencode([{
    name  = "frontend"
    image = "${aws_ecr_repository.frontend.repository_url}:latest"
    portMappings = [{containerPort=80}]
    environment = [
      { name = "REACT_APP_API_BASE_URL", value = "http://localhost:9090/api" }
    ]
  }])
}

resource "aws_ecs_task_definition" "backend" {
  family                   = "backend"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu    = 256
  memory = 512
  execution_role_arn = aws_iam_role.ecs_exec.arn

  container_definitions = jsonencode([{
    name  = "backend"
    image = "${aws_ecr_repository.backend.repository_url}:latest"
    portMappings = [{containerPort=1995}]
    environment = [
      { name = "DB_HOST", value = aws_db_instance.postgres.endpoint },
      { name = "DB_USER", value = aws_db_instance.postgres.username },
      { name = "DB_PASSWORD", value = aws_db_instance.postgres.password }
    ]
  }])
}

# resource "aws_ecs_task_definition" "blog_backend" {
#   family                   = "blog-backend"
#   requires_compatibilities = ["FARGATE"]
#   network_mode             = "awsvpc"
#   cpu                      = 256
#   memory                   = 512
#   execution_role_arn       = aws_iam_role.ecs_exec.arn

#   container_definitions = jsonencode([{
#     name  = "blog-backend"
#     image = "${aws_ecr_repository.blog_backend.repository_url}:latest"
#     portMappings = [{ containerPort = 1995 }]
#   }])
# }

# resource "aws_ecs_task_definition" "blog_frontend" {
#   family                   = "blog-frontend"
#   requires_compatibilities = ["FARGATE"]
#   network_mode             = "awsvpc"
#   cpu                      = 256
#   memory                   = 512
#   execution_role_arn       = aws_iam_role.ecs_exec.arn

#   container_definitions = jsonencode([{
#     name  = "blog-frontend"
#     image = "${aws_ecr_repository.blog_frontend.repository_url}:latest"
#     portMappings = [{ containerPort = 80 }]
#   }])
# }

resource "aws_ecs_service" "frontend" {
  name            = "frontend"
  cluster         = aws_ecs_cluster.app.id
  task_definition = aws_ecs_task_definition.frontend.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = module.vpc.private_subnets
    security_groups = [aws_security_group.ecs_sg.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.frontend.arn
    container_name   = "frontend"
    container_port   = 80
  }
}

resource "aws_ecs_service" "backend" {
  name            = "backend"
  cluster         = aws_ecs_cluster.app.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = module.vpc.private_subnets
    security_groups = [aws_security_group.ecs_backend_sg.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.backend.arn
    container_name   = "backend"
    container_port   = 1995
  }
}

# resource "aws_ecs_service" "blog_backend" {
#   name            = "blog-backend"
#   cluster         = aws_ecs_cluster.app.id
#   task_definition = aws_ecs_task_definition.blog_backend.arn
#   desired_count   = 1
#   launch_type     = "FARGATE"

#   network_configuration {
#     subnets         = module.vpc.private_subnets
#     security_groups = [aws_security_group.ecs_sg.id]
#   }

#   load_balancer {
#     target_group_arn = aws_lb_target_group.blog_backend.arn
#     container_name   = "blog-backend"
#     container_port   = 1995
#   }
# }

# resource "aws_ecs_service" "blog_frontend" {
#   name            = "blog-frontend"
#   cluster         = aws_ecs_cluster.app.id
#   task_definition = aws_ecs_task_definition.blog_frontend.arn
#   desired_count   = 1
#   launch_type     = "FARGATE"

#   network_configuration {
#     subnets         = module.vpc.private_subnets
#     security_groups = [aws_security_group.ecs_sg.id]
#   }

#   load_balancer {
#     target_group_arn = aws_lb_target_group.blog_frontend.arn
#     container_name   = "blog-frontend"
#     container_port   = 80
#   }
# }
