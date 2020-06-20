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

/**
 * Start game request sent by the client
 */
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
    var minRobotStartDistance: Double = 300.0,
    var robotTimeoutSeconds: Double = 15.0,
    var robotType: String = "lambda",
    var apiHost: String = ""
)

/**
 * Stop game request sent by the client
 */
data class StopGameRequest(
    var action: String = "",
    var gameId: String = ""
)

/**
 * Response object for the start game request
 */
data class GameResponse(var game: Game)

