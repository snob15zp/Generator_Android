package com.inhealion.generator.data.repository

import com.inhealion.generator.model.Result
import com.inhealion.generator.model.tryWithResult
import io.paperdb.Book
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class SingleEntityRepository<T>(private val name: String) : CrudRepository<T> {
    private val book: Book get() = Paper.book(name)

    override suspend fun get(): Result<T?> = withContext(Dispatchers.IO) {
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

    override suspend fun remove(): Result<Unit> = withContext(Dispatchers.IO) {
        tryWithResult {
            book.delete(KEY)
        }
    }

    companion object {
        private const val KEY = "key"
    }
}
