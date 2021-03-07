package com.inhealion.generator.presentation.device

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
sealed class ImportAction : Parcelable {

    @Parcelize
    data class  UpdateFirmware(val version: String) : ImportAction()

    @Parcelize
    data class ImportFolder(val folderId: String) : ImportAction()
}
