package com.inhealion.generator.di

import com.inhealion.generator.main.MainViewModel
import com.inhealion.generator.manager.AuthorizationManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel { MainViewModel() }

    single { AuthorizationManager(get(), get()) }
}
