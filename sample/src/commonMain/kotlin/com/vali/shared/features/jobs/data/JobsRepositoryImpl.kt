package com.vali.shared.features.jobs.data

import com.vali.shared.features.jobs.domain.JobsRepository

class JobsRepositoryImpl(
    private val remoteDataSource: JobsRemoteDataSource
) : JobsRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}