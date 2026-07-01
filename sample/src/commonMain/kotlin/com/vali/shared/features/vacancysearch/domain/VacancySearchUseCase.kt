package com.vali.shared.features.vacancysearch.domain

class VacancySearchUseCase(
    private val repository: VacancySearchRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}