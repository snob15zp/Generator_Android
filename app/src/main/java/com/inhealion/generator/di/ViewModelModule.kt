package com.inhealion.generator.di

import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.presentation.device.viewmodel.DiscoveryViewModel
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.device.viewmodel.ImportViewModel
import com.inhealion.generator.presentation.login.LoginViewModel
import com.inhealion.generator.presentation.programs.viewmodel.FolderViewModel
import com.inhealion.generator.presentation.programs.viewmodel.ProgramsViewModel
import com.inhealion.generator.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { DiscoveryViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { FolderViewModel(get(), get(), get(), get()) }
    viewModel { (folder: Folder) -> ProgramsViewModel(folder, get(), get(), get(), get()) }
    viewModel { (importAction: ImportAction) -> ImportViewModel(importAction, get()) }
    viewModel { SettingsViewModel(get(), get()) }
}
