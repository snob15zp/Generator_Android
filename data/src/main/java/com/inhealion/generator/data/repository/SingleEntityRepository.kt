package com.inhealion.generator.data.repository

import com.inhealion.generator.model.Result
import com.inhealion.generator.model.tryWithResult
import io.paperdb.Book
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class SingleEntityRepository<T> : CrudRepository<T> {
    private val book: Book get() = Paper.book()

    override suspend fun get(): Result<T> = withContext(Dispatchers.IO) {
        tryWithResult {
            book.read(KEY)
        }
    }

    override suspend fun save(entity: T) = withContext(Dispatchers.IO) {
        tryWithResult {
            book.write(KEY, entity)
            Unit
        }
    }

    companion object {
        private const val KEY = "key"
    }
}
