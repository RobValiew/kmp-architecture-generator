package com.vali.shared.features.orders.data

import com.vali.shared.features.orders.domain.OrdersRepository

class OrdersRepositoryImpl(
    private val remoteDataSource: OrdersRemoteDataSource
) : OrdersRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}