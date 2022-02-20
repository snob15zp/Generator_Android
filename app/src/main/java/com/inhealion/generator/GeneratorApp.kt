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
import org.koin.core.logger.Level

class GeneratorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@GeneratorApp)
            modules(
                networkModule,
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
