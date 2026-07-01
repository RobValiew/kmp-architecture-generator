package com.vali.shared.features.news.data

import com.vali.shared.features.news.domain.NewsRepository

class NewsRepositoryImpl(
    private val remoteDataSource: NewsRemoteDataSource
) : NewsRepository {

    override suspend fun load() {
        remoteDataSource.load()
    }
}