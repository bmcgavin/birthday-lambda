output "lambda_invoke_url" {
  value = "${module.environment.api_gateway_invoke_url}${local.env}/"
}
