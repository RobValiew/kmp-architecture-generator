package com.vali.shared.features.vacancysearch.domain

interface VacancySearchRepository {
    suspend fun load()
}