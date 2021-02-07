package com.inhealion.networking.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val id: String,
    val name: String? = null,
    val email: String? = null,
) : Parcelable
