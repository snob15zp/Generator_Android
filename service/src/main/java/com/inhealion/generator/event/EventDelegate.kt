package com.inhealion.generator.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

open class FlowEventDelegate<T> : EventDelegate<T> {
    var channel = MutableSharedFlow<T>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun observe() = channel.asSharedFlow()

    override suspend fun post(value: T) {
        channel.emit(value)
    }

    override fun offer(value: T) {
        channel.tryEmit(value)
    }

    fun clear() {
        reinitialize()
    }

    private fun reinitialize() {
        channel.resetReplayCache()
    }
}

interface EventDelegate<T> {

    suspend fun post(value: T)

    fun offer(value: T)

    fun observe(): Flow<T>
}
