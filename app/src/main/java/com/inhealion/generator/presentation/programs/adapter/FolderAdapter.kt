package com.inhealion.generator.presentation.programs.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inhealion.generator.R
import com.inhealion.generator.databinding.FolderItemBinding
import com.inhealion.generator.extension.itemCallbackOf
import java.util.*

class FolderAdapter(
    private val clickAction: (String) -> Unit
) : ListAdapter<FolderUiModel, RecyclerView.ViewHolder>(itemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(FolderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as ViewHolder).apply {
            viewBinding.folderTextView.text = item.name
            viewBinding.folderStateTextView.apply {
                setTextColor(resources.getColor(if (item.isExpired) R.color.textError else R.color.secondaryTextColor))
                text = if (item.isExpired) resources.getString(R.string.expired) else item.expiredPeriod
            }
            viewBinding.root.isEnabled = !item.isExpired
            viewBinding.root.setOnClickListener { clickAction(item.id) }
        }
    }

    inner class ViewHolder(val viewBinding: FolderItemBinding) : RecyclerView.ViewHolder(viewBinding.root)
}


data class FolderUiModel(
    val id: String,
    val name: String,
    val expiredAt: Date,
    val expiredPeriod: String,
    val isExpired: Boolean
)


private fun itemCallback() = itemCallbackOf<FolderUiModel>(
    areItemsTheSame = { lhs, rhs -> lhs.id == rhs.id },
    areContentsTheSame = { lhs, rhs -> lhs == rhs }
)
