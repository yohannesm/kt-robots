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

/**
 * Lambda robot request data classes
 */
data class LambdaRobotRequest(
    var command: LambdaRobotCommand = LambdaRobotCommand.getBuild,
    var lambdaRobot: LambdaRobot = LambdaRobot(),
    var gameInfo: GameInfo = GameInfo(),
    var index: Int = 0,
    var gameId: String = ""
)

/**
 * Type of command send to the robot by the server, during game
 * startup the getBuild action is used, after that the
 * getAction is used
 */
enum class LambdaRobotCommand {
    getBuild,
    getAction
}

/**
 * Lambda robot response data classes
 */
data class LambdaRobotResponse(
    var robotBuild: LambdaRobotBuild = LambdaRobotBuild(),
    var robotAction: LambdaRobotAction = LambdaRobotAction(),
    var hasError: Boolean = false,
    var errorMessage: String = ""
)

/**
 * The build of the robot, includes the types of missile, radar, armor and engine
 */
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

/**
 * Equipment type enum
 */
enum class LambdaRobotRadarType {

    /**
     * 200 meters range, 45 degrees resolution (0 pts)
     */
    ultraShortRange,

    /**
     * 400 meters range, 20 degrees resolution (1 pt)
     */
    shortRange,

    /**
     * 600 meters range, 10 degrees resolution (2 pts)
     */
    midRange,

    /**
     * 800 meters range, 8 degrees resolution (3 pts)
     */
    longRange,

    /**
     * 1,000 meters range, 5 degrees resolution (4 pts)
     */
    ultraLongRange
}

/**
 * Equipment type enum
 */
enum class LambdaRobotEngineType {

    /**
     * 60 m/s max. speed, 7 m/s^2 acceleration (0 pts)
     */
    economy,

    /**
     *  80 m/s max. speed, 8 m/s^2 acceleration (1 pt)
     */
    compact,

    /**
     * 100 m/s max. speed, 10 m/s^2 acceleration (2 pts)
     */
    standard,

    /**
     * 120 m/s max. speed, 12 m/s^2 acceleration (3 pts)
     */
    large,

    /**
     * 140 m/s max. speed, 13 m/s^2 acceleration (4 pts)
     */
    extraLarge
}

/**
 * Equipment type enum
 */
enum class LambdaRobotArmorType {

    /**
     * 50 direct hit, 25 near hit, 12 far hit, 10 collision, +35 m/s max. speed, 30 m/s^2 deceleration (0 pts)
     */
    ultraLight,

    /**
     * 16 direct hit, 8 near hit, 4 far hit, 3 collision, +25 m/s max. speed, 25 m/s^2 deceleration (1 pt)
     */
    light,

    /**
     * 8 direct hit, 4 near hit, 2 far hit, 2 collision, +0 m/s max. speed, 20 m/s^2 deceleration (2 pts)
     */
    medium,

    /**
     * 4 direct hit, 2 near hit, 1 far hit, 1 collision, -25 m/s max. speed, 15 m/s^2 deceleration (3 pts)
     */
    heavy,

    /**
     * 2 direct hit, 1 near hit, 0 far hit, 1 collision, -45 m/s max. speed, 10 m/s^2 deceleration (4 pts)
     */
    ultraHeavy
}

/**
 * Equipment type enum
 */
enum class LambdaRobotMissileType {

    /**
     * 1,200 meters range, 250 m/s velocity, 0 direct hit bonus, 0 near hit bonus, 0 far hit bonus, 0 sec. reload (0 pts)
     */
    dart,

    /**
     * 900 meters range, 200 m/s velocity, 2 direct hit bonus, 1 near hit bonus, 0.5 far hit bonus, 0.5 sec. reload (1 pt)
     */
    arrow,

    /**
     * 700 meters range, 150 m/s velocity, 6 direct hit bonus, 3 near hit bonus, 1 far hit bonus, 1 sec. reload (2 pts)
     */
    javelin,

    /**
     * 500 meters range, 100 m/s velocity, 10 direct hit bonus, 8 near hit bonus, 6 far hit bonus, 1.5 sec. reload (3 pts)
     */
    cannon,

    /**
     * 350 meters range, 75 m/s velocity, 40 direct hit bonus, 20 near hit bonus, 10 far hit bonus, 2 sec. reload (4 pts)
     */
    BFG,

    /**
     * 1200 meters range, 1750 m/s velocity, 30 direct hit bonus, 15 near hit bonus, 0 far hit bonus, 4 sec. reload (5 pts)
     */
    sniperRifle
}

/**
 * This is the action that will be sent to the server
 */
data class LambdaRobotAction(
    var speed: Double = 0.0,
    var heading: Double = 0.0,
    var fireMissileHeading: Double = 0.0,
    var fireMissileDistance: Double = 0.0,
    var fired: Boolean = false,
    var arrivedAtDestination: Boolean = false
)

/**
 * Lambda robot state data classes
 */
@DynamoDBTable(tableName = "ktrobots-state")
data class LambdaRobotStateRecord(
    @DynamoDBHashKey(attributeName = "PK")
    var robotId: String = "",

    @DynamoDBAttribute(attributeName = "state")
    var state: LambdaRobotState = LambdaRobotState(),

    @DynamoDBAttribute(attributeName = "expire")
    var expire: Long = (System.currentTimeMillis()/1000L) + 300 // Set TTL to 5 min
)

/**
 * Simple class to help keep track of the state of the robot
 */
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
) {

    //--- Methods ---

    /**
     * Create a new object with the initialized status
     */
    fun initialize(): LambdaRobotState {
        return this.copy(initialized = true)
    }
}
