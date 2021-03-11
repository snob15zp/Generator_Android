package com.inhealion.generator.networking.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RefreshToken(

    @Json(name = "token")
    val token: String? = null
)
