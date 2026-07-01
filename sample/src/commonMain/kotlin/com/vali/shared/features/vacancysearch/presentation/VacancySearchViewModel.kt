package com.vali.shared.features.vacancysearch.presentation

import com.vali.shared.features.vacancysearch.domain.VacancySearchUseCase
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

class VacancySearchViewModel(
    private val useCase: VacancySearchUseCase,
    private val reducer: VacancySearchReducer = VacancySearchReducer()
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(VacancySearchState())
    val state: StateFlow<VacancySearchState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<VacancySearchEffect>()
    val effect: SharedFlow<VacancySearchEffect> = _effect.asSharedFlow()

    fun onAction(action: VacancySearchAction) {
        when (action) {
            VacancySearchAction.Load -> load()
            VacancySearchAction.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            reduce(VacancySearchResult.Loading)

            try {
                useCase()
                reduce(VacancySearchResult.Success)
            } catch (exception: Exception) {
                val message = exception.message ?: "Unknown error"
                reduce(VacancySearchResult.Error(message))
                _effect.emit(VacancySearchEffect.ShowMessage(message))
            }
        }
    }

    private fun reduce(result: VacancySearchResult) {
        val currentState = _state.value
        val newState = reducer.reduce(currentState, result)
        _state.value = newState
    }

    fun clear() {
        viewModelScope.cancel()
    }
}