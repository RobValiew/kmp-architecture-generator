package com.vali.shared.features.jobs.domain

interface JobsRepository {
    suspend fun load()
}