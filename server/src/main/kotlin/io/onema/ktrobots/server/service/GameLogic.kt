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

import io.onema.ktrobots.commons.domain.*
import io.onema.ktrobots.commons.utils.*
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.*
import kotlin.random.Random
import kotlin.streams.toList

/**
 * Main logic for the game
 */
class GameLogic<TRobot : RobotService<TResponse>, TResponse>(private val robotService: TRobot) {

    //--- Fields ---
    private val gameMessages = mutableListOf<Message>()

    //--- Methods ---

    /**
     * Initialize the game by getting each of the robots build and placing them on the board
     * This method returns a copy of the game state rather than updating the existing one
     */
    fun initialize(game: Game, lambdaRobotArns: List<String>): Game {
        val (robots, t1) = measureTimeMillis { buildRobots(game, lambdaRobotArns) }
        log.info("⏱️ Getting robots builds took $t1 ms")

        val (positionedRobots, t2) = measureTimeMillis { placeOnGameBoard(game, robots) }
        log.debug("⏱️ Placing robots in the board took $t2 ms")
        return game.copy(messages = gameMessages, robots = positionedRobots, status = GameStatus.nextTurn)
    }

    /**
     * Compute next turn, get robot actions, apply action to game, move robots, update missiles and update the
     * overall game status
     */
    fun nextTurn(game: Game): Game = runBlocking {
        gameMessages.clear()
        // Get all actions asynchronously
        val (robotsToActions, t1) = measureTimeMillis { getActions(game) }
        log.info("⏱️ Get next actions from robots took $t1 ms ")

        // Apply all robot actions
        val (gameWithActions, t2) = measureTimeMillis { applyAllActionsToGame(game, robotsToActions) }
        log.debug("⏱️ Applying all robot actions took $t2 ms ")

        // Move all robots
        val (gameWithMoves, t3) = measureTimeMillis { moveAllRobotsInGame(gameWithActions) }
        log.debug("⏱️ Moving all robot took $t3 ms ")

        // Update missile states
        val (gameWithMissileUpdates, t4) = measureTimeMillis { updateAllMissiles(gameWithMoves) }
        log.debug("⏱️ Updating missiles took $t4 ms ")

        // Update game status to finished if it needs to be updated
        val (gameWithUpdatedState, t5) = measureTimeMillis { updateGameStatus(gameWithMissileUpdates) }
        log.debug("⏱️ Updating game status took $t5 ms ")

        gameWithUpdatedState.copy(messages = gameMessages)
    }

    private fun getActions(game: Game): List<Pair<LambdaRobot, LambdaRobotAction>> {
        return game.robots
            .filter { it.isAlive() }
            .parallelStream()
            .map {
                val req = LambdaRobotRequest(
                    command = LambdaRobotCommand.getAction,
                    lambdaRobot = it,
                    gameInfo = game.info,
                    index = it.index,
                    gameId = game.id
                )
                it to robotService.callRobot(it.arn, req)
            }
            .map { (robot, response) ->
                robot to robotService.deserialize(response)
            }
            .map {(robot, response) ->
                val action = if (!response.hasError) {
                    response.robotAction
                } else {
                    gameMessages.add("🚨 ACTION ERROR: ${robot.name}(R${robot.index}), will maintain speed and heading. Error: ${response.errorMessage}", game)
                    LambdaRobotAction(speed = robot.speed, heading = robot.heading)
                }
                robot to action
            }
            .toList()
    }

    private fun applyAllActionsToGame(game: Game, robotsToActions: List<Pair<LambdaRobot, LambdaRobotAction>>): Game {
        return robotsToActions.fold(game) { curGame, (robot, action) ->
            applyRobotAction(curGame, robot, action)
        }
    }

    private fun moveAllRobotsInGame(game: Game): Game {
        return game.robots.filter { it.isAlive() }.fold(game) { curGame, robot ->
            moveRobot(curGame, robot)
        }
    }

    private fun updateAllMissiles(game: Game): Game {
        return game.missiles.fold(game) { curGame, missile ->
            when (missile.status) {
                MissileStatus.flying ->
                    moveMissile(curGame, missile)
                in MissileStatus.explodingStatus() ->
                    assesMissileDamage(curGame, missile)
                else ->
                    curGame.updateMissile(missile.copy(status = MissileStatus.destroyed))
            }
        }.cleanupMissiles()
    }

