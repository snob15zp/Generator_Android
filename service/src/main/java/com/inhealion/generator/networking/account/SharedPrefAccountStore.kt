package com.inhealion.generator.networking.account

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.inhealion.generator.networking.api.model.User

class SharedPrefAccountStore(context: Context) : AccountStore {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    override fun store(user: User) = sharedPreferences.edit()
        .putString(PREFS_USER_TOKEN_KEY, user.token)
        .apply()

    override fun load(): User? = sharedPreferences.getString(PREFS_USER_TOKEN_KEY, null)?.let { User(token = it) }

    override fun remove() = sharedPreferences.edit().remove(PREFS_USER_TOKEN_KEY).apply()

    companion object {
        private const val PREFS_NAME = "com.inhealion.generator.networking.account.SharedPrefAccountStore"
        private const val PREFS_USER_TOKEN_KEY = "PREFS_USER_TOKEN_KEY"
    }
}
