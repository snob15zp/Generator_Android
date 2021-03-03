package com.inhealion.generator.presentation.device

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.DialogInterface
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
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.inhealion.generator.R
import com.inhealion.generator.databinding.DiscoveryFragmentBinding
import com.inhealion.generator.device.model.BleDevice
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.device.adapter.DeviceUiModel
import com.inhealion.generator.presentation.device.adapter.DiscoveryDeviceAdapter
import com.inhealion.generator.presentation.device.viewmodel.DiscoveryViewModel
import com.inhealion.generator.presentation.login.LoginDialogFragment
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.presentation.main.CONNECT_REQUEST_KEY
import com.inhealion.generator.presentation.main.FullscreenDialogFragment
import com.inhealion.generator.presentation.main.RESULT_KEY
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.koin.androidx.viewmodel.ext.android.viewModel


class DiscoveryDialogFragment : FullscreenDialogFragment<DiscoveryFragmentBinding>() {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.devicesRecyclerView.adapter = adapter

        binding.closeImage.setOnClickListener { dismiss() }
        binding.grantAccessButton.setOnClickListener { gotoSettings() }
        binding.errorOverlay.retryButton.setOnClickListener { viewModel.start() }

        viewModel.apply {
            state.observe(viewLifecycleOwner) { switchToState(it) }
            finish.observe(viewLifecycleOwner) { dismiss() }
        }

        checkPermissions()
    }

    override fun onDismiss(dialog: DialogInterface) {
        setResult()
        super.onDismiss(dialog)
    }

    private fun setResult() {
        parentFragmentManager.setFragmentResult(
            CONNECT_REQUEST_KEY,
            bundleOf(RESULT_KEY to viewModel.isDeviceSelected)
        )
        // TODO for compatibility with old fragmentManager
        (targetFragment as? DiscoveryDialogListener)?.onResult(viewModel.isDeviceSelected)
    }

    private fun switchToState(state: State<List<BleDevice>>) {
        when (state) {
            is State.Failure -> {
                binding.progressBar.isVisible = false
                binding.errorOverlay.root.isVisible = true
                binding.errorOverlay.errorTextView.text = state.error
            }
            is State.Success -> {
                binding.errorOverlay.root.isVisible = false
                adapter.submitList((state.data).map {
                    DeviceUiModel(
                        it.name ?: "<Unknown>", it.address
                    )
                })
            }
            is State.InProgress -> {
                binding.errorOverlay.root.isVisible = false
                binding.progressBar.isVisible = true
            }
            else -> Unit
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

    companion object {
        fun show(fragmentManager: FragmentManager, target: Fragment? = null) {
            target?.let { require(it is DiscoveryDialogListener) }
            DiscoveryDialogFragment()
                .apply {
                    setTargetFragment(target, -1)
                }
                .show(fragmentManager, "DiscoveryDialogFragment")
        }
    }

    interface DiscoveryDialogListener {
        fun onResult(isDeviceSelected: Boolean)
    }
}
