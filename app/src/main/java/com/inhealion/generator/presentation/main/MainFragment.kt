package com.inhealion.generator.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.navigation.fragment.findNavController
import com.inhealion.generator.databinding.MainFragmentBinding
import com.inhealion.generator.presentation.login.LoginDialogFragment
import com.inhealion.generator.presentation.main.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LOGIN_REQUEST_KEY = "loginResult"
const val CONNECT_REQUEST_KEY = "connectionResult"
const val RESULT_KEY = "result"

class MainFragment : BaseFragment<MainFragmentBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?) -> MainFragmentBinding
        get() = { inflater, parent -> MainFragmentBinding.inflate(inflater, parent, false) }

    private val viewModel: MainViewModel by viewModel()

    private val fragmentResultListener = FragmentResultListener { key, result ->
        when (key) {
            LOGIN_REQUEST_KEY -> handleLoginResult(result)
            CONNECT_REQUEST_KEY -> handleConnectionResult(result)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.setFragmentResultListener(LOGIN_REQUEST_KEY, viewLifecycleOwner, fragmentResultListener)
        childFragmentManager.setFragmentResultListener(CONNECT_REQUEST_KEY, viewLifecycleOwner, fragmentResultListener)

        with(viewModel) {
            showLogin.observe(viewLifecycleOwner) { launchLoginDialog() }
            showFolders.observe(viewLifecycleOwner) { navigateToFolders() }
            showDeviceConnection.observe(viewLifecycleOwner) { launchConnectionDialog() }
            navigate()
        }
    }

    private fun handleConnectionResult(result: Bundle) {

    }

    private fun handleLoginResult(result: Bundle) {
        if (!result.getBoolean(RESULT_KEY)) {
            back()
        } else {
            viewModel.navigate()
        }
    }

    private fun launchConnectionDialog() {
    }

    private fun navigateToFolders() {
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToFolderFragment())
    }

    private fun launchLoginDialog() {
        LoginDialogFragment.show(childFragmentManager)
    }
}

