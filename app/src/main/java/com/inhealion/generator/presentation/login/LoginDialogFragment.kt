package com.inhealion.generator.presentation.login

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.inhealion.generator.databinding.LoginFragmentBinding
import com.inhealion.generator.extension.hideKeyboard
import com.inhealion.generator.extension.requireString
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.api.model.User
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

        binding.closeImage.setOnClickListener { dismiss() }
        binding.signInButton.setOnClickListener {
            hideKeyboard()
            viewModel.signIn(
                binding.loginText.requireString(),
                binding.passwordText.requireString()
            )
        }
    }

    private fun switchState(state: State<User>) {
        when (state) {
            is State.Success<*> -> {
                binding.loadingOverlay.root.isVisible = false
                binding.errorText.isVisible = false
                dismiss()
            }
            is State.Failure -> {
                binding.loadingOverlay.root.isVisible = false
                binding.errorText.text = state.error
                binding.errorText.isVisible = true
            }
            is State.InProgress -> {
                binding.loadingOverlay.root.isVisible = true
            }
            else -> {
                binding.loadingOverlay.root.isVisible = false
                binding.errorText.isVisible = false
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        parentFragmentManager.setFragmentResult(
            LOGIN_REQUEST_KEY,
            bundleOf(RESULT_KEY to (viewModel.state.value is State.Success))
        )
        super.onDismiss(dialog)
    }

    companion object {
        fun show(fragmentManager: FragmentManager) {
            LoginDialogFragment().show(fragmentManager, "LoginDialogFragment")
        }
    }
}
