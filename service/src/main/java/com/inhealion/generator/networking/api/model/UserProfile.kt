package com.inhealion.generator.networking.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class UserProfile(
    @Json(name = "id")
    val id: String,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "email")
    val email: String? = null,

    @Json(name = "surname")
    val surname: String? = null,

    @Json(name = "date_of_birth")
    val birthday: String? = null,

    @Json(name = "address")
    val address: String? = null,

    @Json(name = "phone_number")
    val phone: String? = null
)
