package com.vali.shared.features.news.presentation

sealed interface NewsEffect {
    data class ShowMessage(val message: String) : NewsEffect
}