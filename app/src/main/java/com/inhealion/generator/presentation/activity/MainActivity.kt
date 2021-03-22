package com.inhealion.generator.presentation.activity

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.NavHostFragment
import com.inhealion.generator.R
import com.inhealion.generator.databinding.ActivityMainBinding
import com.inhealion.generator.extension.observe
import com.inhealion.generator.model.UiImportState
import com.inhealion.generator.presentation.device.DiscoveryDialogFragment
import com.inhealion.generator.presentation.login.LoginDialogFragment
import com.inhealion.generator.presentation.main.CONNECT_REQUEST_KEY
import com.inhealion.generator.presentation.main.LOGIN_REQUEST_KEY
import com.inhealion.generator.presentation.main.RESULT_KEY
import com.inhealion.generator.presentation.main.viewmodel.MainViewModel
import com.inhealion.generator.presentation.view.NoticeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModel()

    override val noticeView: NoticeView
        get() = binding.noticeView

    override val importState: LiveData<UiImportState>
        get() = viewModel.importState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(viewModel) {
            action.observe(this@MainActivity) {
                println("TTT > action $it")
                when (it) {
                    MainViewModel.Action.ShowDeviceConnection -> launchConnectionDialog()
                    MainViewModel.Action.ShowFolders -> navigateToFolders()
                    MainViewModel.Action.ShowLogin -> launchLoginDialog()
                }
            }
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
            .observe(CONNECT_REQUEST_KEY, this, ::handleConnectionResult)
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
            .observe(LOGIN_REQUEST_KEY, this, ::handleLoginResult)
    }
}
