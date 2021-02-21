package com.inhealion.generator.networking.api.model

import com.squareup.moshi.Json
import java.util.*

data class UserProfile(
    val id: String,

    val name: String? = null,

    val email: String? = null,

    val surname: String? = null,

    @Json(name = "date_of_birth")
    val birthday: String? = null,

    val address: String? = null,

    @Json(name = "phone_number")
    val phone: String? = null
)
