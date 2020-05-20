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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.onema.ktrobots.commons.domain.*
import io.onema.ktrobots.commons.utils.angleToXY
import io.onema.ktrobots.commons.utils.distanceToXY
import io.onema.ktrobots.commons.utils.normalizeAngle180
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import java.util.*
import kotlin.math.*


abstract class Robot {

    //--- Fields ---
    val mapper: ObjectMapper
        get() = jacksonObjectMapper()

    val table: LambdaRobotStateTable
        get() = LambdaRobotStateTable()

    protected lateinit var robot: LambdaRobot
    protected lateinit var gameInfo: GameInfo

    private val httpClient = HttpClient() {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.NONE
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 120000
        }
    }

    private lateinit var scanClient: ScanClient

    //--- Methods ---
    abstract fun getBuild(state: LambdaRobotState): Pair<LambdaRobotBuild, LambdaRobotState>

    abstract fun getAction(state: LambdaRobotState): Pair<LambdaRobotAction, LambdaRobotState>

    fun handle(request: LambdaRobotRequest): LambdaRobotResponse {
        log.info("Request: ${mapper.writeValueAsString(request)}")
        log.info("Table name ${System.getenv("GAME_STATE_TABLE")}")
        robot = request.lambdaRobot
        gameInfo = request.gameInfo
        scanClient = ScanClient(request.gameInfo.apiUrl, request.gameId, robot.id, httpClient, mapper)

        val robotId = if(request.lambdaRobot.id.isEmpty())  RobotFactory.generateId(request.index, request.gameId) else request.lambdaRobot.id
        log.info("Checking for Robot ID: $robotId")
        val record: LambdaRobotStateRecord = table
            .getById(robotId)
            .orElse(LambdaRobotStateRecord(robotId))
        log.info("Robot state ${mapper.writeValueAsString(record)}")
        var state = record.state

        val (newState, response) = when(request.command) {
            LambdaRobotCommand.getBuild -> {
                log.info("Calling get build")
                val (build, newState) = getBuild(state)
                Pair(newState, LambdaRobotResponse(robotBuild = build))
            }
            LambdaRobotCommand.getAction -> {
                log.info("Calling action")
                val (action, newState) = getAction(state)
                Pair(newState, LambdaRobotResponse(robotAction = action))
            }
        }
        log.info("Saving new state")
        table.save(record.copy(state = newState))
        return response
    }

    /**
     * @return distance to the nearest object
     */
    fun scan(heading: Double, resolution: Double): ScanEnemiesResponse {
        val response = scanClient.scan(heading, resolution)
        log.info("Scan: heading = $heading, resolution = $resolution, scan result = $response")
        return response
    }

    /**
     * Determine the angle in degrees relative to the current robot position
     * Returns a value between -180 and 180 degrees
     */
    fun angleToXY(x: Double, y: Double): Double {
//        return normalizeAngle(atan2(x - robot.x, y - robot.y) * 180.0 / PI)
        return angleToXY(x, y, robot)
    }

    /**
     * Determine the distance relative to the current robot position
     */
    fun distanceToXY(x: Double, y: Double): Double {
//        val deltaX = x - robot.x
//        val deltaY = y - robot.y
//        return sqrt(deltaX.pow(2.0) + deltaY.pow(2.0))
        return distanceToXY(x, y, robot)
    }

    /**
     * Normalize angle to be between -180 and 180
     */
    fun normalizeAngle(angle: Double): Double {
//        val result = angle % 360.0
//        return if(result < -180.0) result + 360.0 else result
        return normalizeAngle180(angle)
    }

    /**
     * Fire a missile in a given direction with impact at a given distance.
     * A missile can only be fired in the robot reload cooldown is 0
     */
    fun LambdaRobotAction.fireMissile(heading: Double, distance: Double): LambdaRobotAction {
        log.info("Firing missile!")
        return this.copy(fireMissileHeading = heading, fireMissileDistance = distance, fired = true)
    }

    /**
     * Fire a missile at the given position
     */
    fun LambdaRobotAction.fireMissileToXY(x: Double, y: Double): LambdaRobotAction {
        val heading = angleToXY(x, y)
        val distance = distanceToXY(x, y)
        return this.fireMissile(heading, distance)
    }

    fun LambdaRobotAction.moveToXY(x: Double, y: Double): LambdaRobotAction{
        val heading = angleToXY(x, y)
        val distance = distanceToXY(x, y)
        val (speed, arrived) = if (distance <= gameInfo.collisionRange) {
            0.0 to true
        } else {
            sqrt(distance * 2.0 * robot.deceleration()) * gameInfo.secondsPerTurn to false
        }
        log.info("Move to: X=$x, Y=$y, heading=$heading, distance=$distance")

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

    fun LambdaRobotState.initialize(): LambdaRobotState {
        return this.copy(initialized = true)
    }

    companion object {
        val log = LogManager.getLogger(LambdaRobotFunction::class.java)
    }
}

class LambdaRobotStateTable {
    private val mapper: DynamoDBMapper
        get() = DynamoDBMapper(
            AmazonDynamoDBClientBuilder.defaultClient(),
            DynamoDBMapperConfig.builder().withTableNameOverride(tableOverride).build())

    private val tableOverride: DynamoDBMapperConfig.TableNameOverride
        get() = DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(
            System.getenv("GAME_STATE_TABLE"))

    fun getById(id: String): Optional<LambdaRobotStateRecord> {
        return Optional.ofNullable(mapper.load(LambdaRobotStateRecord::class.java, id))
    }

    fun save(state: LambdaRobotStateRecord) {
        mapper.save(state)
    }
}

class ScanClient(private val apiUrl: String, private val gameId: String, private val robotId: String, private val httpClient: HttpClient, private val mapper: ObjectMapper) {
    fun scan(heading: Double, resolution: Double): ScanEnemiesResponse = runBlocking {
        val requestBody = ScanEnemiesRequest(gameId, robotId, heading, resolution)
        val response = httpClient.post<ScanEnemiesResponse>("http://$apiUrl/scan") {
            contentType(ContentType.Application.Json)
            body = requestBody
        }
        response
    }
}
