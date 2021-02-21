package com.inhealion.generator.model

sealed class Result<out T> {

    class Success<out T>(val value: T) : Result<T>()
    data class Failure<T>(val exception: Throwable) : Result<T>()

    fun exceptionOrNull(): Throwable? =
        when (this) {
            is Failure -> exception
            else -> null
        }

    fun valueOrNull(): T? = if (this is Success<T>) this.value else null

    companion object {
        inline fun <T> success(value: T): Result<T> =
            Success(value)

        inline fun <T> failure(exception: Throwable): Failure<T> =
            Failure(exception)
    }

}

inline fun <T, R> Result<T>.mapSuccess(action: (T) -> Result<R>): Result<R> =
    when (this){
        is Result.Success<T> -> action.invoke(this.value)
        is Result.Failure<T> -> Result.failure(this.exception)
    }


inline fun <T> Result<T>.onFailure(action: (exception: Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let { action(it) }
    return this
}

inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (this is Result.Success<T>) action(value)
    return this
}


inline fun <T> tryWithResult(action: () -> T): Result<T> {
    return try {
        Result.success(action())
    } catch (ex: Exception) {
        Result.failure(ex)
    }
}
