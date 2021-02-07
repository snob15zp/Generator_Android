package com.inhealion.generator.device.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inhealion.generator.databinding.DeviceItemBinding
import com.inhealion.generator.extension.itemCallbackOf

class DiscoveryDeviceAdapter(val onClickAction: (String) -> Unit) :
    ListAdapter<DeviceUiModel, RecyclerView.ViewHolder>(itemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(DeviceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val device = getItem(position)
        (holder as ViewHolder).viewBinding.apply {
            deviceTextView.text = device.name
            root.setOnClickListener { onClickAction(device.address) }
        }
    }

    inner class ViewHolder(val viewBinding: DeviceItemBinding) : RecyclerView.ViewHolder(viewBinding.root) {
    }
}

private fun itemCallback() = itemCallbackOf<DeviceUiModel>(
    areItemsTheSame = { lhs, rhs -> lhs.address == rhs.address },
    areContentsTheSame = { lhs, rhs -> lhs == rhs }
)


data class DeviceUiModel(
    val name: String,
    val address: String
)
