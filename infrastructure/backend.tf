terraform {
  backend "s3" {
    bucket        = "enops-tf-state-bucket"
    key           = "enopse2e/infrastructure/terraform.tfstate"
    region        = "us-east-1"
    encrypt       = true

    # New way (Terraform ≥ 1.9/1.10): use S3 object locking as a lockfile
    use_lockfile  = true
  }
}