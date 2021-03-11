package com.inhealion.generator.networking.adapter

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DateJsonAdapter : JsonAdapter<Date?>() {
    private val dateFormatter: SimpleDateFormat get() = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

    @Synchronized
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Date? {
        if (reader.peek() == JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        val string = reader.nextString()
        return dateFormatter.parse(string)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val string = dateFormatter.format(value)
            writer.value(string)
        }
    }
}
