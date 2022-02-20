package com.inhealion.generator.presentation.programs.viewmodel

import androidx.lifecycle.viewModelScope
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.LogoutManager
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.UserProfile
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.utils.ApiErrorStringProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FolderViewModel(
    private val userRepository: UserRepository,
    private val generatorApiCoroutinesClient: GeneratorApiCoroutinesClient,
    private val apiErrorStringProvider: ApiErrorStringProvider,
    private val logoutManager: LogoutManager
) : BaseViewModel<FolderViewModel.UserData>() {

    private var folders: List<Folder>? = null

    private val cache = mutableMapOf<String, CacheItem>()

    fun load() {
        viewModelScope.launch {
            postState(State.InProgress())

            val userId = userRepository.get().valueOrNull()?.id ?: run {
                //postState(State.Failure(stringResource(R.string.error_unknown)))
                logoutManager.logout()
                return@launch
            }
            fetch(KEY_USER_PROFILE) { generatorApiCoroutinesClient.fetchUserProfile(userId) }
                .flatMapConcat { userProfile ->
                    fetch(KEY_FOLDERS) { generatorApiCoroutinesClient.fetchFolders(userId) }
                        .map { UserData(userProfile, it) }
                }
                .catch { postState(State.Failure(apiErrorStringProvider.getErrorMessage(it), it)) }
                .collect {
                    folders = it.folders
                    postState(State.Success(it))
                }
        }
    }

    fun refresh() {
        cache.remove(KEY_FOLDERS)
        load()
    }

    private inline fun <reified T : Any> fetch(key: String, action: () -> Flow<T>): Flow<T> =
        cache[key]
            ?.let { if (System.currentTimeMillis() - it.time < MAX_CACHE_TIME) flowOf(it.obj as T) else null }
            ?: action().onEach { cache[key] = CacheItem(System.currentTimeMillis(), it) }

    fun getFolder(id: String) = folders?.firstOrNull { it.id == id }

    data class CacheItem(val time: Long, val obj: Any)
    data class UserData(val userProfile: UserProfile, val folders: List<Folder>)

    companion object {
        private val MAX_CACHE_TIME = TimeUnit.MINUTES.toMillis(30L)
        private const val KEY_USER_PROFILE = "KEY_USER_PROFILE"
        private const val KEY_FOLDERS = "KEY_FOLDERS"
    }
}

