package com.inhealion.generator.extension

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

fun ViewGroup.inflateView(resId: Int, attachToRoot: Boolean = false): View =
    LayoutInflater.from(context).inflate(resId, this, attachToRoot)
