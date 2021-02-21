package com.inhealion.generator.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.inhealion.generator.databinding.LoginFragmentBinding
import com.inhealion.generator.extension.hideKeyboard
import com.inhealion.generator.extension.requireString
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.main.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment<LoginFragmentBinding>() {
    private val viewModel: LoginViewModel by viewModel()

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> LoginFragmentBinding
        get() = { inflater, parent -> LoginFragmentBinding.inflate(inflater, parent, false) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) { switchState(it) }

        binding.signInButton.setOnClickListener {
            hideKeyboard()
            viewModel.signIn(
                binding.loginText.requireString(),
                binding.passwordText.requireString()
            )
        }
    }

    private fun switchState(state: State) {
        when (state) {
            is State.Success<*> -> {
                binding.loadingOverlay.root.isVisible = false
                binding.errorText.isVisible = false
                back()
            }
            is State.Failure -> {
                binding.loadingOverlay.root.isVisible = false
                binding.errorText.text = state.error
                binding.errorText.isVisible = true
            }
            State.Idle -> {
                binding.loadingOverlay.root.isVisible = false
                binding.errorText.isVisible = false
            }
            State.InProgress -> {
                binding.loadingOverlay.root.isVisible = true
            }
        }
    }
}
