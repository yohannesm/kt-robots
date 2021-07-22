/**
 * This file is part of the ONEMA ktrobots Package.
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
import kotlin.math.max
import kotlin.random.Random

/**
 * HotShot will do up to 2 scans per turn. If a target if found the scan resolution is halved allowing
 * the robot to refine the targeting area for the next turn.
 * If no targets are found, it will rotate the scan heading.
 * If after a full 360 sweep to targets are detected, HotShot will move to a new location
 * If HotShot is hit, it will move to a new location
 * HotShot will only shoot if the cooldawn is 0 and a target has been detected
 */
class HotShot : Robot(), LambdaRobotFunction {

    //--- Methods ---
    override fun getBuild(state: LambdaRobotState): Pair<LambdaRobotBuild, LambdaRobotState> {
        return LambdaRobotBuild(
            name = "HotShot",
            armor = LambdaRobotArmorType.medium,
            engine = LambdaRobotEngineType.large,
            radar = LambdaRobotRadarType.shortRange ,
            missile = LambdaRobotMissileType.javelin
        ) to state
    }

    override fun getAction(state: LambdaRobotState): Pair<LambdaRobotAction, LambdaRobotState> {

        // Check if the state need to be initialized
        if (!state.initialized) {
            state.initialized = true
            state.noHitSweep = 0.0
            state.scanResolution = robot.radar.maxResolution
            state.scanHeading = angleToXY(gameInfo.boardWidth / 2, gameInfo.boardHeight / 2)
        }

        // scan left scan area
        val leftScan = scan(state.scanHeading - state.scanResolution, state.scanResolution)

        val scan = if(leftScan.found && leftScan.distance > gameInfo.farHitRange && leftScan.distance <= robot.missile.range) {
            log("Target found", "scanHeading = ${state.scanHeading}, range = ${leftScan.distance}")

            // update scan heading
            state.scanHeading = normalizeAngle(state.scanHeading - state.scanResolution)

            // narrow scan resolution
            state.scanResolution = max(0.1, state.scanResolution / 2.0)
            state.targetRange = leftScan.distance
            leftScan
        } else {
            // scan right
            val rightScan = scan(state.scanHeading + state.scanResolution, state.scanResolution)
            if (rightScan.found && rightScan.distance > gameInfo.farHitRange && rightScan.distance <= robot.missile.range) {
                log("Target found", "scanHeading = ${state.scanHeading}, range = ${rightScan.distance}")

                // update scan heading
                state.scanHeading = normalizeAngle(state.scanHeading + state.scanResolution)

                // narrow scan resolution
                state.scanResolution = max(0.1, state.scanResolution / 2.0)
                state.targetRange = rightScan.distance
                rightScan
            } else {
               log("No target found", "scanHeading = ${state.scanHeading}")

                // look into adjacent area
                state.scanHeading = normalizeAngle(state.scanHeading + 3.0 * robot.radar.maxResolution)

                // reset resolution to max
                state.scanResolution = robot.radar.maxResolution

                // increase the no-it sweep tracker so we know when to move
                state.noHitSweep = state.noHitSweep + 2.0 * robot.radar.maxResolution
                rightScan
            }
        }

        // Check if a target was found and if the reload cooldown is 0
        val action = if (scan.found && robot.canFire()) {

            // reset sweep tracker to indicate we found something
            state.noHitSweep = 0.0

            // Fire a missile where the target was detected
            LambdaRobotAction().fireMissile(scan.heading, scan.distance)
        } else {
            LambdaRobotAction()
        }

        // Check if we are not moving and take action
        if (state.gotoX == 0.0 || state.gotoY == 0.0) {
            val wasHurt = robot.damage > state.lastDamage
            if (wasHurt) {
                log("Damage detected", "taking evasive action")

                // Take evasive action
                state.gotoX = gameInfo.collisionRange + Random.nextDouble() * (gameInfo.boardWidth - 2.0 * gameInfo.collisionRange)
                state.gotoY = gameInfo.collisionRange + Random.nextDouble() * (gameInfo.boardHeight - 2.0 * gameInfo.collisionRange)
            } else if (state.noHitSweep >= 360 && (state.gotoX == 0.0 || state.gotoY == 0.0)) {
                log("Nothing found in immediate surroundings", "Moving to new location")

                // Move to a new random location on the board
                state.gotoX = gameInfo.collisionRange + Random.nextDouble() * (gameInfo.boardWidth - 2.0 * gameInfo.collisionRange)
                state.gotoY = gameInfo.collisionRange + Random.nextDouble() * (gameInfo.boardHeight - 2.0 * gameInfo.collisionRange)
            }
        }
        state.lastDamage = robot.damage

        // Check if we are moving and have reached our destination
        return if(state.gotoX != 0.0 || state.gotoY != 0.0) {

            // reset sweep tracker while moving
            state.noHitSweep = 0.0

            // we've reached our destination, stop moving
            val newAction =  action.moveToXY(state.gotoX, state.gotoY)
            if (newAction.arrivedAtDestination) {
                state.gotoX = 0.0
                state.gotoY = 0.0
            }

            // Return action and state
            newAction to state
        } else {

            // Return action and state
            action to state
        }
    }
}