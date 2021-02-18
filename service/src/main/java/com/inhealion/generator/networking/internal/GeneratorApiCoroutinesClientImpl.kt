package com.inhealion.generator.networking.internal

import com.inhealion.generator.model.Result
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.account.AccountStore

internal class GeneratorApiCoroutinesClientImpl(
    baseUrl: String,
    private val accountStore: AccountStore
) : BaseGeneratorApiClient(baseUrl, accountStore), GeneratorApiCoroutinesClient {

    override suspend fun signIn(login: String, password: String) =
        sendRequest { service.login(login, password)?.also { accountStore.store(it) } }

    override suspend fun fetchFolders(userProfileId: String) =
        sendRequest { service.fetchFolders(userProfileId) }


    override suspend fun fetchPrograms(folderId: String) =
        sendRequest { service.fetchPrograms(folderId) }


    override suspend fun downloadFolder(folderId: String) =
        sendRequest { service.downloadFolder(folderId)?.byteStream() }


    override suspend fun logout() =
        sendRequest { service.logout() }


    private suspend fun <T> sendRequest(request: suspend () -> T?) =
        try {
            request()
                ?.let { Result.success(it) }
                ?: Result.failure((ApiError.NotFound))
        } catch (e: Exception) {
            Result.failure(handleError(e))
        }
}
