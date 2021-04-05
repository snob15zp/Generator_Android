package com.inhealion.generator

import android.app.Application
import com.inhealion.generator.data.di.repositoryModule
import com.inhealion.generator.di.*
import com.inhealion.generator.extension.initFirebase
import com.inhealion.generator.extension.initLogger
import com.inhealion.generator.extension.initRepository
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
                eventModule,
                appModule,
                serviceModule,
                viewModelModule,
                repositoryModule,
                utilsModule
            )
        }

        initFirebase()
        initLogger()
        initRepository()
    }
}
