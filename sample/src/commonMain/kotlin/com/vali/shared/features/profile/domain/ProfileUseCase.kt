package com.vali.shared.features.profile.domain

class ProfileUseCase(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}