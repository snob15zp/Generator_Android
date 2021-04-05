package com.inhealion.generator.device.internal

import android.content.Context
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.device.Generator

class DeviceConnectionFactoryImpl(private val context: Context): DeviceConnectionFactory {
    override fun connect(address: String): Generator = GenG070V1(address, context)
}
