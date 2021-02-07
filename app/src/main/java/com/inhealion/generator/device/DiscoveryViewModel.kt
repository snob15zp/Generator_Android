package com.inhealion.generator.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothSocket
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.IOException
import java.io.OutputStream
import java.util.*


class DiscoveryViewModel(
    private val scanner: Scanner
) : ViewModel() {

    val inProgress = MutableLiveData(false)
    val devices = MutableLiveData<List<BluetoothDevice>>()

    fun start() {
        val list = mutableListOf<BluetoothDevice>()
        viewModelScope.launch {
            inProgress.postValue(true)
            scanner.scan().collect {
                when (it) {
                    is BluetoothDiscoveryAction.Found -> {
                        if (!list.contains(it.device)) {
                            devices.postValue(list.apply { add(it.device) })
                        }
                    }
                    else -> Unit
                }
            }
            inProgress.postValue(false)
        }
    }

    fun sendData(address: String) {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = bluetoothAdapter.getRemoteDevice(address)
        if(device.bondState != BOND_BONDED) {
            return
        }

        var socket: BluetoothSocket? = null
        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(SERIAL_UUID)
            socket.connect()
            socket.outputStream.write(byteArrayOf(1, 2, 3))
        } catch (e: Exception) {
            Timber.e(e, "Unable to send data")
        }

    }

    companion object {
        private val SERIAL_UUID = UUID.fromString("00001801-0000-1000-8000-00805F9B34FB")
        //private val SERIAL_UUID = UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455")
    }
}
