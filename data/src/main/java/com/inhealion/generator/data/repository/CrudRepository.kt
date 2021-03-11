package com.inhealion.generator.data.repository

import com.inhealion.generator.model.Result

interface CrudRepository<T> {

    suspend fun get(): Result<T?>

    suspend fun save(entity: T): Result<Unit>

    suspend fun remove(): Result<Unit>
}
