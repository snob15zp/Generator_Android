package com.inhealion.generator.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import com.inhealion.generator.R
import com.inhealion.generator.model.UiImportState
import com.inhealion.generator.presentation.view.NoticeView

abstract class BaseActivity : AppCompatActivity() {

    abstract val noticeView: NoticeView
    abstract val importState: LiveData<UiImportState>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        importState.observe(this) {
            with(noticeView) {
                when (it) {
                    UiImportState.InProgress -> showInfo(R.string.notice_import_is_in_progress)
                    is UiImportState.Failed -> showError(it.message)
                    UiImportState.Success -> showSuccess(R.string.notice_import_success)
                    else -> isVisible = false
                }
            }
        }
    }
}
