package com.inhealion.generator.networking.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FirmwareVersion(
    @Json(name = "id")
    val id: String,

    @Json(name = "version")
    val version: String
)
