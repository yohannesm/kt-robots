/**
 * This file is part of the ONEMA RobotServer Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 *
 * copyright (c) 2020, Juan Manuel Torres (http://onema.io)
 *
 * @author Juan Manuel Torres <software@onema.io>
 */

package io.onema.ktrobots.server.config

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsync
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.lambda.LambdaAsyncClient


/**
 * This class builds different aws clients
 */
@Configuration
@EnableDynamoDBRepositories(basePackages = ["io.onema.ktrobots.server.data"])
class AWSConfig {

    //--- Methods ---

    /**
     * Get a new DynamoDB client
     */
    @Bean
    fun amazonDynamoDB(): AmazonDynamoDBAsync = AmazonDynamoDBAsyncClientBuilder.defaultClient()

    /**
     * Get a new lambda async client
     */
    @Bean
    fun amazonLambdaClient(): LambdaAsyncClient = LambdaAsyncClient.builder().build()
}
