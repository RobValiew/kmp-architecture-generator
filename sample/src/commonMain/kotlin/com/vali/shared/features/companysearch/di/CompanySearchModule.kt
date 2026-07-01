package com.vali.shared.features.companysearch.di

import com.vali.shared.features.companysearch.data.CompanySearchRemoteDataSource
import com.vali.shared.features.companysearch.data.CompanySearchRepositoryImpl
import com.vali.shared.features.companysearch.domain.CompanySearchRepository
import com.vali.shared.features.companysearch.domain.CompanySearchUseCase
import com.vali.shared.features.companysearch.presentation.CompanySearchViewModel
import com.vali.shared.features.companysearch.presentation.CompanySearchReducer
import org.koin.dsl.module

val companySearchModule = module {
    single { CompanySearchRemoteDataSource(get()) }
    single<CompanySearchRepository> { CompanySearchRepositoryImpl(get()) }
    factory { CompanySearchUseCase(get()) }
    factory { CompanySearchReducer() }
    factory { CompanySearchViewModel(get(), get()) }
}