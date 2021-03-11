package com.inhealion.generator.event

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

open class FlowEventDelegate<T> : EventDelegate<T> {
    var channel: ConflatedBroadcastChannel<T>

    constructor() {
        channel = createChannel()
    }

    constructor(initialValue: T) : this() {
        channel = createChannel(initialValue)
    }

    override fun observe() = channel.asFlow()

    override suspend fun post(value: T) {
        channel.send(value)
    }

    override fun offer(value: T) {
        channel.offer(value)
    }

    fun clear() {
        reinitialize()
    }

    private fun reinitialize() {
        channel.close()
        channel = createChannel()
    }

    private fun createChannel(initialValue: T? = null): ConflatedBroadcastChannel<T> {
        return if (initialValue == null) {
            ConflatedBroadcastChannel<T>()
        } else {
            ConflatedBroadcastChannel<T>(initialValue)
        }
    }
}

interface EventDelegate<T> {

    suspend fun post(value: T)

    fun offer(value: T)

    fun observe(): Flow<T>
}
