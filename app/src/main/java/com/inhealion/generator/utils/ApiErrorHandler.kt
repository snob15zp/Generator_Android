package com.inhealion.generator.utils

import com.inhealion.generator.R
import com.inhealion.generator.networking.ApiError

interface ApiErrorHandler {
    fun getErrorMessage(error: Throwable): String
}

class ApiErrorHandlerImpl(
    private val stringProvider: StringProvider
) : ApiErrorHandler {

    override fun getErrorMessage(error: Throwable) =
        when (error) {
            is ApiError.ServerError -> getApiErrorMessage(error)
            is ApiError.NetworkError -> stringProvider.getString(R.string.network_error)
            else -> stringProvider.getString(R.string.api_error_unknown)
        }


    private fun getApiErrorMessage(error: ApiError.ServerError) =
        when (error.status) {
            else -> stringProvider.getString(R.string.api_error_unknown)
        }
}

