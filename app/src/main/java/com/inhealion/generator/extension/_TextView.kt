package com.inhealion.generator.extension

import android.widget.TextView
import androidx.core.view.isVisible


fun TextView.setTextOrHide(text: String?) {
    this.text = text
    isVisible = text != null
}