    private fun updateGameStatus(game: Game): Game {
        return when(val count = game.aliveCount()) {
            0 -> {
                gameMessages.add("All robots have perished. Game Over.", game)
                game.copy(status = GameStatus.finished, missiles = listOf())
            }
            1 -> {
                val winner = game.robots.first { it.isAlive() }
                gameMessages.add("$winner is victorious! Game Over.", game)
                game.copy(status = GameStatus.finished, missiles = listOf())
            }
            else -> {
                if (count > game.info.maxGameTurns) {
                    gameMessages.add("Reached max turns. $count robots are left. Game Over.", game)
                    game.copy(status = GameStatus.finished, missiles = listOf())
                } else {
                    game
                }
            }
        }
    }

    private fun applyRobotAction(game: Game, robot: LambdaRobot, action: LambdaRobotAction): Game {
        val cooldown = if(robot.reloadCoolDown > 0) {
            0.0.coerceAtLeast(robot.reloadCoolDown - game.info.secondsPerTurn)
        } else {
            robot.reloadCoolDown
        }

        // Update speed and heading
        val targetSpeed = 0.0.coerceAtLeast(action.speed.coerceAtMost(robot.maxSpeed))
        val targetHeading = normalizeAngle(action.heading)

        // Fire missile
        return if(cooldown == 0.0 && action.fired) {
            // robot state values
            val totalMissileFired = robot.totalMissileFiredCount + 1
            val newCooldown = robot.missile.reloadCooldown

            // Add missile
            val missile = LambdaRobotMissile(
                id = "${robot.id}:M$totalMissileFired",
                robotId = robot.id,
                status = MissileStatus.flying,
                x = robot.x,
                y = robot.y,
                speed = robot.missile.velocity,
                heading = normalizeAngle(action.fireMissileHeading),
                range = 0.0.coerceAtLeast(action.fireMissileDistance.coerceAtMost(robot.missile.range)),
                directHitDamageBonus = robot.missile.directHitDamageBonus,
                nearHitDamageBonus = robot.missile.nearHitDamageBonus,
                farHitDamageBonus = robot.missile.farHitDamageBonus
            )
            val newRobot = robot.copy(targetSpeed = targetSpeed, targetHeading = targetHeading, reloadCoolDown = newCooldown, totalMissileFiredCount = totalMissileFired)
            game.copy(missiles = game.missiles + missile).updateRobot(newRobot)
        } else {
            val newRobot = robot.copy(targetSpeed = targetSpeed, targetHeading = targetHeading, reloadCoolDown = cooldown)
            game.updateRobot(newRobot)
        }
    }

    private fun assesMissileDamage(game: Game, missile: LambdaRobotMissile): Game {
        val updatedRobots = mutableListOf<LambdaRobot>()

        // Compute damage dealt by missile
        findRobotsByDistance(game, missile) { current, (robot, distance) ->
            // Check type of damage
            val (damage, damageType) = if (current.status == MissileStatus.explodingDirect && distance <= game.info.directHitRange) {
                robot.armor.directHitDamage + missile.directHitDamageBonus to "direct"
            } else if(current.status == MissileStatus.explodingNear && distance <= game.info.nearHitRange) {
                robot.armor.nearHitDamage + missile.nearHitDamageBonus to "near"
            } else if(current.status == MissileStatus.explodingFar && distance <= game.info.farHitRange) {
                robot.armor.farHitDamage + missile.farHitDamageBonus to "far"
            } else {
                0.0 to ""
            }

            // Record damage dealt
            if(robot.isAlive()) {
                val from: LambdaRobot? = game.robots.find { it.id == current.robotId }?.addDamageDealt(damage)?.addHit()
                val robotWithDamage = robot.doDamage(damage, game.info.gameTurn)
                if (from != null && damage > 0.0) {
                    if(!robotWithDamage.isAlive()) {
                        val (f, d) = if(robotWithDamage.id == from.id) {
                            gameMessages.add("🤦‍♀️️ $robotWithDamage, killed itself", game)
                            from to robotWithDamage
                        } else {
                            gameMessages.add("🦾 $robotWithDamage was killed by $from", game)
                            from.copy(totalKills = from.totalKills + 1) to robotWithDamage
                        }
                        updatedRobots.add(f)
                        updatedRobots.add(d)

                    } else {
                        if(robotWithDamage.id == from.id) {
                            gameMessages.add("🤦‍♀️️ $robotWithDamage, caused $damage damage to itself", game)
                        } else {
                            gameMessages.add("🦾 $robotWithDamage received $damage $damageType from  $from", game)
                        }
                        updatedRobots.add(from)
                        updatedRobots.add(robotWithDamage)
                    }
                }
            }
            current
        }

        // Update missile status
        val updatedMissile: LambdaRobotMissile = missile.updateExplodeStatus()
        return updatedRobots
            .fold(game) { g, r -> g.updateRobot(r)}
            .updateMissile(updatedMissile)
    }

