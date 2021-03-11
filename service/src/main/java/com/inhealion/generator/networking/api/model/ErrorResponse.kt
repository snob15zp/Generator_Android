package com.inhealion.generator.networking.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ErrorResponse(
    @Json(name = "errors")
    val errors: Error
)

@JsonClass(generateAdapter = true)
data class Error(
    @Json(name = "status")
    val status: Int? = null,

    @Json(name = "message")
    val message: String? = null
)
