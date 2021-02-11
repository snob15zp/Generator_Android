package com.inhealion.generator

import android.app.Application
import com.inhealion.generator.di.appModule
import com.inhealion.generator.di.serviceModule
import com.inhealion.generator.di.viewModelModule
import com.inhealion.generator.extension.initGeneratorApiClient
import com.inhealion.generator.extension.initLogger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class GeneratorApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@GeneratorApp)
            modules(
                appModule,
                serviceModule,
                viewModelModule
            )
        }

        initLogger()
    }
}
