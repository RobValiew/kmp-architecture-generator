package com.vali.shared.features.testfeature.presentation

sealed interface TestFeatureResult {
    data object Loading : TestFeatureResult
    data object Success : TestFeatureResult
    data class Error(val message: String) : TestFeatureResult
}