package com.xa.inventorymaster

import androidx.lifecycle.LiveData

class ItemRepository(private val itemDao: ItemDao) {
    val allItems: LiveData<List<Item>> = itemDao.getAllItems()

    suspend fun insert(item: Item) {
        itemDao.insert(item)
    }

    suspend fun update(item: Item) {
        itemDao.update(item)
    }

    suspend fun getAllItems(): List<Item> {
        return itemDao.getAllItemsList()
    }
}