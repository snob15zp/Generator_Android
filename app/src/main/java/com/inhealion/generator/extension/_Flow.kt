package com.inhealion.generator.extension

import com.inhealion.generator.model.Result
import com.inhealion.generator.model.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

inline fun <T, R> Flow<Result<T>>.mapSuccessResult(crossinline action: suspend (value: T) -> R):
        Flow<Result<R>> {
    return this.map { result -> result.map { action(it) } }
}

inline fun <T, R> Flow<Result<T>>.flatMapSuccessResult(crossinline action: suspend (value: T) -> Result<R>):
        Flow<Result<R>> {
    return map {
        when (it) {
            is Result.Success -> action(it.value)
            is Result.Failure -> Result.failure(it.exception)
        }
    }
}
