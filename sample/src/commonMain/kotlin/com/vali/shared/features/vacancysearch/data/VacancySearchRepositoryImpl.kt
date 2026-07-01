package com.vali.shared.features.vacancysearch.data

import com.vali.shared.features.vacancysearch.domain.VacancySearchRepository

class VacancySearchRepositoryImpl(
    private val remoteDataSource: VacancySearchRemoteDataSource
) : VacancySearchRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}