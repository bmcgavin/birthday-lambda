output "api_gateway_invoke_url" {
  value = aws_api_gateway_deployment.api.invoke_url
}

output "api_id" {
  value = aws_api_gateway_rest_api.api.id
}

output "stage_name" {
  value = aws_api_gateway_deployment.api.stage_name
}
