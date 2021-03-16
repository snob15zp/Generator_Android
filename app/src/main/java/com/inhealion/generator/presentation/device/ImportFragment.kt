package com.inhealion.generator.presentation.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.navigation.fragment.navArgs
import com.inhealion.generator.R
import com.inhealion.generator.databinding.ImportFragmentBinding
import com.inhealion.generator.model.MessageDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.device.viewmodel.ImportViewModel
import com.inhealion.generator.presentation.dialogs.ERROR_DIALOG_REQUEST_KEY
import com.inhealion.generator.presentation.dialogs.MessageDialog
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.service.FileType
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

    private val viewModel: ImportViewModel by viewModel { parametersOf(navArgs<ImportFragmentArgs>().value.importAction) }

//    private val dialog: AlertDialog by lazy {
//        MaterialAlertDialogBuilder(requireContext())
//            .setMessage(R.string.import_was_canceled)
//            .setNegativeButton(R.string.button_no) { _, _ -> back() }
//            .setPositiveButton(R.string.button_yes) { _, _ -> viewModel.import() }
//            .create()
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            ERROR_DIALOG_REQUEST_KEY,
            viewLifecycleOwner,
            fragmentResultListener
        )

        with(binding) {
            cancelButton.setOnClickListener { back() }
            closeImage.setOnClickListener { back() }
            titleTextView.text = when (viewModel.importAction) {
                is ImportAction.ImportFolder -> getString(R.string.import_folder)
                is ImportAction.UpdateFirmware -> getString(R.string.flash_firmware)
            }
        }

        with(viewModel) {
            importState.observe(viewLifecycleOwner, ::handleImportStateChanged)
        }
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
                progressTextView.isVisible = false
            }
            is ImportState.Error -> {
                progressCircular.isVisible = false
                progressTextView.isVisible = false
                MessageDialog.show(
                    parentFragmentManager,
                    MessageDialogData(getString(R.string.error_dialog_title), importState.message)
                )
            }
            ImportState.Finished -> {
                actionTextView.text = getString(R.string.done)
                progressTextView.isVisible = false
                MessageDialog.show(
                    parentFragmentManager,
                    MessageDialogData("", getString(R.string.import_success))
                )
            }
            ImportState.Idle -> Unit
            ImportState.Canceled -> Unit
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.bind(requireContext())
    }

    override fun onPause() {
        super.onPause()
        viewModel.dispose(requireContext())
    }

    private fun handleProgressChanged(progress: Int?) {
        if (progress != null && progress > 0) {
            binding.progressTextView.text = getString(R.string.percent_value, progress)
        } else {
            binding.progressTextView.text = null
        }
    }
}
