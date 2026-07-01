package com.vali.shared.features.auth.data

import com.vali.shared.features.auth.domain.AuthRepository

class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}