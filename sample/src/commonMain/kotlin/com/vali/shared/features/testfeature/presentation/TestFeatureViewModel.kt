package com.vali.shared.features.testfeature.presentation

import com.vali.shared.features.testfeature.domain.TestFeatureUseCase
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

class TestFeatureViewModel(
    private val useCase: TestFeatureUseCase,
    private val reducer: TestFeatureReducer = TestFeatureReducer()
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(TestFeatureState())
    val state: StateFlow<TestFeatureState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<TestFeatureEffect>()
    val effect: SharedFlow<TestFeatureEffect> = _effect.asSharedFlow()

    fun onAction(action: TestFeatureAction) {
        when (action) {
            TestFeatureAction.Load -> load()
            TestFeatureAction.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            reduce(TestFeatureResult.Loading)

            try {
                useCase()
                reduce(TestFeatureResult.Success)
            } catch (exception: Exception) {
                val message = exception.message ?: "Unknown error"
                reduce(TestFeatureResult.Error(message))
                _effect.emit(TestFeatureEffect.ShowMessage(message))
            }
        }
    }

    private fun reduce(result: TestFeatureResult) {
        val currentState = _state.value
        val newState = reducer.reduce(currentState, result)
        _state.value = newState
    }

    fun clear() {
        viewModelScope.cancel()
    }
}