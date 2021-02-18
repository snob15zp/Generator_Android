package com.inhealion.generator.di

import com.inhealion.generator.device.DiscoveryViewModel
import com.inhealion.generator.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { DiscoveryViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }
}
