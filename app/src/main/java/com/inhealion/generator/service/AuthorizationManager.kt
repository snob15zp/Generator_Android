package com.inhealion.generator.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.event.UnauthorizedUserEvent
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.LogoutManager
import com.inhealion.generator.networking.api.model.User
import com.inhealion.generator.presentation.activity.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class AuthorizationManager(
    unauthorizedUserEvent: UnauthorizedUserEvent,
    private val apiClient: GeneratorApiCoroutinesClient,
    private val userRepository: UserRepository,
    private val logoutManager: LogoutManager
) {

    init {
        GlobalScope.launch {
            unauthorizedUserEvent.observe().collect {
                logout()
            }
        }
    }

    suspend fun signIn(login: String, password: String): Flow<User> =
        apiClient.signIn(login, password).onEach {
            userRepository.save(it)
        }

    fun logout() {
        runBlocking(Dispatchers.IO) {
            apiClient.logout()
                .catch { Timber.w("Logout raise error ${it.message}") }
                .collect()
        }
        logoutManager.logout()
    }
}
