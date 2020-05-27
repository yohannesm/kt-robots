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

/**
 * This is your robot, update the build and ensure you add custom logic to
 * move, evade, attack, scan and more.
 */
class BringYourOwnRobot : Robot(), LambdaRobotFunction {

    //--- Fields ---
    private val NAME = "BringYourOwnRobot"

    //--- Methods ---
    override fun getBuild(state: LambdaRobotState): Pair<LambdaRobotBuild, LambdaRobotState> {
        return LambdaRobotBuild(
            name = NAME,
            armor = LambdaRobotArmorType.ultraHeavy,
            engine = LambdaRobotEngineType.economy,
            radar = LambdaRobotRadarType.ultraShortRange,
            missile = LambdaRobotMissileType.dart
        ) to state.initialize()
    }

    override fun getAction(state: LambdaRobotState): Pair<LambdaRobotAction, LambdaRobotState> {

        /**
         * Bring your robot to life!
         */
        return LambdaRobotAction() to state
    }
}