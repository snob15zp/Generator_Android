package com.inhealion.generator.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.inhealion.generator.R
import com.inhealion.generator.device.BluetoothScannerImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import timber.log.Timber

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val scanner = BluetoothScannerImpl(this)

        lifecycleScope.launchWhenStarted {
            scanner.scan().collect {
                Timber.d("TTT > $it")
            }
        }

        lifecycleScope.launchWhenStarted {
           delay(2000)

        }
    }
}
