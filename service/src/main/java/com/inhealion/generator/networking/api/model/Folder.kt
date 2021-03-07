package com.inhealion.generator.networking.api.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.concurrent.TimeUnit

@Parcelize
@JsonClass(generateAdapter = true)
data class Folder(
    @Json(name = "id")
    val id: String,

    @Json(name = "name")
    val name: String,

    @Json(name = "expires_in")
    val expiresIn: Long,

    @Json(name = "created_at")
    val createdAt: Date
) : Parcelable {

    val expiredAt: Date
        get() = Date(createdAt.time + TimeUnit.SECONDS.toMillis(expiresIn))

    val isExpired: Boolean
        get() = System.currentTimeMillis() > createdAt.time + TimeUnit.SECONDS.toMillis(expiresIn)
}
