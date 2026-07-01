package com.vali.shared.features.testfeature.data

import com.vali.shared.features.testfeature.domain.TestFeatureRepository

class TestFeatureRepositoryImpl(
    private val remoteDataSource: TestFeatureRemoteDataSource
) : TestFeatureRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}