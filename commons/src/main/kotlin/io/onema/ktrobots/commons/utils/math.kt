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

package io.onema.ktrobots.commons.utils

import io.onema.ktrobots.commons.domain.Locatable
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Normalizes the angle in degrees
 */
fun normalizeAngle(angle: Double): Double {
    val result = angle % 360
    return when {
        result <= -180.0 -> {
            result + 360.0
        }
        result > 180.0 -> {
            result - 360.0
        }
        else -> {
            result
        }
    }
}

/**
 * Determine the angle in degrees relative to the current robot position
 * Returns a value between -180 and 180 degrees
 */
fun angleToXY(obj1: Locatable, obj2: Locatable): Double = angleToXY(obj1.x, obj1.y, obj2.x, obj2.y)

/**
 * Determine the angle in degrees relative to the current robot position
 * Returns a value between -180 and 180 degrees
 */
fun angleToXY(x: Double, y: Double, obj: Locatable): Double = angleToXY(x, y, obj.x, obj.y)

/**
 * Determine the angle in degrees relative to the current robot position
 * Returns a value between -180 and 180 degrees
 */
fun angleToXY(x1: Double, y1: Double, x2: Double, y2: Double): Double {
    return normalizeAngle180(atan2(x1 - x2, y1 - y2) * 180.0 / PI)
}

/**
 * Determine the distance relative to the current robot position
 */
fun distanceToXY(obj1: Locatable, obj2: Locatable): Double = distanceToXY(obj1.x, obj1.y, obj2.x, obj2.y)

/**
 * Determine the distance relative to the current robot position
 */
fun distanceToXY(x: Double, y: Double, obj: Locatable): Double = distanceToXY(x, y, obj.x, obj.y)

/**
 * Determine the distance relative to the current robot position
 */
fun distanceToXY(x1: Double, y1: Double, x2: Double, y2: Double): Double {
    val deltaX = x1 - x2
    val deltaY = y1 - y2
    return sqrt(deltaX.pow(2.0) + deltaY.pow(2.0))
}

/**
 * Normalize angle to be between -180 and 180
 */
fun normalizeAngle180(angle: Double): Double {
    val result = angle % 360.0
    return if(result < -180.0) result + 360.0 else result
}

