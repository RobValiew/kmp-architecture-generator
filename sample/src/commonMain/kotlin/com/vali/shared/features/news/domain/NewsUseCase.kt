package com.vali.shared.features.news.domain

class NewsUseCase(
    private val repository: NewsRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}