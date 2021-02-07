package com.inhealion.networking.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Program(
    val id: String,
    val name: String
): Parcelable
