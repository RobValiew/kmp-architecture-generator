package com.vali.shared.features.orders.domain

interface OrdersRepository {
    suspend fun load()
}