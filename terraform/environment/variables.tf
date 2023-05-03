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
