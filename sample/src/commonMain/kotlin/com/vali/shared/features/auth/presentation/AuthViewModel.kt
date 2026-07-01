package com.vali.shared.features.auth.presentation

import com.vali.shared.features.auth.domain.AuthUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val useCase: AuthUseCase,
    private val reducer: AuthReducer = AuthReducer()
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<AuthEffect>()
    val effect: SharedFlow<AuthEffect> = _effect.asSharedFlow()

    fun onAction(action: AuthAction) {
        when (action) {
            AuthAction.Load -> load()
            AuthAction.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            reduce(AuthResult.Loading)

            try {
                useCase()
                reduce(AuthResult.Success)
            } catch (exception: Exception) {
                val message = exception.message ?: "Unknown error"
                reduce(AuthResult.Error(message))
                _effect.emit(AuthEffect.ShowMessage(message))
            }
        }
    }

    private fun reduce(result: AuthResult) {
        val currentState = _state.value
        val newState = reducer.reduce(currentState, result)
        _state.value = newState
    }

    fun clear() {
        viewModelScope.cancel()
    }
}