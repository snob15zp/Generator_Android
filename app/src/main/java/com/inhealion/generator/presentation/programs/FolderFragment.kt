package com.inhealion.generator.presentation.programs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.inhealion.generator.R
import com.inhealion.generator.databinding.FolderFragmentBinding
import com.inhealion.generator.extension.setTextOrHide
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.api.model.Folder
import com.inhealion.generator.networking.api.model.UserProfile
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.presentation.programs.adapter.FolderAdapter
import com.inhealion.generator.presentation.programs.adapter.FolderUiModel
import com.inhealion.generator.presentation.programs.viewmodel.FolderViewModel
import com.inhealion.generator.utils.StringProvider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class FolderFragment : BaseFragment<FolderFragmentBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?) -> FolderFragmentBinding
        get() = { inflater, parent -> FolderFragmentBinding.inflate(inflater, parent, false) }

    private val viewModel: FolderViewModel by viewModel()
    private val stringProvider: StringProvider by inject()

    private val folderAdapter: FolderAdapter by lazy { FolderAdapter(::onFolderSelected) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        viewModel.state.observe(viewLifecycleOwner) { switchState(it) }
        viewModel.load()
    }

    private fun initUi() = with(binding) {
        errorOverlay.retryButton.setOnClickListener { viewModel.load() }
        foldersRecyclerView.adapter = folderAdapter
        swipeRefreshLayout.setOnRefreshListener { viewModel.refresh() }
    }

    private fun onFolderSelected(id: String) {
        val folder = viewModel.getFolder(id) ?: return
        findNavController().navigate(
            FolderFragmentDirections.actionFolderFragmentToProgramFragment(folder)
        )
    }

    private fun switchState(state: State<Pair<UserProfile, List<Folder>>>) = with(binding) {
        when (state) {
            State.Idle -> {
                swipeRefreshLayout.isRefreshing = false
                loadingOverlay.root.isVisible = false
                errorOverlay.root.isVisible = false
            }
            is State.Failure -> {
                swipeRefreshLayout.isRefreshing = false
                errorOverlay.errorTextView.text = state.error
                loadingOverlay.root.isVisible = false
                errorOverlay.root.isVisible = true
            }
            is State.Success -> {
                swipeRefreshLayout.isRefreshing = false
                loadingOverlay.root.isVisible = false
                errorOverlay.root.isVisible = false
                itemDivider.isVisible = true
                bind(state.data.first, state.data.second)
            }
            is State.InProgress -> {
                swipeRefreshLayout.isRefreshing = false
                loadingOverlay.root.isVisible = true
                errorOverlay.root.isVisible = false
            }
        }
    }

    private fun bind(userProfile: UserProfile, folders: List<Folder>) {
        bindUserProfile(userProfile)
        folderAdapter.submitList(folders.map {
            FolderUiModel(
                it.id,
                it.name,
                it.expiredAt,
                stringProvider.getRelativeTimeSpanString(it.expiredAt.time),
                it.isExpired
            )
        }.sortedBy { it.expiredAt.time })
    }

    private fun bindUserProfile(userProfile: UserProfile) = with(binding) {
        nameTextView.text = stringProvider.getString(
            R.string.name_pattern,
            userProfile.name ?: "",
            userProfile.surname ?: ""
        )

        birthdayTextView.text = userProfile.birthday
        phoneTextView.setTextOrHide(userProfile.phone)
        addressTextView.setTextOrHide(userProfile.address)
        emailTextView.setTextOrHide(userProfile.email)
    }
}
