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

package io.onema.ktrobots.commons.domain

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument

/**
 * Missile definition, default values and concrete implementations
 */
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


/**
 * Default definition
 */
@DynamoDBDocument
data class DefaultMissile(override var name: String = "default") : Missile(name)

/**
 * Definition of a Dart missile, it is quick has a long range but deals very little damage
 */
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


/**
 * Definition of an Arrow missile.
 */
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


/**
 * Definition of an Javelin missile.
 */
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

/**
 * Definition of a Cannon missile.
 */
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


/**
 * Definition of a BFG missile.
 */
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

/**
 * Radar definition, default values and concrete implementations
 */
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

/**
 * Default definition
 */
@DynamoDBDocument
data class DefaultRadar(override var name: String = "default") : Radar(name)


/**
 * Definition of an UtraShortRange radar
 */
@DynamoDBDocument
data class UltraShortRange(override var name: String =  "ultra short range") : Radar(
    points = 0,
    range = 200.0,
    maxResolution = 45.0
)

/**
 * Definition of an ShortRange radar
 */
@DynamoDBDocument
data class ShortRange(override var name: String =  "short range") : Radar(
    points = 1,
    range = 400.0,
    maxResolution = 20.0
)

/**
 * Definition of an MidRange radar
 */
@DynamoDBDocument
data class MidRange(override var name: String =  "mid range") : Radar(
    points = 2,
    range = 600.0,
    maxResolution = 10.0
)

/**
 * Definition of an LongRange radar
 */
@DynamoDBDocument
data class LongRange(override var name: String =  "long range") : Radar(
    points = 3,
    range = 800.0,
    maxResolution = 8.0
)

/**
 * Definition of an UltraLongRange radar
 */
@DynamoDBDocument
data class UltraLongRange(override var name: String =  "ultra long range") : Radar(
    points = 4,
    range = 1000.0,
    maxResolution = 5.0
)

/**
 * Engine definition, default values and concrete implementations
 */
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

/**
 * Default definition
 */
@DynamoDBDocument
data class DefaultEngine(override var name: String = "default") : Engine(name)

/**
 * Definition of an Economy class engine.
 */
@DynamoDBDocument
data class Economy(override var name: String = "economy") : Engine(
    points = 0,
    maxSpeed = 60.0,
    acceleration = 7.0
)

/**
 * Definition of an Compact class engine.
 */
@DynamoDBDocument
data class Compact(override var name: String = "compact") : Engine(
    points = 1,
    maxSpeed = 80.0,
    acceleration = 8.0
)

/**
 * Definition of an Standard class engine.
 */
@DynamoDBDocument
data class Standard(override var name: String = "standard") : Engine(
    points = 2,
    maxSpeed = 100.0,
    acceleration = 10.0
)

/**
 * Definition of an Large class engine.
 */
@DynamoDBDocument
data class Large(override var name: String = "large") : Engine(
    points = 3,
    maxSpeed = 120.0,
    acceleration = 12.0
)

/**
 * Definition of an ExtraLarge class engine.
 */
@DynamoDBDocument
data class ExtraLarge(override var name: String = "ExtraLarge") : Engine(
    points = 4,
    maxSpeed = 140.0,
    acceleration = 13.0
)

/**
 * Armor definition, default values and concrete implementations
 */
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

/**
 * Default definition
 */
@DynamoDBDocument
data class DefaultArmor(override var name: String = "default") : Armor(name)

/**
 * Definition of UltraLight armor.
 */
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

/**
 * Definition of Light armor.
 */
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

/**
 * Definition of Medium armor.
 */
@DynamoDBDocument
data class Medium(override var name: String = "medium") : Armor(
    points = 2,
    directHitDamage = 8.0,
    nearHitDamage = 4.0,
    farHitDamage = 2.0,
    collisionDamage = 2.0,
    deceleration = 20.0
)

/**
 * Definition of Heavy armor.
 */
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

/**
 * Definition of UltraHeavy armor.
 */
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
