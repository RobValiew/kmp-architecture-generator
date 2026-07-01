package com.vali.shared.features.products.data

import com.vali.shared.features.products.domain.ProductsRepository

class ProductsRepositoryImpl(
    private val remoteDataSource: ProductsRemoteDataSource
) : ProductsRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}