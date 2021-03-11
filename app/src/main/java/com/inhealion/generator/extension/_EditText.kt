package com.inhealion.generator.extension

import android.widget.EditText


fun EditText.requireString() = text?.toString() ?: ""
