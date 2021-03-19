package com.inhealion.generator.presentation.device

import android.os.Parcelable
import androidx.annotation.Keep
import com.inhealion.generator.device.model.BleDevice
import kotlinx.parcelize.Parcelize

@Keep
sealed class ImportAction : Parcelable {
    abstract val device: BleDevice

    @Parcelize
    data class UpdateFirmware(val version: String, override val device: BleDevice) : ImportAction()

    @Parcelize
    data class ImportFolder(val folderId: String, override val device: BleDevice) : ImportAction()
}
