package com.vali.shared.features.auth.domain

class AuthUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}