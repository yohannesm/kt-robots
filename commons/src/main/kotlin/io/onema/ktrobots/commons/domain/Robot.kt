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
import java.util.*

@DynamoDBDocument
data class LambdaRobot(
    /**
     * Index position of robot. Starts at `0`.
     */
    var index: Int = 0,

    /**
     * Globally unique robot ID.
     */
    override var id: String = "",

    /**
     * Robot display name.
     */
    var name: String = "",

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
    var maxSpeed: Double = engine.maxSpeed + armor.speedModifier

    @DynamoDBIgnore
    fun isAlive(): Boolean = status == LambdaRobotStatus.alive
    fun deceleration(): Double = armor.deceleration
    fun doDamage(damage: Double, turn: Int, isCollision: Boolean = false): LambdaRobot {
        val newDamage = this.damage + damage
        val collisions = if(isCollision) totalCollisions + 1 else totalCollisions
        return if(newDamage >= maxDamage) {
            copy(damage = maxDamage, status = LambdaRobotStatus.dead, timeOfDeathGameTurn = turn, totalCollisions = collisions)
        } else {
            copy(damage = newDamage, totalCollisions = collisions)
        }
    }
    fun doMove(moveData: MoveData): LambdaRobot = copy(x = moveData.x, y = moveData.y, totalTravelDistance = moveData.distance)
    fun canFire(): Boolean = reloadCoolDown == 0.0
    fun addDamageDealt(damage: Double) = copy(totalDamageDealt = totalDamageDealt + damage)
    fun addHit() = copy(totalMissileHitCount = totalMissileHitCount + 1)


    override fun toString(): String {
        return "$name (R$index)"
    }
}

@DynamoDBDocument
enum class LambdaRobotStatus {
    undefined,
    alive,
    dead
}

@DynamoDBDocument
open class Missile(
    /**
     * The name of the missile
     */
    open var name: String = "",

    var points: Int = 0,

    /**
     * Number of seconds between each missile launch. (s)
     */
    var reloadCooldown: Double = 2.0,

    /**
     * Travel velocity for missile. (m/s)
     */
    var velocity: Double = 150.0,

    /**
     * Maximum range for missile. (m)
     */
    var range: Double = 700.0,

    /**
     * Bonus damage on target for a direct hit.
     */
    var directHitDamageBonus: Double = 3.0,

    /**
     * Bonus damage on target for a near hit.
     */
    var nearHitDamageBonus: Double = 2.1,

    /**
     * Bonus damage on target for a far hit.
     */
    var farHitDamageBonus: Double = 1.0
)


@DynamoDBDocument
data class DefaultMissile(override var name: String = "default") : Missile(name)


@DynamoDBDocument
data class Dart(override var name: String = "dart") : Missile(
    points = 0,
    range = 1200.0,
    velocity = 250.0,
    directHitDamageBonus = 0.0,
    nearHitDamageBonus = 0.0,
    farHitDamageBonus = 0.0,
    reloadCooldown = 0.0
)


@DynamoDBDocument
data class Arrow(override var name: String = "arrow") : Missile(
    points = 1,
    range = 900.0,
    velocity = 200.0,
    directHitDamageBonus = 1.0,
    nearHitDamageBonus = 1.0,
    farHitDamageBonus = 0.0,
    reloadCooldown = 1.0
)


@DynamoDBDocument
data class Javelin(override var name: String = "javelin") : Missile(
    points = 2,
    range = 700.0,
    velocity = 150.0,
    directHitDamageBonus = 3.0,
    nearHitDamageBonus = 2.0,
    farHitDamageBonus = 1.0,
    reloadCooldown = 2.0
)


@DynamoDBDocument
data class Cannon(override var name: String = "cannon") : Missile(
    points = 3,
    range = 500.0,
    velocity = 100.0,
    directHitDamageBonus = 6.0,
    nearHitDamageBonus = 4.0,
    farHitDamageBonus = 2.0,
    reloadCooldown = 3.0
)


@DynamoDBDocument
data class BFG(override var name: String = "BFG") : Missile(
    points = 4,
    range = 350.0,
    velocity = 75.0,
    directHitDamageBonus = 12.0,
    nearHitDamageBonus = 8.0,
    farHitDamageBonus = 4.0,
    reloadCooldown = 5.0
)

