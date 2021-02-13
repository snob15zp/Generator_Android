package com.inhealion.generator.extension

import android.app.Application
import com.inhealion.generator.BuildConfig.BASE_URL
import com.inhealion.networking.GeneratorApiClient
import com.inhealion.networking.account.AccountStore
import org.koin.android.ext.android.inject
import timber.log.Timber


fun Application.initLogger() {
    Timber.plant(Timber.DebugTree())
}

fun Application.initGeneratorApiClient() {
    val accountStore: AccountStore by inject()
    GeneratorApiClient.initialize(BASE_URL, accountStore)
}