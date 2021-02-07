package com.inhealion.generator.extension

import androidx.recyclerview.widget.DiffUtil

inline fun <T> itemCallbackOf(
    crossinline areItemsTheSame: (old: T, new: T) -> Boolean,
    crossinline areContentsTheSame: (old: T, new: T) -> Boolean = { old, new -> old == new },
    crossinline getChangePayload: (old: T, new: T) -> Any? = { _, _ -> null }
) = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = areItemsTheSame(oldItem, newItem)
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = areContentsTheSame(oldItem, newItem)
    override fun getChangePayload(oldItem: T, newItem: T): Any? = getChangePayload(oldItem, newItem)
}

