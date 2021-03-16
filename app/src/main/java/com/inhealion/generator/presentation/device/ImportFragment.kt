package com.inhealion.generator.presentation.device

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.inhealion.generator.R
import com.inhealion.generator.databinding.ImportFragmentBinding
import com.inhealion.generator.extension.observe
import com.inhealion.generator.model.MessageDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.device.viewmodel.ImportViewModel
import com.inhealion.generator.presentation.dialogs.ERROR_DIALOG_REQUEST_KEY
import com.inhealion.generator.presentation.dialogs.MessageDialog
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.presentation.main.CONNECT_REQUEST_KEY
import com.inhealion.generator.presentation.main.RESULT_KEY
import com.inhealion.generator.service.ImportService
import com.inhealion.generator.service.ImportService.Companion.IMPORT_BROADCAST_ACTION
import com.inhealion.generator.service.ImportService.Companion.KEY_IMPORT_STATE
import com.inhealion.generator.service.ImportState
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ImportFragment : BaseFragment<ImportFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> ImportFragmentBinding
        get() = { inflater, parent -> ImportFragmentBinding.inflate(inflater, parent, false) }

    private val fragmentResultListener = FragmentResultListener { key, result ->
        when (key) {
            ERROR_DIALOG_REQUEST_KEY -> back()
        }
    }

    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(requireContext())
    }

    private val broadcastReceiver = ImportBroadcastReceiver()
    private val viewModel: ImportViewModel by viewModel { parametersOf(navArgs<ImportFragmentArgs>().value.importAction) }

    private val dialog: AlertDialog by lazy {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.import_was_canceled)
            .setNegativeButton(R.string.button_no) { _, _ -> back() }
            .setPositiveButton(R.string.button_yes) { _, _ -> viewModel.import() }
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            ERROR_DIALOG_REQUEST_KEY,
            viewLifecycleOwner,
            fragmentResultListener
        )

        binding.cancelButton.setOnClickListener { back() }
        binding.closeImage.setOnClickListener { back() }
        binding.titleTextView.text = when (viewModel.importAction) {
            is ImportAction.ImportFolder -> getString(R.string.import_folder)
            is ImportAction.UpdateFirmware -> getString(R.string.flash_firmware)
        }

        with(viewModel) {
            state.observe(viewLifecycleOwner) { switchState(it) }
            currentAction.observe(viewLifecycleOwner) { binding.actionTextView.text = it }
            currentProgress.observe(viewLifecycleOwner) { handleProgressChanged(it) }
        }

        localBroadcastManager.registerReceiver(broadcastReceiver, IntentFilter(IMPORT_BROADCAST_ACTION))
        requireContext().startService(Intent(context, ImportService::class.java))
        requireContext().bindService(Intent(context, ImportService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                println("SSS > onServiceConnected")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                println("SSS > onServiceDisconnected")
            }

        }, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isCanceled) {
            dialog.show()
        }
    }

    override fun onStop() {
        super.onStop()
        dialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("IS_CANCELED", viewModel.isCanceled)
    }

    private fun handleProgressChanged(progress: Int?) {
        if (progress != null && progress > 0) {
            binding.progressTextView.text = getString(R.string.percent_value, progress)
        } else {
            binding.progressTextView.text = null
        }
    }

    private fun switchState(state: State<*>) = with(binding) {
        println("RRR > state changed $state")
        when (state) {
            is State.Success -> {
                actionTextView.text = getString(R.string.done)
                MessageDialog.show(
                    parentFragmentManager,
                    MessageDialogData("", getString(R.string.import_success))
                )
            }
            is State.Failure -> {
                progressCircular.isVisible = false
                MessageDialog.show(
                    parentFragmentManager,
                    MessageDialogData(getString(R.string.error_dialog_title), state.error)
                )
            }
            is State.InProgress -> {
                progressCircular.isVisible = true
                progressCircular.isIndeterminate = state.progress < 0
                progressCircular.progress = state.progress

                progressTextView.isVisible = state.progress >= 0
                progressTextView.text = if (state.progress >= 0) {
                    getString(R.string.percent_value, state.progress)
                } else null
            }
            State.Idle -> Unit
        }
    }

    inner class ImportBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getParcelableExtra<ImportState>(KEY_IMPORT_STATE)
            println("SSS > import state: $state")
        }
    }
}
