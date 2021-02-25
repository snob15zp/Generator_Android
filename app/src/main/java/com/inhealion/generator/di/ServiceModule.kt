package com.inhealion.generator.di

import com.inhealion.generator.BuildConfig.BASE_URL
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.bluetooth.BleDeviceScanner
import com.inhealion.generator.device.bluetooth.BluetoothScannerImpl
import com.inhealion.generator.device.internal.DeviceConnectionFactoryImpl
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.account.SharedPrefAccountStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val serviceModule = module {
    single<AccountStore> { SharedPrefAccountStore(androidContext()) }
    single {
        GeneratorApiCoroutinesClient.initialize(BASE_URL, androidContext(), get())
        GeneratorApiCoroutinesClient.instance()
    }
    factory<BleDeviceScanner> { BluetoothScannerImpl() }
    single<DeviceConnectionFactory> { DeviceConnectionFactoryImpl() }
}
