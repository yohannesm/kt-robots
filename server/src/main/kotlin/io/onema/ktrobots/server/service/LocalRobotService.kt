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

package io.onema.ktrobots.server.service

import io.onema.ktrobots.commons.domain.LambdaRobotRequest
import io.onema.ktrobots.commons.domain.LambdaRobotResponse
import io.onema.ktrobots.commons.utils.measureTimeMillis
import io.onema.ktrobots.lambda.Robot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.reflect.full.createInstance

@Service
class LocalRobotService : RobotService<LambdaRobotResponse> {

    val robots = mutableMapOf<String, Robot>()
    val log: Logger = LoggerFactory.getLogger(LocalRobotService::class.java)
    // --- Methods ---
    /**
     * Invoke Local function
     */
    override fun callRobot(robotResourceName: String, request: LambdaRobotRequest): LambdaRobotResponse {
        val id = request.lambdaRobot.id
        if (!robots.containsKey(id) || id.isEmpty()) {
            val robotClass = Class.forName(robotResourceName).kotlin
            val robot: Robot = robotClass.createInstance() as Robot
            robots[id] = robot
        }
        val robot = robots[id] ?: throw Exception("Unable to find robot with id $id")
        return robot.handle(request)
    }

    /**
     * Deserialize invocation response object
     */
    override fun deserialize(response: LambdaRobotResponse): LambdaRobotResponse = response
}