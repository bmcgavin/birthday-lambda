data "local_file" "archive" {
  filename = "${path.module}/../../.holy-lambda/build/latest.zip"
}

resource "aws_lambda_function" "lambda" {
  architectures = [
    "x86_64",
  ]
  environment {
    variables = var.variables
  }
  function_name                  = "${var.name}-${var.env}"
  filename                       = "${path.module}/../../.holy-lambda/build/latest.zip"
  runtime                        = var.runtime
  handler                        = "bmcgavin.birthday.api.LambdaEntrypoint"
  layers                         = []
  memory_size                    = 256
  reserved_concurrent_executions = -1
  role                           = aws_iam_role.lambda.arn
  source_code_hash               = data.local_file.archive.content_base64sha256
  tags                           = {}
  tags_all                       = {}
  timeout                        = 60
  tracing_config {
    mode = "PassThrough"
  }
  depends_on = [data.local_file.archive]
}

resource "aws_lambda_permission" "lambda" {
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "arn:aws:execute-api:${var.region}:${data.aws_caller_identity.current.account_id}:${aws_api_gateway_rest_api.api.id}/*/*"
  statement_id  = "${var.name}-${var.env}"
}
