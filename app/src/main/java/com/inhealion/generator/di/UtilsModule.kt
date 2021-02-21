package com.inhealion.generator.di

import com.inhealion.generator.utils.StringProvider
import com.inhealion.generator.utils.StringProviderImpl
import com.inhealion.generator.utils.ApiErrorHandler
import com.inhealion.generator.utils.ApiErrorHandlerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilsModule = module {
    single<StringProvider> { StringProviderImpl(androidContext()) }
    single<ApiErrorHandler> { ApiErrorHandlerImpl(get()) }
}
