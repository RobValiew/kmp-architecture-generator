package com.vali.shared.features.settings.domain

class SettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}