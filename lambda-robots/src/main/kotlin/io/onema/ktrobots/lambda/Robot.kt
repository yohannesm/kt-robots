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

package io.onema.ktrobots.lambda

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.onema.ktrobots.commons.domain.*
import io.onema.ktrobots.commons.utils.angleToXY
import io.onema.ktrobots.commons.utils.distanceToXY
import io.onema.ktrobots.commons.utils.normalizeAngle180
import io.onema.ktrobots.lambda.utils.LambdaRobotStateTable
import io.onema.ktrobots.lambda.utils.ScanClient
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random


/**
 * Base robot class. This has the entry point to the robot and is in charge of loading and saving it's state
 * It also provides several convenience methods including a scan method to talk to the game server.
 */
abstract class Robot {

    //--- Properties ---
    private val mapper: ObjectMapper
        get() = jacksonObjectMapper()

    private val table: LambdaRobotStateTable
        get() = LambdaRobotStateTable()

    //--- Fields ---
    protected lateinit var robot: LambdaRobot

    protected lateinit var gameInfo: GameInfo

    private val httpClient = HttpClient() {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 700
        }
    }

    private lateinit var scanClient: ScanClient

    //--- Methods ---

    /**
     * Get the build of the robot. Returns a lambda robot build and lambda robot state pair
     */
    abstract fun getBuild(state: LambdaRobotState): Pair<LambdaRobotBuild, LambdaRobotState>

    /**
     * Get the next action for the robot. Returns a lambda robot action and lambda robot state pair
     */
    abstract fun getAction(state: LambdaRobotState): Pair<LambdaRobotAction, LambdaRobotState>

    /**
     * Handle the lambda request to get the build or action of the robot
     */
    fun handle(request: LambdaRobotRequest): LambdaRobotResponse {
        robot = request.lambdaRobot
        log("Request", mapper.writeValueAsString(request))
        log("Table name", System.getenv("GAME_STATE_TABLE"))
        val robotId: String = robot.id ?: LambdaRobot.generateId(request.index, request.gameId)
        gameInfo = request.gameInfo
        scanClient = ScanClient(
            request.gameInfo.apiUrl,
            request.gameId,
            robotId,
            httpClient
        )

        log("Robot ID check", robotId)
        val record: LambdaRobotStateRecord = table
            .getById(robotId)
            .orElse(LambdaRobotStateRecord(robotId))
        log("Robot state", mapper.writeValueAsString(record))
        val state = record.state

        val (newState, response) = when(request.command) {
            LambdaRobotCommand.getBuild -> {
                logger.info("Calling get build")
                val (build, newState) = getBuild(state)
                Pair(newState, LambdaRobotResponse(robotBuild = build))
            }
            LambdaRobotCommand.getAction -> {
                logger.info("Calling action")
                val (action, newState) = getAction(state)
                Pair(newState, LambdaRobotResponse(robotAction = action))
            }
        }
        logger.info("Saving new state")
        table.save(record.copy(state = newState))
        return response
    }

    /**
     * Call the game server to request the distance and heading to the
     * nearest object if one was found
     */
    fun scan(heading: Double, resolution: Double): ScanEnemiesResponse {
        val response = scanClient.scan(heading, resolution)
        log("Scan", "heading = $heading, resolution = $resolution, scan result = $response")
        return response
    }

    /**
     * Determine the angle in degrees relative to the current robot position
     * Returns a value between -180 and 180 degrees
     */
    fun angleToXY(x: Double, y: Double): Double {
        return angleToXY(x, y, robot)
    }

    /**
     * Determine the distance relative to the current robot position
     */
    fun distanceToXY(x: Double, y: Double): Double {
        return distanceToXY(x, y, robot)
    }

    /**
     * Normalize angle to be between -180 and 180
     */
    fun normalizeAngle(angle: Double): Double {
        return normalizeAngle180(angle)
    }

    /**
     * Extension Fire a missile in a given direction with impact at a given distance.
     * A missile can only be fired in the robot reload cooldown is 0
     */
    fun LambdaRobotAction.fireMissile(heading: Double, distance: Double): LambdaRobotAction {
        log("Firing missile", "fireMissileHeading = $heading, fireMissileDistance = $distance")
        return this.copy(fireMissileHeading = heading, fireMissileDistance = distance, fired = true)
    }

    /**
     * LambdaRobotAction extension function to fire a missile at the given position
     */
    fun LambdaRobotAction.fireMissileToXY(x: Double, y: Double): LambdaRobotAction {
        val heading = angleToXY(x, y)
        val distance = distanceToXY(x, y)
        return this.fireMissile(heading, distance)
    }

    fun log(index: String, message: String) {
        logger.info("${robot.name}-${robot.index}: $index: $message")
    }

    /**
     * LambdaRobotAction extension function to move the robot at the given position
     */
    fun LambdaRobotAction.moveToXY(x: Double, y: Double): LambdaRobotAction{
        val heading = angleToXY(x, y)
        val distance = distanceToXY(x, y)
        val (speed, arrived) = if (distance <= gameInfo.collisionRange) {
            0.0 to true
        } else {
            sqrt(distance * 2.0 * robot.deceleration()) * gameInfo.secondsPerTurn to false
        }
        log("Move to", "X=$x, Y=$y, heading=$heading, distance=$distance")

        return if (abs(normalizeAngle(robot.heading - heading)) > 0.1) {
            val newSpeed = min(robot.maxTurnSpeed, speed)
            if (robot.speed <= robot.maxTurnSpeed) {
                this.copy(heading = heading, speed = newSpeed, arrivedAtDestination = arrived)
            } else {
                this.copy(speed = newSpeed, arrivedAtDestination = arrived)
            }
        } else {
            this.copy(speed = speed, arrivedAtDestination = arrived, heading = heading)
        }
    }

    /**
     * Check if the robot is too close to the game board border
     * and return a new random heading if it does
     *
     * @param minDistanceToEdge the minimum distance to check for before
     *  making a turn, the value must be greater than 0 to be taken into consideration
     */
    fun getNewHeading(minDistanceToEdge: Int = 100): Double  = when {
        minDistanceToEdge < 0 -> {
            robot.heading
        }
        robot.x < minDistanceToEdge -> {
            // Too close to the left, turn right
            45.0 + Random.nextDouble() * 90.0
        }
        robot.x > (gameInfo.boardWidth - minDistanceToEdge) -> {
            // Too close to the right, turn left
            -45.0 - Random.nextDouble() * 90.0
        }
        robot.y < minDistanceToEdge -> {
            // Too close to the bottom, turn up
            -45.0 + Random.nextDouble() * 90.0
        }
        robot.y > (gameInfo.boardHeight - minDistanceToEdge) -> {
            // Too close to the top, turn down
            135.0 + Random.nextDouble() * 90.0
        }
        else -> {
            robot.heading
        }
    }

    companion object {
        val logger: Logger = LogManager.getLogger(LambdaRobotFunction::class.java)
    }
}
