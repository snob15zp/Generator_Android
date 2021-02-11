package com.inhealion.networking

import com.inhealion.networking.account.AccountStore
import com.inhealion.networking.api.model.Folder
import com.inhealion.networking.api.model.Program
import com.inhealion.networking.api.model.User
import com.inhealion.networking.internal.GeneratorApiCoroutinesClientImpl
import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.lang.IllegalStateException

interface GeneratorApiCoroutinesClient {

    suspend fun signIn(login: String, password: String): Flow<Result<User>>

    suspend fun fetchFolders(userProfileId: String): Flow<Result<List<Folder>>>

    suspend fun fetchPrograms(folderId: String): Flow<Result<List<Program>>>

    suspend fun downloadFolder(folderId: String): Flow<Result<InputStream?>>

    suspend fun logout(): Flow<Result<Unit>>

    companion object {
        private var instance: GeneratorApiCoroutinesClient? = null

        @JvmStatic
        fun instance(): GeneratorApiCoroutinesClient =
            instance ?: throw IllegalStateException("Client not initialized yet")

        @JvmStatic
        fun initialize(baseUrl: String, accountStore: AccountStore) {
            instance = GeneratorApiCoroutinesClient(baseUrl, accountStore)
        }

        @JvmStatic
        @JvmName("create")
        operator fun invoke(baseUrl: String, accountStore: AccountStore): GeneratorApiCoroutinesClient {
            return GeneratorApiCoroutinesClientImpl(baseUrl, accountStore)
        }
    }
}
