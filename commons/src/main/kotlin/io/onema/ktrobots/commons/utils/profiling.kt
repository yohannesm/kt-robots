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

/**
 * Custom implementation of the measureTimeMillis that returns the value from the block call in
 * a response to time pair
 */
inline fun <TResponse> measureTimeMillis(block: () -> TResponse): Pair<TResponse, Long> {
    val start = System.currentTimeMillis()
    val result: TResponse = block()
    return result to System.currentTimeMillis() - start
}