@DynamoDBDocument
open class Radar(
    open var name: String = "",
    var points: Int = 0,
    /**
     * Maximum range at which the radar can detect an opponent. (m)
     */
    var range: Double = 600.0,

    /**
     * Maximum degrees the radar can scan beyond the selected heading. (degrees)
     */
    var maxResolution: Double = 10.0
)



@DynamoDBDocument
data class DefaultRadar(override var name: String = "default") : Radar(name)


@DynamoDBDocument
data class UltraShortRange(override var name: String =  "ultra short range") : Radar(
    points = 0,
    range = 200.0,
    maxResolution = 45.0
)

@DynamoDBDocument
data class ShortRange(override var name: String =  "short range") : Radar(
    points = 1,
    range = 400.0,
    maxResolution = 20.0
)

@DynamoDBDocument
data class MidRange(override var name: String =  "mid range") : Radar(
    points = 2,
    range = 600.0,
    maxResolution = 10.0
)

@DynamoDBDocument
data class LongRange(override var name: String =  "long range") : Radar(
    points = 3,
    range = 800.0,
    maxResolution = 8.0
)

@DynamoDBDocument
data class UltraLongRange(override var name: String =  "ultra long range") : Radar(
    points = 4,
    range = 1000.0,
    maxResolution = 5.0
)

@DynamoDBDocument
open class Engine(
    open var name: String = "",
    var points: Int = 0,

    /**
     * Maximum speed for robot. (m/s)
     */
    var maxSpeed: Double = 100.0,

    /**
     * Acceleration when speeding up. (m/s^2)
     */
    var acceleration: Double = 10.0
)


@DynamoDBDocument
data class DefaultEngine(override var name: String = "default") : Engine(name)


@DynamoDBDocument
data class Economy(override var name: String = "economy") : Engine(
    points = 0,
    maxSpeed = 60.0,
    acceleration = 7.0
)

@DynamoDBDocument
data class Compact(override var name: String = "compact") : Engine(
    points = 1,
    maxSpeed = 80.0,
    acceleration = 8.0
)

@DynamoDBDocument
data class Standard(override var name: String = "standard") : Engine(
    points = 2,
    maxSpeed = 100.0,
    acceleration = 10.0
)

@DynamoDBDocument
data class Large(override var name: String = "large") : Engine(
    points = 3,
    maxSpeed = 120.0,
    acceleration = 12.0
)

@DynamoDBDocument
data class ExtraLarge(override var name: String = "ExtraLarge") : Engine(
    points = 4,
    maxSpeed = 140.0,
    acceleration = 13.0
)


@DynamoDBDocument
open class Armor(
    open var name: String = "",
    var points: Int = 0,
    /**
     * Amount of damage the robot receives from a collision.
     */
    var collisionDamage: Double = 2.0,

    /**
     * Amount of damage the robot receives from a direct hit.
     */
    var directHitDamage: Double = 8.0,

    /**
     * Amount of damage the robot receives from a near hit.
     */
    var nearHitDamage: Double = 4.0,

    /**
     * Amount of damage the robot receives from a far hit.
     */
    var farHitDamage: Double = 2.0,

    /**
     * Deceleration when speeding up. (m/s^2)
     */
    var deceleration: Double = 20.0,

    /**
     * Modifications to the robot build, should be applied
     */
    var speedModifier: Double = 0.0
)



@DynamoDBDocument
data class DefaultArmor(override var name: String = "default") : Armor(name)


@DynamoDBDocument
data class UltraLight(override var name: String = "ultra light") : Armor(
    points = 0,
    directHitDamage = 50.0,
    nearHitDamage = 25.0,
    farHitDamage = 12.0,
    collisionDamage = 10.0,
    speedModifier = 35.0,
    deceleration = 30.0

)

@DynamoDBDocument
data class Light(override var name: String = "light") : Armor(
    points = 1,
    directHitDamage = 16.0,
    nearHitDamage = 8.0,
    farHitDamage = 4.0,
    collisionDamage = 3.0,
    speedModifier = 25.0,
    deceleration = 25.0
)

@DynamoDBDocument
data class Medium(override var name: String = "medium") : Armor(
    points = 2,
    directHitDamage = 8.0,
    nearHitDamage = 4.0,
    farHitDamage = 2.0,
    collisionDamage = 2.0,
    deceleration = 20.0
)

