package com.inhealion.generator.networking.account

import com.inhealion.generator.networking.api.model.User

interface AccountStore {

    fun store(user: User)

    fun load(): User?

    fun remove()
}
