package com.inhealion.generator.service

import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.api.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

class AuthorizationManager(
    private val apiClient: GeneratorApiCoroutinesClient,
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository
) {

    suspend fun signIn(login: String, password: String): Flow<User> =
        apiClient.signIn(login, password).onEach { userRepository.save(it) }

    suspend fun logout() {
        apiClient.logout().collect {
            deviceRepository.remove()
            userRepository.remove()
        }
    }
}
