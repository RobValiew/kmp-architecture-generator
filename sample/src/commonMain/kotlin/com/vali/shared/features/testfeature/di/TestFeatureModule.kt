package com.vali.shared.features.testfeature.di

import com.vali.shared.features.testfeature.data.TestFeatureRemoteDataSource
import com.vali.shared.features.testfeature.data.TestFeatureRepositoryImpl
import com.vali.shared.features.testfeature.domain.TestFeatureRepository
import com.vali.shared.features.testfeature.domain.TestFeatureUseCase
import com.vali.shared.features.testfeature.presentation.TestFeatureViewModel
import com.vali.shared.features.testfeature.presentation.TestFeatureReducer
import org.koin.dsl.module

val testFeatureModule = module {
    single { TestFeatureRemoteDataSource() }
    single<TestFeatureRepository> { TestFeatureRepositoryImpl(get()) }
    factory { TestFeatureUseCase(get()) }
    factory { TestFeatureReducer() }
    factory { TestFeatureViewModel(get(), get()) }
}