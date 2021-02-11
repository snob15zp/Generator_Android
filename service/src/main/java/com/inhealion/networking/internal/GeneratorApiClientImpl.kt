package com.inhealion.networking.internal

import com.inhealion.networking.ApiCallback
import com.inhealion.networking.ApiError
import com.inhealion.networking.GeneratorApiClient
import com.inhealion.networking.account.AccountStore
import com.inhealion.networking.api.GeneratorService
import com.inhealion.networking.api.model.ErrorResponse
import com.inhealion.networking.api.model.Folder
import com.inhealion.networking.api.model.Program
import com.inhealion.networking.api.model.User
import com.inhealion.service.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.InputStream
import java.util.*

class GeneratorApiClientImpl(
    baseUrl: String,
    private val accountStore: AccountStore
) : GeneratorApiClient {

    private val service: GeneratorService
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

    override fun signIn(login: String, password: String, callback: ApiCallback<User>) = sendRequest(callback) {
        service.login(login, password)?.also {
            accountStore.store(it)
        }
    }

    override fun fetchFolders(userProfileId: String, callback: ApiCallback<List<Folder>>) = sendRequest(callback) {
        service.fetchFolders(userProfileId)
    }

    override fun fetchPrograms(folderId: String, callback: ApiCallback<List<Program>>) = sendRequest(callback) {
        service.fetchPrograms(folderId)
    }

    override fun downloadFolder(folderId: String, callback: ApiCallback<InputStream?>) = sendRequest(callback) {
        service.downloadFolder(folderId)?.byteStream()
    }

    override fun logout(callback: ApiCallback<Unit>) = sendRequest(callback) {
        service.logout().also { accountStore.remove() }
    }

    private fun <T> sendRequest(callback: ApiCallback<T>, request: suspend () -> T?) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                request()?.let { callback.success(it) } ?: callback.failure(ApiError.NotFound)
            } catch (e: Exception) {
                Timber.d(e, "Could not to send request")
                val error = when (e) {
                    is HttpException -> e.response()?.errorBody()?.string()?.let {
                        val errorResponse = moshi.adapter(ErrorResponse::class.java).fromJson(it)
                        ApiError.ServerError(
                            errorResponse?.errors?.status,
                            errorResponse?.errors?.message
                        )
                    } ?: ApiError.Unknown
                    else -> ApiError.Unknown
                }
                callback.failure(error)
            }
        }
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
