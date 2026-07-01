package com.vali.shared.features.auth.presentation

sealed interface AuthEffect {
    data class ShowMessage(val message: String) : AuthEffect
}