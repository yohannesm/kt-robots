/**
 * This file is part of the ONEMA ktrobots Package.
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed
 * with this source code.
 */

package io.onema.ktrobots.commons.utils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * This extension function was taken from:
 * https://jivimberg.io/blog/2018/05/04/parallel-map-in-kotlin/
 * Modified version parallel map to be used with mapIndexed
 */
suspend fun <A, B> Iterable<A>.parallelMapIndexed(block: suspend (Int, A) -> B): List<B> = coroutineScope {
    mapIndexed { i, it  ->
        async {
            block(i, it)
        }
    }.awaitAll()
}
