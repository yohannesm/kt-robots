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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperFieldModel
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTyped

/**
 * Main lambda robot object, this holds all the information for the robot
 */
@DynamoDBDocument
data class LambdaRobot(
    /**
     * Index position of robot. Starts at `0`.
     */
    var index: Int = 0,

    /**
     * Globally unique robot ID.
     */
    override var id: String? = null,

    /**
     * Robot display name.
     */
    var name: String? = null,

    /**
     * Robot ARN
     */
    var arn: String = "",

    // current state

    /**
     * Robot status. Either `Alive` or `Dead`.
     */
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.S)
    var status: LambdaRobotStatus = LambdaRobotStatus.alive,

    /**
     * Robot horizontal position.
     */
    override var x: Double = 0.0,

    /**
     * Robot vertical position.
     */
    override var y: Double = 0.0,

    /**
     * Robot speed. Between `0` and `MaxSpeed`. (m/s)
     */
    var speed: Double = 0.0,

    /**
     * Robot heading. Between `0` and `360`. (degrees)
     */
    var heading: Double = 0.0,

    /**
     * Accumulated robot damage. Between `0` and `MaxDamage`.
     */
    var damage: Double = 0.0,

    /**
     * Number of seconds before the robot can fire another missile. (s)
     */
    var reloadCoolDown: Double = 0.0,

    // target state

    /**
     * Desired speed for robot. The current speed will be adjusted accordingly every turn. (m/s)
     */
    var targetSpeed: Double = 0.0,

    /**
     * Desired heading for robot. The heading will be adjusted accordingly every turn. (degrees)
     */
    var targetHeading: Double = 0.0,

    // robot stats

    /**
     * Total distance traveled by robot during the match. (m)
     */
    var totalTravelDistance: Double = 0.0,

    /**
     * Number of missiles fired by robot during match.
     */
    var totalMissileFiredCount: Int = 0,

    /**
     * Number of missiles that hit a target during match.
     */
    var totalMissileHitCount: Int = 0,

    /**
     * Number of confirmed kills during match.
     */
    var totalKills: Int = 0,

    /**
     * Damage dealt by missiles during match.
     */
    var totalDamageDealt: Double = 0.0,

    /**
     * Number of collisions with walls or other robots during match.
     */
    var totalCollisions: Int = 0,

    /**
     * Game turn during which the robot died. `-1` if robot is alive.
     */
    var timeOfDeathGameTurn: Int = -1,

    // robot characteristics

    var engine: Engine = DefaultEngine(),


    /**
     * Maximum speed at which the robot can change heading without a sudden stop. (m/s)
     */
    var maxTurnSpeed: Double = 50.0,

    var radar: Radar = DefaultRadar(),

    /**
     * Maximum damage before the robot is destroyed.
     */
    var maxDamage: Double = 100.0,

    /**
     * Armor for the robot
     */
    var armor: Armor = DefaultArmor(),

    // missile characteristics
    var missile: Missile = DefaultMissile()
) : Locatable {

    //--- Methods ---

    var maxSpeed: Double = engine.maxSpeed + armor.speedModifier

    /**
     * If the status is equal to alive return true, false otherwise
     */
    @DynamoDBIgnore
    fun isAlive(): Boolean = status == LambdaRobotStatus.alive

    /**
     * Get the deceleration base on the armor that the robot has
     */
    fun deceleration(): Double = armor.deceleration

    /**
     * Set new damage and record the type of damage it was
     */
    fun doDamage(damage: Double, turn: Int, isCollision: Boolean = false): LambdaRobot {
        val newDamage = this.damage + damage
        val collisions = if(isCollision) totalCollisions + 1 else totalCollisions
        return if(newDamage >= maxDamage) {
            copy(damage = maxDamage, status = LambdaRobotStatus.dead, timeOfDeathGameTurn = turn, totalCollisions = collisions)
        } else {
            copy(damage = newDamage, totalCollisions = collisions)
        }
    }

    /**
     * Update the position and total distance traveled by the robot
     */
    fun doMove(moveData: MoveData): LambdaRobot = copy(x = moveData.x, y = moveData.y, totalTravelDistance = moveData.distance)

    /**
     * Check if the reload cool-down is down to zero and return true, false otherwise
     */
    fun canFire(): Boolean = reloadCoolDown == 0.0

    /**
     * Increase the damage dealt by this robot
     */
    fun addDamageDealt(damage: Double) = copy(totalDamageDealt = totalDamageDealt + damage)

    /**
     * Add a hit to the totalMissileHItCount
     */
    fun addHit() = copy(totalMissileHitCount = totalMissileHitCount + 1)

    override fun toString(): String {
        return "$name (R$index)"
    }

    companion object {

        /**
         * Generate a new game id for the robot
         */
        fun generateId(index: Int, gameId: String): String = "$gameId:Robot$index"

        /**
         * Create lambda robot using the build retrieved from the function.
         * This ensures that the game config is respected
         */
        fun create(index: Int, build: LambdaRobotBuild, game: Game, arn: String): Pair<LambdaRobot, String> {
            // TODO test with an invalid value
            val robot = LambdaRobot(
                index = index,
                id = generateId(index, game.id),
                status = LambdaRobotStatus.alive,
                name = build.name,
                arn = arn
            )

            val radar: Radar = when(build.radar) {
                LambdaRobotRadarType.ultraShortRange -> UltraShortRange()
                LambdaRobotRadarType.shortRange -> ShortRange()
                LambdaRobotRadarType.midRange -> MidRange()
                LambdaRobotRadarType.longRange -> LongRange()
                LambdaRobotRadarType.ultraLongRange -> UltraLongRange()

                // Invalid radar type, Will be disqualified
            }

            val engine: Engine = when(build.engine) {
                LambdaRobotEngineType.economy -> Economy()
                LambdaRobotEngineType.compact -> Compact()
                LambdaRobotEngineType.standard -> Standard()
                LambdaRobotEngineType.large -> Large()
                LambdaRobotEngineType.extraLarge -> ExtraLarge()

                // Invalid engine type, will be disqualified
            }

            val armor: Armor = when(build.armor) {
                LambdaRobotArmorType.ultraLight -> UltraLight()
                LambdaRobotArmorType.light -> Light()
                LambdaRobotArmorType.medium -> Medium()
                LambdaRobotArmorType.heavy -> Heavy()
                LambdaRobotArmorType.ultraHeavy -> UltraHeavy()

                // Invalid armor type, will be disqualified
            }

            val missile: Missile = when(build.missile) {
                LambdaRobotMissileType.dart -> Dart()
                LambdaRobotMissileType.arrow -> Arrow()
                LambdaRobotMissileType.javelin -> Javelin()
                LambdaRobotMissileType.cannon -> Cannon()
                LambdaRobotMissileType.BFG -> BFG()
                LambdaRobotMissileType.sniperRifle -> SniperRifle()

                // Invalid missile type, will be disqualified
            }

            // Ensure game config is respected by the build
            val totalPoints = radar.points + engine.points + armor.points + missile.points

            // Check if build is under the max number of build points
            return if(totalPoints > game.info.maxBuildPoints) {
                val reason = "Total points $totalPoints, maxed allowed ${game.info.maxBuildPoints}"
                robot.copy(status = LambdaRobotStatus.dead) to reason
            } else{
                val description = "Radar: ${radar.name}, Engine: ${engine.name}, Armor: ${armor.name}, Missile: ${missile.name}"
                robot.copy(radar = radar, engine = engine, armor = armor, missile = missile) to description
            }
        }
    }
}

/**
 * Status of the robot, either dead or alive
 */
@DynamoDBDocument
enum class LambdaRobotStatus {
    undefined,
    alive,
    dead
}