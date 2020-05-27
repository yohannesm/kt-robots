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

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import io.onema.ktrobots.commons.domain.LambdaRobotRequest
import io.onema.ktrobots.commons.domain.LambdaRobotResponse


/**
 * Simple interface to enable robot function to work with AWS Lambda default "handleRequest" method.
 * Any robot that wants to work in AWS lambda just has to implement this interface.
 */
interface LambdaRobotFunction: RequestHandler<LambdaRobotRequest, LambdaRobotResponse> {

    //--- Methods ---

    /**
     * Main entry point of the robot logic
     */
    fun handle(request: LambdaRobotRequest): LambdaRobotResponse

    /**
     * Main entry point for the lambda function
     */
    override fun handleRequest(request: LambdaRobotRequest, context: Context): LambdaRobotResponse {
        return handle(request)
    }
}
