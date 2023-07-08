package org.scidsg.hushline.android.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "settings", indices = [Index(value = ["key"], unique = true)])
//@TypeConverters(AnyTypeConverter::class)
data class SettingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val key: String,
    val value: String
)

/*data class SettingsEntity<T>(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val key: String,
    val value: T
) where T : CharSequence,
        T : Collection<*>,
        T : Comparable<T> {
    init {
        require(value is Number || value is Array<*>) {
            "Invalid type for value: ${value::class.simpleName}"
        }
    }
}*/
