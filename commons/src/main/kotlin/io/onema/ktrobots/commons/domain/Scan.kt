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

package io.onema.ktrobots.commons.domain

data class ScanEnemiesRequest(
    val gameId: String = "",
    val robotId: String = "",
    val heading: Double = 0.0,
    val resolution: Double = 0.0
)

data class ScanEnemiesResponse(
    val found: Boolean = false,
    val distance: Double = 0.0,
    val heading: Double = 0.0
)
