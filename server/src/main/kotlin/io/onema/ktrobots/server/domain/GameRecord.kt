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

package io.onema.ktrobots.server.domain

import com.amazonaws.services.dynamodbv2.datamodeling.*
import io.onema.ktrobots.commons.domain.Game

/**
 * This class contains the information that will be saved in the game state table
 */
@DynamoDBTable(tableName = "kt-robots-server-game-table")
data class GameRecord(

    @DynamoDBHashKey(attributeName = "PK")
    var primaryKey: String = "",

    @DynamoDBAttribute(attributeName = "game")
    var game: Game = Game(),

    @DynamoDBAttribute(attributeName = "lambdaRobotArns")
    var lambdaRobotArns: List<String> = listOf()
)
