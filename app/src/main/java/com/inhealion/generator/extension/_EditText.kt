package com.inhealion.generator.extension

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


fun EditText.requireString() = text?.toString() ?: ""

fun EditText.onTextChanged(action: (String) -> Unit) = run {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            action(s?.toString() ?: "")
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}
