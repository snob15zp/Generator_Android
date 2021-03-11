package com.inhealion.generator.networking

sealed class ApiError : Exception() {
    data class ServerError(val status: Int?, val errorMessage: String?) : ApiError()
    object Unknown : ApiError()
    object NetworkError : ApiError()
    object Unauthorized : ApiError()
}
