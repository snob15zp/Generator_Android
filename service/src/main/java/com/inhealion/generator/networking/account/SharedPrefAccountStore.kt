package com.inhealion.generator.networking.account

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.inhealion.generator.networking.api.model.User

class SharedPrefAccountStore(context: Context) : AccountStore {
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    @SuppressLint("ApplySharedPref")
    override fun store(user: User) = sharedPreferences.edit {
        putString(PREFS_USER_TOKEN_KEY, user.token)
        commit()
    }

    override fun load(): User? = sharedPreferences.getString(PREFS_USER_TOKEN_KEY, null)?.let { User(token = it) }

    @SuppressLint("ApplySharedPref")
    override fun remove() = sharedPreferences.edit {
        clear()
        commit()
    }


    companion object {
        private const val PREFS_NAME = "com.inhealion.generator.networking.account.SharedPrefAccountStore"
        private const val PREFS_USER_TOKEN_KEY = "PREFS_USER_TOKEN_KEY"
    }
}
