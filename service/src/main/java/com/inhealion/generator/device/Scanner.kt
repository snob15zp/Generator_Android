package com.inhealion.generator.device

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.Flow

interface Scanner {
    fun scan(): Flow<BluetoothDiscoveryAction>
}
