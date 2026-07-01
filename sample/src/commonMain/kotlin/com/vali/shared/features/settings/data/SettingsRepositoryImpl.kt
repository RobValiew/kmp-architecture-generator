package com.vali.shared.features.settings.data

import com.vali.shared.features.settings.domain.SettingsRepository

class SettingsRepositoryImpl(
    private val remoteDataSource: SettingsRemoteDataSource
) : SettingsRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}