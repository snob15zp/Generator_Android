package com.inhealion.generator.presentation.device

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.inhealion.generator.R
import com.inhealion.generator.databinding.ImportActivityBinding
import com.inhealion.generator.device.model.BleDevice

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

    companion object {

        fun start(context: Context, action: ImportAction) {
            context.startActivity(intent(context, action))
        }

        fun intent(context: Context, action: ImportAction) = Intent(context, ImportActivity::class.java).apply {
            putExtras(ImportFragmentArgs.Builder(action).build().toBundle())
        }
    }
}
