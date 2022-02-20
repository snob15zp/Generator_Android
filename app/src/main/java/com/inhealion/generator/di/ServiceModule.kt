package com.inhealion.generator.di

import com.inhealion.generator.BuildConfig.BASE_URL
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.bluetooth.BleDeviceScanner
import com.inhealion.generator.device.bluetooth.BluetoothScannerImpl
import com.inhealion.generator.device.internal.DeviceConnectionFactoryImpl
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.networking.GeneratorApiInterceptor
import com.inhealion.generator.networking.LogoutManager
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.networking.account.SharedPrefAccountStore
import com.inhealion.generator.service.ImportManager
import com.inhealion.generator.service.ImportNotificationManager
import com.inhealion.generator.service.LogoutManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module


val serviceModule = module {
    single<LogoutManager> { LogoutManagerImpl(androidContext(), get(), get(), get(), get()) }
    single<AccountStore> { SharedPrefAccountStore(androidContext()) }

    single {
        GeneratorApiCoroutinesClient.initialize(BASE_URL, androidContext(), get(), get(), getAll())
        GeneratorApiCoroutinesClient.instance()
    }
    factory<BleDeviceScanner> { BluetoothScannerImpl() }
    single<DeviceConnectionFactory> { DeviceConnectionFactoryImpl(androidContext()) }
    factory { ImportManager(get(), get(), get(), get()) }
    single { ImportNotificationManager(androidContext()) }
}
