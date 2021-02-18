package com.inhealion.generator.manager

import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.model.Result
import com.inhealion.generator.model.onFailure
import com.inhealion.generator.model.onSuccess
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.api.model.User
import kotlinx.coroutines.flow.collect

class AuthorizationManager(
    private val apiClient: GeneratorApiCoroutinesClient,
    private val userRepository: UserRepository
) {

    suspend fun signIn(login: String, password: String): Result<User> =
        apiClient.signIn(login, password)
            .onSuccess {
                userRepository.save(it)
            }

}
