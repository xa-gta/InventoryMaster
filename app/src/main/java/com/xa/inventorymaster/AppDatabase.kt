package com.xa.inventorymaster

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Item::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE items ADD COLUMN weight TEXT NOT NULL DEFAULT 'unit'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE items ADD COLUMN safetyStock INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a temporary table with the new schema
                db.execSQL("""
                    CREATE TABLE item_table_temp (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        price REAL NOT NULL,
                        quantityBought INTEGER NOT NULL,
                        quantityRemaining INTEGER NOT NULL,
                        storageLocation TEXT NOT NULL,
                        photoPath TEXT,
                        weight TEXT NOT NULL,
                        safetyStock INTEGER NOT NULL
                    )
                """.trimIndent())

                // Copy data from the old table to the new table, mapping storageLocation values
                db.execSQL("""
                    INSERT INTO item_table_temp (id, name, price, quantityBought, quantityRemaining, storageLocation, photoPath, weight, safetyStock)
                    SELECT id, name, price, quantityBought, quantityRemaining,
                        CASE
                            WHEN storageLocation = 'Shelf A' THEN 'KITCHEN_A'
                            WHEN storageLocation = 'Shelf B' THEN 'KITCHEN_B'
                            ELSE 'KITCHEN_A' -- Default to KITCHEN_A for unknown values
                        END,
                        photoPath, weight, safetyStock
                    FROM item_table
                """.trimIndent())

                // Drop the old table
                db.execSQL("DROP TABLE item_table")

                // Rename the temporary table to the original name
                db.execSQL("ALTER TABLE item_table_temp RENAME TO item_table")
            }
        }
    }
}