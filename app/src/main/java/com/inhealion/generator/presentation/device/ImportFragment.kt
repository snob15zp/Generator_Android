package com.inhealion.generator.presentation.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.inhealion.generator.R
import com.inhealion.generator.databinding.ImportFragmentBinding
import com.inhealion.generator.model.ErrorDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.device.viewmodel.ImportViewModel
import com.inhealion.generator.presentation.dialogs.ERROR_DIALOG_REQUEST_KEY
import com.inhealion.generator.presentation.dialogs.ErrorDialog
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.presentation.main.CONNECT_REQUEST_KEY
import com.inhealion.generator.presentation.main.LOGIN_REQUEST_KEY
import com.inhealion.generator.presentation.main.RESULT_KEY
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ImportFragment : BaseFragment<ImportFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> ImportFragmentBinding
        get() = { inflater, parent -> ImportFragmentBinding.inflate(inflater, parent, false) }

    private val fragmentResultListener = FragmentResultListener { key, result ->
        when (key) {
            CONNECT_REQUEST_KEY -> handleConnectionResult(result)
            ERROR_DIALOG_REQUEST_KEY -> back()
        }
    }

    private val viewModel: ImportViewModel by viewModel { parametersOf(navArgs<ImportFragmentArgs>().value.importAction) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(CONNECT_REQUEST_KEY, viewLifecycleOwner, fragmentResultListener)
        parentFragmentManager.setFragmentResultListener(
            ERROR_DIALOG_REQUEST_KEY,
            viewLifecycleOwner,
            fragmentResultListener
        )

        binding.cancelButton.setOnClickListener { back() }
        binding.closeImage.setOnClickListener { back() }

        with(viewModel) {
            showDiscovery.observe(viewLifecycleOwner) { DiscoveryDialogFragment.show(parentFragmentManager) }
            state.observe(viewLifecycleOwner) { switchState(it) }
            currentAction.observe(viewLifecycleOwner) { binding.actionTextView.text = it }
            currentProgress.observe(viewLifecycleOwner) { handleProgressChanged(it) }

            import()
        }
    }

    private fun handleProgressChanged(progress: Int?) {
        if (progress != null && progress > 0) {
            binding.progressTextView.text = getString(R.string.persent_value, progress)
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

    private fun switchState(state: State<Nothing>) {
        when (state) {
            is State.Success -> Unit
            is State.Failure -> {
                binding.progressCircular.isVisible = false
                ErrorDialog.show(
                    parentFragmentManager,
                    ErrorDialogData(getString(R.string.connection_error_title), state.error)
                )
            }
            State.Idle -> Unit
            State.InProgress -> {
                binding.progressCircular.isVisible = true
            }
        }
    }
}
