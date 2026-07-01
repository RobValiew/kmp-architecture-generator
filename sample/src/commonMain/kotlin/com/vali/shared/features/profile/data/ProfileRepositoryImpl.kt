package com.vali.shared.features.profile.data

import com.vali.shared.features.profile.domain.ProfileRepository

class ProfileRepositoryImpl(
    private val remoteDataSource: ProfileRemoteDataSource
) : ProfileRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}