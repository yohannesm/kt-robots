output "YourRobotARN" {
  description = "The is the ARN of your robot, you will need this to run your game server"
  value = module.BringYourOwnRobot.robot_arn
}

output "YosemiteSamRobotArn" {
  value = module.YosemiteSam.robot_arn
}

output "HotShotRobotArn" {
  value = module.HotShot.robot_arn
}

output "RoboDogRobotArn" {
  value = module.RoboDog.robot_arn
}

output "TargetRobotArn" {
  value = module.TargetRobot.robot_arn
}
