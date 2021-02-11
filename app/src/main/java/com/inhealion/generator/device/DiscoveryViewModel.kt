package com.inhealion.generator.device

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.device.bluetooth.BleDeviceScanner
import com.inhealion.generator.device.model.BleDevice
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*


class DiscoveryViewModel(
    private val scanner: BleDeviceScanner
) : ViewModel() {

    val inProgress = MutableLiveData(false)
    val devices = MutableLiveData<List<BleDevice>>()

    fun start() {
        val list = mutableListOf<BleDevice>()
        viewModelScope.launch {
            inProgress.postValue(true)

            scanner.scan()
                .collect {
                    if (!list.contains(it)) list.add(it)
                    devices.postValue(list)
                }
            inProgress.postValue(false)
        }
    }

    fun sendData(address: String) {
        viewModelScope.launch {
            scanner.connect(address)
        }
    }

    companion object {
        //private val SERIAL_UUID = UUID.fromString("00001800-0000-1000-8000-00805F9B34FB")
        //private val SERIAL_UUID = UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455")
        private val SERIAL_UUID = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616")
    }
}
