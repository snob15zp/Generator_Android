package com.inhealion.generator.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inhealion.generator.R
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
