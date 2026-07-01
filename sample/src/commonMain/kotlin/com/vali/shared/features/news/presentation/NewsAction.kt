package com.vali.shared.features.news.presentation

sealed interface NewsAction {
    data object Load : NewsAction
    data object Retry : NewsAction
}