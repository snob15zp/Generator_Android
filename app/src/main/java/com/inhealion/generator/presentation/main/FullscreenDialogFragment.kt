package com.inhealion.generator.presentation.main

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding

abstract class FullscreenDialogFragment<T : ViewBinding> : DialogFragment() {
    protected lateinit var binding: T
    protected abstract val bindingInflater: (LayoutInflater, ViewGroup?) -> T

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = bindingInflater.invoke(inflater, container)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) = Dialog(requireActivity()).apply {
        // creating the fullscreen dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(RelativeLayout(activity).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        })
        window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
}
