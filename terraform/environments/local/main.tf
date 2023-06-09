locals {
  name = "birthday"
  env  = "local"
}

module "environment" {
  source = "../../environment"

  name    = local.name
  env     = local.env
  region  = "us-east-1"
  vpc_id  = ""
  runtime = "provided"
  variables = {
    DB_TYPE              = "dynamodb"
    DB_ENDPOINT_HOST     = "$LOCALSTACK_HOSTNAME"
    DB_ENDPOINT_PORT     = "4566"
    DB_ENDPOINT_PROTOCOL = "http"
  }
}
