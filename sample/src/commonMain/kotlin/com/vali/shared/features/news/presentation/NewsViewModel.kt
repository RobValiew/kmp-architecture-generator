package com.vali.shared.features.news.presentation

import com.vali.shared.features.news.domain.NewsUseCase
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

class NewsViewModel(
    private val useCase: NewsUseCase,
    private val reducer: NewsReducer = NewsReducer()
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(NewsState())
    val state: StateFlow<NewsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<NewsEffect>()
    val effect: SharedFlow<NewsEffect> = _effect.asSharedFlow()

    fun onAction(action: NewsAction) {
        when (action) {
            NewsAction.Load -> load()
            NewsAction.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            reduce(NewsResult.Loading)

            try {
                useCase()
                reduce(NewsResult.Success)
            } catch (exception: Exception) {
                val message = exception.message ?: "Unknown error"
                reduce(NewsResult.Error(message))
                _effect.emit(NewsEffect.ShowMessage(message))
            }
        }
    }

    private fun reduce(result: NewsResult) {
        val currentState = _state.value
        val newState = reducer.reduce(currentState, result)
        _state.value = newState
    }

    fun clear() {
        viewModelScope.cancel()
    }
}