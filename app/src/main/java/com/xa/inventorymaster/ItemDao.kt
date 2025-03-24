package com.xa.inventorymaster

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ItemDao {
    @Insert
    suspend fun insert(item: Item)

    @Update
    suspend fun update(item: Item)

    @Query("SELECT * FROM item_table")
    fun getAllItems(): LiveData<List<Item>>

    @Query("SELECT * FROM item_table")
    suspend fun getAllItemsList(): List<Item>
}

// This is a test message to check if grok is able to read files - SECRET CODE IS 3579