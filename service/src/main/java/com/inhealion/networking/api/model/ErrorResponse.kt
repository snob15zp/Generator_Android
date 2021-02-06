package com.inhealion.networking.api.model

data class ErrorResponse(
    val errors: Error
)

data class Error(
    val status: Int? = null,
    val message: String? = null
)
