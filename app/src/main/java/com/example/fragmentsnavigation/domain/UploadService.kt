package com.example.fragmentsnavigation.domain

import com.example.fragmentsnavigation.data.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface UploadService {
    @POST("orders/uploadImage")
    @Multipart
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part("orderId") orderId: String,
        //@Part("userId") userId: String,
        @Part body: MultipartBody.Part
    )

    @GET("orders/all")
    suspend fun getAllOrders(@Header("Authorization") token: String): ListOfOrders

    @GET("orders/search/{name}")
    suspend fun search(@Header("Authorization") token: String, @Path("name") name: String): ListOfOrders

    @GET("orders/id/{id}")
    suspend fun getOrderById(@Header("Authorization") token: String, @Path("id") id: Int): OrderInfo

    @GET("orders/name/{name}")
    suspend fun getOrderByName(@Header("Authorization") token: String, @Path("name") name: String): OrderInfo

    @POST("orders/image/delete/{name}")
    suspend fun deleteImage(@Path("name") name: String): Unit

    @POST("orders/new")
    suspend fun newOrder(@Header("Authorization") token: String, @Body order: OrderCreateRequest): OrderInfo

    @POST("user/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse
}