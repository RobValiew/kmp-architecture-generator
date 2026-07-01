package com.vali.shared.features.testfeature.presentation

sealed interface TestFeatureAction {
    data object Load : TestFeatureAction
    data object Retry : TestFeatureAction
}