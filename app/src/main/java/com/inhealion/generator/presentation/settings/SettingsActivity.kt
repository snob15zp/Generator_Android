package com.inhealion.generator.presentation.settings

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.navigation.ActivityNavigator
import com.inhealion.generator.databinding.SettingsActivityBinding
import com.inhealion.generator.model.UiImportState
import com.inhealion.generator.presentation.activity.BaseActivity
import com.inhealion.generator.presentation.main.viewmodel.SettingsActivityViewModel
import com.inhealion.generator.presentation.view.NoticeView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : BaseActivity() {

    private lateinit var binding: SettingsActivityBinding
    private val viewModel: SettingsActivityViewModel by viewModel()

    val toolbar: Toolbar get() = binding.toolbar

    override val noticeView: NoticeView
        get() = binding.noticeView

    override val importState: LiveData<UiImportState>
        get() = viewModel.importState

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

    companion object {
        fun start(activity: FragmentActivity) {
            val options = ActivityOptions.makeSceneTransitionAnimation(activity).toBundle()
            activity.startActivity(Intent(activity, SettingsActivity::class.java), options)
        }
    }
}
