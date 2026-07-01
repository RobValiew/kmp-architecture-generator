package com.vali.shared.features.orders.domain

class OrdersUseCase(
    private val repository: OrdersRepository
) {
    suspend operator fun invoke() {
        repository.load()
    }
}