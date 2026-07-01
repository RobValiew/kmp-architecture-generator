package com.vali.shared.core.network

import org.koin.dsl.module

val networkModule = module {
    single {
        HttpClientFactory.create()
    }
}