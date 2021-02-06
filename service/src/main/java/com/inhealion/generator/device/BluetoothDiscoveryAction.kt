package com.inhealion.generator.device

import android.bluetooth.BluetoothDevice

sealed class BluetoothDiscoveryAction {
    object Started : BluetoothDiscoveryAction()
    object Finished : BluetoothDiscoveryAction()
    data class Found(val device: BluetoothDevice) : BluetoothDiscoveryAction()
}
