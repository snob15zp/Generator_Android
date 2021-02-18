package com.inhealion.generator.networking.api

import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.Program
import com.inhealion.generator.networking.api.model.User
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

interface GeneratorService {
    @FormUrlEncoded
    @POST("api/users/login")
    suspend fun login(@Field("login") login: String, @Field("password") password: String): User?

    @GET("/api/profiles/{id}/folders")
    suspend fun fetchFolders(@Path("id") userProfileId: String): List<Folder>?

    @GET("/api/folders/{id}/programs")
    suspend fun fetchPrograms(@Path("id") folderId: String): List<Program>?

    @Streaming
    @GET("/api/folders/{id}/download")
    suspend fun downloadFolder(@Path("id") folderId: String): ResponseBody?

    @POST("/api/users/logout")
    suspend fun logout()
}