@DynamoDBDocument
data class Heavy(override var name: String = "heavy") : Armor(
    points = 3,
    directHitDamage = 4.0,
    nearHitDamage = 2.0,
    farHitDamage = 1.0,
    collisionDamage = 1.0,
    speedModifier = -25.0,
    deceleration = 15.0
)

@DynamoDBDocument
data class UltraHeavy(override var name: String = "ultra heavy") : Armor(
    points = 4,
    directHitDamage = 2.0,
    nearHitDamage = 1.0,
    farHitDamage = 0.0,
    collisionDamage = 1.0,
    speedModifier = -45.0,
    deceleration = 10.0
)

@DynamoDBDocument
class RobotFactory {
    companion object {

        fun generateId(index: Int, gameId: String): String = "$gameId:Robot$index"

        /**
         * Create lambda robot using the build retrieved from the function.
         * This ensures that the game config is respected
         */
        fun create(index: Int, build: LambdaRobotBuild, game: Game, arn: String): Pair<LambdaRobot, String> {
            val robot = LambdaRobot(
                index = index,
                id = generateId(index, game.id),
                status = LambdaRobotStatus.alive,
                name = build.name,
                arn = arn
            )

            val radarOption: Optional<Radar> = when(build.radar) {
                LambdaRobotRadarType.ultraShortRange -> Optional.of(UltraShortRange())
                LambdaRobotRadarType.shortRange -> Optional.of(ShortRange())
                LambdaRobotRadarType.midRange -> Optional.of(MidRange())
                LambdaRobotRadarType.longRange -> Optional.of(LongRange())
                LambdaRobotRadarType.ultraLongRange -> Optional.of(UltraLongRange())

                // Invalid radar type, Will be disqualified
            }

            val engineOption: Optional<Engine> = when(build.engine) {
                LambdaRobotEngineType.economy -> Optional.of(Economy())
                LambdaRobotEngineType.compact -> Optional.of(Compact())
                LambdaRobotEngineType.standard -> Optional.of(Standard())
                LambdaRobotEngineType.large -> Optional.of(Large())
                LambdaRobotEngineType.extraLarge -> Optional.of(ExtraLarge())

                // Invalid engine type, will be disqualified
            }

            val armorOption: Optional<Armor> = when(build.armor) {
                LambdaRobotArmorType.ultraLight -> Optional.of(UltraLight())
                LambdaRobotArmorType.light -> Optional.of(Light())
                LambdaRobotArmorType.medium -> Optional.of(Medium())
                LambdaRobotArmorType.heavy -> Optional.of(Heavy())
                LambdaRobotArmorType.ultraHeavy -> Optional.of(UltraHeavy())

                // Invalid armor type, will be disqualified
            }

            val missileOption: Optional<Missile> = when(build.missile) {
                LambdaRobotMissileType.dart -> Optional.of(Dart())
                LambdaRobotMissileType.arrow -> Optional.of(Arrow())
                LambdaRobotMissileType.javelin -> Optional.of(Javelin())
                LambdaRobotMissileType.cannon -> Optional.of(Cannon())
                LambdaRobotMissileType.BFG -> Optional.of(BFG())

                // Invalid missile type, will be disqualified
            }

            // Ensure game config is respected by the build
            val isInvalidConfig = listOf(radarOption, engineOption, armorOption, missileOption).any {it.isEmpty}
            return if(isInvalidConfig) {
                // disqualified
                val reason = "Invalid configuration type"
                Pair(robot.copy(status = LambdaRobotStatus.dead), reason)
            } else {
                val radar = radarOption.get()
                val engine = engineOption.get()
                val armor = armorOption.get()
                val missile = missileOption.get()
                val totalPoints = radar.points + engine.points + armor.points + missile.points

                // Check if build is under the max number of build points
                return if(totalPoints > game.info.maxBuildPoints) {
                    val reason = "Total points $totalPoints, maxed allowed ${game.info.maxBuildPoints}"
                    Pair(robot.copy(status = LambdaRobotStatus.dead), reason)
                } else{
                    val description = "Radar: ${radar.name}, Engine: ${engine.name}, Armor: ${armor.name}, Missile: ${missile.name}"
                    Pair(robot.copy(radar = radar, engine = engine, armor = armor, missile = missile), description)
                }
            }

        }
    }
}