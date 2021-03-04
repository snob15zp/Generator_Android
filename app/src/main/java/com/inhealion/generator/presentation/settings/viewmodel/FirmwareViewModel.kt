package com.inhealion.generator.presentation.settings.viewmodel

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.viewModelScope
import com.inhealion.generator.R
import com.inhealion.generator.data.model.VersionInfo
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.data.repository.VersionInfoRepository
import com.inhealion.generator.device.DeviceConnectionFactory
import com.inhealion.generator.lifecyle.ActionLiveData
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.GeneratorApiCoroutinesClient
import com.inhealion.generator.presentation.main.viewmodel.BaseViewModel
import com.inhealion.generator.utils.StringProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class FirmwareViewModel(
    private val api: GeneratorApiCoroutinesClient,
    private val connectionFactory: DeviceConnectionFactory,
    private val deviceRepository: DeviceRepository,
    private val versionInfoRepository: VersionInfoRepository,
    private val stringProvider: StringProvider
) : BaseViewModel<VersionInfo>() {

    val showDiscovery = ActionLiveData()

    fun load(forceReload: Boolean = false) = viewModelScope.launch {
        postState(State.Idle)

        val device = deviceRepository.get().valueOrNull()
        if (device == null) {
            showDiscovery.sendAction()
            return@launch
        }

        if (BluetoothAdapter.getDefaultAdapter()?.isEnabled != true) {
            postState(State.Failure(stringProvider.getString(R.string.error_bluetooth_disabled)))
            return@launch
        }

        if (!forceReload) {
            versionInfoRepository.get().valueOrNull()?.let {
                postState(State.Success(it))
                return@launch
            }
        }

        postState(State.InProgress())
        api.getLatestFirmwareVersion()
            .catch { postState(State.Failure(stringProvider.getString(R.string.error_get_latest_version))) }
            .map { firmwareVersion ->
                val deviceVersion: String? = try {
                    connectionFactory.connect(device.address).use { it.version }
                } catch (e: Exception) {
                    val errorMessage = stringProvider.getString(R.string.connection_error_message, device.name ?: "")
                    postState(State.Failure(errorMessage))
                    null
                }
                VersionInfo(firmwareVersion.version, deviceVersion, Date()).also { versionInfoRepository.save(it) }
            }
            .collect { postState(State.Success(it)) }
    }
}
