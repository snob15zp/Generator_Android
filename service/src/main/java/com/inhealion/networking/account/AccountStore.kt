package com.inhealion.networking.account

import com.inhealion.networking.api.model.User

interface AccountStore {

    fun store(user: User)

    fun load(): User?

    fun remove()
}
