package com.example.fragmentsnavigation

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.example.fragmentsnavigation.domain.UploadRepository
import com.example.fragmentsnavigation.domain.UploadRepositoryImpl
import com.example.fragmentsnavigation.domain.UploadService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val ORDER_ID_KEY = "ORDER_ID_KEY"
const val USER_ID_KEY = "USER_ID_KEY"
const val IMAGES_URI_ARRAY_KEY = "IMAGES_URI_ARRAY_KEY"

class App: Application() {

    lateinit var uploadApi: UploadService
    lateinit var uploadRepository: UploadRepository
    lateinit var prefs: SharedPreferences

    override fun onCreate() {
        configureSharedPrefs()
        configureUploadApi()
        configureUploadRepository()

        super.onCreate()
    }

    private fun configureUploadApi(){
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor{ chain ->
                val request = chain.request().newBuilder()
                    //.addHeader("Authorization",  "Bearer ${UUID.randomUUID()}")
                    .addHeader("Content-type",  "application/json")
                    .build()
                return@addInterceptor chain.proceed(request)
            }
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://172.16.1.54:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        uploadApi = retrofit.create(UploadService::class.java)
    }

    private fun configureUploadRepository(){
        uploadRepository = UploadRepositoryImpl(uploadApi, prefs)
    }

    private fun configureSharedPrefs(){
        prefs = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE)
    }

}