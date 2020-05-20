# ----------------------------------------
# Create an on-demand dynamo table that
# will be used to store the game state
# ----------------------------------------
resource "aws_dynamodb_table" "robot_state" {
  name           = "KT-Robots-State"
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "robotId"

  attribute {
    name = "robotId"
    type = "S"
  }

  ttl {
    attribute_name = "expire"
    enabled        = true
  }
}
