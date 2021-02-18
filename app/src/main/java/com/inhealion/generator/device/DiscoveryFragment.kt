package com.inhealion.generator.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.inhealion.generator.R
import com.inhealion.generator.databinding.DiscoveryFragmentBinding
import com.inhealion.generator.device.adapter.DeviceUiModel
import com.inhealion.generator.device.adapter.DiscoveryDeviceAdapter
import com.inhealion.generator.main.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class DiscoveryFragment : BaseFragment() {
    override val layoutId get() = R.layout.discovery_fragment

    private val viewModel: DiscoveryViewModel by viewModel()

    private lateinit var binding: DiscoveryFragmentBinding
    private val adapter: DiscoveryDeviceAdapter by lazy {
        DiscoveryDeviceAdapter(::onDeviceSelected)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DiscoveryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.devicesRecyclerView.adapter = adapter

        viewModel.apply {
            devices.observe(viewLifecycleOwner) { list ->
                adapter.submitList(list.map { DeviceUiModel(it.name ?: "<Unknown>", it.address) })
            }
            deviceInfo.observe(viewLifecycleOwner) {
                Toast.makeText(requireContext(), "Device serial: $it", Toast.LENGTH_LONG).show()
            }
            inProgress.observe(viewLifecycleOwner) {
                binding.progressBar.isVisible = it
                //binding.devicesRecyclerView.isVisible = !it
            }
            errorMessage.observe(viewLifecycleOwner) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
        viewModel.start()
    }

    private fun onDeviceSelected(address: String) {
        viewModel.sendData(address)
    }

}
