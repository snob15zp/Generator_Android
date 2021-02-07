package com.inhealion.generator.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber


@ExperimentalCoroutinesApi
class BluetoothScannerImpl(private val context: Context) : Scanner {

    private val adapter = BluetoothAdapter.getDefaultAdapter()

    override fun scan(): Flow<BluetoothDiscoveryAction> = callbackFlow {
        val filter = IntentFilter()

        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        val broadcastReceiver = BluetoothBroadcastReceiver(this)
        context.registerReceiver(broadcastReceiver, filter)
        adapter.startDiscovery()

        awaitClose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    inner class BluetoothBroadcastReceiver(private val channel: SendChannel<BluetoothDiscoveryAction>) :
        BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> channel.offer(BluetoothDiscoveryAction.Started)
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> channel.close()
                BluetoothDevice.ACTION_FOUND ->
                    (intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice)?.let {
                        Timber.d("Found device: $it")
                        channel.offer(BluetoothDiscoveryAction.Found(it))
                    }
            }
        }
    }
}

