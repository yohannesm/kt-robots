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

/**
 * Interface for the service that will serve as the means to communicate with the robots
 */
interface RobotService<AResponse> {

    // --- Methods ---

    /**
     * Make a call to the robot using the robot identifier and the LambdaRobotRequest
     */
    fun callRobot(robotResourceName: String, request: LambdaRobotRequest): AResponse

    /**
     * Deserialize the message returned by the call to the robot, depending on the implementation
     * The response will look different
     */
    fun deserialize(response: AResponse): LambdaRobotResponse
}