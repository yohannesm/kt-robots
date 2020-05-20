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

class TargetRobot : Robot(), LambdaRobotFunction {

    private val NAME = "TargetRobot"

    override fun getBuild(state: LambdaRobotState): Pair<LambdaRobotBuild, LambdaRobotState> {
        return LambdaRobotBuild(
            name = NAME,
            armor = LambdaRobotArmorType.heavy,
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