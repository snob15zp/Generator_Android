package com.inhealion.generator.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

abstract class BaseFragment: Fragment() {

    protected open val layoutId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutId?.let { inflater.inflate(it, container, false) }
    }

    protected open fun back() {
        if (!findNavController().popBackStack()) {
            requireActivity().finish()
        }
    }
}
