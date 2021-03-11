package com.inhealion.generator.device

interface DeviceConnectionFactory  {
    suspend fun connect(address: String): Generator
}
