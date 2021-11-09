package com.inhealion.generator.networking.internal

import android.content.Context
import com.inhealion.generator.networking.ApiError
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.LogoutManager
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.api.model.FirmwareVersion
import com.inhealion.generator.networking.api.model.User
import kotlinx.coroutines.flow.*
import okhttp3.ResponseBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipInputStream

internal class GeneratorApiCoroutinesClientImpl(
    baseUrl: String,
    context: Context,
    private val accountStore: AccountStore,
    private val logoutManager: LogoutManager
) : BaseGeneratorApiClient(context, baseUrl, accountStore), GeneratorApiCoroutinesClient {

    private val downloadFolder = context.getDir("download", Context.MODE_PRIVATE)

    override suspend fun signIn(login: String, password: String) = sendRequest(false) {
        service.login(login, password)?.also { accountStore.store(it) }
    }

    override suspend fun fetchFolders(userProfileId: String) =
        sendRequest { service.fetchFolders(userProfileId) }

    override suspend fun fetchPrograms(folderId: String) =
        sendRequest { service.fetchPrograms(folderId) }

    override suspend fun downloadFolder(folderId: String) = flow {
        service.downloadFolder(folderId)
            ?.let { emit(it) }
            ?: throw ApiError.ServerError(404, "Resource not found")
    }
        .catch { throw handleError(it) }
        .map { extractFiles(it, folderId) }

    override suspend fun downloadFirmware(version: String) = flow {
        service.downloadFirmware(version)
            ?.let { emit(it) }
            ?: throw ApiError.ServerError(404, "Resource not found")
    }
        .catch { throw handleError(it) }
        .map { extractFiles(it, version) }

    override suspend fun logout(): Flow<Unit> = flow {
        service.logout()
        accountStore.remove()
        emit(Unit)
    }

    override suspend fun fetchUserProfile(userId: String) =
        sendRequest { service.fetchUserProfile(userId) }

    override suspend fun getLatestFirmwareVersion(): Flow<FirmwareVersion> =
        sendRequest { service.getLatestFirmwareVersion() }

    override suspend fun fetchFolder(folderId: String) =
        sendRequest { service.fetchFolder(folderId) }

    private fun extractFiles(responseBody: ResponseBody, name: String): String {
        val folder = File(downloadFolder, name)
        if (folder.exists()) folder.deleteRecursively()
        folder.mkdirs()

        ZipInputStream(responseBody.byteStream()).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                val file = File(folder, entry.name)
                val canonicalPath = file.canonicalPath
                if (canonicalPath.startsWith(folder.canonicalPath)) {
                    FileOutputStream(File(folder, entry.name)).use { zipInputStream.copyTo(it) }
                    zipInputStream.closeEntry()
                } else {
                    throw SecurityException("Bad file path: $canonicalPath")
                }
                entry = zipInputStream.nextEntry
            }

        }
        return folder.absolutePath
    }

    private suspend fun <T> sendRequest(
        refreshToken: Boolean = true,
        request: suspend () -> T?
    ): Flow<T> {
        return flow {
            try {
                val response = request()
                response?.let { emit(it) } ?: throw ApiError.ServerError(404, "Resource not found")
            } catch (e: Exception) {
                Timber.e(e, "Send request failed")
                if (e.isAuthError() && refreshToken) {
                    if (refreshToken()) {
                        return@flow emitAll(sendRequest(false, request))
                    } else {
                        logoutManager.logout()
                    }
                }
                throw handleError(e)
            }
        }
    }

    private suspend fun refreshToken(): Boolean =
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

    private fun Exception.isAuthError() = (this as? HttpException)?.code() == 401
}
