package com.inhealion.generator.presentation.device

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ImportAction : Parcelable {

    @Parcelize
    object UpdateFirmware : ImportAction()

    @Parcelize
    data class ImportFolder(val folderId: String) : ImportAction()
}
