package com.inhealion.generator.presentation.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.inhealion.generator.R
import com.inhealion.generator.presentation.device.DiscoveryDialogFragment
import com.inhealion.generator.presentation.settings.viewmodel.SettingsViewModel
import com.inhealion.generator.service.AuthorizationManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat(), DiscoveryDialogFragment.DiscoveryDialogListener {

    private val authorizationManager: AuthorizationManager by inject()
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_main, rootKey)

        findPreference<Preference>("account")?.apply {
            title = getString(R.string.settings_account)
            summary = null
        }

        findPreference<Preference>("device")?.apply {
            summary = getString(R.string.settings_device_not_connected)
            setOnPreferenceClickListener {
                showDiscoveryDevices()
                true
            }
        }

        findPreference<Preference>("firmware")?.setOnPreferenceClickListener {
            findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToFirmwareFragment())
            true
        }

        findPreference<Preference>("logout")?.setOnPreferenceClickListener {
            authorizationManager.logout()
            requireActivity().finish()
            true
        }

        findPreference<Preference>("about")?.let {
            val appVersion = getFormattedAppVersion(requireContext())
            it.title = getString(R.string.settings_version_name, appVersion)
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
                summary = it?.address ?: getString(R.string.settings_device_not_connected)
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

    override fun onResult(isDeviceSelected: Boolean) {
        viewModel.loadDeviceInfo()
    }

    fun getFormattedAppVersion(context: Context): String = with(context) {
        val manager = this.packageManager
        val name = this.packageName
        val info = manager.getPackageInfo(name, PackageManager.GET_ACTIVITIES)

        return buildString {
            var versionName = info.versionName
            if (versionName.contains("-")) {
                versionName = versionName.substring(0, versionName.indexOf("-"))
            }
            append(versionName)
            append(".")
            append(info.versionCode)
        }
    }

    private fun setupToolbar() {
        requireActivity().findViewById<Toolbar>(R.id.toolbar).apply {
            title = context.getString(R.string.settings_title)
            setNavigationOnClickListener { requireActivity().finish() }
        }
    }

    private fun showDiscoveryDevices() {
        DiscoveryDialogFragment.show(parentFragmentManager, this)
    }
}
