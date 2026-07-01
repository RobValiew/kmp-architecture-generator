package com.vali.shared.features.vacancysearch.di

import com.vali.shared.features.vacancysearch.data.VacancySearchRemoteDataSource
import com.vali.shared.features.vacancysearch.data.VacancySearchRepositoryImpl
import com.vali.shared.features.vacancysearch.domain.VacancySearchRepository
import com.vali.shared.features.vacancysearch.domain.VacancySearchUseCase
import com.vali.shared.features.vacancysearch.presentation.VacancySearchViewModel
import com.vali.shared.features.vacancysearch.presentation.VacancySearchReducer
import org.koin.dsl.module

val vacancySearchModule = module {
    single { VacancySearchRemoteDataSource(get()) }
    single<VacancySearchRepository> { VacancySearchRepositoryImpl(get()) }
    factory { VacancySearchUseCase(get()) }
    factory { VacancySearchReducer() }
    factory { VacancySearchViewModel(get(), get()) }
}