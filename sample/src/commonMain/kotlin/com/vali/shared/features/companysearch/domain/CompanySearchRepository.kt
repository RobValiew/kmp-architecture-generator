package com.vali.shared.features.companysearch.domain

interface CompanySearchRepository {
    suspend fun load()
}