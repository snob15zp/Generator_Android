package com.inhealion.generator.networking

import com.inhealion.generator.model.Result
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.Program
import com.inhealion.generator.networking.api.model.User
import com.inhealion.generator.networking.api.model.UserProfile
import com.inhealion.generator.networking.internal.GeneratorApiCoroutinesClientImpl
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

interface GeneratorApiCoroutinesClient {

    suspend fun signIn(login: String, password: String): Flow<User>

    suspend fun fetchFolders(userProfileId: String): Flow<List<Folder>>

    suspend fun fetchPrograms(folderId: String): Flow<List<Program>>

    suspend fun downloadFolder(folderId: String): Flow<InputStream?>

    suspend fun logout(): Flow<Unit>

    suspend fun fetchUserProfile(userId: String): Flow<UserProfile>

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
