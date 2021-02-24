package com.inhealion.generator.device.internal

import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.Generator

class DeviceConnectionFactoryImpl: DeviceConnectionFactory {
    override suspend fun connect(address: String): Generator = GenG070V1(address)
}
