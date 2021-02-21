package com.inhealion.generator.service

import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.model.Result
import com.inhealion.generator.model.onSuccess
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.api.model.User

class AuthorizationManager(
    private val apiClient: GeneratorApiCoroutinesClient,
    private val userRepository: UserRepository
) {

    suspend fun signIn(login: String, password: String): Result<User> =
        apiClient.signIn(login, password)
            .onSuccess {
                userRepository.save(it)
            }

    suspend fun logout() {
        userRepository.remove()
        apiClient.logout()
    }

}
