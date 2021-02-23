package com.inhealion.generator.model

import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.utils.ApiErrorHandler

sealed class State<out T : Any> {
    data class Success<out T : Any>(val data: T) : State<T>()
    data class Failure(val error: String, val exception: Throwable? = null) : State<Nothing>()
    object Unauthorized : State<Nothing>()
    object InProgress : State<Nothing>()
    object Idle : State<Nothing>()

    companion object {

        fun success(data: Any = Any()) = Success(data)

        fun apiError(error: Throwable, apiErrorHandler: ApiErrorHandler) = when (error) {
            is ApiError.Unauthorized -> Unauthorized
            else -> Failure(apiErrorHandler.getErrorMessage(error), error)
        }

    }
}

val <T> T.exhaustive: T
    get() = this


