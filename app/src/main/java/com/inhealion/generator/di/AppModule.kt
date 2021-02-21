package com.inhealion.generator.di

import com.inhealion.generator.service.AuthorizationManager
import org.koin.dsl.module

val appModule = module {
    single { AuthorizationManager(get(), get()) }
}
