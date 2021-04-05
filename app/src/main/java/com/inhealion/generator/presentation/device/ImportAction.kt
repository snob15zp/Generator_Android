package com.inhealion.generator.presentation.device

import android.os.Parcelable
import androidx.annotation.Keep
import com.inhealion.generator.device.model.BleDevice
import kotlinx.parcelize.Parcelize

@Keep
sealed class ImportAction : Parcelable {
    abstract val address: String

    @Parcelize
    data class UpdateFirmware(val version: String, override val address: String) : ImportAction()

    @Parcelize
    data class ImportFolder(val folderId: String, override val address: String) : ImportAction()
}
