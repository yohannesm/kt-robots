data "aws_caller_identity" "current" {}


locals {
  dyanmodb_table      = "arn:aws:dynamodb:*:${data.aws_caller_identity.current.account_id}:table/${var.dynamodb_table}"
  dynamodb_stream     = "arn:aws:dynamodb:*:${data.aws_caller_identity.current.account_id}:table/${var.dynamodb_table}/index/*"
}

data "aws_iam_policy_document" "dynamodb" {
  statement {
    effect = "Allow"

    actions = [
      "dynamodb:PutItem",
      "dynamodb:UpdateItem",
      "dynamodb:GetItem",
      "dynamodb:DeleteItem",
      "dynamodb:Query"
    ]

    resources = [
      local.dyanmodb_table,
      local.dynamodb_stream
    ]
  }
}

module "lambda" {
  source        = "claranet/lambda/aws"
  version       = "1.2.0"
  function_name = var.function_name
  description   = "${var.function_name} lambda function"
  handler       = "io.onema.ktrobots.lambda.functions.${var.function_name}"
  runtime       = "java11"
  timeout       = 15
  memory_size   = 1536

  // Specify a file or directory for the source code.
  source_path   = "${path.root}/../build/libs/lambda-robots-dev-all.jar"
  build_command = "cp '$source' '$filename' "

  // Add additional trusted entities for assuming roles (trust relationships).
  trusted_entities = ["lambda.amazonaws.com"]

  // Attach a policy.
  policy = {
    json = data.aws_iam_policy_document.dynamodb.json
  }

  // Add environment variables.
  environment = {
    variables = {
      GAME_STATE_TABLE = var.dynamodb_table
    }
  }
}

# ----------------------------------------
# WARNING: The following commands attach
# Invoke policy to allow anyone to invoke
# this function. This is done so that
# the game server can invoke from a
# different account
# ----------------------------------------
resource "null_resource" "lambda_created" {
  provisioner "local-exec" {
    command = "aws lambda add-permission --function-name ${module.lambda.function_arn} --action lambda:InvokeFunction --statement-id ${var.function_name}-invoke-function --principal '*' --region ${var.region}"
  }

  provisioner "local-exec" {
    when = destroy
    command = "aws lambda remove-permission --function-name ${module.lambda.function_arn} --statement-id ${var.function_name}-invoke-function --region ${var.region}"
  }
}
