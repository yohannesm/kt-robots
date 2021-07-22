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
import io.onema.ktrobots.lambda.Robot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.reflect.full.createInstance

/**
 * This service calls a robot class with in the same project. The robotResourceName is the fully qualified
 * name of the class, this includes the namespace and the class name.
 */
@Service
class LocalRobotService : RobotService<LambdaRobotResponse> {

    //--- Fields ---
    val robots = mutableMapOf<String, Robot>()

    // --- Methods ---

    /**
     * Invoke Local function. The robotResourceName is the fully qualified name of the class including namespace
     * and class name.
     */
    override fun callRobot(robotResourceName: String, request: LambdaRobotRequest): LambdaRobotResponse {
        return try {
            val id = request.lambdaRobot.id ?: ""
            if (!robots.containsKey(id) || id.isEmpty()) {
                val robotClass = Class.forName(robotResourceName).kotlin
                val robot: Robot = robotClass.createInstance() as Robot
                robots[id] = robot
            }
            val robot = robots[id] ?: throw Exception("Unable to find robot with id $id")
            robot.handle(request)
        } catch (e: Exception) {
            LambdaRobotResponse(hasError = true, errorMessage = e.message ?: "An error occurred but could not get any information from the exception.")

        }
    }

    /**
     * Deserialize invocation response object
     */
    override fun deserialize(response: LambdaRobotResponse): LambdaRobotResponse = response

    companion object {
        protected val log: Logger = LoggerFactory.getLogger(GameLogic::class.java)
    }
}
