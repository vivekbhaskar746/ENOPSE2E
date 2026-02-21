resource "aws_db_subnet_group" "db" {
  name       = "app-db-subnets"
  subnet_ids = module.vpc.database_subnets
}

resource "aws_security_group" "rds_sg" {
  name   = "rds-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    security_groups = [aws_security_group.ecs_backend_sg.id]
  }

  ingress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_db_instance" "postgres" {
  identifier             = "app-postgres"
  engine                 = "mysql"
  instance_class         = "db.t4g.micro"
  allocated_storage      = 20
   username               = "root" # Updated username
  password               = "vivekbhaskar" # Updated password
  skip_final_snapshot    = true
  db_subnet_group_name   = aws_db_subnet_group.db.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  db_name                = "mydb" # Corrected attribute for database name
}
