package com.inhealion.generator.di

import com.inhealion.generator.BuildConfig.BASE_URL
import com.inhealion.generator.device.BluetoothScannerImpl
import com.inhealion.generator.device.BleDeviceScanner
import com.inhealion.networking.GeneratorApiClient
import com.inhealion.networking.account.AccountStore
import com.inhealion.networking.account.SharedPrefAccountStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val serviceModule = module {
    single<AccountStore> { SharedPrefAccountStore(androidContext()) }
    single {
        GeneratorApiClient.initialize(BASE_URL, get())
        GeneratorApiClient.instance()
    }
    factory<BleDeviceScanner> { BluetoothScannerImpl() }
}
