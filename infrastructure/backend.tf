terraform {
  backend "s3" {
    bucket         = "enops-tf-state-bucket"
    key            = "enopse2e/infrastructure/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}