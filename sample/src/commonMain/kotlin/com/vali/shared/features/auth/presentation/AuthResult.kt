package com.vali.shared.features.auth.presentation

sealed interface AuthResult {
    data object Loading : AuthResult
    data object Success : AuthResult
    data class Error(val message: String) : AuthResult
}