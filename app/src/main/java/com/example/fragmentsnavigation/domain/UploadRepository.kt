package com.example.fragmentsnavigation.domain

import android.graphics.Bitmap
import com.example.fragmentsnavigation.data.OrderInfo
import com.example.fragmentsnavigation.data.OrderResponse
import com.example.fragmentsnavigation.data.OrderResponseImages

interface UploadRepository {
    suspend fun uploadImage(orderId: String, userId: String, image: Bitmap): Unit

    suspend fun getAllOrders(): RequestResult<List<OrderInfo>>

    suspend fun search(query: String): RequestResult<List<OrderInfo>>

    suspend fun getOrderById(id: Int): OrderInfo?

    suspend fun getOrderByName(name: String): OrderInfo?

    suspend fun newOrder(name: String): OrderInfo?

    suspend fun deleteImage(name: String): Unit

    suspend fun login(username: String, password: String): RequestResult<String>
}