package io.onema.ktrobots.lambda.utils

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import io.onema.ktrobots.commons.domain.LambdaRobotStateRecord
import java.util.*

/**
 * Wrapper class to get elements from the robot state table and save the robot state
 */
class LambdaRobotStateTable {

    //--- Properties ---
    /**
     * DynamoDBMapper with table override
     */
    private val mapper: DynamoDBMapper
        get() = DynamoDBMapper(
            AmazonDynamoDBClientBuilder.defaultClient(),
            DynamoDBMapperConfig.builder()
                .withTableNameOverride(tableOverride).build()
        )

    /**
     * Table override the value comes from the "GAME_STATE_TABLE"
     * environment variable
     */
    private val tableOverride: DynamoDBMapperConfig.TableNameOverride
        get() = DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(
            System.getenv("GAME_STATE_TABLE")
        )


    //--- Methods ---
    /**
     * Get an element by ID from the game state table
     */
    fun getById(id: String): Optional<LambdaRobotStateRecord> {
        return Optional.ofNullable(
            mapper.load(
                LambdaRobotStateRecord::class.java,
                id
            )
        )
    }

    /**
     * Save the robot state
     */
    fun save(state: LambdaRobotStateRecord) {
        mapper.save(state)
    }
}