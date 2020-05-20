/**
 * This file is part of the ONEMA hello Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.ktrobots.lambda.functions

import io.onema.ktrobots.commons.domain.*
import io.onema.ktrobots.lambda.LambdaRobotFunction
import io.onema.ktrobots.lambda.Robot
import kotlin.random.Random

class YosemiteSam : Robot(), LambdaRobotFunction {
    override fun getBuild(state: LambdaRobotState): Pair<LambdaRobotBuild, LambdaRobotState> {
        return Pair(LambdaRobotBuild(
            name = "YosemiteSam",
            armor = LambdaRobotArmorType.light,
            engine = LambdaRobotEngineType.extraLarge,
            radar = LambdaRobotRadarType.ultraShortRange,
            missile = LambdaRobotMissileType.dart
        ), state.initialize())
    }

    override fun getAction(state: LambdaRobotState): Pair<LambdaRobotAction, LambdaRobotState> {
        val speed = if(robot.speed == 0.0) {
            robot.maxTurnSpeed
        } else {
            Random.nextDouble() * robot.maxSpeed
        }

        // Check if the robot needs to turn
        val heading = when {
            robot.x < 100 -> {
                // Too close to the left, turn right
                45.0 + Random.nextDouble() * 90.0
            }
            robot.x > (gameInfo.boardWidth - 100) -> {
                // Too close to the right, turn left
                -45.0 - Random.nextDouble() * 90.0
            }
            robot.y < 100 -> {
                // Too close to the bottom, turn up
                -45.0 + Random.nextDouble() * 90.0
            }
            robot.y > (gameInfo.boardHeight - 100) -> {
                // Too close to the top, turn down
                135.0 + Random.nextDouble() * 90.0
            }
            else -> {
                robot.heading
            }
        }
        val action = if(robot.reloadCoolDown == 0.0) {
            LambdaRobotAction(speed, heading).fireMissile(
                heading = Random.nextDouble() * 360.0,
                distance = 50.0 + Random.nextDouble() * robot.missile.range
            )
        } else {
            LambdaRobotAction(speed, heading)
        }

        // YosemiteSam doesn't have state
        return action to state
    }
}