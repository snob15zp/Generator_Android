package com.inhealion.generator.networking.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id")
    val id: String? = null,

    @Json(name = "token")
    val token: String,

    @Json(name = "profile")
    val profile: UserProfile? = null
)
