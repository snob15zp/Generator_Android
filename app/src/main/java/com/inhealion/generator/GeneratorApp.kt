package com.inhealion.generator

import android.app.Application
import com.inhealion.generator.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GeneratorApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@GeneratorApp)
            modules(
                appModule
            )
        }
    }
}
