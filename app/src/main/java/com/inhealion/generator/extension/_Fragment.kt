package com.inhealion.generator.extension

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.LifecycleOwner

fun Fragment.hideKeyboard() {
    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.view?.windowToken, 0)
}

fun DialogFragment.observe(key: String, lifecycleOwner: LifecycleOwner, listener: (Bundle) -> Unit) {
    parentFragmentManager.setFragmentResultListener(
        key,
        lifecycleOwner,
        { _, result ->
            listener(result)
            parentFragmentManager.clearFragmentResultListener(key)
        })
}
