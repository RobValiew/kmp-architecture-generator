package com.vali.shared.features.companysearch.presentation

sealed interface CompanySearchAction {
    data object Load : CompanySearchAction
    data object Retry : CompanySearchAction
}