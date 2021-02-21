package com.inhealion.generator.networking.internal

import com.inhealion.generator.model.Result
import com.inhealion.generator.model.tryWithResult
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.api.model.User
import retrofit2.HttpException
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

internal class GeneratorApiCoroutinesClientImpl(
    baseUrl: String,
    private val accountStore: AccountStore
) : BaseGeneratorApiClient(baseUrl, accountStore), GeneratorApiCoroutinesClient {
    private val refreshTokenAttempt = AtomicInteger(0)

    override suspend fun signIn(login: String, password: String) =
        sendRequest { service.login(login, password)?.also { accountStore.store(it) } }

    override suspend fun fetchFolders(userProfileId: String) = sendRequest { service.fetchFolders(userProfileId) }

    override suspend fun fetchPrograms(folderId: String) = sendRequest { service.fetchPrograms(folderId) }

    override suspend fun downloadFolder(folderId: String) =
        sendRequest { service.downloadFolder(folderId)?.byteStream() }

    override suspend fun logout(): Result<Unit> {
        accountStore.remove()
        return tryWithResult { service.logout() }
    }

    override suspend fun fetchUserProfile(userId: String) = sendRequest { service.fetchUserProfile(userId) }

    private suspend fun <T> sendRequest(request: suspend () -> T?): Result<T> {
        return try {
            request()
                .also { refreshTokenAttempt.set(0) }
                ?.let { Result.success(it) }
                ?: Result.failure((ApiError.ServerError(404, "Resource not found")))
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 401 && refreshTokenAttempt.getAndIncrement() == 0) {
                try {
                    service.refreshToken()?.token?.let {
                        accountStore.store(User(token = it))
                        return sendRequest(request)
                    }
                } catch (ex: Exception) {
                    //Ignore
                    Timber.w(ex, "Unable to refresh token")
                }
            }

            Result.failure(handleError(e))
        }
    }
}
