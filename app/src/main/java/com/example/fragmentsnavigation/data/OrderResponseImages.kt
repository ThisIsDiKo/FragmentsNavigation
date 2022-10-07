package com.example.fragmentsnavigation.data

data class OrderResponseImages(
    val id: Int,
    val userName: String,
    val orderName : String,
    val createdAt: String,
    val images: List<String>
)
