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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.onema.ktrobots.commons.domain.LambdaRobotRequest
import io.onema.ktrobots.commons.domain.LambdaRobotResponse
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import software.amazon.awssdk.services.lambda.model.InvocationType
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import software.amazon.awssdk.services.lambda.model.InvokeResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

/**
 * This service calls robots hosted in lambda functions. The robotResourceName is the
 * Lambda ARN.
 */
@Service
class LambdaRobotService(private val lambda: LambdaAsyncClient) : RobotService<CompletableFuture<InvokeResponse>> {

    //--- Fields ---
    private val mapper: ObjectMapper = jacksonObjectMapper()

    // --- Methods ---

    /**
     * Invoke Lambda function
     */
    override fun callRobot(robotResourceName: String, request: LambdaRobotRequest): CompletableFuture<InvokeResponse> {
        val json = SdkBytes.fromUtf8String(mapper.writeValueAsString(request))
        return lambda.invoke(
            InvokeRequest.builder()
                .payload(json)
                .functionName(robotResourceName)
                .invocationType(InvocationType.REQUEST_RESPONSE).build()
        )
    }

    /**
     * Deserialize invocation response object
     */
    override fun deserialize(response: CompletableFuture<InvokeResponse>): LambdaRobotResponse {
        return try {
            deserializePayload(response)
        } catch (e: CompletionException) {
            LambdaRobotResponse(hasError = true, errorMessage = "The lambda function is taking too long and has timed out. ${e.message}")
        }
    }

    private fun deserializePayload(response: CompletableFuture<InvokeResponse>): LambdaRobotResponse {
        val invokeResponse: InvokeResponse = response.join()
        val payload = invokeResponse.payload().asUtf8String()
        return try {
            mapper.readValue(payload)
        } catch (e: Exception) {
            LambdaRobotResponse(hasError = true, errorMessage = payload ?: "Error de-serializing lambda response. No message found")
        }
    }
}
