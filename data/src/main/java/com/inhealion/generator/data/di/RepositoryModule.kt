package com.inhealion.generator.data.di

import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.data.repository.UserRepositoryImpl
import org.koin.dsl.module


val repositoryModule = module {

    single<UserRepository> { UserRepositoryImpl() }

}
