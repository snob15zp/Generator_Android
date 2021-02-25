package com.inhealion.generator.device.bluetooth

import com.inhealion.generator.device.model.BleDevice
import com.juul.kable.Scanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber


class BluetoothScannerImpl : BleDeviceScanner {

    override suspend fun scan(): Flow<BleDevice> =
        Scanner()
            .advertisements
            .map {
                Timber.d("Device found $it")
                BleDevice(it.name, it.address)
            }
}
