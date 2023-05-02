locals {
  api_id = module.environment.api_id
}
output "lambda_invoke_url" {
  value = "http://${local.api_id}.execute-api.localhost.localstack.cloud:4566/local/"
}
