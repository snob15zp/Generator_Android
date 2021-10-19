package com.inhealion.generator.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.data.repository.UserRepository
import com.inhealion.generator.networking.LogoutManager
import com.inhealion.generator.networking.account.AccountStore
import com.inhealion.generator.presentation.activity.MainActivity
import kotlinx.coroutines.runBlocking

class LogoutManagerImpl(
    private val context: Context,
    private val deviceRepository: DeviceRepository,
    private val sharedPrefManager: SharedPrefManager,
    private val accountStore: AccountStore,
    private val userRepository: UserRepository,
) : LogoutManager {

    override fun logout() {
        runBlocking {
//            deviceRepository.remove()
//            userRepository.remove()
        }
        accountStore.remove()
        sharedPrefManager.clear()
        ContextCompat.startActivity(
            context,
            Intent(context, MainActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) },
            null
        )
    }
}
