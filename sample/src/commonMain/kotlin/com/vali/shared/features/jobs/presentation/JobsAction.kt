package com.vali.shared.features.jobs.presentation

sealed interface JobsAction {
    data object Load : JobsAction
    data object Retry : JobsAction
}