package com.inhealion.generator.model

sealed class State<out T : Any> {
    data class Success<out T : Any>(val data: T) : State<T>()
    data class Failure(val error: String, val exception: Throwable? = null) : State<Nothing>()
    data class InProgress(val progress: Int = -1) : State<Nothing>()
    object Idle : State<Nothing>()

    val isFinished: Boolean get() = (this is Success) or (this is Failure)

    companion object {
        fun success(data: Any = Any()) = Success(data)
    }
}

