package com.inhealion.generator.extension

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.inhealion.generator.BuildConfig.BASE_URL
import com.inhealion.generator.data.RepositoryInitializer
import com.inhealion.generator.networking.GeneratorApiClient
import com.inhealion.generator.networking.account.AccountStore
import org.koin.android.ext.android.inject
import timber.log.Timber


fun Application.initLogger() {
    Timber.plant(Timber.DebugTree())
}

fun Application.initGeneratorApiClient() {
    val accountStore: AccountStore by inject()
    GeneratorApiClient.initialize(this, BASE_URL, accountStore)
}

fun Application.initRepository() {
    RepositoryInitializer.init(this)
}

fun Application.initFirebase() {
    FirebaseApp.initializeApp(this)
}
