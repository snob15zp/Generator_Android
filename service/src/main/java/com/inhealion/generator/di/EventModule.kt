package com.inhealion.generator.di

import com.inhealion.generator.event.UnauthorizedUserEvent
import com.inhealion.generator.event.UnauthorizedUserEventImpl
import org.koin.dsl.module

val eventModule = module {
    single<UnauthorizedUserEvent> { UnauthorizedUserEventImpl() }
}
