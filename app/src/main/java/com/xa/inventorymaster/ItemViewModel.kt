package com.xa.inventorymaster

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ItemViewModel(application: Application, private val activity: MainActivity) : AndroidViewModel(application) {
    private val repository: ItemRepository
    val allItems: LiveData<List<Item>>

    init {
        val itemDao = DatabaseProvider.getDatabase(application).itemDao()
        repository = ItemRepository(itemDao)
        allItems = repository.allItems
    }

    suspend fun insert(item: Item) {
        repository.insert(item)
    }

    suspend fun update(item: Item) {
        repository.update(item)
    }

    suspend fun getAllItems(): List<Item> {
        return repository.getAllItems()
    }

    // Form data handling methods
    private val formData = MutableLiveData<Map<String, String>>().apply {
        value = mapOf(
            KEY_NAME to "",
            KEY_PRICE to "",
            KEY_QUANTITY_BOUGHT to "",
            KEY_QUANTITY_REMAINING to "",
            KEY_STORAGE_LOCATION to "",
            KEY_SAFETY_STOCK to "",
            KEY_UNIT to ""
        )
    }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_PRICE = "price"
        const val KEY_QUANTITY_BOUGHT = "quantityBought"
        const val KEY_QUANTITY_REMAINING = "quantityRemaining"
        const val KEY_STORAGE_LOCATION = "storageLocation"
        const val KEY_SAFETY_STOCK = "safetyStock"
        const val KEY_UNIT = "unit"
    }

    fun updateFormField(key: String, value: String) {
        val currentData = formData.value?.toMutableMap() ?: mutableMapOf()
        currentData[key] = value
        formData.value = currentData
    }

    fun getFormName(): String = formData.value?.get(KEY_NAME) ?: ""
    fun getFormPrice(): String = formData.value?.get(KEY_PRICE) ?: ""
    fun getFormQuantityBought(): String = formData.value?.get(KEY_QUANTITY_BOUGHT) ?: ""
    fun getFormQuantityRemaining(): String = formData.value?.get(KEY_QUANTITY_REMAINING) ?: ""
    fun getFormStorageLocation(): String = formData.value?.get(KEY_STORAGE_LOCATION) ?: ""
    fun getFormSafetyStock(): String = formData.value?.get(KEY_SAFETY_STOCK) ?: ""
    fun getFormUnit(): String = formData.value?.get(KEY_UNIT) ?: ""

    fun saveFormData(
        name: String,
        price: String,
        quantityBought: String,
        quantityRemaining: String,
        storageLocation: String,
        safetyStock: String,
        unit: String
    ) {
        formData.value = mapOf(
            KEY_NAME to name,
            KEY_PRICE to price,
            KEY_QUANTITY_BOUGHT to quantityBought,
            KEY_QUANTITY_REMAINING to quantityRemaining,
            KEY_STORAGE_LOCATION to storageLocation,
            KEY_SAFETY_STOCK to safetyStock,
            KEY_UNIT to unit
        )
    }

    fun clearFormData() {
        formData.value = mapOf(
            KEY_NAME to "",
            KEY_PRICE to "",
            KEY_QUANTITY_BOUGHT to "",
            KEY_QUANTITY_REMAINING to "",
            KEY_STORAGE_LOCATION to "",
            KEY_SAFETY_STOCK to "",
            KEY_UNIT to ""
        )
    }
}

class ItemViewModelFactory(
    private val application: Application,
    private val activity: MainActivity
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemViewModel(application, activity) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}