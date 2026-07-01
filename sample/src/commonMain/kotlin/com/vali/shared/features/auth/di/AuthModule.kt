package com.vali.shared.features.auth.di

import com.vali.shared.features.auth.data.AuthRemoteDataSource
import com.vali.shared.features.auth.data.AuthRepositoryImpl
import com.vali.shared.features.auth.domain.AuthRepository
import com.vali.shared.features.auth.domain.AuthUseCase
import com.vali.shared.features.auth.presentation.AuthViewModel
import com.vali.shared.features.auth.presentation.AuthReducer
import org.koin.dsl.module

val authModule = module {
    single { AuthRemoteDataSource() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    factory { AuthUseCase(get()) }
    factory { AuthReducer() }
    factory { AuthViewModel(get(), get()) }
}