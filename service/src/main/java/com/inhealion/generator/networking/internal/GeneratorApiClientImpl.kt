package com.inhealion.generator.networking.internal

import com.inhealion.generator.networking.ApiCallback
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiClient
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.Program
import com.inhealion.generator.networking.api.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.InputStream

internal class GeneratorApiClientImpl(
    baseUrl: String,
    private val accountStore: AccountStore
) : BaseGeneratorApiClient(baseUrl, accountStore), GeneratorApiClient {

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
                request()?.let { callback.success(it) } ?: callback.failure(ApiError.ServerError(404, "Resource not found"))
            } catch (e: Exception) {
                callback.failure(handleError(e))
            }
        }
    }
}
