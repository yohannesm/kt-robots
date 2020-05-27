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

package io.onema.ktrobots.server.controller

import io.onema.ktrobots.commons.domain.*
import io.onema.ktrobots.server.data.GameTableRepository
import io.onema.ktrobots.server.domain.GameRecord
import io.onema.ktrobots.server.service.GameLoop
import io.onema.ktrobots.server.service.LambdaRobotService
import io.onema.ktrobots.server.service.LocalRobotService
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import software.amazon.awssdk.services.lambda.LambdaAsyncClient
import java.util.*


/**
 * Websocket controller, this deals with starting and stopping a game
 */
@Controller
class GameController(
    val repo: GameTableRepository,
    val template: SimpMessagingTemplate,
    val lambda: LambdaAsyncClient,
    val environment: Environment
) {

    //--- Fields ---
    val log: Logger = LoggerFactory.getLogger(GameController::class.java)

    val loopJobs = mutableMapOf<String, Job>()

    //--- Methods ---
    /**
     * Start a new game
     */
    @MessageMapping("/start")
    @SendTo("/topic/game")
    fun startGame(req: StartGameRequest): GameResponse {
        val gameOption: Optional<GameRecord> = loadExistingGame()

        return if (gameOption.isEmpty) {
           startNewGame(req)
        } else {
            GameResponse(gameOption.get().game)
        }
    }

    /**
     * Force to stop the current game, this will cancelAndJoin the running gameloop and delete the
     * DynamoDB record for the game.
     */
    @MessageMapping("/stop")
    @SendTo("/topic/game")
    fun gameStop(req: StopGameRequest): GameResponse {
        log.info("Stopping game ${req.gameId}")
        val itemOption: Optional<GameRecord> = repo.findByPrimaryKey(req.gameId)

        // Delete the item if it is present in the table
        itemOption.ifPresent(this::deleteLoop)
        return GameResponse(Game(status = GameStatus.finished))
    }

    private fun loadExistingGame(): Optional<GameRecord> {
        // Check if there are any active jobs and return a game id
        val results = loopJobs.filter { it.value.isActive }
            .map { (gameId, job) -> if (job.isActive) gameId else "" }
            .filter { it.isNotEmpty() }
            .map { repo.findByPrimaryKey(it) }
        return if (results.isNotEmpty()) {
            results.first()
        } else {
            Optional.empty()
        }
    }

    private fun startNewGame(req: StartGameRequest): GameResponse {
        val game = Game(
            id = UUID.randomUUID().toString(),
            status = GameStatus.start,
            info = GameInfo(
                maxGameTurns = req.maxTurns,
                boardWidth = req.boardWidth,
                boardHeight = req.boardHeight,
                secondsPerTurn = req.secondsPerTurn,
                maxBuildPoints = req.maxBuildPoints,
                directHitRange = req.directHitRange,
                nearHitRange = req.nearHitRange,
                farHitRange = req.farHitRange,
                collisionRange = req.collisionRange,
                apiUrl = "${req.apiHost}:${environment.getProperty("local.server.port")}"
            ),
            minRobotStartDistance = req.minRobotStartDistance,
            robotTimeoutSeconds = req.robotTimeoutSeconds
        )
        log.info("Starting game ${game.id}")
        val record = GameRecord(game.id, game, req.robotArns)
        repo.save(record)

        loopJobs[game.id] = CoroutineScope(Dispatchers.IO).launch {
            when (req.robotType) {
                "lambda" -> {

                    // Start a new Lambda game loop
                    val lambdaGameLoop = GameLoop(template, repo, LambdaRobotService(lambda))
                    lambdaGameLoop.start(game)
                }
                else -> {

                    // Start a new local game loop
                    val localGameLoop = GameLoop(template, repo, LocalRobotService())
                    localGameLoop.start(game)
                }
            }
        }
        return GameResponse(game)
    }

    private fun deleteLoop(record: GameRecord) {
        val gameId = record.game.id
        if(loopJobs.containsKey(gameId) && loopJobs[gameId]?.isActive == true) runBlocking {
            loopJobs[gameId]?.cancelAndJoin()
            loopJobs.remove(gameId)
            log.info("Game loop deleted")
        }
    }
}
