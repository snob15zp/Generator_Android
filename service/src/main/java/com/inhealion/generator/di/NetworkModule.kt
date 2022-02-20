package com.inhealion.generator.di

import com.inhealion.generator.networking.GeneratorApiInterceptor
import okhttp3.Interceptor
import org.koin.dsl.bind
import org.koin.dsl.module

val networkModule = module {
    single { GeneratorApiInterceptor(get()) } bind Interceptor::class
}
