package com.inhealion.generator.networking.internal

import android.content.Context
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.api.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.ZipInputStream

internal class GeneratorApiCoroutinesClientImpl(
    baseUrl: String,
    private val context: Context,
    private val accountStore: AccountStore
) : BaseGeneratorApiClient(baseUrl, accountStore), GeneratorApiCoroutinesClient {
    private val downloadFolder = context.getDir("download", Context.MODE_PRIVATE)
    private val refreshTokenAttempt = AtomicInteger(0)

    override suspend fun signIn(login: String, password: String) =
        sendRequest { service.login(login, password)?.also { accountStore.store(it) } }

    override suspend fun fetchFolders(userProfileId: String) = sendRequest { service.fetchFolders(userProfileId) }

    override suspend fun fetchPrograms(folderId: String) = sendRequest { service.fetchPrograms(folderId) }

    override suspend fun downloadFolder(folderId: String) = flow {
        service.downloadFolder(folderId)?.let { responseBody ->
            val folder = File(downloadFolder, folderId)
            if (folder.exists()) folder.deleteRecursively()
            folder.mkdirs()

            try {
                ZipInputStream(responseBody.byteStream()).use { zipInputStream ->
                    var entry = zipInputStream.nextEntry
                    while (entry != null) {
                        FileOutputStream(File(folder, entry.name)).use { zipInputStream.copyTo(it) }
                        zipInputStream.closeEntry()
                        entry = zipInputStream.nextEntry
                    }
                }
                emit(folder.absolutePath)
            } catch (e: Exception) {
                Timber.e(e, "Failed to download file")
                throw handleError(e)
            }
        } ?: throw ApiError.ServerError(404, "Resource not found")
    }


    override suspend fun logout(): Flow<Unit> = flow {
        accountStore.remove()
        service.logout()
        emit(Unit)
    }

    override suspend fun fetchUserProfile(userId: String) = sendRequest { service.fetchUserProfile(userId) }

    private suspend fun <T> sendRequest(request: suspend () -> T?): Flow<T> {
        return flow {
            try {
                val response = request()
                refreshTokenAttempt.set(0)
                response?.let { emit(it) } ?: throw ApiError.ServerError(404, "Resource not found")
            } catch (e: Exception) {
                Timber.e(e, "Send request failed")
                if (refreshTokenIsNeeded(e)) {
                    return@flow emitAll(sendRequest(request))
                }

                throw when (e) {
                    is ApiError -> e
                    else -> handleError(e)
                }
            }
        }
    }

    private suspend fun refreshTokenIsNeeded(error: Exception): Boolean =
        if (error is HttpException && error.code() == 401 && refreshTokenAttempt.getAndIncrement() == 0) {
            try {
                service.refreshToken()?.token?.let {
                    accountStore.store(User(token = it))
                    true
                } ?: false
            } catch (ex: Exception) {
                //Ignore
                Timber.w(ex, "Unable to refresh token")
                false
            }
        } else {
            false
        }
}
