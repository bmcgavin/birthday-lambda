resource "aws_security_group" "lambda_sg" {
  description = "Lambda SG"
  egress      = []
  ingress     = []
  name        = "${var.name}-${var.env}"
  tags        = {}
  tags_all    = {}
  vpc_id      = var.vpc_id

  timeouts {}
}
