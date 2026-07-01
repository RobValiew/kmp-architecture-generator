package com.vali.shared.features.products.domain

interface ProductsRepository {
    suspend fun load()
}