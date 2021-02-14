package com.inhealion.generator.di

import com.inhealion.generator.BuildConfig.BASE_URL
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.bluetooth.BleDeviceScanner
import com.inhealion.generator.device.bluetooth.BluetoothScannerImpl
import com.inhealion.generator.device.internal.DeviceConnectionFactoryImpl
import com.inhealion.networking.GeneratorApiCoroutinesClient
import com.inhealion.networking.account.AccountStore
import com.inhealion.networking.account.SharedPrefAccountStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val serviceModule = module {
    single<AccountStore> { SharedPrefAccountStore(androidContext()) }
    single {
        GeneratorApiCoroutinesClient.initialize(BASE_URL, get())
        GeneratorApiCoroutinesClient.instance()
    }
    factory<BleDeviceScanner> { BluetoothScannerImpl() }
    single<DeviceConnectionFactory> { DeviceConnectionFactoryImpl() }
}
