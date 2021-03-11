package com.inhealion.generator.networking

import android.content.Context
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.Program
import com.inhealion.generator.networking.api.model.User
import com.inhealion.generator.networking.internal.GeneratorApiClientImpl
import java.io.InputStream

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
        fun initialize(context: Context, baseUrl: String, accountStore: AccountStore) {
            instance = GeneratorApiClient(context, baseUrl, accountStore)
        }

        @JvmStatic
        @JvmName("create")
        operator fun invoke(context: Context, baseUrl: String, accountStore: AccountStore): GeneratorApiClient {
            return GeneratorApiClientImpl(context, baseUrl, accountStore)
        }
    }
}
