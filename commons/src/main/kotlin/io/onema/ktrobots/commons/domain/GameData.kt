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

package io.onema.ktrobots.commons.domain

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable

data class StartGameRequest(
    var action: String,
    var robotArns: List<String> = listOf(),
    var boardWidth: Double = 0.0,
    var boardHeight: Double = 0.0,
    var secondsPerTurn: Double = 0.0,
    var maxTurns: Int = 0,
    var maxBuildPoints: Int = 0,
    var directHitRange: Double = 0.0,
    var nearHitRange: Double = 0.0,
    var farHitRange: Double = 0.0,
    var collisionRange: Double = 0.0,
    var minRobotStartDistance: Double = 0.0,
    var robotTimeoutSeconds: Double = 0.0,
    var robotType: String = "lambda",
    var apiHost: String = ""
)

data class StopGameRequest(
    var action: String = "",
    var gameId: String = ""
)

data class GameResponse(var game: Game)

data class LambdaRobotRequest(
    var command: LambdaRobotCommand = LambdaRobotCommand.getBuild,
    var lambdaRobot: LambdaRobot = LambdaRobot(),
    var gameInfo: GameInfo = GameInfo(),
    var index: Int = 0,
    var gameId: String = ""
)

enum class LambdaRobotCommand {
    getBuild,
    getAction
}

data class LambdaRobotResponse(
    var robotBuild: LambdaRobotBuild = LambdaRobotBuild(),
    var robotAction: LambdaRobotAction = LambdaRobotAction(),
    var hasError: Boolean = false,
    var errorMessage: String = ""
)

data class LambdaRobotBuild  (

    /// Name of Lambda-Robot.
    var name: String = "",

    /// Type of Radar. Affects radar scan range and resolution.
    var radar: LambdaRobotRadarType = LambdaRobotRadarType.ultraShortRange,

    /// Type of Engine. Affects max. speed and acceleration.
    var engine: LambdaRobotEngineType = LambdaRobotEngineType.economy,

    /// Type of Armor. Affects hit damage, collision damage, max. speed, and deceleration.
    var armor: LambdaRobotArmorType = LambdaRobotArmorType.ultraLight,

    /// Type of Missile. Affects weapon range, velocity, hit damage, and reload speed.
    var missile: LambdaRobotMissileType = LambdaRobotMissileType.dart
)

data class LambdaRobotAction(
    var speed: Double = 0.0,
    var heading: Double = 0.0,
    var fireMissileHeading: Double = 0.0,
    var fireMissileDistance: Double = 0.0,
    var fired: Boolean = false,
    var arrivedAtDestination: Boolean = false
)

@DynamoDBTable(tableName = "ktrobots-state")
data class LambdaRobotStateRecord(
    @DynamoDBHashKey(attributeName = "robotId")
    var robotId: String = "",

    @DynamoDBAttribute(attributeName = "state")
    var state: LambdaRobotState = LambdaRobotState(),

    @DynamoDBAttribute(attributeName = "expire")
    var expire: Long = (System.currentTimeMillis()/1000L) + 300 // Set TTL to 5 min

)

@DynamoDBDocument
data class LambdaRobotState(
    var initialized: Boolean = false,
    var scanHeading: Double = 0.0,
    var lastDamage: Double = 0.0,
    var targetRange: Double = 0.0,
    var noHitSweep: Double = 0.0,
    var scanResolution: Double = 0.0,
    var gotoX: Double = 0.0,
    var gotoY: Double = 0.0
)
