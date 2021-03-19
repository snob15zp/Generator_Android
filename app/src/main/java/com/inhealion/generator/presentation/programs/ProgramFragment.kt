package com.inhealion.generator.presentation.programs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.inhealion.generator.R
import com.inhealion.generator.databinding.ProgramsFragmentBinding
import com.inhealion.generator.device.model.BleDevice
import com.inhealion.generator.extension.observe
import com.inhealion.generator.model.MessageDialogData
import com.inhealion.generator.model.State
import com.inhealion.generator.networking.api.model.Program
import com.inhealion.generator.presentation.activity.ImportActivity
import com.inhealion.generator.presentation.device.DiscoveryDialogFragment
import com.inhealion.generator.presentation.device.ImportAction
import com.inhealion.generator.presentation.device.ImportFragmentArgs
import com.inhealion.generator.presentation.dialogs.MessageDialog
import com.inhealion.generator.presentation.main.BaseFragment
import com.inhealion.generator.presentation.main.CONNECT_REQUEST_KEY
import com.inhealion.generator.presentation.main.RESULT_KEY
import com.inhealion.generator.presentation.programs.adapter.ProgramUiModel
import com.inhealion.generator.presentation.programs.adapter.ProgramsAdapter
import com.inhealion.generator.presentation.programs.viewmodel.ProgramsViewModel
import com.inhealion.generator.utils.StringProvider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ProgramFragment : BaseFragment<ProgramsFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> ProgramsFragmentBinding
        get() = { inflater, parent -> ProgramsFragmentBinding.inflate(inflater, parent, false) }


    private val viewModel: ProgramsViewModel by viewModel { parametersOf(navArgs<ProgramFragmentArgs>().value.folder) }
    private val adapter: ProgramsAdapter by lazy { ProgramsAdapter() }
    private val stringProvider: StringProvider by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            nameTextView.text = viewModel.folder.name
            infoTextView.text = stringProvider.getRelativeTimeSpanString(viewModel.folder.expiredAt.time)
            errorOverlay.retryButton.setOnClickListener { viewModel.load() }
            importButton.setOnClickListener { viewModel.import(requireContext()) }

            programsRecyclerView.adapter = adapter
            (programsRecyclerView.layoutManager as GridLayoutManager).apply {
                spanCount = 3
            }
        }

        with(viewModel) {
            state.observe(viewLifecycleOwner) { switchState(it) }
            showDiscovery.observe(viewLifecycleOwner) {
                DiscoveryDialogFragment.show(parentFragmentManager)
                    .observe(CONNECT_REQUEST_KEY, viewLifecycleOwner, ::handleConnectionResult)
            }
            device.observe(viewLifecycleOwner) { showImportActivity(viewModel.folder.id, it) }
            isImportInProgress.observe(viewLifecycleOwner) {
                binding.importButton.isVisible = !it
            }
            load()
        }
    }

    private fun showImportActivity(folderId: String, device: BleDevice) {
        startActivity(
            Intent(requireContext(), ImportActivity::class.java).apply {
                putExtras(ImportFragmentArgs(ImportAction.ImportFolder(folderId, device)).toBundle())
            }
        )
    }

    private fun handleConnectionResult(result: Bundle) {
        if (result.getBoolean(RESULT_KEY)) {
            viewModel.import(requireContext())
        } else {
            MessageDialog.show(
                parentFragmentManager,
                MessageDialogData("", getString(R.string.device_not_connected_message))
            )
        }
    }

    override fun setupToolbar(toolbar: Toolbar) {
        super.setupToolbar(toolbar)
        toolbar.title = getString(R.string.programs_title)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
        toolbar.setNavigationOnClickListener { back() }
    }

    private fun switchState(state: State<List<Program>>) = with(binding) {
        when (state) {
            State.Idle -> {
                loadingOverlay.root.isVisible = false
                errorOverlay.root.isVisible = false
            }
            is State.Failure -> {
                errorOverlay.errorTextView.text = state.error
                loadingOverlay.root.isVisible = false
                errorOverlay.root.isVisible = true
            }
            is State.Success -> {
                adapter.submitList((state.data).map { ProgramUiModel(it.id, it.name) })
                loadingOverlay.root.isVisible = false
                errorOverlay.root.isVisible = false
            }
            is State.InProgress -> {
                loadingOverlay.root.isVisible = true
                errorOverlay.root.isVisible = false
            }
        }
    }
}
