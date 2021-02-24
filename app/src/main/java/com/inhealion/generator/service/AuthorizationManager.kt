package com.inhealion.generator.service

import android.app.Activity
import android.content.Intent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.api.model.User
import com.inhealion.generator.presentation.activity.MainActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class AuthorizationManager(
    private val apiClient: GeneratorApiCoroutinesClient,
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository,
    private val sharedPrefManager: SharedPrefManager
) {

    suspend fun signIn(login: String, password: String): Flow<User> =
        apiClient.signIn(login, password).onEach { userRepository.save(it) }

    suspend fun logout() {
        apiClient.logout()
            .catch { Timber.w("Logout raise error ${it.message}") }
            .collect()

        deviceRepository.remove()
        userRepository.remove()
        sharedPrefManager.clear()
    }
}
