package com.inhealion.generator.device.bluetooth

import com.inhealion.generator.device.model.BleDevice
import kotlinx.coroutines.flow.Flow

interface BleDeviceScanner {
    fun scan(): Flow<BleDevice>
}
