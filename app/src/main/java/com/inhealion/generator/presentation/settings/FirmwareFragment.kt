package com.inhealion.generator.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.inhealion.generator.R
import com.inhealion.generator.data.model.VersionInfo
import com.inhealion.generator.databinding.FirmwareFragmentBinding
import com.inhealion.generator.extension.observe
import com.inhealion.generator.model.MessageDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.device.DiscoveryDialogFragment
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.device.ImportActivity
import com.inhealion.generator.presentation.dialogs.MessageDialog
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.presentation.main.CONNECT_REQUEST_KEY
import com.inhealion.generator.presentation.main.RESULT_KEY
import com.inhealion.generator.presentation.settings.viewmodel.FirmwareViewModel
import com.inhealion.generator.service.ImportService
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

class FirmwareFragment : BaseFragment<FirmwareFragmentBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FirmwareFragmentBinding
        get() = { inflater, parent -> FirmwareFragmentBinding.inflate(inflater, parent, false) }

    private val viewModel: FirmwareViewModel by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            showDiscovery.observe(viewLifecycleOwner) {
                DiscoveryDialogFragment.show(parentFragmentManager)
                    .observe(CONNECT_REQUEST_KEY, viewLifecycleOwner, ::handleConnectionResult)
            }
            state.observe(viewLifecycleOwner, ::switchState)
            load()
        }

        binding.forceFlashButton.setOnClickListener { flash() }
        binding.updateButton.setOnClickListener { flash() }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    private fun flash() {
        val version = viewModel.latestVersionName ?: run {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_version), Toast.LENGTH_LONG).show()
            return
        }
        val action = ImportAction.UpdateFirmware(version, viewModel.device!!.address)
        viewModel.reset()
        ImportService.start(requireContext(), action)
        ImportActivity.start(requireContext(), action)
    }

    override fun setupToolbar(toolbar: Toolbar) = with(toolbar) {
        title = getString(R.string.settings_firmware)
        setNavigationOnClickListener { back() }

        inflateMenu(R.menu.toolbar_firmware_menu)
        menu.findItem(R.id.menu_reload_action)
            .setOnMenuItemClickListener {
                viewModel.load(true)
                true
            }
    }.run { }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<Toolbar>(R.id.toolbar)?.menu?.removeItem(R.id.menu_reload_action)
    }

    private fun switchState(state: State<VersionInfo>) = with(binding) {
        loadingOverlay.root.isVisible = !state.isFinished

        when (state) {
            State.Idle -> {
                noUpdateTextView.isVisible = false
                updateButton.isVisible = false
                latestVersionValueTextView.text = getString(R.string.version_unavailable)
                deviceVersionValueTextView.text = getString(R.string.settings_device_not_connected)
            }
            is State.InProgress -> Unit
            is State.Failure -> {
                MessageDialog.show(
                    parentFragmentManager,
                    MessageDialogData(getString(R.string.error_dialog_title), state.error)
                )
            }
            is State.Success -> {
                latestVersionValueTextView.text = state.data.latestVersion
                deviceVersionValueTextView.text = state.data.deviceVersion
                    ?: getString(R.string.settings_device_not_connected)
                lastUpdateDateTextView.text = getString(
                    R.string.last_updated_at,
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(state.data.lastCheckAt)
                )

                noUpdateTextView.isVisible = !state.data.isUpdateRequired && state.data.deviceVersion != null
                updateButton.isVisible = state.data.isUpdateRequired && state.data.deviceVersion != null
                forceFlashButton.isVisible = !state.data.isUpdateRequired && state.data.deviceVersion != null
            }
        }
    }

    private fun handleConnectionResult(result: Bundle) {
        if (result.getBoolean(RESULT_KEY)) {
            viewModel.load()
        } else with(binding) {
            deviceVersionValueTextView.text = getString(R.string.settings_device_not_connected)
            noUpdateTextView.isVisible = false
            updateButton.isVisible = false
        }
    }
}
