package com.inhealion.generator.presentation.main

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.inhealion.generator.R
import com.inhealion.generator.presentation.activity.SettingsActivity

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    protected lateinit var binding: T
    protected abstract val bindingInflater: (LayoutInflater, ViewGroup?) -> T

    private val toolbar: Toolbar? get() = binding.root.findViewById(R.id.toolbar)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = bindingInflater.invoke(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar ?: activity?.findViewById(R.id.toolbar))?.let { setupToolbar(it) }
    }

    protected open fun setupToolbar(toolbar: Toolbar) {
        toolbar.title = getString(R.string.app_title)
        toolbar.menu.findItem(R.id.menu_info_action)
            .setOnMenuItemClickListener {
                val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity()).toBundle()
                startActivity(Intent(requireContext(), SettingsActivity::class.java), options)
                true
            }
    }

    protected open fun back() {
        if (!findNavController().popBackStack()) {
            requireActivity().finish()
        }
    }
}