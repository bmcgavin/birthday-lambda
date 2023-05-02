resource "aws_dynamodb_table" "birthday" {
  name      = var.name
  hash_key  = "username"
  range_key = "birthday"

  billing_mode   = "PROVISIONED"
  read_capacity  = 20
  write_capacity = 20

  attribute {
    name = "username"
    type = "S"
  }

  attribute {
    name = "birthday"
    type = "S"
  }
}
