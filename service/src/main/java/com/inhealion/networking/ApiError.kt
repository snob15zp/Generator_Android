package com.inhealion.networking

sealed class ApiError : Exception() {
    data class ServerError(val status: Int?, val errorMessage: String?) : ApiError()
    object NotFound : ApiError()
    object Unknown : ApiError()
}
