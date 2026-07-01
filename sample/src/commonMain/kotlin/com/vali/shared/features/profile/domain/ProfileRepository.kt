package com.vali.shared.features.profile.domain

interface ProfileRepository {
    suspend fun load()
}