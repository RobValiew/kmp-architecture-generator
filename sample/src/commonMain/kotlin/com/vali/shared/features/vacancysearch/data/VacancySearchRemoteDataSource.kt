package com.vali.shared.features.vacancysearch.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class VacancySearchRemoteDataSource(
    private val httpClient: HttpClient
) {

    suspend fun load() {
        // TODO: Replace endpoint with real API URL
        httpClient.get("https://example.com/api/vacancySearch").body<Unit>()
    }
}