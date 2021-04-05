package com.inhealion.generator.di

import com.inhealion.generator.service.AuthorizationManager
import com.inhealion.generator.service.SharedPrefManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { AuthorizationManager(get(), get(), get(),get()) }
    single { SharedPrefManager(androidContext()) }
}
