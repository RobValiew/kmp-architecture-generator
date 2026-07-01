package com.vali.shared.features.testfeature.presentation

sealed interface TestFeatureEffect {
    data class ShowMessage(val message: String) : TestFeatureEffect
}