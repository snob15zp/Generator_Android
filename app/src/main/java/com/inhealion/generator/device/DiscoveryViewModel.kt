package com.inhealion.generator.device

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.device.bluetooth.BleDeviceScanner
import com.inhealion.generator.device.model.BleDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class DiscoveryViewModel(
    private val scanner: BleDeviceScanner,
    private val deviceConnectionFactory: DeviceConnectionFactory
) : ViewModel() {

    val inProgress = MutableLiveData(false)
    val devices = MutableLiveData<List<BleDevice>>()
    val deviceInfo = MutableLiveData<String>()
    val errorMessage = MutableLiveData<String>()

    private var discoveryJob: Job? = null

    fun start() {
        inProgress.postValue(true)
        val list = mutableListOf<BleDevice>()
        discoveryJob = scanner.scan()
            .onEach {
                if (!list.contains(it)) list.add(it)
                devices.postValue(list)
            }.launchIn(viewModelScope)
    }

    fun sendData(address: String) {
        inProgress.value = false
        discoveryJob?.cancel()
        discoveryJob = null

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                with(deviceConnectionFactory.connect(address)) {
                    deviceInfo.postValue(serial.decodeToString())
                    //putFile("file_01.txt", ByteArrayInputStream(DUMMY_PROGRAM.encodeToByteArray()))
                }
            }.onFailure { errorMessage.postValue("Connection failed") }
        }
    }

    companion object {
        private val DUMMY_PROGRAM = """
             NAme, test_1
             FRequencies, 1
             OFfset,  3
             ONset,  1
             DUration, 10
             NEgative, 0
             UP, 1
             inverse, 0
             OutVoltage, 10
             10
        """.trimIndent()
    }
}
