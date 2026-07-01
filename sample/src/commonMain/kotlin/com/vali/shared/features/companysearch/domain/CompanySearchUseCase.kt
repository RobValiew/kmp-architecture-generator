package com.vali.shared.features.companysearch.domain

class CompanySearchUseCase(
    private val repository: CompanySearchRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}