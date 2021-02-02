package com.inhealion.networking

import com.inhealion.networking.account.AccountStore
import com.inhealion.networking.api.model.Folder
import com.inhealion.networking.api.model.Program
import com.inhealion.networking.api.model.User
import com.inhealion.networking.internal.GeneratorApiClientImpl
import java.io.InputStream
import java.lang.IllegalStateException

interface GeneratorApiClient {

    fun signIn(login: String, password: String, callback: ApiCallback<User>)

    fun fetchFolders(userProfileId: String, callback: ApiCallback<List<Folder>>)

    fun fetchPrograms(folderId: String, callback: ApiCallback<List<Program>>)

    fun downloadFolder(folderId: String, callback: ApiCallback<InputStream?>)

    fun logout(callback: ApiCallback<Unit>)

    companion object {
        private var instance: GeneratorApiClient? = null

        @JvmStatic
        fun instance(): GeneratorApiClient = instance ?: throw IllegalStateException("Client not initialized yet")

        @JvmStatic
        fun initialize(baseUrl: String, accountStore: AccountStore) {
            instance = GeneratorApiClient(baseUrl, accountStore)
        }

        @JvmStatic
        @JvmName("create")
        operator fun invoke(baseUrl: String, accountStore: AccountStore): GeneratorApiClient {
            return GeneratorApiClientImpl(baseUrl, accountStore)
        }
    }
}
