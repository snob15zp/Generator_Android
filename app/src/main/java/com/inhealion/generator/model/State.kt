package com.inhealion.generator.model

import java.lang.Exception

sealed class State {
    data class Success<T>(val data: T) : State()
    data class Failure(val error: String, val exception: Throwable? = null) : State()
    object InProgress: State()
    object Idle : State()

    companion object {
        fun success() = State.Success(Any())
    }
}


