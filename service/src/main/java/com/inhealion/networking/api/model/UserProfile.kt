package com.inhealion.networking.api.model

data class UserProfile(
    val id: String,
    val name: String? = null,
    val email: String? = null,
)
