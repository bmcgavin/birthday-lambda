resource "aws_cloudwatch_log_group" "apigateway" {
  name              = "API-Gateway-Execution-Logs_${aws_api_gateway_rest_api.api.id}/${var.env}"
  retention_in_days = 7
  tags = {
    env     = var.env
    service = var.name
  }
}

resource "aws_cloudwatch_log_group" "apigateway_access" {
  name              = "/aws/api-gateway/${var.name}-${var.env}"
  retention_in_days = 0
  tags = {
    env     = var.env
    service = var.name
  }
  tags_all = {}
}
