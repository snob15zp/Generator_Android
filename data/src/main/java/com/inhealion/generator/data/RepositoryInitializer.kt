package com.inhealion.generator.data

import android.content.Context
import io.paperdb.Paper

object RepositoryInitializer {

    fun init(context: Context) {
        Paper.init(context)
    }
}
