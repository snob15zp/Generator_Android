package com.inhealion.generator.device

import com.inhealion.generator.device.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface BleDeviceScanner {
    fun scan(): Flow<BleDevice>
    suspend fun connect(address: String)
}
