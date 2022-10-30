package com.example.fragmentsnavigation.domain

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import com.example.fragmentsnavigation.data.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.util.*

class UploadRepositoryImpl(
    private val api: UploadService,
    private val prefs: SharedPreferences
): UploadRepository {

    private val TAG = this::class.java.simpleName

    override suspend fun uploadImage(orderId: String, userId: String, image: Bitmap) {
        try{
            val token = prefs.getString("token", "") ?: ""
            Log.e(TAG, "Load token from prefs: $token")
            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            val byteArray = stream.toByteArray()
            val body = MultipartBody.Part.createFormData(
                "photo[content]", UUID.randomUUID().toString() + ".jpeg",
                byteArray.toRequestBody("image/*".toMediaTypeOrNull(), 0, byteArray.size)
            )
            //api.uploadImage(token =  "Bearer $token", orderId, userId, body)
            Log.e(TAG, "Upload order id is $orderId")
            api.uploadImage(token =  "Bearer $token", orderId, body)
        }
        catch (e: Exception){
            Log.e(TAG, "Unexpected exception was caught ${e.toString()}")
        }
    }

    override suspend fun getAllOrders(): RequestResult<List<OrderInfo>> {
        return try{
            val token = prefs.getString("token", "") ?: ""
            Log.e(TAG, "Load token from prefs: $token")
            val list = api.getAllOrders("Bearer $token")
            RequestResult.Authorized(data = list.orders)
            //list.orders
        }
        catch(e: HttpException){
            if (e.code() == 401){
                Log.e(TAG, "Unauthorized access")
                RequestResult.Unauthorized()
            }
            else {
                Log.e(TAG, "Unknown error")
                RequestResult.UnknownError()
            }
            //emptyList()
        }
        catch (e: Exception){
            Log.e(TAG, "Unexpected exception was caught ${e.toString()}")
            //emptyList()
            RequestResult.UnknownError()
        }
    }

    override suspend fun search(query: String): RequestResult<List<OrderInfo>>{
        return try{
            val token = prefs.getString("token", "") ?: ""
            val list = api.search("Bearer $token", name = query)
            RequestResult.Authorized(data = list.orders)
            //list.orders
        }
        catch(e: HttpException){
            if (e.code() == 401){
                Log.e(TAG, "Unauthorized access")
                RequestResult.Unauthorized()
            }
            else {
                Log.e(TAG, "Unknown error")
                RequestResult.UnknownError()
            }
            //emptyList()
        }
        catch (e: Exception){
            Log.e(TAG, "Unexpected exception was caught ${e.toString()}")
            //emptyList()
            RequestResult.UnknownError()
        }
    }

    override suspend fun getOrderById(id: Int): OrderInfo? {
        return try {
            val token = prefs.getString("token", "") ?: ""
            Log.e(TAG, "Load token from prefs: $token")
            api.getOrderById(token = "Bearer $token", id = id)
        }
        catch (e: Exception){
            Log.e(TAG, "Unexpected exception was caught ${e.toString()}")
            null
        }
    }

    override suspend fun getOrderByName(name: String): OrderInfo? {
        return try {
            val token = prefs.getString("token", "") ?: ""
            api.getOrderByName(token = "Bearer $token", name = name)
        }
        catch (e: Exception){
            Log.e(TAG, "Unexpected exception was caught ${e.toString()}")
            null
        }
    }

    override suspend fun newOrder(name: String): OrderInfo? {
        return try {
            val token = prefs.getString("token", "") ?: ""
            val orderCreateRequest = OrderCreateRequest(id = -1, userId = -1, orderName = name)
            api.newOrder(token = "Bearer $token", order = orderCreateRequest)
        }
        catch (e: Exception){
            Log.e(TAG, "Unexpected exception was caught ${e.toString()}")
            null
        }
    }

    override suspend fun deleteImage(name: String) {
        api.deleteImage(name)
    }

    override suspend fun login(username: String, password: String): RequestResult<String> {
        Log.e(TAG, "Start login")
        return try {
            val res = api.login(
                LoginRequest(
                    username = username,
                    password = password
                )
            ).token
            RequestResult.Authorized(res)
        }
        catch(e: HttpException){
            if (e.code() == 401){
                RequestResult.Unauthorized()
            }
            else {
                Log.e(TAG, "Unknown error : ${e.message()}")
                RequestResult.UnknownError()
            }
        }
        catch(e: Exception){
            Log.e(TAG, "Unknown error : $e")
            RequestResult.UnknownError()
        }
    }
}