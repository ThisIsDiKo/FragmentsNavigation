package com.example.fragmentsnavigation.domain

sealed class RequestResult<T>(val data: T? = null){
    class Authorized<T>(data: T? = null): RequestResult<T>(data)
    class Unauthorized<T>: RequestResult<T>()
    class UnknownError<T>: RequestResult<T>()
}
