package com.inhealion.generator.networking.internal

import android.content.Context
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.adapter.DateJsonAdapter
import com.inhealion.generator.networking.api.GeneratorService
import com.inhealion.generator.networking.api.model.ErrorResponse
import com.inhealion.service.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File
import java.util.*

internal open class BaseGeneratorApiClient(
    context: Context,
    baseUrl: String,
    private val accountStore: AccountStore
) {
    protected val service: GeneratorService
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()

    init {
        val logging = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE)
        }

        val client = OkHttpClient.Builder()
            .cache(
                Cache(
                    directory = File(context.cacheDir, "http_cache"),
                    maxSize = 50L * 1024L * 1024L // 50 MiB
                )
            )
            .addInterceptor(AuthHeaderInterceptor())
            .addInterceptor(logging)
            .build()

        service = Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeneratorService::class.java)
    }


    protected fun handleError(error: Throwable) = when (error) {
        is ApiError -> error
        is HttpException -> parseHttpException(error)
        is java.net.ConnectException -> ApiError.NetworkError
        else -> ApiError.Unknown
    }.also {
        Timber.d(error, "Could not to send request")
    }

    private fun parseHttpException(error: HttpException): ApiError {
        if (error.code() == 401) return ApiError.Unauthorized

        return error.response()?.errorBody()?.string()?.let {
            try {
                moshi.adapter(ErrorResponse::class.java).fromJson(it)
            } catch (e: Exception) {
                Timber.d(e, "Could not to parse error response")
                null
            }?.let { errorResponse ->
                ApiError.ServerError(
                    errorResponse.errors.status,
                    errorResponse.errors.message
                )
            }
        } ?: ApiError.Unknown
    }

    inner class AuthHeaderInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            val builder = request.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")

            accountStore.load()?.let {
                builder.addHeader("Authorization", "Bearer ${it.token}")
            }

            request = builder.build()
            return chain.proceed(request)
        }

    }
}
