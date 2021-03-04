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
import com.inhealion.generator.extension.observe
import com.inhealion.generator.model.MessageDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.device.viewmodel.ImportViewModel
import com.inhealion.generator.presentation.dialogs.ERROR_DIALOG_REQUEST_KEY
import com.inhealion.generator.presentation.dialogs.MessageDialog
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.presentation.main.CONNECT_REQUEST_KEY
import com.inhealion.generator.presentation.main.RESULT_KEY
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
            ImportAction.UpdateFirmware -> getString(R.string.flash_firmware)
        }

        with(viewModel) {
            showDiscovery.observe(viewLifecycleOwner) {
                DiscoveryDialogFragment.show(parentFragmentManager)
                    .observe(CONNECT_REQUEST_KEY, viewLifecycleOwner, ::handleConnectionResult)
            }
            state.observe(viewLifecycleOwner) { switchState(it) }
            currentAction.observe(viewLifecycleOwner) { binding.actionTextView.text = it }
            currentProgress.observe(viewLifecycleOwner) { handleProgressChanged(it) }

            import()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cancel()
    }

    private fun handleProgressChanged(progress: Int?) {
        if (progress != null && progress > 0) {
            binding.progressTextView.text = getString(R.string.percent_value, progress)
        } else {
            binding.progressTextView.text = null
        }
    }

    private fun handleConnectionResult(result: Bundle) {
        if (result.getBoolean(RESULT_KEY)) {
            viewModel.import()
        } else {
            back()
        }
    }

    private fun switchState(state: State<*>) {
        println("RRR > state changed $state")
        when (state) {
            is State.Success -> {
                binding.actionTextView.text = getString(R.string.done)
                MessageDialog.show(
                    parentFragmentManager,
                    MessageDialogData("", getString(R.string.import_success))
                )
            }
            is State.Failure -> {
                binding.progressCircular.isVisible = false
                MessageDialog.show(
                    parentFragmentManager,
                    MessageDialogData(getString(R.string.error_dialog_title), state.error)
                )
            }
            is State.InProgress -> {
                binding.progressCircular.isVisible = true
                binding.progressCircular.isIndeterminate = state.progress < 0
                binding.progressCircular.progress = state.progress

                binding.progressTextView.isVisible = state.progress > 0
                binding.progressTextView.text = if (state.progress > 0) {
                    getString(R.string.percent_value, state.progress)
                } else null
            }
            State.Idle -> Unit
        }
    }
}
