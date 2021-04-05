package com.inhealion.generator.device.bluetooth

import com.inhealion.generator.device.model.BleDevice
import com.juul.kable.Scanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.*


class BluetoothScannerImpl : BleDeviceScanner {

    override suspend fun scan(): Flow<BleDevice> =
        Scanner()
            .advertisements
            .filter { it.name?.toLowerCase(Locale.ROOT)?.startsWith(DEVICE_NAME_PREFIX) == true }
            .map {
                Timber.d("Device found $it")
                BleDevice(it.name, it.address)
            }

    companion object {
        private const val DEVICE_NAME_PREFIX = "inhealion"
    }
}
