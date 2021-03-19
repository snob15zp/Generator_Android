package com.inhealion.generator.device.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class BleDevice(
    val name: String?,
    val address: String
) : Parcelable
