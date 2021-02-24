package com.inhealion.generator.device.bluetooth

import com.inhealion.generator.device.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface BleDeviceScanner {
    suspend fun scan(): Flow<BleDevice>
}
