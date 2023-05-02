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
}
