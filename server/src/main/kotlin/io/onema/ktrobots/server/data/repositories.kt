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

package io.onema.ktrobots.server.data

import io.onema.ktrobots.server.domain.GameRecord
import org.socialsignin.spring.data.dynamodb.repository.EnableScan
import org.springframework.data.repository.CrudRepository
import java.util.*

@EnableScan
interface GameTableRepository : CrudRepository<GameRecord, String> {
    fun findByPrimaryKey(pk: String): Optional<GameRecord>
    fun deleteBy(pk: String)
}