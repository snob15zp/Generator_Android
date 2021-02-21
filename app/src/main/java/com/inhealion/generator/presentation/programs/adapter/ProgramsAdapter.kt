package com.inhealion.generator.presentation.programs.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.inhealion.generator.databinding.ProgramItemBinding
import com.inhealion.generator.extension.itemCallbackOf

class ProgramsAdapter : ListAdapter<ProgramUiModel, RecyclerView.ViewHolder>(itemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ProgramItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as ViewHolder).apply {
            viewBinding.programTextView.text = item.name
        }
    }

    inner class ViewHolder(val viewBinding: ProgramItemBinding) : RecyclerView.ViewHolder(viewBinding.root)
}

data class ProgramUiModel(
    val id: String,
    val name: String
)


private fun itemCallback() = itemCallbackOf<ProgramUiModel>(
    areItemsTheSame = { lhs, rhs -> lhs.id == rhs.id },
    areContentsTheSame = { lhs, rhs -> lhs == rhs }
)
