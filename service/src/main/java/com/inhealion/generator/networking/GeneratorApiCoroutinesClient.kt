package com.inhealion.generator.networking

import com.inhealion.generator.model.Result
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.Program
import com.inhealion.generator.networking.api.model.User
import com.inhealion.generator.networking.api.model.UserProfile
import com.inhealion.generator.networking.internal.GeneratorApiCoroutinesClientImpl
import java.io.InputStream

interface GeneratorApiCoroutinesClient {

    suspend fun signIn(login: String, password: String): Result<User>

    suspend fun fetchFolders(userProfileId: String): Result<List<Folder>>

    suspend fun fetchPrograms(folderId: String): Result<List<Program>>

    suspend fun downloadFolder(folderId: String): Result<InputStream?>

    suspend fun logout(): Result<Unit>

    suspend fun fetchUserProfile(userId: String): Result<UserProfile>

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
