package com.inhealion.generator.utils

import com.inhealion.generator.R
import com.inhealion.generator.networking.ApiError

interface ApiErrorStringProvider {
    fun getErrorMessage(error: Throwable): String
}

class ApiErrorStringProviderImpl(
    private val stringProvider: StringProvider
) : ApiErrorStringProvider {

    override fun getErrorMessage(error: Throwable) =
        when (error) {
            is ApiError.ServerError -> getApiErrorMessage(error)
            is ApiError.Unauthorized -> stringProvider.getString(R.string.error_bad_credentials)
            is ApiError.NetworkError -> stringProvider.getString(R.string.network_error)
            else -> stringProvider.getString(R.string.error_unknown)
        }


    private fun getApiErrorMessage(error: ApiError.ServerError) =
        when (error.status) {
            else -> stringProvider.getString(R.string.error_unknown)
        }
}