    private fun moveRobot(game: Game, robot: LambdaRobot): Game {

        // compute new heading
        val heading = if(robot.heading != robot.targetHeading) robot.targetHeading else robot.heading

        // compute new speed
        val oldSpeed = robot.speed
        val updatedSpeed = when {
            robot.targetSpeed > robot.speed -> {
                min(robot.targetSpeed, robot.speed + robot.engine.acceleration * game.info.secondsPerTurn)
            }
            robot.targetSpeed < robot.speed -> {
                max(robot.targetSpeed, robot.speed - robot.deceleration() * game.info.secondsPerTurn)
            }
            else -> {
                robot.speed
            }
        }
        val effectiveSpeed = (updatedSpeed + oldSpeed) / 2.0
        val robotWithSH = robot.copy(heading = heading, speed = updatedSpeed)

        // move robot
        val moveData: MoveData = moveObject(game, robotWithSH.x, robotWithSH.y, robotWithSH.totalTravelDistance, effectiveSpeed, robotWithSH.heading, Double.MAX_VALUE)
        val movedRobot = robotWithSH.doMove(moveData)

        // check for collision with wall
        val robotWithDamage = if(moveData.collision) {
            val r = movedRobot.doDamage(movedRobot.armor.collisionDamage, game.info.gameTurn, isCollision = true)
            if(!r.isAlive()) {
                gameMessages.add("💥 $r was destroyed by wall collision", game)
            } else {
                gameMessages.add("💥 $r received ${r.armor.collisionDamage} damage by wall collision", game)
            }
            r
        } else {
            movedRobot
        }

        // check if robot collides with any other robot
        val updatedRobot = findRobotsByDistance(game, robotWithDamage) { current, (other, distance ) ->
            // Lambda applies collision with any objects within range and continues looking for more damage
            if(current.isAlive() && distance < game.info.collisionRange) {
                val r = current.doDamage(robot.armor.collisionDamage, game.info.gameTurn, isCollision = true)
                if(!r.isAlive()) {
                    gameMessages.add("💥 $r was destroyed by collision with ${other.name}", game)
                } else {
                    gameMessages.add("💥 $r was damaged ${r.armor.collisionDamage} by collision with ${other.name}", game)
                }
                r
            } else {
                current
            }
        }
        return game.updateRobot(updatedRobot)
    }

    private fun moveMissile(game: Game, missile: LambdaRobotMissile): Game {
        val moveData = moveObject(game, missile.x, missile.y, missile.distance, missile.speed, missile.heading, missile.range)
        val updatedMissile = if(moveData.collision) {
            missile.copy(status = MissileStatus.explodingDirect, speed = 0.0, x = moveData.x, y = moveData.y)
        } else {
            missile.doMove(moveData)
        }
        return game.updateMissile(updatedMissile)
    }


    private fun buildRobots(game: Game, arns: List<String>) =  runBlocking {
        val result = arns
            // Try to get a build from the lambda function
            .parallelMapIndexed { i, arn ->
                val req = LambdaRobotRequest(command = LambdaRobotCommand.getBuild, gameInfo = game.info, index = i, gameId = game.id)
                Triple(i, arn, robotService.callRobot(arn, req))
            }
            // Serialize the result and get the build
            .map { (i, arn, response) ->
                Triple(i, arn, robotService.deserialize(response))
            }
            .map {(i, arn, response) ->

                val build = response.robotBuild

                // use the build to create the robots and place them in the board
                val (robot, description) = if (!response.hasError) {
                    // Build robot using the build from lambda
                    LambdaRobot.create(i, build, game, arn)
                } else {
                    // there was an error getting the response from the robot function, set it as dead
                    LambdaRobot(status = LambdaRobotStatus.dead) to response.errorMessage
                }
                val message = if (robot.status == LambdaRobotStatus.alive) {
                    "$robot has joined the battle with the following config: $description"
                } else {
                    "$robot was disqualified due to bad configuration: $description"
                }
                gameMessages.add(message, game)
                robot

            }
        result
    }

