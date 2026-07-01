package com.vali.shared.features.companysearch.data

import com.vali.shared.features.companysearch.domain.CompanySearchRepository

class CompanySearchRepositoryImpl(
    private val remoteDataSource: CompanySearchRemoteDataSource
) : CompanySearchRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}