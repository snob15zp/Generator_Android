package com.inhealion.networking.internal

import com.inhealion.networking.ApiError
import com.inhealion.networking.GeneratorApiCoroutinesClient
import com.inhealion.networking.account.AccountStore
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

internal class GeneratorApiCoroutinesClientImpl(
    baseUrl: String,
    private val accountStore: AccountStore
) : BaseGeneratorApiClient(baseUrl, accountStore), GeneratorApiCoroutinesClient {

    override suspend fun signIn(login: String, password: String) = flow {
        sendRequest(this) { service.login(login, password)?.also { accountStore.store(it) } }
    }

    override suspend fun fetchFolders(userProfileId: String) = flow {
        sendRequest(this) { service.fetchFolders(userProfileId) }
    }

    override suspend fun fetchPrograms(folderId: String) = flow {
        sendRequest(this) { service.fetchPrograms(folderId) }
    }

    override suspend fun downloadFolder(folderId: String) = flow {
        sendRequest(this) { service.downloadFolder(folderId)?.byteStream() }
    }

    override suspend fun logout() = flow {
        sendRequest(this) { service.logout() }
    }

    private suspend fun <T> sendRequest(flowCollector: FlowCollector<Result<T>>, request: suspend () -> T?) {
        try {
            request()
                ?.let { flowCollector.emit(Result.success(it)) }
                ?: flowCollector.emit(Result.failure((ApiError.NotFound)))
        } catch (e: Exception) {
            flowCollector.emit(Result.failure(handleError(e)))
        }
    }
}
