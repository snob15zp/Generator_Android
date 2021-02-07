package com.inhealion.generator.di

import com.inhealion.generator.device.BluetoothScannerImpl
import com.inhealion.generator.device.Scanner
import com.inhealion.networking.account.AccountStore
import com.inhealion.networking.account.SharedPrefAccountStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val serviceModule = module {
    single<AccountStore> { SharedPrefAccountStore(androidContext()) }
    factory<Scanner> { BluetoothScannerImpl(androidContext()) }
}
