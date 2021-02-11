package com.inhealion.generator.device.bluetooth

import android.bluetooth.BluetoothAdapter
import com.inhealion.generator.device.model.BleDevice
import com.juul.kable.Scanner
import com.juul.kable.peripheral
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber


class BluetoothScannerImpl : BleDeviceScanner {

    override fun scan(): Flow<BleDevice> =
        Scanner()
            .advertisements
            .map {
                Timber.d("Device found $it")
                BleDevice(it.name, it.address)
            }

    override suspend fun connect(address: String) = withContext(Dispatchers.IO) {

        val bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)
        val peripheral = peripheral(bluetoothDevice)

        peripheral.connect()
    }
}
