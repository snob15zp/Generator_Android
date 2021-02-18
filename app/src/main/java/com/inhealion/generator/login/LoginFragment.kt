package com.inhealion.generator.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.inhealion.generator.R
import com.inhealion.generator.databinding.DiscoveryFragmentBinding
import com.inhealion.generator.databinding.LoginFragmentBinding
import com.inhealion.generator.main.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragment() {
    override val layoutId: Int
        get() = R.layout.login_fragment


    private val viewModel: LoginViewModel by viewModel()
    private lateinit var binding: LoginFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is LoginViewModel.State.Success -> {
                    binding.errorText.isVisible = false
                    back()
                }
                is LoginViewModel.State.Failure -> {
                    binding.errorText.text = it.error
                    binding.errorText.isVisible = true
                }
                LoginViewModel.State.Idle -> {
                    binding.errorText.isVisible = false
                }
            }
        }

        binding.signInButton.setOnClickListener {
            viewModel.signIn(
                binding.loginText.text!!.toString(),
                binding.passwordText.text!!.toString()
            )
        }
    }
}
