package com.inhealion.generator.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.inhealion.generator.R
import com.inhealion.generator.databinding.ImportActivityBinding

class ImportActivity : AppCompatActivity() {

    private lateinit var binding: ImportActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ImportActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (savedInstanceState == null) {
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).apply {
                navController.setGraph(
                    R.navigation.import_nav_graph,
                    intent.extras
                )
            }
        }
    }
}
