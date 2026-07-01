package com.vali.shared.features.jobs.presentation

import com.vali.shared.features.jobs.domain.JobsUseCase
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

class JobsViewModel(
    private val useCase: JobsUseCase,
    private val reducer: JobsReducer = JobsReducer()
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(JobsState())
    val state: StateFlow<JobsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<JobsEffect>()
    val effect: SharedFlow<JobsEffect> = _effect.asSharedFlow()

    fun onAction(action: JobsAction) {
        when (action) {
            JobsAction.Load -> load()
            JobsAction.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            reduce(JobsResult.Loading)

            try {
                useCase()
                reduce(JobsResult.Success)
            } catch (exception: Exception) {
                val message = exception.message ?: "Unknown error"
                reduce(JobsResult.Error(message))
                _effect.emit(JobsEffect.ShowMessage(message))
            }
        }
    }

    private fun reduce(result: JobsResult) {
        val currentState = _state.value
        val newState = reducer.reduce(currentState, result)
        _state.value = newState
    }

    fun clear() {
        viewModelScope.cancel()
    }
}