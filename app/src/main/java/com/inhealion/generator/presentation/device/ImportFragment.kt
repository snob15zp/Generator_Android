package com.inhealion.generator.presentation.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.inhealion.generator.databinding.ImportFragmentBinding
import com.inhealion.generator.model.State
import com.inhealion.generator.presentation.device.viewmodel.ImportViewModel
import com.inhealion.generator.presentation.main.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ImportFragment : BaseFragment<ImportFragmentBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?) -> ImportFragmentBinding
        get() = { inflater, parent -> ImportFragmentBinding.inflate(inflater, parent, false) }


    private val viewModel: ImportViewModel by viewModel { parametersOf(navArgs<ImportFragmentArgs>().value.importAction) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelButton.setOnClickListener { back() }
        binding.closeImage.setOnClickListener { back() }

        with(viewModel) {
            showDiscovery.observe(viewLifecycleOwner) {
                findNavController().navigate(ImportFragmentDirections.actionImportFragmentToDiscoveryFragment())
            }

            state.observe(viewLifecycleOwner) { switchState(it) }
        }

        viewModel.import()
    }

    private fun switchState(state: State<Nothing>) {
        when (state) {
            is State.Success -> Unit
            is State.Failure -> Unit
            State.Idle -> Unit
            State.InProgress -> Unit
            else -> Unit
        }
    }
}
