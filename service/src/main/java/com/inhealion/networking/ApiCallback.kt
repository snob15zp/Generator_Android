package com.inhealion.networking

import java.lang.Exception

interface ApiCallback<T> {
    fun success(value: T)

    fun failure(error: Exception)
}
