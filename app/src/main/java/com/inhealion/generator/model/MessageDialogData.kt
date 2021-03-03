package com.inhealion.generator.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MessageDialogData(
    val title: String,
    val message: String
): Parcelable
