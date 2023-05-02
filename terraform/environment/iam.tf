data "aws_caller_identity" "current" {}

locals {
  split    = split(var.name, "-")
  initials = [for s in local.split : substr(s, 0, 1)]
  iam_name = length(var.name) > 30 ? join("", local.initials) : var.name
}

data "aws_iam_policy_document" "lambda_execution_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
    effect = "Allow"
  }
}

data "aws_iam_policy_document" "lambda_execution_logs" {
  #   statement {
  #     actions = [
  #       "ec2:DescribeNetworkInterfaces",
  #       "ec2:CreateNetworkInterface",
  #       "ec2:DeleteNetworkInterface",
  #       "ec2:DescribeInstances",
  #       "ec2:AttachNetworkInterface",
  #     ]
  #     resources = [
  #       "*",
  #     ]
  #   }
  statement {
    actions = [
      "logs:CreateLogStream",
      "logs:CreateLogGroup",
    ]
    resources = [
      "arn:aws:logs:${var.region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${var.name}-${var.env}*:*",
    ]
  }
  statement {
    actions = [
      "logs:PutLogEvents",
    ]
    resources = [
      "arn:aws:logs:${var.region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${var.name}-${var.env}*:*:*",

    ]
  }
  statement {
    actions = [
      "ssm:GetParametersByPath",
      "ssm:GetParameter",
    ]
    resources = [
      "arn:aws:ssm:*:${data.aws_caller_identity.current.account_id}:parameter/*"
    ]
  }
  statement {
    actions = [
      "dynamodb:GetItem",
      "dynamodb:PutItem"
    ]
    resources = [
      "arn:aws:ssm:*:${data.aws_caller_identity.current.account_id}:table/*"
    ]
  }
}

resource "aws_iam_policy" "lambda_execution_logs" {
  name   = "${var.name}-${var.env}"
  policy = data.aws_iam_policy_document.lambda_execution_logs.json
}

resource "aws_iam_role" "lambda" {
  assume_role_policy    = data.aws_iam_policy_document.lambda_execution_assume_role.json
  force_detach_policies = false
  max_session_duration  = 3600
  name                  = "${local.iam_name}-${var.env}-${var.region}-lambdaRole"
  path                  = "/service-role/"
}

resource "aws_iam_role_policy_attachment" "lambda" {
  role       = aws_iam_role.lambda.name
  policy_arn = aws_iam_policy.lambda_execution_logs.arn

}
