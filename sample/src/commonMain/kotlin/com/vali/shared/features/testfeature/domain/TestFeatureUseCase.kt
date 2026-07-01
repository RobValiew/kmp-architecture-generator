package com.vali.shared.features.testfeature.domain

class TestFeatureUseCase(
    private val repository: TestFeatureRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}