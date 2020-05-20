provider "aws" {
  version = ">= 2.52"
  region  = var.region
}

# ----------------------------------------
# THIS IS YOUR ROBOT!
# You may change the "function_name"
# just make sure that it matches
# the name of your class!
# DO NOT MODIFY THE MODULE NAME
# ----------------------------------------
module "BringYourOwnRobot" {
  source = "./modules/robot"
  dynamodb_table = module.robot_state_table.table_name
  function_name = "BringYourOwnRobot"
  region = var.region
}

# ----------------------------------------
# ########## DO NOT MODIFY ###############
# ----------------------------------------
# ----------------------------------------
# Example robots
# ----------------------------------------
module "HotShot" {
  source = "./modules/robot"
  dynamodb_table = module.robot_state_table.table_name
  function_name = "HotShot"
  region = var.region
}

module "RoboDog" {
  source = "./modules/robot"
  dynamodb_table = module.robot_state_table.table_name
  function_name = "RoboDog"
  region = var.region
}

module "YosemiteSam" {
  source = "./modules/robot"
  dynamodb_table = module.robot_state_table.table_name
  function_name = "YosemiteSam"
  region = var.region
}

module "TargetRobot" {
  source = "./modules/robot"
  dynamodb_table = module.robot_state_table.table_name
  function_name = "TargetRobot"
  region = var.region
}
# ----------------------------------------
# The base class for your function will
# check for this table.
# ----------------------------------------
# Create a dynamodb table that can be used
# To record the state of your robot
# ----------------------------------------
module "robot_state_table" {
  source = "./modules/game_state"
}
