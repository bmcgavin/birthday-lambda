resource "aws_dynamodb_table" "birthday" {
  name     = "${var.name}-${var.env}"
  hash_key = "username"

  billing_mode   = "PROVISIONED"
  read_capacity  = 1
  write_capacity = 1

  attribute {
    name = "username"
    type = "S"
  }

}
