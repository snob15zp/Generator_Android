package com.inhealion.generator.networking

import okhttp3.Interceptor
import okhttp3.Response

class GeneratorApiInterceptor(
    private val logoutManager: LogoutManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val url = response.request.url.encodedPath

        if (response.code in TOKEN_ERROR_CODES && url !in UNAUTHORIZED_REQUESTS) {
            logoutManager.logout()
        }

        return response
    }

    companion object {
        private val TOKEN_ERROR_CODES = listOf(401, 403)
        private val UNAUTHORIZED_REQUESTS = listOf("")
    }

}
