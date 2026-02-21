module "alb" {
  source  = "terraform-aws-modules/alb/aws"
  name    = "prod-small-alb"
  vpc_id  = module.vpc.vpc_id
  subnets = module.vpc.public_subnets
  # Disable deletion protection
  enable_deletion_protection = false

  security_group_ingress_rules = {
    all_ipv4 = {
      from_port = 0
      to_port   = 0
      protocol  = "-1"
      cidr_ipv4 = "0.0.0.0/0"
    }
  }
}

resource "aws_lb_target_group" "frontend" {
  name     = "tg-frontend"
  port     = 80
  protocol = "HTTP"
  vpc_id   = module.vpc.vpc_id
  target_type = "ip" 
  health_check { path = "/" }
}

resource "aws_lb_target_group" "backend" {
  name     = "tg-backend"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = module.vpc.vpc_id
  target_type = "ip" 
  health_check { path = "/" }
}

# resource "aws_lb_target_group" "blog_backend" {
#   name        = "tg-blog-backend"
#   port        = 8080
#   protocol    = "HTTP"
#   vpc_id      = module.vpc.vpc_id
#   target_type = "ip"
#   health_check {
#     path = "/health"
#   }
# }

# resource "aws_lb_target_group" "blog_frontend" {
#   name        = "tg-blog-frontend"
#   port        = 80
#   protocol    = "HTTP"
#   vpc_id      = module.vpc.vpc_id
#   target_type = "ip"
#   health_check {
#     path = "/"
#   }
# }

resource "aws_lb_listener" "http" {
  load_balancer_arn = module.alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.frontend.arn
  }
}

resource "aws_lb_listener_rule" "api" {
  listener_arn = aws_lb_listener.http.arn
  priority     = 10

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.backend.arn
  }

  condition {
    path_pattern { values = ["/api/*"] }
  }
}
