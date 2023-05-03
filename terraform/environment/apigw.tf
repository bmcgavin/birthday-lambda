resource "aws_api_gateway_account" "account" {
  cloudwatch_role_arn = aws_iam_role.api_gateway_cloudwatch.arn
}

resource "aws_api_gateway_deployment" "api" {
  rest_api_id = aws_api_gateway_rest_api.api.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.api_hello,
      aws_api_gateway_resource.api_hello_proxy,
      aws_api_gateway_method.api_hello_proxy_get,
      aws_api_gateway_integration.api_hello_proxy_get,
      aws_api_gateway_method.api_hello_proxy_put,
      aws_api_gateway_integration.api_hello_proxy_put,
      aws_api_gateway_rest_api.api
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_resource" "api_hello" {
  parent_id   = aws_api_gateway_rest_api.api.root_resource_id
  path_part   = "hello"
  rest_api_id = aws_api_gateway_rest_api.api.id
}

resource "aws_api_gateway_resource" "api_hello_proxy" {
  parent_id   = aws_api_gateway_resource.api_hello.id
  path_part   = "{proxy+}"
  rest_api_id = aws_api_gateway_rest_api.api.id
}

resource "aws_api_gateway_method" "api_hello_proxy_get" {
  api_key_required     = false
  authorization        = "NONE"
  authorization_scopes = []
  http_method          = "GET"
  request_models       = {}
  request_parameters   = {}
  resource_id          = aws_api_gateway_resource.api_hello_proxy.id
  rest_api_id          = aws_api_gateway_rest_api.api.id
}

resource "aws_api_gateway_integration" "api_hello_proxy_get" {
  cache_key_parameters    = []
  cache_namespace         = aws_api_gateway_resource.api_hello_proxy.id
  connection_type         = "INTERNET"
  http_method             = aws_api_gateway_method.api_hello_proxy_get.http_method
  integration_http_method = "POST"
  passthrough_behavior    = "WHEN_NO_MATCH"
  request_parameters      = {}
  request_templates       = {}
  resource_id             = aws_api_gateway_resource.api_hello_proxy.id
  rest_api_id             = aws_api_gateway_rest_api.api.id
  timeout_milliseconds    = 29000
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.lambda.invoke_arn
}

resource "aws_api_gateway_method" "api_hello_proxy_put" {
  api_key_required     = false
  authorization        = "NONE"
  authorization_scopes = []
  http_method          = "PUT"
  request_models       = {}
  request_parameters   = {}
  resource_id          = aws_api_gateway_resource.api_hello_proxy.id
  rest_api_id          = aws_api_gateway_rest_api.api.id
}

resource "aws_api_gateway_integration" "api_hello_proxy_put" {
  cache_key_parameters    = []
  cache_namespace         = aws_api_gateway_resource.api_hello_proxy.id
  connection_type         = "INTERNET"
  http_method             = aws_api_gateway_method.api_hello_proxy_put.http_method
  integration_http_method = "POST"
  passthrough_behavior    = "WHEN_NO_MATCH"
  request_parameters      = {}
  request_templates       = {}
  resource_id             = aws_api_gateway_resource.api_hello_proxy.id
  rest_api_id             = aws_api_gateway_rest_api.api.id
  timeout_milliseconds    = 29000
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.lambda.invoke_arn
}

resource "aws_api_gateway_rest_api" "api" {
  api_key_source               = "HEADER"
  binary_media_types           = []
  disable_execute_api_endpoint = false
  minimum_compression_size     = -1
  name                         = "${var.name}-${var.env}"
  tags = {
    "STAGE" = var.env
  }

  endpoint_configuration {
    types = [
      "EDGE",
    ]
  }
}

resource "aws_api_gateway_stage" "api" {
  depends_on            = [aws_cloudwatch_log_group.apigateway]
  cache_cluster_enabled = false
  deployment_id         = aws_api_gateway_deployment.api.id
  rest_api_id           = aws_api_gateway_rest_api.api.id
  stage_name            = var.env
  tags                  = {}
  tags_all              = {}
  variables             = {}
  xray_tracing_enabled  = false
  access_log_settings {
    destination_arn = aws_cloudwatch_log_group.apigateway_access.arn
    format          = "{\"requestId\":\"$$context.requestId\",\"ip\":\"$$context.identity.sourceIp\",\"caller\":\"$$context.identity.caller\",\"user\":\"$$context.identity.user\",\"requestTime\":$$context.requestTimeEpoch,\"httpMethod\":\"$$context.httpMethod\",\"resourcePath\":\"$$context.resourcePath\",\"status\":$$context.status,\"protocol\":\"$$context.protocol\",\"responseLength\":$$context.responseLength}"
  }
}

resource "aws_api_gateway_method_settings" "api" {
  method_path = "*/*"
  rest_api_id = aws_api_gateway_rest_api.api.id
  stage_name  = aws_api_gateway_stage.api.stage_name

  settings {
    cache_data_encrypted                       = false
    cache_ttl_in_seconds                       = 300
    caching_enabled                            = false
    data_trace_enabled                         = false
    logging_level                              = "INFO"
    metrics_enabled                            = false
    require_authorization_for_cache_control    = true
    throttling_burst_limit                     = 5000
    throttling_rate_limit                      = 10000
    unauthorized_cache_control_header_strategy = "SUCCEED_WITH_RESPONSE_HEADER"
  }
}
