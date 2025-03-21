package com.xa.inventorymaster

import android.app.Application
import androidx.room.Room

class App : Application() {
    companion object {
        lateinit var database: ItemDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            ItemDatabase::class.java,
            "item_database"
        ).build()
    }
}