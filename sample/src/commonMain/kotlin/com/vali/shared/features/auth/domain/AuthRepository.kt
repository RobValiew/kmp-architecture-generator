package com.vali.shared.features.auth.domain

interface AuthRepository {
    suspend fun load()
}