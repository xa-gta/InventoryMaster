package com.xa.inventorymaster

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

enum class StorageLocation {
    KITCHEN_A, KITCHEN_B, KITCHEN_C, KITCHEN_D, KITCHEN_E, KITCHEN_F,
    COFFEE_SHELF, GUEST_LOFT, MAIN_LOFT
}

class Converters {
    @TypeConverter
    fun fromStorageLocation(value: StorageLocation?): String? {
        return value?.name
    }

    @TypeConverter
    fun toStorageLocation(value: String?): StorageLocation? {
        return value?.let { StorageLocation.valueOf(it) }
    }
}

@Entity(tableName = "item_table")
@TypeConverters(Converters::class)
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val quantityBought: Int,
    val quantityRemaining: Int,
    val storageLocation: StorageLocation,
    val photoPath: String?,
    val weight: String,
    val safetyStock: Int
)