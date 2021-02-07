package com.inhealion.networking.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String? = null,
    val token: String,
    val profile: UserProfile? = null
): Parcelable
