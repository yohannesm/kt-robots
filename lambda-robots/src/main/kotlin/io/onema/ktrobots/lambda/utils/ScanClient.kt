package io.onema.ktrobots.lambda.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.onema.ktrobots.commons.domain.ScanEnemiesRequest
import io.onema.ktrobots.commons.domain.ScanEnemiesResponse
import kotlinx.coroutines.runBlocking

/**
 * Simple wrapper to make calls to the game server to scan for targets
 */
class ScanClient(
    private val apiUrl: String,
    private val gameId: String,
    private val robotId: String,
    private val httpClient: HttpClient
) {
    //--- Methods ---

    /**
     * Scan client, it sends messages to the game server to scan for other
     */
    fun scan(heading: Double, resolution: Double): ScanEnemiesResponse = runBlocking {
        val requestBody = ScanEnemiesRequest(
            gameId,
            robotId,
            heading,
            resolution
        )
        val response =
            httpClient.post<ScanEnemiesResponse>("http://$apiUrl/scan") {
                contentType(ContentType.Application.Json)
                body = requestBody
            }
        response
    }
}