package com.inhealion.generator.networking.api

import com.inhealion.generator.networking.api.model.*
import okhttp3.ResponseBody
import retrofit2.http.*

interface GeneratorService {
    @FormUrlEncoded
    @POST("api/users/login")
    suspend fun login(@Field("login") login: String, @Field("password") password: String): User?

    @GET("api/users/{id}/folders")
    suspend fun fetchFolders(@Path("id") userId: String): List<Folder>?

    @GET("api/folders/{id}/programs")
    suspend fun fetchPrograms(@Path("id") folderId: String): List<Program>?

    @Streaming
    @GET("api/folders/{id}/download")
    suspend fun downloadFolder(@Path("id") folderId: String): ResponseBody?

    @Streaming
    @GET("api/firmware/{version}/download")
    suspend fun downloadFirmware(@Path("version") version: String): ResponseBody?

    @POST("api/users/logout")
    suspend fun logout()

    @GET("api/users/{id}/profile")
    suspend fun fetchUserProfile(@Path("id") userId: String): UserProfile?

    @PUT("api/users/refresh")
    suspend fun refreshToken(): RefreshToken?

    @GET("api/firmware/latest")
    suspend fun getLatestFirmwareVersion(): FirmwareVersion

    @GET("api/folders/{id}")
    suspend fun fetchFolder(@Path("id") id: String): Folder
}
