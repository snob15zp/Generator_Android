package com.inhealion.generator.presentation.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentResultListener
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.inhealion.generator.R
import com.inhealion.generator.databinding.ImportFragmentBinding
import com.inhealion.generator.model.MessageDialogData
import com.inhealion.generator.presentation.device.viewmodel.ImportViewModel
import com.inhealion.generator.presentation.dialogs.MESSAGE_DIALOG_REQUEST_KEY
import com.inhealion.generator.presentation.dialogs.MessageDialog
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.service.FileType
import com.inhealion.generator.service.ImportState
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ImportFragment : BaseFragment<ImportFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> ImportFragmentBinding
        get() = { inflater, parent -> ImportFragmentBinding.inflate(inflater, parent, false) }

    private val fragmentResultListener = FragmentResultListener { key, _ ->
        when (key) {
            MESSAGE_DIALOG_REQUEST_KEY -> {
                viewModel.cancel(requireContext())
                back()
            }
        }
    }

    private var dialog: DialogFragment? = null

    private val viewModel: ImportViewModel by viewModel {
        parametersOf(navArgs<ImportFragmentArgs>().value.importAction)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            MESSAGE_DIALOG_REQUEST_KEY,
            viewLifecycleOwner,
            fragmentResultListener
        )

        with(binding) {
            cancelButton.setOnClickListener {
                viewModel.cancel(requireContext())
                back()
            }
            titleTextView.text = when (viewModel.importAction) {
                is ImportAction.ImportFolder -> getString(R.string.import_folder)
                is ImportAction.UpdateFirmware -> getString(R.string.flash_firmware)
            }

            actionTextView.text = getString(R.string.action_initializing)
            progressCircular.isVisible = true
            progressTextView.isVisible = false
        }

        with(viewModel) {
            importState.observe(viewLifecycleOwner, ::handleImportStateChanged)
        }

        activity?.onBackPressedDispatcher?.addCallback {
            Snackbar.make(binding.root, getString(R.string.import_back_alert_message), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
    }

    private fun handleImportStateChanged(importState: ImportState) = with(binding) {
        progressCircular.isVisible = importState.isActive
        when (importState) {
            ImportState.Connecting -> {
                actionTextView.text = getString(R.string.action_connecting)
                progressTextView.isVisible = false
            }
            ImportState.Downloading -> {
                actionTextView.text = getString(R.string.action_download)
                progressTextView.isVisible = false
            }
            is ImportState.Importing -> {
                actionTextView.text = when (importState.fileType) {
                    FileType.MCU -> getString(R.string.action_import_mcu_firmware)
                    else -> getString(R.string.action_import)
                }
                progressTextView.isVisible = true
                handleProgressChanged(importState.progress)
            }
            ImportState.Rebooting -> {
                actionTextView.text = getString(R.string.action_reboot)
                progressTextView.isVisible = false
            }
            is ImportState.Failed -> {
                progressTextView.isVisible = false
                dialog = MessageDialog.show(
                    parentFragmentManager,
                    MessageDialogData(getString(R.string.error_dialog_title), importState.message)
                )
            }
            ImportState.Success -> {
                actionTextView.text = getString(R.string.done)
                progressTextView.isVisible = false
                dialog = MessageDialog.show(
                    parentFragmentManager,
                    MessageDialogData(getString(R.string.done), getString(R.string.import_success))
                )
            }
            ImportState.Idle -> dialog?.dismiss()
            ImportState.Canceled -> Unit
        }
    }

    private fun handleProgressChanged(progress: Int?) {
        if (progress != null && progress > 0) {
            binding.progressTextView.text = getString(R.string.percent_value, progress)
        } else {
            binding.progressTextView.text = null
        }
    }
}
