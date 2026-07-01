package com.vali.shared.features.jobs.domain

class JobsUseCase(
    private val repository: JobsRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}