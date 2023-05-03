variable "name" {
  type = string
}

variable "env" {
  type = string
}

variable "region" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "runtime" {
  type = string
}

variable "variables" {
  type = map(string)
}

variable "own_api_gateway_account" {
  type = bool
  default = true
}
