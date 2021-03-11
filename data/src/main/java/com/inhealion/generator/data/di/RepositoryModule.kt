package com.inhealion.generator.data.di

import com.inhealion.generator.data.repository.*
import com.inhealion.generator.data.repository.DeviceRepositoryImpl
import com.inhealion.generator.data.repository.UserRepositoryImpl
import org.koin.dsl.module


val repositoryModule = module {
    single<UserRepository> { UserRepositoryImpl() }
    single<DeviceRepository> { DeviceRepositoryImpl() }
    single<VersionInfoRepository> { VersionInfoRepositoryImpl() }
}
