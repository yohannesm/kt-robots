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

/**
 * RoboDog chases after a target if it finds one, it has an ultra short range radar, which means that it needs
 * to be very close to it's target before it chases
 */
class RoboDog : Robot(), LambdaRobotFunction {

    //--- Methods ---
    override fun getBuild(state: LambdaRobotState): Pair<LambdaRobotBuild, LambdaRobotState> {
        return LambdaRobotBuild(
            name = "RoboDog",
            armor = LambdaRobotArmorType.light,
            engine = LambdaRobotEngineType.standard,
            radar = LambdaRobotRadarType.ultraShortRange,
            missile = LambdaRobotMissileType.cannon
        ) to state.copy(initialized = true)
    }

    override fun getAction(state: LambdaRobotState): Pair<LambdaRobotAction, LambdaRobotState> {
        val speed = if(robot.speed == 0.0) {
            robot.maxTurnSpeed
        } else {
            Random.nextDouble() * robot.maxSpeed
        }

        val scan = scan(robot.heading, robot.radar.maxResolution)

        // Check if the robot needs to turn
        val heading = getNewHeading(20)
        val scanHeading = if(scan.found) scan.heading else heading
        val action = if(robot.reloadCoolDown == 0.0) {
            val distance =
                if (scan.distance > 20 && scan.distance < 300)
                    scan.distance
                else
                    50.0 + Random.nextDouble() * (robot.missile.range - 10.0)
            LambdaRobotAction(speed, scanHeading).fireMissile(scanHeading, distance)
        } else {
            LambdaRobotAction(speed, scanHeading)
        }

        // roboDog doesn't have state
        return action to state
    }
}