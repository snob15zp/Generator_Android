package com.inhealion.generator.presentation.device

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.inhealion.generator.R
import com.inhealion.generator.databinding.DiscoveryFragmentBinding
import com.inhealion.generator.device.model.BleDevice
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.device.adapter.DeviceUiModel
import com.inhealion.generator.presentation.device.adapter.DiscoveryDeviceAdapter
import com.inhealion.generator.presentation.device.viewmodel.DiscoveryViewModel
import com.inhealion.generator.presentation.main.BaseFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.koin.androidx.viewmodel.ext.android.viewModel


class DiscoveryFragment : BaseFragment<DiscoveryFragmentBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?) -> DiscoveryFragmentBinding
        get() = { inflater, parent -> DiscoveryFragmentBinding.inflate(inflater, parent, false) }

    private val viewModel: DiscoveryViewModel by viewModel()

    private val adapter: DiscoveryDeviceAdapter by lazy {
        DiscoveryDeviceAdapter(::onDeviceSelected)
    }

    private val permissionListener = object : PermissionListener {
        override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
            binding.permissionRequiredOverlay.isVisible = false
            viewModel.start()
        }

        override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
            binding.permissionRequiredOverlay.isVisible = true
        }

        override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, token: PermissionToken) {
            token.continuePermissionRequest()
        }
    }

    override fun setupToolbar(toolbar: Toolbar) {
        super.setupToolbar(toolbar)
        with(toolbar) {
            title = context.getString(R.string.discovery_title)
            menu.findItem(R.id.menu_info_action).isVisible = false
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            setNavigationOnClickListener { back() }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.devicesRecyclerView.adapter = adapter

        binding.grantAccessButton.setOnClickListener { gotoSettings() }

        viewModel.apply {
            state.observe(viewLifecycleOwner) { switchToState(it) }
            finish.observe(viewLifecycleOwner) { back() }
        }

        checkPermissions()
    }

    private fun switchToState(state: State) {
        when (state) {
            is State.Failure -> Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
            is State.Success<*> -> adapter.submitList((state.data as List<BleDevice>).map {
                DeviceUiModel(
                    it.name ?: "<Unknown>", it.address
                )
            })
            State.Idle -> Unit
            State.InProgress -> binding.progressBar.isVisible = true
        }
    }

    private fun gotoSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun checkPermissions() {
        Dexter.withContext(requireContext())
            .withPermission(ACCESS_FINE_LOCATION)
            .withListener(permissionListener)
            .onSameThread()
            .check()
    }

    private fun onDeviceSelected(device: DeviceUiModel) {
        viewModel.saveDevice(device)
    }

}
