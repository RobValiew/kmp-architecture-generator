package com.vali.shared.features.auth.presentation

sealed interface AuthAction {
    data object Load : AuthAction
    data object Retry : AuthAction
}