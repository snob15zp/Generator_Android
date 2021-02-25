package com.inhealion.generator.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavOptions
import androidx.navigation.createGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.inhealion.generator.R
import com.inhealion.generator.databinding.ActivityMainBinding
import com.inhealion.generator.extension.setFragmentResultListener
import com.inhealion.generator.presentation.device.DiscoveryDialogFragment
import com.inhealion.generator.presentation.login.LoginDialogFragment
import com.inhealion.generator.presentation.main.CONNECT_REQUEST_KEY
import com.inhealion.generator.presentation.main.LOGIN_REQUEST_KEY
import com.inhealion.generator.presentation.main.MainFragmentDirections
import com.inhealion.generator.presentation.main.RESULT_KEY
import com.inhealion.generator.presentation.main.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModel()

    private val fragmentResultListener = { key: String, result: Bundle ->
        when (key) {
            LOGIN_REQUEST_KEY -> handleLoginResult(result)
            CONNECT_REQUEST_KEY -> handleConnectionResult(result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFragmentResultListener(LOGIN_REQUEST_KEY, fragmentResultListener)
        setFragmentResultListener(CONNECT_REQUEST_KEY, fragmentResultListener)

        with(viewModel) {
            showLogin.observe(this@MainActivity) { launchLoginDialog() }
            showFolders.observe(this@MainActivity) { navigateToFolders() }
            showDeviceConnection.observe(this@MainActivity) { launchConnectionDialog() }

            navigate()
        }

    }

    private fun handleConnectionResult(result: Bundle) {
        navigateToFolders()
    }

    private fun handleLoginResult(result: Bundle) {
        if (!result.getBoolean(RESULT_KEY)) {
            finish()
        } else {
            viewModel.navigate()
        }
    }

    private fun launchConnectionDialog() {
        DiscoveryDialogFragment.show(supportFragmentManager)
    }

    private fun navigateToFolders() {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).apply {
            navController.graph = navController.navInflater.inflate(R.navigation.main_nav_graph).apply {
                startDestination = R.id.folderFragment
            }
        }
    }

    private fun launchLoginDialog() {
        LoginDialogFragment.show(supportFragmentManager)
    }
}
