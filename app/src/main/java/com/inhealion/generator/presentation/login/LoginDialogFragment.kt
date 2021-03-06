package com.inhealion.generator.presentation.login

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.inhealion.generator.R
import com.inhealion.generator.databinding.LoginFragmentBinding
import com.inhealion.generator.extension.hideKeyboard
import com.inhealion.generator.extension.onTextChanged
import com.inhealion.generator.extension.requireString
import com.inhealion.generator.model.MessageDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.dialogs.MessageDialog
import com.inhealion.generator.presentation.main.FullscreenDialogFragment
import com.inhealion.generator.presentation.main.LOGIN_REQUEST_KEY
import com.inhealion.generator.presentation.main.RESULT_KEY
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginDialogFragment : FullscreenDialogFragment<LoginFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> LoginFragmentBinding
        get() = { inflater, container -> LoginFragmentBinding.inflate(inflater, container, false) }

    private val viewModel: LoginViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) { switchState(it) }

        with(binding) {
            updateButtonState()
            loginText.onTextChanged { updateButtonState() }
            passwordText.onTextChanged { updateButtonState() }

            closeImage.setOnClickListener { dismiss() }
            signInButton.setOnClickListener {
                hideKeyboard()
                viewModel.signIn(
                    loginText.requireString(),
                    passwordText.requireString()
                )
            }
        }
    }

    private fun updateButtonState() = with(binding) {
        signInButton.isEnabled = !loginText.text.isNullOrBlank() == true && !passwordText.text.isNullOrBlank()
    }

    private fun switchState(state: State<*>) {
        binding.loadingOverlay.root.isVisible = !state.isFinished

        when (state) {
            is State.Success -> dismiss()
            is State.Failure -> MessageDialog.show(
                parentFragmentManager,
                MessageDialogData(
                    getString(R.string.error_dialog_title),
                    state.error
                )
            )
            is State.InProgress -> Unit
            else -> {
                binding.loadingOverlay.root.isVisible = false
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        parentFragmentManager.setFragmentResult(
            LOGIN_REQUEST_KEY,
            bundleOf(RESULT_KEY to viewModel.isLoginSuccess)
        )
        super.onDismiss(dialog)
    }

    companion object {
        fun show(fragmentManager: FragmentManager) =
            LoginDialogFragment().also { it.show(fragmentManager, "LoginDialogFragment") }

    }
}
