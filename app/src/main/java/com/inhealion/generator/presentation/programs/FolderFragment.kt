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
import com.inhealion.generator.utils.StringProvider
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.api.model.UserProfile
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.presentation.programs.adapter.FolderAdapter
import com.inhealion.generator.presentation.programs.adapter.FolderUiModel
import com.inhealion.generator.presentation.programs.viewmodel.FolderViewModel
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

        binding.errorOverlay.retryButton.setOnClickListener { viewModel.load() }
        binding.foldersRecyclerView.adapter = folderAdapter

        with(viewModel) {
            showLoginForm.observe(viewLifecycleOwner) { findNavController().navigate(R.id.loginFragment) }
            userProfile.observe(viewLifecycleOwner) { bindUserProfile(it) }
            state.observe(viewLifecycleOwner) { switchState(it) }
            folders.observe(viewLifecycleOwner) { folders ->
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
            load()
        }
    }

    private fun onFolderSelected(id: String) {
        val folder = viewModel.folders.value?.find { it.id == id } ?: return
        findNavController().navigate(
            FolderFragmentDirections.actionFolderFragmentToProgramFragment(folder)
        )
    }

    private fun switchState(state: State) = with(binding) {
        when (state) {
            State.Idle -> {
                showUserProfileControls(false)
                loadingOverlay.root.isVisible = false
                errorOverlay.root.isVisible = false
            }
            is State.Failure -> {
                errorOverlay.errorTextView.text = state.error
                loadingOverlay.root.isVisible = false
                errorOverlay.root.isVisible = true
            }
            is State.Success<*> -> {
                loadingOverlay.root.isVisible = false
                errorOverlay.root.isVisible = false
                showUserProfileControls(true)
            }
            State.InProgress -> {
                loadingOverlay.root.isVisible = true
                errorOverlay.root.isVisible = false
                showUserProfileControls(false)
            }
        }
    }

    private fun showUserProfileControls(show: Boolean) = with(binding) {
        phoneTextView.isVisible = show
        addressTextView.isVisible = show
        emailTextView.isVisible = show
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
