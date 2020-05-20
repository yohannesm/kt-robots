variable "function_name" {
  description = "The name of the robot without spaces"
  type = string
}

variable "dynamodb_table" {
  description = "The name of the robot state dynamodb table"
  type = string
}

variable "region" {
  type = string
}
