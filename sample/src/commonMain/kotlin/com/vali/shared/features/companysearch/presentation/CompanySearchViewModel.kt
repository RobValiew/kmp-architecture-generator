package com.vali.shared.features.companysearch.presentation

import com.vali.shared.features.companysearch.domain.CompanySearchUseCase
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

class CompanySearchViewModel(
    private val useCase: CompanySearchUseCase,
    private val reducer: CompanySearchReducer = CompanySearchReducer()
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(CompanySearchState())
    val state: StateFlow<CompanySearchState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CompanySearchEffect>()
    val effect: SharedFlow<CompanySearchEffect> = _effect.asSharedFlow()

    fun onAction(action: CompanySearchAction) {
        when (action) {
            CompanySearchAction.Load -> load()
            CompanySearchAction.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            reduce(CompanySearchResult.Loading)

            try {
                useCase()
                reduce(CompanySearchResult.Success)
            } catch (exception: Exception) {
                val message = exception.message ?: "Unknown error"
                reduce(CompanySearchResult.Error(message))
                _effect.emit(CompanySearchEffect.ShowMessage(message))
            }
        }
    }

    private fun reduce(result: CompanySearchResult) {
        val currentState = _state.value
        val newState = reducer.reduce(currentState, result)
        _state.value = newState
    }

    fun clear() {
        viewModelScope.cancel()
    }
}