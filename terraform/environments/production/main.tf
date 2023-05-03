locals {
  name = "birthday"
  env  = "production"
}

module "environment" {
  source = "../../environment"

  name    = local.name
  env     = local.env
  region  = var.region
  vpc_id  = var.vpc_id
  runtime = "provided"
  variables = {
    DB_TYPE              = "dynamodb"
    DB_ENDPOINT_HOST     = "dynamodb.${var.region}.amazonaws.com"
    DB_ENDPOINT_PORT     = "443"
    DB_ENDPOINT_PROTOCOL = "https"
  }
  own_api_gateway_account = false
}
