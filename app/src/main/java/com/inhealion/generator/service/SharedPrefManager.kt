package com.inhealion.generator.service

import android.content.Context
import androidx.core.content.edit

class SharedPrefManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var isDiscoveryWasShown: Boolean
        get() = sharedPreferences.getBoolean(DISCOVERY_SCREEN_SHOW_KEY, false)
        set(value) {
            sharedPreferences.edit {
                putBoolean(DISCOVERY_SCREEN_SHOW_KEY, value)
                commit()
            }
        }


    fun clear() {
        sharedPreferences.edit {
            clear()
            commit()
        }
    }

    companion object {
        private const val PREFS_NAME = "com.inhealion.generator.service.SharedPrefManager"

        private const val DISCOVERY_SCREEN_SHOW_KEY = "DISCOVERY_SCREEN_SHOW_KEY"
    }

}
