package com.inhealion.generator.presentation.settings

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.inhealion.generator.R
import com.inhealion.generator.data.repository.DeviceRepository
import com.inhealion.generator.presentation.activity.ImportActivity
import com.inhealion.generator.presentation.activity.MainActivity
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.device.ImportFragmentArgs
import com.inhealion.generator.service.AuthorizationManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat() {

    private val authorizationManager: AuthorizationManager by inject()
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_main, rootKey)

        findPreference<Preference>("account")?.apply {
            title = getString(R.string.settings_account)
            summary = null
        }

        findPreference<Preference>("device")?.apply {
            summary = getString(R.string.settins_devce_not_connected)
            setOnPreferenceClickListener {
                showDiscoveryDevices()
                true
            }
        }

        findPreference<Preference>("firmware")?.setOnPreferenceClickListener {
            startActivity(
                Intent(requireContext(), ImportActivity::class.java).apply {
                    putExtras(ImportFragmentArgs(ImportAction.UpdateFirmware).toBundle())
                }
            )
            true
        }

        findPreference<Preference>("logout")?.setOnPreferenceClickListener {
            lifecycleScope.launch {
                authorizationManager.logout()
                finishAffinity(requireActivity())
                startActivity(
                    Intent(requireActivity(), MainActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle()
                )
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.device.observe(viewLifecycleOwner) {
            findPreference<Preference>("device")?.apply {
                title = it?.name ?: getString(R.string.settings_device)
                summary = it?.address ?: getString(R.string.settins_devce_not_connected)
            }
        }

        viewModel.userProfile.observe(viewLifecycleOwner) {
            findPreference<Preference>("account")?.apply {
                title = getString(R.string.name_pattern, it.name, it.surname)
                summary = it.email
            }
        }
        viewModel.loadDeviceInfo()
        viewModel.loadUserInfo()
    }

    private fun setupToolbar() {
        requireActivity().findViewById<Toolbar>(R.id.toolbar).apply {
            title = context.getString(R.string.settings_title)
            menu.findItem(R.id.menu_info_action).isVisible = false
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            setNavigationOnClickListener { requireActivity().finish() }
        }
    }

    private fun showDiscoveryDevices() {
        findNavController().navigate(R.id.settingsDiscoveryFragment)
    }
}
