package com.vali.shared.features.products.domain

class ProductsUseCase(
    private val repository: ProductsRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}