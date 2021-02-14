package com.inhealion.generator.di

import com.inhealion.generator.device.DiscoveryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { DiscoveryViewModel(get(), get()) }
}
