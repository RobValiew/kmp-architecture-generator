package com.vali.shared.features.settings.domain

interface SettingsRepository {
    suspend fun load()
}