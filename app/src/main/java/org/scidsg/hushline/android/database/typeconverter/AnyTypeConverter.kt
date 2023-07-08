package org.scidsg.hushline.android.database.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson

class AnyTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromAny(any: Any): String {
        return gson.toJson(any)
    }

    @TypeConverter
    fun toAny(json: String): Any {
        return gson.fromJson(json, Any::class.java)
    }
}