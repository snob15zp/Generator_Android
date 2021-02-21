package com.inhealion.generator.lifecyle

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

open class SingleLiveData<T>(default: T? = null) : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)

    init {
        default?.let { value = it }
    }

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer<T> { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    @MainThread
    override fun setValue(value: T?) {
        pending.set(true)
        super.setValue(value)
    }
}

class ActionLiveData : SingleLiveData<Unit>() {

    @MainThread
    fun sendAction() {
        value = Unit
    }
}
