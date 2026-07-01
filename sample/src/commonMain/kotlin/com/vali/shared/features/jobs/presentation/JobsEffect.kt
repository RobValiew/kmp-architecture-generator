package com.vali.shared.features.jobs.presentation

sealed interface JobsEffect {
    data class ShowMessage(val message: String) : JobsEffect
}