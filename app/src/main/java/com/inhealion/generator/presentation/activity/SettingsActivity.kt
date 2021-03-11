package com.inhealion.generator.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.ActivityNavigator
import com.inhealion.generator.R
import com.inhealion.generator.databinding.SettingsActivityBinding
import org.koin.android.ext.android.bind

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    val toolbar: Toolbar get() = binding.toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    override fun finish() {
        super.finish()
        ActivityNavigator.applyPopAnimationsToPendingTransition(this)
    }
}
