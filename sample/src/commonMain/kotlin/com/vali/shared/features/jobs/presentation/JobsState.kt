package com.vali.shared.features.jobs.presentation

data class JobsState(
    val isLoading: Boolean = false,
    val error: String? = null
)