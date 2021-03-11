package com.inhealion.generator.utils

import android.content.Context
import android.icu.text.RelativeDateTimeFormatter
import android.text.format.DateUtils
import androidx.annotation.StringRes
import com.inhealion.generator.R
import org.joda.time.*
import org.joda.time.format.PeriodFormatterBuilder
import java.text.SimpleDateFormat
import java.util.*


private const val SHORT_DATE_FORMAT = "yyyy-MM-dd"

interface StringProvider {

    fun getString(@StringRes id: Int): String

    fun getString(@StringRes id: Int, vararg args: Any): String

    fun formatDate(date: Date, format: String = SHORT_DATE_FORMAT): String

    fun getRelativeTimeSpanString(time: Long): String
}

class StringProviderImpl(private val context: Context) : StringProvider {

    override fun getString(@StringRes id: Int) = context.resources.getString(id)

    override fun getString(@StringRes id: Int, vararg args: Any) = context.resources.getString(id, *args)

    override fun formatDate(date: Date, format: String): String =
        SimpleDateFormat(format, Locale.getDefault()).format(date)

    override fun getRelativeTimeSpanString(time: Long): String {
        val now = LocalDate.now()
        val date = LocalDate(time)
        val years = Years.yearsBetween(now, date).years
        val months = Months.monthsBetween(now, date).months
        val days = Days.daysBetween(now, date).days

        return when {
            years > 0 -> getString(R.string.expires_at, formatDate(Date(time)))
            months > 0 -> context.resources.getQuantityString(R.plurals.expires_in_month, months, months)
            else -> context.resources.getQuantityString(R.plurals.expires_in_days, days, days)
        }
    }
}
