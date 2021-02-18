package com.inhealion.generator.data.repository

import com.inhealion.generator.networking.api.model.User


internal class UserRepositoryImpl : SingleEntityRepository<User>(), UserRepository
