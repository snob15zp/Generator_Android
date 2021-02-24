package com.inhealion.generator.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import com.inhealion.generator.databinding.MainFragmentBinding

const val LOGIN_REQUEST_KEY = "loginResult"
const val CONNECT_REQUEST_KEY = "connectionResult"
const val RESULT_KEY = "result"

class MainFragment : BaseFragment<MainFragmentBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?) -> MainFragmentBinding
        get() = { inflater, parent -> MainFragmentBinding.inflate(inflater, parent, false) }
}

