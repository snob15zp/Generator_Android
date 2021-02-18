package com.inhealion.generator.networking.api.model

import com.squareup.moshi.Json
import java.util.Date
import java.util.concurrent.TimeUnit

data class Folder(
    val id: String,
    val name: String,

    @Json(name = "expires_in")
    val expiresIn: Long,

    @Json(name = "created_at")
    val createdAt: Date
) {

    val expiredAt: Date
        get() = Date(createdAt.time + TimeUnit.SECONDS.toMillis(expiresIn))

    val isExpired: Boolean
        get() = System.currentTimeMillis() > createdAt.time + TimeUnit.SECONDS.toMillis(expiresIn)
}
