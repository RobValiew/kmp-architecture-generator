package com.vali.shared.features.companysearch.presentation

sealed interface CompanySearchEffect {
    data class ShowMessage(val message: String) : CompanySearchEffect
}