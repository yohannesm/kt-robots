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

package io.onema.ktrobots.server.service

import io.onema.ktrobots.commons.domain.Game
import io.onema.ktrobots.commons.domain.GameStatus
import io.onema.ktrobots.commons.domain.Message
import io.onema.ktrobots.commons.utils.measureTimeMillis
import io.onema.ktrobots.server.data.GameTableRepository
import io.onema.ktrobots.server.domain.GameRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate

/**
 * Main game loop, this takes care of interacting with the game logic on every turn
 */
class GameLoop<TRobot : RobotService<TResponse>, TResponse>(val template: SimpMessagingTemplate, val repo: GameTableRepository, robotService: TRobot) {

    //--- Fields ---
    val log: Logger = LoggerFactory.getLogger(GameLoop::class.java)

    val logic = GameLogic(robotService)

    //--- Methods ---

    /**
     * Start the game loop
     */
    suspend fun start(game: Game) = coroutineScope {
        log.info("Game has started!")
        try {
            val lastGame =
            (0..game.info.maxGameTurns).fold(game) { currentGame, i ->
                val updatedGame = turn(i, currentGame.id)
                ensureActive()
                updatedGame
            }
            val message = listOf(Message(game.info.maxGameTurns, "Max turns exceeded, Game Over."))
            template.convertAndSend("/topic/game", GameRecord(game = lastGame.copy(messages = message, status = GameStatus.finished)))
        } finally {
            repo.deleteById(game.id)
            log.info("Exiting")
        }
    }

    /**
     * Turn calls the appropriate method based on the game state on each turn
     */
    fun turn(turnNumber: Int, gameId: String): Game {
        // Get new game state from DB
        val gameRecord: GameRecord = repo.findByPrimaryKey(gameId).orElseThrow{ CancellationException("No game found") }
        val game = gameRecord.game

        val updatedGame = when(game.status) {
            GameStatus.start -> initialize(gameRecord)
            GameStatus.nextTurn -> computeNextTurn(gameRecord)
            else -> {
                val exitMessage = "Game finished with ${game.status.name} status"
                log.info(exitMessage)
                throw CancellationException(exitMessage)
            }
        }

        updatedGame.messages.forEach { log.info("📢 ${it.text}") }
        template.convertAndSend("/topic/game", GameRecord(game = updatedGame))
        val gameInfo = updatedGame.info.copy(gameTurn = turnNumber + 1)
        repo.save(GameRecord(gameId, updatedGame.copy(info = gameInfo), gameRecord.lambdaRobotArns))
        return updatedGame
    }

    private fun initialize(gameRecord: GameRecord): Game {
        val game = gameRecord.game
        log.info("Game status is started. Initializing ${game.aliveCount()} robots (total: ${game.robots.count()})")
        val updatedGame= logic.initialize(game, gameRecord.lambdaRobotArns)
        log.info("Done: ${updatedGame.aliveCount()} out of ${updatedGame.robots.count()} robots ready")

        return updatedGame
    }

    private fun computeNextTurn(gameRecord: GameRecord): Game {
        val game = gameRecord.game
        log.info("\uD83C\uDFCE️ Star turn (#${game.info.gameTurn}) Invoking ${game.aliveCount()} robots (total: ${game.robots.count()}) for Game: ${game.id}")
        val (updatedGame, time) = measureTimeMillis { logic.nextTurn(game) }
        log.info("\uD83C\uDFC1 End turn (#${game.info.gameTurn}) in $time ms: ${game.aliveCount()} robots alive")
        val timeToSleep = (game.info.secondsPerTurn * 1000).toLong() - time
        if(timeToSleep > 0) {
            log.info("\uD83D\uDE34 Sleeping for $timeToSleep ms")
            Thread.sleep(timeToSleep)
        }
        return updatedGame
    }
}
