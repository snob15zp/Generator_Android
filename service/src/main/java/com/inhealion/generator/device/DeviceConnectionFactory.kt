package com.inhealion.generator.device

interface DeviceConnectionFactory  {
    fun connect(address: String): Generator
}
