package com.inhealion.generator.presentation.view

import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.inhealion.generator.R
import com.inhealion.generator.databinding.NoticeViewBinding


class NoticeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val binding: NoticeViewBinding = NoticeViewBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    private val closeHandler = Handler(Looper.getMainLooper())

    init {
        binding.closeImage.setOnClickListener { isVisible = false }
    }

    fun showInfo(@StringRes resId: Int, closable: Boolean = false) {
        bind(
            context.getString(resId),
            closable,
            R.color.notice_info_bg,
            R.color.textColor,
            R.color.colorPrimary,
            R.drawable.ic_outline_info_24
        )
    }

    fun showSuccess(@StringRes resId: Int, closable: Boolean = true) = with(binding) {
        bind(
            context.getString(resId),
            closable,
            R.color.notice_info_bg,
            R.color.textColor,
            R.color.colorPrimary,
            R.drawable.ic_baseline_check_circle_outline_24
        )
        closeHandler.postDelayed({ isVisible = false }, 5000)
    }

    fun showError(error: String, closable: Boolean = true) = with(binding) {
        bind(
            error,
            closable,
            R.color.notice_error_bg,
            R.color.textColor,
            R.color.textError,
            R.drawable.ic_outline_error_outline_24
        )
        closeHandler.postDelayed({ isVisible = false }, 5000)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        closeHandler.removeCallbacksAndMessages(null)
    }

    private fun bind(
        message: String,
        closable: Boolean,
        @ColorRes bgColorResId: Int,
        @ColorRes textColorResId: Int,
        @ColorRes iconColorResId: Int,
        @DrawableRes iconResId: Int
    ) = with(binding) {
        isVisible = true
        setBackgroundColor(context.getColor(bgColorResId))
        closeImage.isVisible = closable
        noticeMessageView.text = message
        noticeMessageView.setTextColor(context.getColor(textColorResId))
        noticeMessageView.setCompoundDrawablesWithIntrinsicBounds(
            getDrawable(context, iconResId)?.apply {
                setTintList(ColorStateList.valueOf(context.getColor(iconColorResId)))
            },
            null,
            null,
            null
        )
    }
}
