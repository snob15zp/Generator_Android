package com.inhealion.generator.extension

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


fun AppCompatActivity.setFragmentResultListener(requestKey: String, listener: (String, Bundle) -> Unit) {
    supportFragmentManager.setFragmentResultListener(requestKey, this, listener)
}
