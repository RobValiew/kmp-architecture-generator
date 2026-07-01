package com.vali.shared.features.news.domain

interface NewsRepository {
    suspend fun load()
}