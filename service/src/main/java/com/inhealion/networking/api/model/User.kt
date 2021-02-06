package com.inhealion.networking.api.model

data class User(
    val id: String? = null,
    val token: String,
    val profile: UserProfile? = null
)
