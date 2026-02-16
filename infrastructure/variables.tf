# variable "acm_cert_arn" { type = string }
# variable "db_user" {
#   description = "Database username"
#   default     = "root"
# }

variable "db_password" {
  description = "Database password"
  default = "vivekbhaskar"
  validation {
    condition = length(var.db_password)>=8
    error_message = "db_password must be 8 characters"
  }
}

# variable "blog_backend_image" {
#   description = "Container image for the blog generator backend"
#   type        = string
# }

# variable "blog_frontend_image" {
#   description = "Container image for the blog generator frontend"
#   type        = string
# }

output "db_host" {
  value       = aws_db_instance.postgres.endpoint
  description = "RDS endpoint for the database"
}
