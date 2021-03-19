package com.inhealion.generator.di

import com.inhealion.generator.events.ImportStateEventDelegate
import com.inhealion.generator.events.ImportStateEventDelegateImpl
import com.inhealion.generator.utils.StringProvider
import com.inhealion.generator.utils.StringProviderImpl
import com.inhealion.generator.utils.ApiErrorStringProvider
import com.inhealion.generator.utils.ApiErrorStringProviderImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilsModule = module {
    single<StringProvider> { StringProviderImpl(androidContext()) }
    single<ApiErrorStringProvider> { ApiErrorStringProviderImpl(get()) }
    single<ImportStateEventDelegate> { ImportStateEventDelegateImpl() }
}
