resource "aws_db_subnet_group" "db" {
  name       = "app-db-subnets"
  subnet_ids = module.vpc.database_subnets
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
  db_name                = "mydb" # Corrected attribute for database name
}