    companion object {

        private val log: Logger = LoggerFactory.getLogger(GameLogic::class.java)

        /**
         * Finds all robots that are alive from the locatable object and sorts them by distance.
         * For each (robot, distance) Pair in the sorted list, a function is applied.
         * The function expects the TLocatable object and the (robot, distance) Pair and
         * must return a TLocatable with an updated state of the original TLocatable object.
         */
        fun <TLocatable : Locatable> findRobotsByDistance(game: Game, obj: TLocatable,
                                                 func: (TLocatable, Pair<LambdaRobot, Double>) -> TLocatable): TLocatable {
            return game.robots
                .filter { it.isAlive() && obj.id != it.id}
                .map { it to distanceToXY(it.x, it.y, obj.x, obj.y) }
                .sortedBy { (_, distance) -> distance }
                .fold(obj, func)
        }

        /**
         * The a set of coordinates (x, y), the total traveled distance by the object, the speed, heading and
         * total range, compute the updated information and return a a MoveData object that represents
         * the updated state.
         */
        fun moveObject(game: Game, startX: Double, startY: Double, startDistance: Double,
                       speed: Double, heading: Double, range: Double): MoveData {
            val distance = startDistance + speed * game.info.secondsPerTurn

            // Ensure robot cannot move beyond its max range
            val data = if(distance > range) {
                MoveData(distance = range, collision = true)
            } else {
                MoveData(distance = distance)
            }

            // compute new position for object
            val delta = data.distance - startDistance
            val sinHeading = sin(heading * Math.PI / 180.0)
            val cosHeading = cos(heading * Math.PI / 180.0)
            val x = startX + delta * sinHeading
            val y = startY + delta * cosHeading

            // ensure object cannot move past game board boundaries
            val newDelta = when {
                x < 0 -> (0 - startX) / sinHeading
                x > game.info.boardWidth -> (game.info.boardWidth - startX) / sinHeading
                y < 0 -> (0 - startY) / cosHeading
                y > game.info.boardHeight -> (game.info.boardHeight - startY) / cosHeading
                else -> delta
            }
            return if(newDelta != delta) {
                // object passed beyond the boundary, set collision to true and update data values
                data.copy(
                    x = x + newDelta * sinHeading,
                    y = y + newDelta * cosHeading,
                    distance = startDistance + newDelta,
                    collision = true
                )
            } else {
                // no wall collision, just update the position
                data.copy(x = x, y = y)
            }
        }

        /**
         * Tail recursive function that assigns robots a place in the game board.
         * This functions ensures that the minimum distance between robots is fulfiled
         * and if it is not, it will retry up to 100 times.
         */
        tailrec fun placeOnGameBoard(game: Game, robots: List<LambdaRobot>, count: Int = 0): List<LambdaRobot> {
            log.debug("Placing robots on game board. Try #$count")
            val info = game.info
            val marginWidth = info.boardWidth * 0.1
            val marginHeight = info.boardHeight * 0.1

            val updatedRobots = robots.map {
                val x = Random.nextDouble() * (info.boardWidth - 2.0 * marginWidth)
                val y = Random.nextDouble() * (info.boardHeight - 2.0 * marginHeight)
                it.copy(x = x, y = y)
            }

            // Verify placement
            val tryAgain = updatedRobots.flatMap{ r1 ->
                updatedRobots.filter{r1 != it}.map { r2 ->
                    distanceToXY(r1, r2) < game.info.minRobotStartDistance
                }
            }.any {it}

            return if (tryAgain && count < 100) {
                placeOnGameBoard(game, updatedRobots, count + 1)
            } else {
                updatedRobots
            }

        }

        /**
         * Scan for other robots given a heading and a resolution.
         * This will return the first robot if finds by distance.
         */
        fun scanRobots(game: Game, robot: LambdaRobot, heading: Double, resolution: Double): Optional<LambdaRobot> {
            val effectiveResolution = 0.01.coerceAtLeast(resolution.coerceAtMost(robot.radar.maxResolution))
            var robotOption = Optional.empty<LambdaRobot>()
            findRobotsByDistance(game, robot) { current, (other, distance) ->

                // check if other is beyond scan range
                if (distance <= robot.radar.range) {

                    // Relative position
                    val deltaX = other.x - current.x
                    val deltaY = other.y - current.x

                    // check if delta is within resolution limit
                    val angle = atan2(deltaX, deltaY) * 180.0 / Math.PI
                    if (robotOption.isEmpty && abs(normalizeAngle(heading - angle)) <= effectiveResolution) {
                        // found a robot within range and resolution
                        robotOption = Optional.of(other)
                    }
                }

                current
            }
            return robotOption
        }

        /**
         * Convenience extension function to add a message to the current list of messages
         */
        fun MutableCollection<Message>.add(message: String, game: Game) {
            this.add(Message(game.info.gameTurn, message))
        }
    }
}

